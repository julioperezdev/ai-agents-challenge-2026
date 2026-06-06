package com.aichallenge.agents.youtubetranscript.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class YoutubeVideoIdExtractorTest {

    private final YoutubeVideoIdExtractor extractor = new YoutubeVideoIdExtractor();

    @Test
    void extractsSupportedYoutubeUrls() {
        assertThat(extractor.extract("https://www.youtube.com/watch?v=FWEInOtngmM")).contains("FWEInOtngmM");
        assertThat(extractor.extract("https://youtube.com/watch?v=FWEInOtngmM")).contains("FWEInOtngmM");
        assertThat(extractor.extract("https://youtu.be/FWEInOtngmM")).contains("FWEInOtngmM");
        assertThat(extractor.extract("https://www.youtube.com/shorts/FWEInOtngmM")).contains("FWEInOtngmM");
        assertThat(extractor.extract("https://youtube.com/shorts/FWEInOtngmM")).contains("FWEInOtngmM");
    }

    @Test
    void rejectsInvalidUrls() {
        assertThat(extractor.extract("https://example.com/watch?v=FWEInOtngmM")).isEmpty();
        assertThat(extractor.extract("https://www.youtube.com/watch")).isEmpty();
        assertThat(extractor.extract("not a url")).isEmpty();
    }
}
