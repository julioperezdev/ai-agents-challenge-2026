package com.aichallenge.agents.youtubetranscript.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "youtube_video")
public class YoutubeVideoEntity {

    @Id
    @Column(name = "video_id", length = 50, nullable = false)
    private String videoId;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected YoutubeVideoEntity() {
    }

    public YoutubeVideoEntity(String videoId, String originalUrl, LocalDateTime now) {
        this.videoId = videoId;
        this.originalUrl = originalUrl;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void updateOriginalUrl(String originalUrl, LocalDateTime now) {
        this.originalUrl = originalUrl;
        this.updatedAt = now;
    }
}
