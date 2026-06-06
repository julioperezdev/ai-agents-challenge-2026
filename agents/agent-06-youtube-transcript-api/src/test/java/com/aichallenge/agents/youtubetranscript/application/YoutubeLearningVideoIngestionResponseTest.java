package com.aichallenge.agents.youtubetranscript.application;

import com.aichallenge.agents.youtubetranscript.domain.Transcript;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptProxyUsage;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptSegment;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YoutubeLearningVideoIngestionResponseTest {

    @Test
    void mapsFreshTranscriptToCompactIngestionResponse() {
        YoutubeTranscriptResponse transcriptResponse = YoutubeTranscriptResponse.found(transcript(), false, insight());

        YoutubeLearningVideoIngestionResponse response = YoutubeLearningVideoIngestionResponse.fromTranscript(
                transcriptResponse,
                "https://www.youtube.com/watch?v=dtAJ2dOd3ko"
        );

        assertThat(response.status()).isEqualTo("VIDEO_INGESTED");
        assertThat(response.videoId()).isEqualTo("dtAJ2dOd3ko");
        assertThat(response.language()).isEqualTo("en");
        assertThat(response.fromCache()).isFalse();
        assertThat(response.transcriptStored()).isTrue();
        assertThat(response.segmentsStored()).isEqualTo(2);
        assertThat(response.readyForAnalysis()).isTrue();
        assertThat(response.nextActions()).containsExactly(new NextActionResponse(
                "ANALYZE_FOR_LEARNING",
                "POST",
                "/api/v1/learning/youtube/videos/dtAJ2dOd3ko/analysis"
        ));
    }

    @Test
    void mapsCachedTranscriptToAlreadyIngestedResponse() {
        YoutubeTranscriptResponse transcriptResponse = YoutubeTranscriptResponse.found(transcript(), true, insight());

        YoutubeLearningVideoIngestionResponse response = YoutubeLearningVideoIngestionResponse.fromTranscript(
                transcriptResponse,
                "https://www.youtube.com/watch?v=dtAJ2dOd3ko"
        );

        assertThat(response.status()).isEqualTo("VIDEO_ALREADY_INGESTED");
        assertThat(response.fromCache()).isTrue();
        assertThat(response.transcriptStored()).isTrue();
        assertThat(response.readyForAnalysis()).isTrue();
    }

    private Transcript transcript() {
        return new Transcript(
                1L,
                "dtAJ2dOd3ko",
                "en",
                TranscriptSource.YOUTUBE_CAPTIONS,
                true,
                "YOUTUBE_TRANSCRIPT_METADATA",
                false,
                "Original transcript text.",
                List.of(
                        new TranscriptSegment(0, 0.0, 2.0, "Original"),
                        new TranscriptSegment(1, 2.0, 2.0, "transcript")
                ),
                TranscriptProxyUsage.empty()
        );
    }

    private TranscriptInsightResponse insight() {
        return new TranscriptInsightResponse(
                "en",
                "es",
                "YOUTUBE_TRANSCRIPT_METADATA",
                false,
                "Original transcript text.",
                "Use fullText as context.",
                "Explicacion en castellano.",
                List.of("Idea clave")
        );
    }
}
