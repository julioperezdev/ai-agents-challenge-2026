package com.aichallenge.agents.youtubetranscript.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "youtube_transcript_segment")
public class YoutubeTranscriptSegmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transcript_id", nullable = false)
    private YoutubeTranscriptEntity transcript;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "start_time", nullable = false)
    private Double start;

    @Column(name = "duration", nullable = false)
    private Double duration;

    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    protected YoutubeTranscriptSegmentEntity() {
    }

    public YoutubeTranscriptSegmentEntity(Integer position, Double start, Double duration, String text) {
        this.position = position;
        this.start = start;
        this.duration = duration;
        this.text = text;
    }

    public Integer getPosition() {
        return position;
    }

    public Double getStart() {
        return start;
    }

    public Double getDuration() {
        return duration;
    }

    public String getText() {
        return text;
    }

    void attachTo(YoutubeTranscriptEntity transcript) {
        this.transcript = transcript;
    }
}
