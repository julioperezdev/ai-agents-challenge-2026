package com.aichallenge.agents.youtubetranscript.application;

import com.aichallenge.agents.youtubetranscript.domain.ImportantSegment;
import com.aichallenge.agents.youtubetranscript.domain.ProjectApplication;
import com.aichallenge.agents.youtubetranscript.domain.Transcript;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptProxyUsage;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptSegment;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptSource;
import com.aichallenge.agents.youtubetranscript.domain.VideoLearningAnalysis;
import com.aichallenge.agents.youtubetranscript.domain.port.TranscriptRepository;
import com.aichallenge.agents.youtubetranscript.domain.port.VideoLearningAnalysisGenerator;
import com.aichallenge.agents.youtubetranscript.domain.port.VideoLearningAnalysisRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyzeYoutubeLearningVideoUseCaseTest {

    @Test
    void returnsCachedAnalysisWhenAvailable() {
        var analysisRepository = new FakeAnalysisRepository(Optional.of(analysis(3L)));
        var useCase = new AnalyzeYoutubeLearningVideoUseCase(
                new FakeTranscriptRepository(Optional.of(transcript())),
                analysisRepository,
                transcript -> analysis(99L)
        );

        VideoLearningAnalysisResponse response = useCase.execute("FWEInOtngmM", false);

        assertThat(response.status()).isEqualTo("ANALYSIS_ALREADY_EXISTS");
        assertThat(response.fromCache()).isTrue();
        assertThat(response.analysisId()).isEqualTo(3L);
        assertThat(analysisRepository.saved).isNull();
    }

    @Test
    void createsAnalysisWhenCacheMisses() {
        var analysisRepository = new FakeAnalysisRepository(Optional.empty());
        var useCase = new AnalyzeYoutubeLearningVideoUseCase(
                new FakeTranscriptRepository(Optional.of(transcript())),
                analysisRepository,
                transcript -> analysis(null)
        );

        VideoLearningAnalysisResponse response = useCase.execute("FWEInOtngmM", false);

        assertThat(response.status()).isEqualTo("ANALYSIS_CREATED");
        assertThat(response.fromCache()).isFalse();
        assertThat(response.summary()).isEqualTo("Resumen");
        assertThat(analysisRepository.saved).isNotNull();
    }

    private Transcript transcript() {
        return new Transcript(
                1L,
                "FWEInOtngmM",
                "en",
                TranscriptSource.YOUTUBE_CAPTIONS,
                true,
                "YOUTUBE_TRANSCRIPT_METADATA",
                false,
                "Transcript text",
                List.of(new TranscriptSegment(0, 0.0, 1.0, "Transcript text")),
                TranscriptProxyUsage.empty()
        );
    }

    private VideoLearningAnalysis analysis(Long id) {
        return new VideoLearningAnalysis(
                id,
                "FWEInOtngmM",
                1L,
                "en",
                "es",
                "LOCAL",
                "extractive-v1",
                "Resumen",
                List.of("Idea"),
                List.of(new ProjectApplication("Aplicacion", "Importancia")),
                List.of(new ImportantSegment(0.0, 1.0, "Razon")),
                List.of("Nota"),
                List.of("Accion"),
                "local-extractive-v1",
                LocalDateTime.now()
        );
    }

    private static class FakeTranscriptRepository implements TranscriptRepository {
        private final Optional<Transcript> transcript;

        private FakeTranscriptRepository(Optional<Transcript> transcript) {
            this.transcript = transcript;
        }

        @Override
        public Optional<Transcript> findFirstByVideoIdAndPreferredLanguages(String videoId, List<String> preferredLanguages) {
            return transcript;
        }

        @Override
        public Optional<Transcript> findFirstByVideoId(String videoId) {
            return transcript;
        }

        @Override
        public Transcript save(String videoId, String originalUrl, Transcript transcript) {
            return transcript;
        }
    }

    private static class FakeAnalysisRepository implements VideoLearningAnalysisRepository {
        private final Optional<VideoLearningAnalysis> cached;
        private VideoLearningAnalysis saved;

        private FakeAnalysisRepository(Optional<VideoLearningAnalysis> cached) {
            this.cached = cached;
        }

        @Override
        public Optional<VideoLearningAnalysis> findLatestByVideoId(String videoId) {
            return cached;
        }

        @Override
        public VideoLearningAnalysis save(VideoLearningAnalysis analysis) {
            saved = analysis;
            return new VideoLearningAnalysis(
                    44L,
                    analysis.videoId(),
                    analysis.transcriptId(),
                    analysis.sourceLanguage(),
                    analysis.outputLanguage(),
                    analysis.provider(),
                    analysis.model(),
                    analysis.summary(),
                    analysis.keyIdeas(),
                    analysis.projectApplications(),
                    analysis.importantSegments(),
                    analysis.personalLearningNotes(),
                    analysis.suggestedActions(),
                    analysis.promptVersion(),
                    analysis.createdAt()
            );
        }
    }
}
