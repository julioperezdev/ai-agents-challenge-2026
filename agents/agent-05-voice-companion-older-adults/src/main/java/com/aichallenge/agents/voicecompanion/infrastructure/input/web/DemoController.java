package com.aichallenge.agents.voicecompanion.infrastructure.input.web;

import com.aichallenge.agents.voicecompanion.application.ConversationSummaryService;
import com.aichallenge.agents.voicecompanion.application.LocalCompanionService;
import com.aichallenge.agents.voicecompanion.domain.ConversationSummary;
import com.aichallenge.agents.voicecompanion.domain.OlderAdultProfile;
import com.aichallenge.agents.voicecompanion.infrastructure.output.MarkdownSummaryRenderer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DemoController {
  private final LocalCompanionService localCompanionService;
  private final ConversationSummaryService summaryService;
  private final MarkdownSummaryRenderer markdownRenderer;
  private final ObjectMapper objectMapper;

  public DemoController(
      LocalCompanionService localCompanionService,
      ConversationSummaryService summaryService,
      MarkdownSummaryRenderer markdownRenderer,
      ObjectMapper objectMapper
  ) {
    this.localCompanionService = localCompanionService;
    this.summaryService = summaryService;
    this.markdownRenderer = markdownRenderer;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/health")
  public HealthResponse health() {
    return new HealthResponse("ok", "local-demo");
  }

  @GetMapping("/demo/default")
  public DemoResponse runDefaultDemo() throws IOException {
    OlderAdultProfile profile = objectMapper.readValue(
        Files.readString(Path.of("examples/older-adult-profile.example.json")),
        OlderAdultProfile.class
    );
    String demoScript = Files.readString(Path.of("examples/demo-script.example.md"));
    return runDemo(new DemoRequest(profile, demoScript));
  }

  @PostMapping("/demo/local")
  public DemoResponse runDemo(@Valid @RequestBody DemoRequest request) {
    LocalCompanionService.DemoConversation conversation =
        localCompanionService.run(request.profile(), request.demoScript());
    ConversationSummary summary =
        summaryService.generate(request.profile(), conversation.transcript(), conversation.remindersConsulted());
    return new DemoResponse(summary, markdownRenderer.render(summary));
  }

  public record DemoRequest(@NotNull OlderAdultProfile profile, @NotBlank String demoScript) {}

  public record DemoResponse(ConversationSummary summary, String markdown) {}

  public record HealthResponse(String status, String mode) {}
}
