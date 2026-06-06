package com.aichallenge.agents.youtubetranscript.application;

import com.aichallenge.agents.youtubetranscript.domain.port.LearningVideoLibraryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListYoutubeLearningVideosUseCase {

    private final LearningVideoLibraryRepository learningVideoLibraryRepository;

    public ListYoutubeLearningVideosUseCase(LearningVideoLibraryRepository learningVideoLibraryRepository) {
        this.learningVideoLibraryRepository = learningVideoLibraryRepository;
    }

    @Transactional(readOnly = true)
    public List<YoutubeLearningVideoListItemResponse> execute() {
        return learningVideoLibraryRepository.listRecentVideos()
                .stream()
                .map(YoutubeLearningVideoListItemResponse::fromLibraryItem)
                .toList();
    }
}
