package com.aichallenge.agents.youtubetranscript.application;

import com.aichallenge.agents.youtubetranscript.domain.ProviderTranscript;
import com.aichallenge.agents.youtubetranscript.domain.Transcript;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptSegment;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptStatus;
import com.aichallenge.agents.youtubetranscript.domain.port.TranscriptProvider;
import com.aichallenge.agents.youtubetranscript.domain.port.TranscriptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class GetYoutubeTranscriptUseCase {

    private final TranscriptRepository transcriptRepository;
    private final TranscriptProvider transcriptProvider;
    private final SpanishTranscriptInsightGenerator insightGenerator;
    private final YoutubeVideoIdExtractor videoIdExtractor;

    public GetYoutubeTranscriptUseCase(
            TranscriptRepository transcriptRepository,
            TranscriptProvider transcriptProvider,
            SpanishTranscriptInsightGenerator insightGenerator
    ) {
        this.transcriptRepository = transcriptRepository;
        this.transcriptProvider = transcriptProvider;
        this.insightGenerator = insightGenerator;
        this.videoIdExtractor = new YoutubeVideoIdExtractor();
    }

    @Transactional
    public YoutubeTranscriptResponse execute(YoutubeTranscriptRequest request) {
        List<String> preferredLanguages = request.normalizedLanguages();
        return videoIdExtractor.extract(request.url())
                .map(videoId -> getTranscript(request, videoId, preferredLanguages))
                .orElseGet(() -> YoutubeTranscriptResponse.failed(
                        TranscriptStatus.INVALID_YOUTUBE_URL,
                        null,
                        "The provided URL is not a valid YouTube video URL"
                ));
    }

    private YoutubeTranscriptResponse getTranscript(
            YoutubeTranscriptRequest request,
            String videoId,
            List<String> preferredLanguages
    ) {
        if (!request.shouldForceRefresh()) {
            var cached = transcriptRepository.findFirstByVideoIdAndPreferredLanguages(videoId, preferredLanguages);
            if (cached.isPresent()) {
                return YoutubeTranscriptResponse.found(cached.get(), true, insightGenerator.generate(cached.get()));
            }
        }

        ProviderTranscript providerTranscript = transcriptProvider.getTranscript(videoId, preferredLanguages);
        if (providerTranscript.status() != TranscriptStatus.TRANSCRIPT_FOUND) {
            return YoutubeTranscriptResponse.failed(providerTranscript.status(), videoId, providerTranscript.reason());
        }

        Transcript saved = transcriptRepository.save(videoId, request.url(), toTranscript(providerTranscript));
        return YoutubeTranscriptResponse.found(saved, false, insightGenerator.generate(saved));
    }

    private Transcript toTranscript(ProviderTranscript providerTranscript) {
        var orderedSegments = providerTranscript.segments().stream()
                .sorted(Comparator.comparingDouble(segment -> segment.start()))
                .toList();

        List<TranscriptSegment> segments = IntStream.range(0, orderedSegments.size())
                .mapToObj(position -> {
                    var segment = orderedSegments.get(position);
                    return new TranscriptSegment(
                            position,
                            segment.start(),
                            segment.duration(),
                            normalizeSegmentText(segment.text())
                    );
                })
                .toList();

        return new Transcript(
                null,
                providerTranscript.videoId(),
                providerTranscript.language(),
                providerTranscript.source(),
                providerTranscript.generated(),
                providerTranscript.languageDetectionMethod() == null ? "FIRST_20_WORDS_CONTEXT_HINT" : providerTranscript.languageDetectionMethod(),
                providerTranscript.languageFallbackUsed(),
                buildFullText(segments),
                segments,
                providerTranscript.proxyUsage()
        );
    }

    private String buildFullText(List<TranscriptSegment> segments) {
        return segments.stream()
                .map(TranscriptSegment::text)
                .map(this::normalizeSegmentText)
                .reduce("", (left, right) -> (left + " " + right).trim())
                .replaceAll("\\s+", " ");
    }

    private String normalizeSegmentText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace('\n', ' ').replaceAll("\\s+", " ").trim();
    }
}
