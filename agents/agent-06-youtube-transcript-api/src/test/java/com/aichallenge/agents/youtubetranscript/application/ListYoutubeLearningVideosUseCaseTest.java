package com.aichallenge.agents.youtubetranscript.application;

import com.aichallenge.agents.youtubetranscript.domain.LearningVideoLibraryItem;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptProxyUsage;
import com.aichallenge.agents.youtubetranscript.domain.port.LearningVideoLibraryRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListYoutubeLearningVideosUseCaseTest {

    @Test
    void listsVideosStoredInTheLearningLibrary() {
        LocalDateTime now = LocalDateTime.of(2026, 6, 4, 9, 20);
        LearningVideoLibraryRepository repository = () -> List.of(new LearningVideoLibraryItem(
                "FWEInOtngmM",
                "https://www.youtube.com/watch?v=FWEInOtngmM",
                "en",
                false,
                true,
                477,
                new TranscriptProxyUsage("webshare", 3, 1000, 9000, 10000, 0.0095, 3.5, 0.000033, "{\"200\":3}", 1.2),
                true,
                12L,
                now.minusDays(1),
                now
        ));

        ListYoutubeLearningVideosUseCase useCase = new ListYoutubeLearningVideosUseCase(repository);

        List<YoutubeLearningVideoListItemResponse> response = useCase.execute();

        assertThat(response).singleElement().satisfies(item -> {
            assertThat(item.videoId()).isEqualTo("FWEInOtngmM");
            assertThat(item.url()).isEqualTo("https://www.youtube.com/watch?v=FWEInOtngmM");
            assertThat(item.language()).isEqualTo("en");
            assertThat(item.transcriptStored()).isTrue();
            assertThat(item.segmentsStored()).isEqualTo(477);
            assertThat(item.proxyUsage().route()).isEqualTo("webshare");
            assertThat(item.proxyUsage().totalBytes()).isEqualTo(10000);
            assertThat(item.analysisAvailable()).isTrue();
            assertThat(item.latestAnalysisId()).isEqualTo(12L);
        });
    }
}
