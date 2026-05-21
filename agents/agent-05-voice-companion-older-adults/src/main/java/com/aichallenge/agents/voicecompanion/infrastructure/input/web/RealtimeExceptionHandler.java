package com.aichallenge.agents.voicecompanion.infrastructure.input.web;

import com.aichallenge.agents.voicecompanion.infrastructure.ai.OpenAIRealtimeException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RealtimeExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(RealtimeExceptionHandler.class);
  private static final Pattern OPENAI_MESSAGE = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]+)\"");

  @ExceptionHandler(OpenAIRealtimeException.class)
  public ResponseEntity<ApiError> handleOpenAIRealtime(OpenAIRealtimeException exception) {
    log.warn("OpenAI Realtime rejected request. status={} body={}", exception.statusCode(), exception.responseBody());
    HttpStatus status = exception.statusCode() == 401 || exception.statusCode() == 403
        ? HttpStatus.BAD_GATEWAY
        : HttpStatus.SERVICE_UNAVAILABLE;
    return ResponseEntity.status(status).body(new ApiError(userMessage(exception), exception.statusCode()));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiError> handleIllegalState(IllegalStateException exception) {
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ApiError(exception.getMessage(), null));
  }

  private String userMessage(OpenAIRealtimeException exception) {
    String openAiMessage = extractOpenAIMessage(exception.responseBody());
    if (openAiMessage.contains("gpt-realtime") && openAiMessage.contains("does not exist or you do not have access")) {
      return "OpenAI rejected the configured Realtime model. Check that your API project has access to that model, or set OPENAI_REALTIME_MODEL to a Realtime model available to your project and restart Spring Boot.";
    }
    return openAiMessage.isBlank() ? exception.getMessage() : openAiMessage;
  }

  private String extractOpenAIMessage(String body) {
    Matcher matcher = OPENAI_MESSAGE.matcher(body == null ? "" : body);
    return matcher.find() ? matcher.group(1) : "";
  }

  public record ApiError(String message, Integer openAiStatus) {}
}
