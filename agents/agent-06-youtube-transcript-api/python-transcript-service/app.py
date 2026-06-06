import json
import logging
import os
import time
from dataclasses import dataclass, field
from typing import List, Optional

from fastapi import FastAPI
from pydantic import BaseModel, Field
from requests import PreparedRequest, Response, Session
from youtube_transcript_api import YouTubeTranscriptApi
from youtube_transcript_api._errors import (
    IpBlocked,
    NoTranscriptFound,
    RequestBlocked,
    TranscriptsDisabled,
    VideoUnavailable,
)
from youtube_transcript_api.proxies import GenericProxyConfig, WebshareProxyConfig

app = FastAPI(title="YouTube Transcript Provider", version="0.1.0")
logger = logging.getLogger("youtube_transcript_provider")


@app.get("/health")
def health():
    return {"status": "ok"}


class TranscriptRequest(BaseModel):
    videoId: str = Field(min_length=1)
    preferredLanguages: Optional[List[str]] = None


@app.post("/internal/youtube/transcripts")
def get_transcript(request: TranscriptRequest):
    languages = request.preferredLanguages or []
    metrics = ProxyUsageMetrics()
    started_at = time.perf_counter()

    try:
        transcript_list = youtube_transcript_api(metrics).list(request.videoId)
        language_fallback_used = False

        if languages:
            try:
                transcript = transcript_list.find_transcript(languages)
            except NoTranscriptFound:
                available_transcripts = list(transcript_list)
                if not available_transcripts:
                    return with_proxy_usage({
                        "status": "TRANSCRIPT_NOT_AVAILABLE",
                        "videoId": request.videoId,
                        "reason": "No public transcript is available for this video",
                    }, metrics, started_at)
                transcript = available_transcripts[0]
                language_fallback_used = True
        else:
            available_transcripts = list(transcript_list)
            if not available_transcripts:
                return with_proxy_usage({
                    "status": "TRANSCRIPT_NOT_AVAILABLE",
                    "videoId": request.videoId,
                    "reason": "No public transcript is available for this video",
                }, metrics, started_at)
            transcript = available_transcripts[0]

        segments = transcript.fetch().to_raw_data()

        return with_proxy_usage({
            "status": "TRANSCRIPT_FOUND",
            "videoId": request.videoId,
            "language": transcript.language_code,
            "isGenerated": transcript.is_generated,
            "languageDetectionMethod": "YOUTUBE_TRANSCRIPT_METADATA",
            "languageFallbackUsed": language_fallback_used,
            "segments": [
                {
                    "start": item["start"],
                    "duration": item["duration"],
                    "text": item["text"],
                }
                for item in segments
            ],
        }, metrics, started_at)

    except TranscriptsDisabled:
        return with_proxy_usage({
            "status": "TRANSCRIPT_NOT_AVAILABLE",
            "videoId": request.videoId,
            "reason": "Transcripts are disabled for this video",
        }, metrics, started_at)
    except NoTranscriptFound:
        return with_proxy_usage({
            "status": "TRANSCRIPT_NOT_AVAILABLE",
            "videoId": request.videoId,
            "reason": "No public transcript found for the requested languages",
        }, metrics, started_at)
    except VideoUnavailable:
        return with_proxy_usage({
            "status": "VIDEO_UNAVAILABLE",
            "videoId": request.videoId,
            "reason": "The video is unavailable",
        }, metrics, started_at)
    except (RequestBlocked, IpBlocked):
        return with_proxy_usage({
            "status": "TRANSCRIPT_PROVIDER_BLOCKED",
            "videoId": request.videoId,
            "reason": provider_blocked_reason(),
        }, metrics, started_at)
    except Exception as exc:
        if is_provider_blocked(exc):
            return with_proxy_usage({
                "status": "TRANSCRIPT_PROVIDER_BLOCKED",
                "videoId": request.videoId,
                "reason": provider_blocked_reason(),
            }, metrics, started_at)
        return with_proxy_usage({
            "status": "PROVIDER_ERROR",
            "videoId": request.videoId,
            "reason": provider_error_reason(exc),
        }, metrics, started_at)


def youtube_transcript_api(metrics: "ProxyUsageMetrics") -> YouTubeTranscriptApi:
    http_client = CountingSession(metrics)
    webshare_username = os.getenv("YOUTUBE_TRANSCRIPT_WEBSHARE_USERNAME")
    webshare_password = os.getenv("YOUTUBE_TRANSCRIPT_WEBSHARE_PASSWORD")
    if webshare_username and webshare_password:
        metrics.route = "webshare"
        return YouTubeTranscriptApi(
            proxy_config=WebshareProxyConfig(
                proxy_username=webshare_username,
                proxy_password=webshare_password,
                filter_ip_locations=webshare_locations(),
            ),
            http_client=http_client,
        )

    http_proxy = os.getenv("YOUTUBE_TRANSCRIPT_PROXY_HTTP")
    https_proxy = os.getenv("YOUTUBE_TRANSCRIPT_PROXY_HTTPS")
    if http_proxy or https_proxy:
        metrics.route = "generic_proxy"
        return YouTubeTranscriptApi(
            proxy_config=GenericProxyConfig(
                http_url=http_proxy,
                https_url=https_proxy,
            ),
            http_client=http_client,
        )

    metrics.route = "direct"
    return YouTubeTranscriptApi(http_client=http_client)


@dataclass
class ProxyUsageMetrics:
    route: str = "direct"
    request_count: int = 0
    request_bytes: int = 0
    response_bytes: int = 0
    statuses: dict[str, int] = field(default_factory=dict)

    @property
    def total_bytes(self) -> int:
        return self.request_bytes + self.response_bytes


class CountingSession(Session):
    def __init__(self, metrics: ProxyUsageMetrics):
        super().__init__()
        self.metrics = metrics

    def send(self, request: PreparedRequest, **kwargs) -> Response:
        self.metrics.request_count += 1
        self.metrics.request_bytes += estimate_prepared_request_bytes(request)
        response = super().send(request, **kwargs)
        self.metrics.response_bytes += estimate_response_bytes(response)
        status_key = str(response.status_code)
        self.metrics.statuses[status_key] = self.metrics.statuses.get(status_key, 0) + 1
        return response


def estimate_prepared_request_bytes(request: PreparedRequest) -> int:
    size = len(request.method or "") + len(request.url or "") + 12
    for name, value in request.headers.items():
        size += len(name) + len(value) + 4
    body = request.body
    if body is None:
        return size
    if isinstance(body, bytes):
        return size + len(body)
    return size + len(str(body).encode("utf-8"))


def estimate_response_bytes(response: Response) -> int:
    size = 12
    for name, value in response.headers.items():
        size += len(name) + len(value) + 4
    return size + len(response.content or b"")


def log_proxy_usage_estimate(
    video_id: str,
    status: str,
    metrics: ProxyUsageMetrics,
    elapsed_seconds: float,
) -> None:
    price_per_gb = proxy_price_per_gb_usd()
    total_mb = metrics.total_bytes / (1024 * 1024)
    estimated_cost = (metrics.total_bytes / (1024 * 1024 * 1024)) * price_per_gb
    payload = {
        "videoId": video_id,
        "status": status,
        "route": metrics.route,
        "requestCount": metrics.request_count,
        "requestBytes": metrics.request_bytes,
        "responseBytes": metrics.response_bytes,
        "totalBytes": metrics.total_bytes,
        "totalMb": round(total_mb, 4),
        "proxyPricePerGbUsd": price_per_gb,
        "estimatedProxyCostUsd": round(estimated_cost, 6),
        "httpStatuses": metrics.statuses,
        "elapsedSeconds": round(elapsed_seconds, 3),
    }
    logger.info("proxy_usage_estimate=%s", json.dumps(payload, separators=(",", ":")))


def with_proxy_usage(payload: dict, metrics: ProxyUsageMetrics, started_at: float) -> dict:
    usage = proxy_usage_payload(
        video_id=payload.get("videoId", ""),
        status=payload.get("status", "UNKNOWN"),
        metrics=metrics,
        elapsed_seconds=time.perf_counter() - started_at,
    )
    payload["proxyUsage"] = usage
    logger.info("proxy_usage_estimate=%s", json.dumps(usage, separators=(",", ":")))
    return payload


def proxy_usage_payload(
    video_id: str,
    status: str,
    metrics: ProxyUsageMetrics,
    elapsed_seconds: float,
) -> dict:
    price_per_gb = proxy_price_per_gb_usd()
    total_mb = metrics.total_bytes / (1024 * 1024)
    estimated_cost = (metrics.total_bytes / (1024 * 1024 * 1024)) * price_per_gb
    return {
        "videoId": video_id,
        "status": status,
        "route": metrics.route,
        "requestCount": metrics.request_count,
        "requestBytes": metrics.request_bytes,
        "responseBytes": metrics.response_bytes,
        "totalBytes": metrics.total_bytes,
        "totalMb": round(total_mb, 4),
        "proxyPricePerGbUsd": price_per_gb,
        "estimatedProxyCostUsd": round(estimated_cost, 6),
        "httpStatuses": metrics.statuses,
        "elapsedSeconds": round(elapsed_seconds, 3),
    }


def proxy_price_per_gb_usd() -> float:
    raw_value = os.getenv("YOUTUBE_TRANSCRIPT_PROXY_PRICE_PER_GB_USD", "3.50")
    try:
        return float(raw_value)
    except ValueError:
        return 3.50


def provider_error_reason(exc: Exception) -> str:
    return provider_blocked_reason() if is_provider_blocked(exc) else str(exc)


def is_provider_blocked(exc: Exception) -> bool:
    message = str(exc).lower()
    return (
        "youtube is blocking requests from your ip" in message
        or "too many 429" in message
        or "/sorry/index" in message
        or "responseerror('too many 429" in message
    )


def provider_blocked_reason() -> str:
    return (
        "YouTube/Google blocked transcript requests from the current IP or proxy route. "
        "Try again later, change the Webshare residential proxy location, or use a different rotating residential proxy pool."
    )


def webshare_locations() -> Optional[List[str]]:
    raw_locations = os.getenv("YOUTUBE_TRANSCRIPT_WEBSHARE_LOCATIONS")
    if not raw_locations:
        return None
    locations = [location.strip().lower() for location in raw_locations.split(",") if location.strip()]
    return locations or None
