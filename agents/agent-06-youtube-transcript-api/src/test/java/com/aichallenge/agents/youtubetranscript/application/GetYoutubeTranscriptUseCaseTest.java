package com.aichallenge.agents.youtubetranscript.application;

import com.aichallenge.agents.youtubetranscript.domain.ProviderTranscript;
import com.aichallenge.agents.youtubetranscript.domain.ProviderTranscriptSegment;
import com.aichallenge.agents.youtubetranscript.domain.Transcript;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptProxyUsage;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptSegment;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptSource;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptStatus;
import com.aichallenge.agents.youtubetranscript.domain.port.TranscriptProvider;
import com.aichallenge.agents.youtubetranscript.domain.port.TranscriptRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GetYoutubeTranscriptUseCaseTest {

    @Test
    void returnsCachedTranscriptWhenAvailable() {
        FakeRepository repository = new FakeRepository(Optional.of(savedTranscript(true)));
        FakeProvider provider = new FakeProvider();
        GetYoutubeTranscriptUseCase useCase = new GetYoutubeTranscriptUseCase(repository, provider, new SpanishTranscriptInsightGenerator());

        YoutubeTranscriptResponse response = useCase.execute(new YoutubeTranscriptRequest(
                "https://www.youtube.com/watch?v=FWEInOtngmM",
                List.of("es", "en"),
                false
        ));

        assertThat(response.status()).isEqualTo(TranscriptStatus.TRANSCRIPT_FOUND);
        assertThat(response.fromCache()).isTrue();
        assertThat(provider.calls).isZero();
    }

    @Test
    void callsProviderAndSavesWhenCacheMisses() {
        FakeRepository repository = new FakeRepository(Optional.empty());
        FakeProvider provider = new FakeProvider();
        GetYoutubeTranscriptUseCase useCase = new GetYoutubeTranscriptUseCase(repository, provider, new SpanishTranscriptInsightGenerator());

        YoutubeTranscriptResponse response = useCase.execute(new YoutubeTranscriptRequest(
                "https://youtu.be/FWEInOtngmM",
                List.of("es"),
                false
        ));

        assertThat(response.status()).isEqualTo(TranscriptStatus.TRANSCRIPT_FOUND);
        assertThat(response.fromCache()).isFalse();
        assertThat(response.fullText()).isEqualTo("Hola mundo Segundo segmento");
        assertThat(response.segments()).extracting(TranscriptSegmentResponse::position).containsExactly(0, 1);
        assertThat(response.insight().outputLanguage()).isEqualTo("es");
        assertThat(provider.calls).isEqualTo(1);
        assertThat(repository.saved).isNotNull();
    }

    @Test
    void usesOriginalTranscriptWhenPreferredLanguagesAreOmitted() {
        FakeRepository repository = new FakeRepository(Optional.empty());
        FakeProvider provider = new FakeProvider("en", false);
        GetYoutubeTranscriptUseCase useCase = new GetYoutubeTranscriptUseCase(repository, provider, new SpanishTranscriptInsightGenerator());

        YoutubeTranscriptResponse response = useCase.execute(new YoutubeTranscriptRequest(
                "https://youtu.be/FWEInOtngmM",
                null,
                false
        ));

        assertThat(provider.requestedLanguages).isEmpty();
        assertThat(response.language()).isEqualTo("en");
        assertThat(response.insight().contextLanguage()).isEqualTo("en");
        assertThat(response.insight().outputLanguage()).isEqualTo("es");
        assertThat(response.insight().languageFallbackUsed()).isFalse();
    }


    @Test
    void usesOriginalTranscriptLanguageAsContextWhenPreferredLanguageIsUnavailable() {
        FakeRepository repository = new FakeRepository(Optional.empty());
        FakeProvider provider = new FakeProvider("en", true);
        GetYoutubeTranscriptUseCase useCase = new GetYoutubeTranscriptUseCase(repository, provider, new SpanishTranscriptInsightGenerator());

        YoutubeTranscriptResponse response = useCase.execute(new YoutubeTranscriptRequest(
                "https://youtu.be/FWEInOtngmM",
                List.of("es"),
                false
        ));

        assertThat(response.language()).isEqualTo("en");
        assertThat(response.insight().contextLanguage()).isEqualTo("en");
        assertThat(response.insight().outputLanguage()).isEqualTo("es");
        assertThat(response.insight().languageFallbackUsed()).isTrue();
        assertThat(response.insight().llmContextPreview()).startsWith("Hola mundo");
    }

    @Test
    void forceRefreshSkipsCache() {
        FakeRepository repository = new FakeRepository(Optional.of(savedTranscript(true)));
        FakeProvider provider = new FakeProvider();
        GetYoutubeTranscriptUseCase useCase = new GetYoutubeTranscriptUseCase(repository, provider, new SpanishTranscriptInsightGenerator());

        YoutubeTranscriptResponse response = useCase.execute(new YoutubeTranscriptRequest(
                "https://www.youtube.com/watch?v=FWEInOtngmM",
                List.of("es"),
                true
        ));

        assertThat(response.fromCache()).isFalse();
        assertThat(provider.calls).isEqualTo(1);
    }

    @Test
    void returnsInvalidYoutubeUrlForUnsupportedUrl() {
        GetYoutubeTranscriptUseCase useCase = new GetYoutubeTranscriptUseCase(new FakeRepository(Optional.empty()), new FakeProvider(), new SpanishTranscriptInsightGenerator());

        YoutubeTranscriptResponse response = useCase.execute(new YoutubeTranscriptRequest(
                "https://example.com/watch?v=FWEInOtngmM",
                null,
                false
        ));

        assertThat(response.status()).isEqualTo(TranscriptStatus.INVALID_YOUTUBE_URL);
    }

    private static Transcript savedTranscript(boolean generated) {
        return new Transcript(
                1L,
                "FWEInOtngmM",
                "es",
                TranscriptSource.YOUTUBE_CAPTIONS,
                generated,
                "STORED_TRANSCRIPT_LANGUAGE",
                false,
                "cached text",
                List.of(new TranscriptSegment(0, 0.0, 1.0, "cached text")),
                TranscriptProxyUsage.empty()
        );
    }

    private static class FakeRepository implements TranscriptRepository {
        private final Optional<Transcript> cached;
        private Transcript saved;

        private FakeRepository(Optional<Transcript> cached) {
            this.cached = cached;
        }

        @Override
        public Optional<Transcript> findFirstByVideoIdAndPreferredLanguages(String videoId, List<String> preferredLanguages) {
            return cached;
        }

        @Override
        public Optional<Transcript> findFirstByVideoId(String videoId) {
            return cached;
        }

        @Override
        public Transcript save(String videoId, String originalUrl, Transcript transcript) {
            saved = transcript;
            return new Transcript(
                    99L,
                    videoId,
                    transcript.language(),
                    transcript.source(),
                    transcript.generated(),
                    transcript.languageDetectionMethod(),
                    transcript.languageFallbackUsed(),
                    transcript.fullText(),
                    transcript.segments(),
                    transcript.proxyUsage()
            );
        }
    }

    private static class FakeProvider implements TranscriptProvider {
        private int calls;
        private final String language;
        private final boolean languageFallbackUsed;
        private List<String> requestedLanguages = List.of();

        private FakeProvider() {
            this("es", false);
        }

        private FakeProvider(String language, boolean languageFallbackUsed) {
            this.language = language;
            this.languageFallbackUsed = languageFallbackUsed;
        }

        @Override
        public ProviderTranscript getTranscript(String videoId, List<String> preferredLanguages) {
            calls++;
            requestedLanguages = preferredLanguages;
            return ProviderTranscript.found(
                    videoId,
                    language,
                    true,
                    "YOUTUBE_TRANSCRIPT_METADATA",
                    languageFallbackUsed,
                    List.of(
                            new ProviderTranscriptSegment(4.2, 2.0, "Segundo\nsegmento"),
                            new ProviderTranscriptSegment(0.0, 4.2, "Hola   mundo")
                    ),
                    new TranscriptProxyUsage("webshare", 2, 100, 900, 1000, 0.001, 3.5, 0.000003, "{\"200\":2}", 0.3)
            );
        }
    }
}
