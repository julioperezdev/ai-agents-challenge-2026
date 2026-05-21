package com.aichallenge.agents.voicecompanion.infrastructure.ai;

public class OpenAIRealtimeException extends RuntimeException {
  private final int statusCode;
  private final String responseBody;

  public OpenAIRealtimeException(int statusCode, String responseBody) {
    super("OpenAI Realtime call failed with status " + statusCode + ": " + responseBody);
    this.statusCode = statusCode;
    this.responseBody = responseBody;
  }

  public int statusCode() {
    return statusCode;
  }

  public String responseBody() {
    return responseBody;
  }
}
