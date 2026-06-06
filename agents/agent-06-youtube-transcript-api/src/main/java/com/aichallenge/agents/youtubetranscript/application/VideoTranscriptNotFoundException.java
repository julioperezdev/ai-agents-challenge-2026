package com.aichallenge.agents.youtubetranscript.application;

public class VideoTranscriptNotFoundException extends RuntimeException {

    public VideoTranscriptNotFoundException(String videoId) {
        super("No transcript is stored for video " + videoId);
    }
}
