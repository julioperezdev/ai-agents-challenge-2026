package com.aichallenge.agents.youtubetranscript.application;

import com.aichallenge.agents.youtubetranscript.domain.Transcript;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptProxyUsage;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptSegment;
import com.aichallenge.agents.youtubetranscript.domain.TranscriptSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocalExtractiveVideoLearningAnalysisGeneratorTest {

    @Test
    void createsSpanishLearningAnalysisFromOriginalTranscript() {
        LocalExtractiveVideoLearningAnalysisGenerator generator = new LocalExtractiveVideoLearningAnalysisGenerator();

        var analysis = generator.analyze(new Transcript(
                7L,
                "FWEInOtngmM",
                "en",
                TranscriptSource.YOUTUBE_CAPTIONS,
                true,
                "YOUTUBE_TRANSCRIPT_METADATA",
                false,
                "Agents can use handoff documents to preserve context. Skills package repeatable coding practices. Compacting helps recover a long session.",
                List.of(
                        new TranscriptSegment(0, 0.0, 3.2, "Agents can use handoff documents to preserve context."),
                        new TranscriptSegment(1, 3.2, 4.0, "Skills package repeatable coding practices.")
                ),
                TranscriptProxyUsage.empty()
        ));

        assertThat(analysis.videoId()).isEqualTo("FWEInOtngmM");
        assertThat(analysis.sourceLanguage()).isEqualTo("en");
        assertThat(analysis.outputLanguage()).isEqualTo("es");
        assertThat(analysis.provider()).isEqualTo("LOCAL");
        assertThat(analysis.summary()).contains("castellano");
        assertThat(analysis.keyIdeas()).isNotEmpty();
        assertThat(analysis.projectApplications()).isNotEmpty();
        assertThat(analysis.importantSegments()).isNotEmpty();
    }

    @Test
    void selectsIdeasDistributedAcrossLongVideos() {
        LocalExtractiveVideoLearningAnalysisGenerator generator = new LocalExtractiveVideoLearningAnalysisGenerator();
        List<TranscriptSegment> segments = List.of(
                new TranscriptSegment(0, 10.0, 3.2, "Al inicio cuento la presion familiar y por que necesitaba construir una startup."),
                new TranscriptSegment(1, 90.0, 4.0, "Seguimos con contexto personal antes de entrar al negocio."),
                new TranscriptSegment(2, 900.0, 5.0, "La empresa encontro clientes cuando enfocamos el producto en agentes de IA para automatizar trabajo real."),
                new TranscriptSegment(3, 1800.0, 5.0, "El fundraising fue dificil porque los inversionistas preguntaban por revenue, mercado y defensibilidad."),
                new TranscriptSegment(4, 2700.0, 5.0, "Aprendi que contratar equipo senior cambia la velocidad de ejecucion y evita errores caros."),
                new TranscriptSegment(5, 3600.0, 5.0, "La leccion final fue convertir la historia personal en disciplina de negocio y producto.")
        );

        var analysis = generator.analyze(new Transcript(
                8L,
                "longVideoId",
                "es",
                TranscriptSource.YOUTUBE_CAPTIONS,
                false,
                "YOUTUBE_TRANSCRIPT_METADATA",
                false,
                segments.stream().map(TranscriptSegment::text).reduce("", (left, right) -> left + " " + right),
                segments,
                TranscriptProxyUsage.empty()
        ));

        assertThat(analysis.promptVersion()).isEqualTo("local-extractive-v3");
        assertThat(analysis.summary()).contains("6 segmentos");
        assertThat(analysis.keyIdeas()).anyMatch(idea -> idea.contains("15:00"));
        assertThat(analysis.keyIdeas()).anyMatch(idea -> idea.contains("30:00"));
        assertThat(analysis.importantSegments())
                .extracting(segment -> segment.start())
                .contains(900.0, 1800.0, 2700.0);
    }

    @Test
    void groupsShortCaptionFragmentsIntoReadableContextWindows() {
        LocalExtractiveVideoLearningAnalysisGenerator generator = new LocalExtractiveVideoLearningAnalysisGenerator();
        List<TranscriptSegment> segments = List.of(
                new TranscriptSegment(0, 120.0, 1.2, "Nexor es"),
                new TranscriptSegment(1, 121.2, 1.3, "un equipo de agentes"),
                new TranscriptSegment(2, 122.5, 1.4, "de inteligencia artificial"),
                new TranscriptSegment(3, 123.9, 1.8, "que automatiza flujos de trabajo"),
                new TranscriptSegment(4, 125.7, 1.4, "para clientes reales"),
                new TranscriptSegment(5, 900.0, 2.0, "Luego hablamos de startup, producto, mercado y aprendizaje comercial.")
        );

        var analysis = generator.analyze(new Transcript(
                9L,
                "fragmentedVideo",
                "es",
                TranscriptSource.YOUTUBE_CAPTIONS,
                false,
                "YOUTUBE_TRANSCRIPT_METADATA",
                false,
                segments.stream().map(TranscriptSegment::text).reduce("", (left, right) -> left + " " + right),
                segments,
                TranscriptProxyUsage.empty()
        ));

        assertThat(analysis.keyIdeas())
                .anyMatch(idea -> idea.contains("Nexor es un equipo de agentes de inteligencia artificial que automatiza flujos de trabajo"));
    }
}
