package com.aichallenge.agents.youtubetranscript.infrastructure.persistence;

import com.aichallenge.agents.youtubetranscript.domain.TranscriptSource;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.ColumnTransformer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "youtube_transcript",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_youtube_transcript_video_language",
                columnNames = {"video_id", "language"}
        )
)
public class YoutubeTranscriptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private YoutubeVideoEntity video;

    @Column(name = "language", length = 10, nullable = false)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 50, nullable = false)
    private TranscriptSource source;

    @Column(name = "generated", nullable = false)
    private Boolean generated;

    @Column(name = "language_detection_method", length = 80, nullable = false)
    private String languageDetectionMethod;

    @Column(name = "language_fallback_used", nullable = false)
    private Boolean languageFallbackUsed;

    @Column(name = "full_text", nullable = false, columnDefinition = "TEXT")
    private String fullText;

    @Column(name = "proxy_route", length = 40, nullable = false)
    private String proxyRoute = "unknown";

    @Column(name = "proxy_request_count", nullable = false)
    private Integer proxyRequestCount = 0;

    @Column(name = "proxy_request_bytes", nullable = false)
    private Long proxyRequestBytes = 0L;

    @Column(name = "proxy_response_bytes", nullable = false)
    private Long proxyResponseBytes = 0L;

    @Column(name = "proxy_total_bytes", nullable = false)
    private Long proxyTotalBytes = 0L;

    @Column(name = "proxy_total_mb", nullable = false)
    private Double proxyTotalMb = 0.0;

    @Column(name = "proxy_price_per_gb_usd", nullable = false)
    private Double proxyPricePerGbUsd = 0.0;

    @Column(name = "proxy_estimated_cost_usd", nullable = false)
    private Double proxyEstimatedCostUsd = 0.0;

    @Column(name = "proxy_http_statuses_json", nullable = false, columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String proxyHttpStatusesJson = "{}";

    @Column(name = "proxy_elapsed_seconds", nullable = false)
    private Double proxyElapsedSeconds = 0.0;

    @OneToMany(mappedBy = "transcript", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<YoutubeTranscriptSegmentEntity> segments = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected YoutubeTranscriptEntity() {
    }

    public YoutubeTranscriptEntity(
            YoutubeVideoEntity video,
            String language,
            TranscriptSource source,
            boolean generated,
            String languageDetectionMethod,
            boolean languageFallbackUsed,
            String fullText,
            LocalDateTime now
    ) {
        this.video = video;
        this.language = language;
        this.source = source;
        this.generated = generated;
        this.languageDetectionMethod = languageDetectionMethod;
        this.languageFallbackUsed = languageFallbackUsed;
        this.fullText = fullText;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public YoutubeVideoEntity getVideo() {
        return video;
    }

    public String getLanguage() {
        return language;
    }

    public TranscriptSource getSource() {
        return source;
    }

    public Boolean getGenerated() {
        return generated;
    }

    public String getLanguageDetectionMethod() {
        return languageDetectionMethod;
    }

    public Boolean getLanguageFallbackUsed() {
        return languageFallbackUsed;
    }

    public String getFullText() {
        return fullText;
    }

    public String getProxyRoute() {
        return proxyRoute;
    }

    public Integer getProxyRequestCount() {
        return proxyRequestCount;
    }

    public Long getProxyRequestBytes() {
        return proxyRequestBytes;
    }

    public Long getProxyResponseBytes() {
        return proxyResponseBytes;
    }

    public Long getProxyTotalBytes() {
        return proxyTotalBytes;
    }

    public Double getProxyTotalMb() {
        return proxyTotalMb;
    }

    public Double getProxyPricePerGbUsd() {
        return proxyPricePerGbUsd;
    }

    public Double getProxyEstimatedCostUsd() {
        return proxyEstimatedCostUsd;
    }

    public String getProxyHttpStatusesJson() {
        return proxyHttpStatusesJson;
    }

    public Double getProxyElapsedSeconds() {
        return proxyElapsedSeconds;
    }

    public List<YoutubeTranscriptSegmentEntity> getSegments() {
        return segments;
    }

    public void replaceWith(
            String fullText,
            boolean generated,
            TranscriptSource source,
            String languageDetectionMethod,
            boolean languageFallbackUsed,
            String proxyRoute,
            int proxyRequestCount,
            long proxyRequestBytes,
            long proxyResponseBytes,
            long proxyTotalBytes,
            double proxyTotalMb,
            double proxyPricePerGbUsd,
            double proxyEstimatedCostUsd,
            String proxyHttpStatusesJson,
            double proxyElapsedSeconds,
            LocalDateTime now
    ) {
        this.fullText = fullText;
        this.generated = generated;
        this.source = source;
        this.languageDetectionMethod = languageDetectionMethod;
        this.languageFallbackUsed = languageFallbackUsed;
        updateProxyUsage(
                proxyRoute,
                proxyRequestCount,
                proxyRequestBytes,
                proxyResponseBytes,
                proxyTotalBytes,
                proxyTotalMb,
                proxyPricePerGbUsd,
                proxyEstimatedCostUsd,
                proxyHttpStatusesJson,
                proxyElapsedSeconds
        );
        this.updatedAt = now;
        this.segments.clear();
    }

    public void updateProxyUsage(
            String proxyRoute,
            int proxyRequestCount,
            long proxyRequestBytes,
            long proxyResponseBytes,
            long proxyTotalBytes,
            double proxyTotalMb,
            double proxyPricePerGbUsd,
            double proxyEstimatedCostUsd,
            String proxyHttpStatusesJson,
            double proxyElapsedSeconds
    ) {
        this.proxyRoute = proxyRoute == null || proxyRoute.isBlank() ? "unknown" : proxyRoute;
        this.proxyRequestCount = proxyRequestCount;
        this.proxyRequestBytes = proxyRequestBytes;
        this.proxyResponseBytes = proxyResponseBytes;
        this.proxyTotalBytes = proxyTotalBytes;
        this.proxyTotalMb = proxyTotalMb;
        this.proxyPricePerGbUsd = proxyPricePerGbUsd;
        this.proxyEstimatedCostUsd = proxyEstimatedCostUsd;
        this.proxyHttpStatusesJson = proxyHttpStatusesJson == null || proxyHttpStatusesJson.isBlank() ? "{}" : proxyHttpStatusesJson;
        this.proxyElapsedSeconds = proxyElapsedSeconds;
    }

    public void addSegment(YoutubeTranscriptSegmentEntity segment) {
        segments.add(segment);
        segment.attachTo(this);
    }
}
