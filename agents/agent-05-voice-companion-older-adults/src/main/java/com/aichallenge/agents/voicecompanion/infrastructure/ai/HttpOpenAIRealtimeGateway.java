package com.aichallenge.agents.voicecompanion.infrastructure.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class HttpOpenAIRealtimeGateway implements OpenAIRealtimeGateway {
  private static final URI REALTIME_CALLS_URI = URI.create("https://api.openai.com/v1/realtime/calls");

  private final OpenAIRealtimeProperties properties;
  private final HttpClient httpClient;

  public HttpOpenAIRealtimeGateway(OpenAIRealtimeProperties properties) {
    this.properties = properties;
    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  }

  @Override
  public String createCall(String offerSdp, String sessionConfigJson) {
    String boundary = "----voice-companion-" + UUID.randomUUID();
    byte[] body = multipartBody(boundary, offerSdp, sessionConfigJson);

    HttpRequest request = HttpRequest.newBuilder(REALTIME_CALLS_URI)
        .timeout(Duration.ofSeconds(30))
        .header("Authorization", "Bearer " + properties.requiredApiKey())
        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
        .POST(HttpRequest.BodyPublishers.ofByteArray(body))
        .build();

    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new OpenAIRealtimeException(response.statusCode(), response.body());
      }
      return response.body();
    } catch (IOException exception) {
      throw new IllegalStateException("Could not connect to OpenAI Realtime API.", exception);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("OpenAI Realtime API call was interrupted.", exception);
    }
  }

  private byte[] multipartBody(String boundary, String offerSdp, String sessionConfigJson) {
    String body = "--" + boundary + "\r\n"
        + "Content-Disposition: form-data; name=\"sdp\"\r\n"
        + "Content-Type: application/sdp\r\n\r\n"
        + offerSdp + "\r\n"
        + "--" + boundary + "\r\n"
        + "Content-Disposition: form-data; name=\"session\"\r\n"
        + "Content-Type: application/json\r\n\r\n"
        + sessionConfigJson + "\r\n"
        + "--" + boundary + "--\r\n";
    return body.getBytes(StandardCharsets.UTF_8);
  }
}
