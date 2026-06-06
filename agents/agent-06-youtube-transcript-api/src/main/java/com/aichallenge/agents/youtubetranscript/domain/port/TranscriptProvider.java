package com.aichallenge.agents.youtubetranscript.domain.port;

import com.aichallenge.agents.youtubetranscript.domain.ProviderTranscript;

import java.util.List;

public interface TranscriptProvider {
    ProviderTranscript getTranscript(String videoId, List<String> preferredLanguages);
}
