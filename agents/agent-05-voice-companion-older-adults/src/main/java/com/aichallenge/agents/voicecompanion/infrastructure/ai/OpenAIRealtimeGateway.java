package com.aichallenge.agents.voicecompanion.infrastructure.ai;

public interface OpenAIRealtimeGateway {
  String createCall(String offerSdp, String sessionConfigJson);
}
