package com.aichallenge.agents.voicecompanion.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DemoScriptParserTest {
  @Test
  void extractsUserTurnsFromMarkdown() {
    var turns = new DemoScriptParser().parse("""
        # Demo
        Persona: Hola.
        Agente: Buen dia.
        Usuario: Que tengo hoy?
        """);

    assertThat(turns).hasSize(2);
    assertThat(turns.get(0).text()).isEqualTo("Hola.");
    assertThat(turns.get(1).text()).isEqualTo("Que tengo hoy?");
  }
}
