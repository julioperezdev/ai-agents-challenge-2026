package com.aichallenge.agents.youtubetranscript.domain.port;

import com.aichallenge.agents.youtubetranscript.domain.LearningVideoLibraryItem;

import java.util.List;

public interface LearningVideoLibraryRepository {
    List<LearningVideoLibraryItem> listRecentVideos();
}
