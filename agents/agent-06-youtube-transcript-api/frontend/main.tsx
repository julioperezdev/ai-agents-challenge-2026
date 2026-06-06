import React, { FormEvent, useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  AlertTriangle,
  BookOpenCheck,
  Captions,
  CheckCircle2,
  Clock3,
  Database,
  ExternalLink,
  FileText,
  Languages,
  Library,
  Loader2,
  PlayCircle,
  RefreshCw,
  Search,
  Server,
  Sparkles,
  Tags
} from "lucide-react";
import "./style.css";

interface NextActionResponse {
  type: string;
  method: string;
  href: string;
}

interface IngestionResponse {
  status: string;
  videoId: string | null;
  url: string | null;
  language: string | null;
  source: string | null;
  isGenerated: boolean;
  fromCache: boolean;
  transcriptStored: boolean;
  segmentsStored: number;
  readyForAnalysis: boolean;
  proxyUsage: ProxyUsageResponse | null;
  nextActions: NextActionResponse[];
  reason: string | null;
}

interface ProxyUsageResponse {
  route: string;
  requestCount: number;
  requestBytes: number;
  responseBytes: number;
  totalBytes: number;
  totalMb: number;
  proxyPricePerGbUsd: number;
  estimatedProxyCostUsd: number;
  httpStatusesJson: string;
  elapsedSeconds: number;
}

interface TranscriptSegment {
  position: number;
  start: number;
  duration: number;
  text: string;
}

interface TranscriptResponse {
  status: string;
  videoId: string | null;
  source: string | null;
  language: string | null;
  isGenerated: boolean;
  fromCache: boolean;
  fullText: string | null;
  segments: TranscriptSegment[];
  insight: {
    contextLanguage: string;
    outputLanguage: string;
    languageDetectionMethod: string;
    languageFallbackUsed: boolean;
    llmContextPreview: string;
    llmInstructions: string;
    spanishExplanation: string;
    keyPoints: string[];
  } | null;
  reason: string | null;
}

interface ProjectApplication {
  idea: string;
  whyItMatters: string;
}

interface ImportantSegment {
  start: number;
  duration: number;
  reason: string;
}

interface LearningAnalysisResponse {
  status: string;
  videoId: string;
  analysisId: number;
  analysisLanguage: string;
  sourceLanguage: string;
  provider: string;
  model: string;
  summary: string;
  keyIdeas: string[];
  projectApplications: ProjectApplication[];
  importantSegments: ImportantSegment[];
  personalLearningNotes: string[];
  suggestedActions: string[];
  promptVersion: string;
  fromCache: boolean;
  createdAt: string;
}

interface LibraryVideoItem {
  videoId: string;
  url: string;
  language: string | null;
  isGenerated: boolean;
  transcriptStored: boolean;
  segmentsStored: number;
  proxyUsage: ProxyUsageResponse | null;
  analysisAvailable: boolean;
  latestAnalysisId: number | null;
  createdAt: string;
  updatedAt: string;
}

type ApiState = "idle" | "loading" | "success" | "error";

function App() {
  const [url, setUrl] = useState("");
  const [preferredLanguages, setPreferredLanguages] = useState("");
  const [forceRefresh, setForceRefresh] = useState(false);
  const [ingestionState, setIngestionState] = useState<ApiState>("idle");
  const [libraryState, setLibraryState] = useState<ApiState>("idle");
  const [transcriptState, setTranscriptState] = useState<ApiState>("idle");
  const [analysisState, setAnalysisState] = useState<ApiState>("idle");
  const [library, setLibrary] = useState<LibraryVideoItem[]>([]);
  const [ingestion, setIngestion] = useState<IngestionResponse | null>(null);
  const [transcript, setTranscript] = useState<TranscriptResponse | null>(null);
  const [analysis, setAnalysis] = useState<LearningAnalysisResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [libraryError, setLibraryError] = useState<string | null>(null);

  const languages = useMemo(
    () =>
      preferredLanguages
        .split(",")
        .map((language) => language.trim())
        .filter(Boolean),
    [preferredLanguages]
  );

  useEffect(() => {
    void loadLibrary();
  }, []);

  async function loadLibrary() {
    setLibraryState("loading");
    setLibraryError(null);

    try {
      const response = await getJson<LibraryVideoItem[]>("/api/v1/learning/youtube/videos");
      setLibrary(response);
      setLibraryState("success");
    } catch (cause) {
      setLibraryState("error");
      setLibraryError(readError(cause));
    }
  }

  async function ingestVideo(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setTranscript(null);
    setAnalysis(null);
    setTranscriptState("idle");
    setAnalysisState("idle");
    setIngestionState("loading");

    try {
      const response = await postJson<IngestionResponse>("/api/v1/learning/youtube/videos", {
        url,
        preferredLanguages: languages.length > 0 ? languages : undefined,
        forceRefresh
      });

      setIngestion(response);
      setIngestionState(response.transcriptStored ? "success" : "error");
      await loadLibrary();
      if (!response.transcriptStored) {
        setError(response.reason ?? "No se pudo ingerir el video.");
      }
    } catch (cause) {
      setIngestionState("error");
      setError(readError(cause));
    }
  }

  async function fetchTranscriptForUrl(targetUrl: string) {
    if (!targetUrl.trim()) {
      setError("Pegá una URL primero.");
      return;
    }

    setError(null);
    setTranscriptState("loading");

    try {
      const response = await postJson<TranscriptResponse>("/api/v1/youtube/transcripts", {
        url: targetUrl,
        preferredLanguages: languages.length > 0 ? languages : undefined,
        forceRefresh: false
      });

      setTranscript(response);
      setTranscriptState(response.status === "TRANSCRIPT_FOUND" ? "success" : "error");
      if (response.status !== "TRANSCRIPT_FOUND") {
        setError(response.reason ?? "No se pudo cargar el transcript.");
      }
    } catch (cause) {
      setTranscriptState("error");
      setError(readError(cause));
    }
  }

  async function fetchTranscript() {
    await fetchTranscriptForUrl(url);
  }

  async function analyzeVideoById(videoId: string | null, forceRefresh = false) {
    if (!videoId) {
      setError("Primero ingerí un video.");
      return;
    }

    setError(null);
    setAnalysisState("loading");

    try {
      const response = await postJson<LearningAnalysisResponse>(
        `/api/v1/learning/youtube/videos/${videoId}/analysis`,
        { forceRefresh }
      );

      setAnalysis(response);
      setAnalysisState("success");
      await loadLibrary();
    } catch (cause) {
      setAnalysisState("error");
      setError(readError(cause));
    }
  }

  async function analyzeVideo(forceRefresh = false) {
    await analyzeVideoById(ingestion?.videoId ?? null, forceRefresh);
  }

  function selectLibraryItem(item: LibraryVideoItem) {
    setUrl(item.url);
    setError(null);
    setTranscript(null);
    setAnalysis(null);
    setTranscriptState("idle");
    setAnalysisState("idle");
    setIngestionState(item.transcriptStored ? "success" : "error");
    setIngestion(toIngestionResponse(item));
  }

  async function analyzeLibraryItem(item: LibraryVideoItem) {
    selectLibraryItem(item);
    await analyzeVideoById(item.videoId, false);
  }

  async function fetchLibraryTranscript(item: LibraryVideoItem) {
    selectLibraryItem(item);
    await fetchTranscriptForUrl(item.url);
  }

  const hasResult = ingestionState === "success" && ingestion;
  const youtubeUrl = ingestion?.videoId ? `https://www.youtube.com/watch?v=${ingestion.videoId}` : "";

  return (
    <div className="app-shell">
      <section className="workspace">
        <aside className="sidebar">
          <div className="brand-mark" aria-hidden="true">
            <BookOpenCheck size={30} />
          </div>
          <div>
            <p className="eyebrow">Personal Learning</p>
            <h1>YouTube Learning Library</h1>
            <p className="intro">
              Guardá videos para aprender después. El sistema persiste la transcripción original y deja el material listo para análisis en castellano.
            </p>
          </div>

          <div className="side-metrics" aria-label="Estado del flujo">
            <Metric icon={<Database size={18} />} label="Persistencia" value={hasResult ? "Guardado" : "Pendiente"} />
            <Metric icon={<Languages size={18} />} label="Idioma" value={ingestion?.language ?? "Original"} />
            <Metric icon={<Captions size={18} />} label="Segmentos" value={ingestion ? String(ingestion.segmentsStored) : "0"} />
          </div>
        </aside>

        <section className="main-panel">
          <form className="ingest-form" onSubmit={ingestVideo}>
            <div className="form-header">
              <div>
                <p className="eyebrow">Ingesta</p>
                <h2>Agregar video</h2>
              </div>
              <button className="icon-button" type="button" onClick={() => setForceRefresh((current) => !current)} title="Alternar refresh">
                <RefreshCw size={18} />
                <span>{forceRefresh ? "Refresh activo" : "Cache activo"}</span>
              </button>
            </div>

            <label className="field">
              <span>URL de YouTube</span>
              <div className="input-wrap">
                <PlayCircle size={20} />
                <input
                  value={url}
                  onChange={(event) => setUrl(event.target.value)}
                  placeholder="https://www.youtube.com/watch?v=..."
                  required
                />
              </div>
            </label>

            <div className="form-grid">
              <label className="field">
                <span>Idiomas preferidos</span>
                <div className="input-wrap">
                  <Languages size={18} />
                  <input
                    value={preferredLanguages}
                    onChange={(event) => setPreferredLanguages(event.target.value)}
                    placeholder="Opcional: es,en"
                  />
                </div>
              </label>

              <label className="toggle-row">
                <input
                  type="checkbox"
                  checked={forceRefresh}
                  onChange={(event) => setForceRefresh(event.target.checked)}
                />
                <span>Forzar nueva consulta</span>
              </label>
            </div>

            <div className="actions">
              <button type="submit" disabled={ingestionState === "loading"}>
                {ingestionState === "loading" ? <Loader2 className="spin" size={18} /> : <Database size={18} />}
                <span>Ingerir video</span>
              </button>
              <button type="button" className="secondary-button" onClick={fetchTranscript} disabled={transcriptState === "loading"}>
                {transcriptState === "loading" ? <Loader2 className="spin" size={18} /> : <FileText size={18} />}
                <span>Ver transcript</span>
              </button>
            </div>
          </form>

          <LibraryPanel
            items={library}
            state={libraryState}
            errorMessage={libraryError}
            selectedVideoId={ingestion?.videoId ?? null}
            onRefresh={loadLibrary}
            onSelect={selectLibraryItem}
            onAnalyze={analyzeLibraryItem}
            onTranscript={fetchLibraryTranscript}
          />

          {error && (
            <div className="alert" role="alert">
              <AlertTriangle size={20} />
              <span>{error}</span>
            </div>
          )}

          {ingestion && (
            <IngestionResult
              result={ingestion}
              youtubeUrl={youtubeUrl}
              analysisState={analysisState}
              onAnalyze={() => analyzeVideo(false)}
              onRefreshAnalysis={() => analyzeVideo(true)}
            />
          )}
          {analysis && <LearningAnalysisPanel analysis={analysis} />}
          {transcript && <TranscriptPanel transcript={transcript} />}
        </section>
      </section>
    </div>
  );
}

function toIngestionResponse(item: LibraryVideoItem): IngestionResponse {
  return {
    status: item.transcriptStored ? "VIDEO_ALREADY_INGESTED" : "VIDEO_REGISTERED",
    videoId: item.videoId,
    url: item.url,
    language: item.language,
    source: item.transcriptStored ? "YOUTUBE_CAPTIONS" : null,
    isGenerated: item.isGenerated,
    fromCache: true,
    transcriptStored: item.transcriptStored,
    segmentsStored: item.segmentsStored,
    readyForAnalysis: item.transcriptStored,
    proxyUsage: item.proxyUsage,
    nextActions: item.transcriptStored
      ? [
          {
            type: "ANALYZE_FOR_LEARNING",
            method: "POST",
            href: `/api/v1/learning/youtube/videos/${item.videoId}/analysis`
          }
        ]
      : [],
    reason: item.transcriptStored ? null : "El video está registrado pero todavía no tiene transcript guardado."
  };
}

function Metric({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) {
  return (
    <div className="metric">
      <span>{icon}</span>
      <div>
        <dt>{label}</dt>
        <dd>{value}</dd>
      </div>
    </div>
  );
}

function LibraryPanel({
  items,
  state,
  errorMessage,
  selectedVideoId,
  onRefresh,
  onSelect,
  onAnalyze,
  onTranscript
}: {
  items: LibraryVideoItem[];
  state: ApiState;
  errorMessage: string | null;
  selectedVideoId: string | null;
  onRefresh: () => void;
  onSelect: (item: LibraryVideoItem) => void;
  onAnalyze: (item: LibraryVideoItem) => void;
  onTranscript: (item: LibraryVideoItem) => void;
}) {
  const proxySummary = summarizeProxyUsage(items);

  return (
    <section className="library-section" aria-label="Videos guardados">
      <div className="section-title">
        <div>
          <p className="eyebrow">Biblioteca</p>
          <h2>Contenido ya insertado</h2>
        </div>
        <button className="icon-button" type="button" onClick={onRefresh} disabled={state === "loading"} title="Actualizar lista">
          {state === "loading" ? <Loader2 className="spin" size={18} /> : <RefreshCw size={18} />}
          <span>Actualizar</span>
        </button>
      </div>

      {state === "error" ? (
        <div className="empty-library" data-error="true">
          <AlertTriangle size={22} />
          <p>{errorMessage ?? "No se pudo cargar la biblioteca."}</p>
        </div>
      ) : items.length === 0 ? (
        <div className="empty-library">
          <Library size={22} />
          <p>No hay videos guardados todavía.</p>
        </div>
      ) : (
        <>
          <div className="proxy-summary" aria-label="Consumo estimado del proxy">
            <Metric icon={<Server size={18} />} label="Proxy total" value={`${proxySummary.totalMb.toFixed(3)} MB`} />
            <Metric icon={<Database size={18} />} label="Costo proxy" value={`USD ${proxySummary.totalCost.toFixed(6)}`} />
            <Metric icon={<Captions size={18} />} label="Promedio" value={`${proxySummary.averageMb.toFixed(3)} MB/video`} />
            <Metric icon={<Clock3 size={18} />} label="Muestra" value={`${proxySummary.measuredVideos}/${items.length} videos`} />
          </div>

          <div className="proxy-projection">
            <span>Proyección 1 GB</span>
            <strong>{proxySummary.estimatedVideosPerGb > 0 ? `${Math.floor(proxySummary.estimatedVideosPerGb)} videos aprox.` : "sin datos suficientes"}</strong>
          </div>

          <div className="library-list">
            {items.map((item) => (
              <article className="library-item" data-active={item.videoId === selectedVideoId} key={item.videoId}>
                <button className="library-main" type="button" onClick={() => onSelect(item)}>
                  <span className="library-icon">
                    <PlayCircle size={20} />
                  </span>
                  <span className="library-copy">
                    <strong>{item.videoId}</strong>
                    <span>{item.url}</span>
                  </span>
                </button>

                <div className="library-meta">
                  <span>
                    <Languages size={14} />
                    {item.language ?? "Original"}
                  </span>
                  <span>
                    <Captions size={14} />
                    {item.segmentsStored}
                  </span>
                  <span>
                    <Server size={14} />
                    {formatProxyUsage(item.proxyUsage, false)}
                  </span>
                  <span data-ready={item.analysisAvailable}>
                    <Sparkles size={14} />
                    {item.analysisAvailable ? "Analizado" : "Sin análisis"}
                  </span>
                  <span>
                    <Clock3 size={14} />
                    {formatDate(item.updatedAt)}
                  </span>
                </div>

                <div className="library-actions">
                  <button className="mini-button" type="button" onClick={() => onTranscript(item)} disabled={!item.transcriptStored}>
                    <FileText size={15} />
                    <span>Transcript</span>
                  </button>
                  <button className="mini-button" type="button" onClick={() => onAnalyze(item)} disabled={!item.transcriptStored}>
                    <Sparkles size={15} />
                    <span>{item.analysisAvailable ? "Ver análisis" : "Analizar"}</span>
                  </button>
                  <a className="mini-link" href={`https://www.youtube.com/watch?v=${item.videoId}`} target="_blank" rel="noreferrer">
                    <ExternalLink size={15} />
                    <span>Abrir</span>
                  </a>
                </div>
              </article>
            ))}
          </div>
        </>
      )}
    </section>
  );
}

function IngestionResult({
  result,
  youtubeUrl,
  analysisState,
  onAnalyze,
  onRefreshAnalysis
}: {
  result: IngestionResponse;
  youtubeUrl: string;
  analysisState: ApiState;
  onAnalyze: () => void;
  onRefreshAnalysis: () => void;
}) {
  return (
    <section className="result-section" aria-label="Resultado de ingesta">
      <div className="result-header">
        <div className="status-badge" data-status={result.transcriptStored ? "ok" : "error"}>
          {result.transcriptStored ? <CheckCircle2 size={18} /> : <AlertTriangle size={18} />}
          <span>{result.status}</span>
        </div>
        {youtubeUrl && (
          <a className="video-link" href={youtubeUrl} target="_blank" rel="noreferrer">
            <PlayCircle size={16} />
            Abrir video
          </a>
        )}
      </div>

      <div className="data-grid">
        <InfoTile icon={<Server size={20} />} label="Video ID" value={result.videoId ?? "-"} />
        <InfoTile icon={<Languages size={20} />} label="Idioma original" value={result.language ?? "-"} />
        <InfoTile icon={<Captions size={20} />} label="Segmentos" value={String(result.segmentsStored)} />
        <InfoTile icon={<Database size={20} />} label="Cache" value={result.fromCache ? "Reutilizado" : "Nuevo"} />
        <InfoTile icon={<Server size={20} />} label="Proxy" value={formatProxyUsage(result.proxyUsage, result.fromCache)} />
      </div>

      <div className="timeline">
        <Step done={result.transcriptStored} label="URL registrada" />
        <Step done={result.transcriptStored} label="Transcript guardado" />
        <Step done={result.readyForAnalysis} label="Listo para análisis" />
      </div>

      <div className="next-actions">
        <div>
          <p className="eyebrow">Siguiente paso</p>
          <h3>Análisis de aprendizaje</h3>
        </div>
        {result.nextActions.length > 0 ? (
          <div className="analysis-actions">
            <button type="button" onClick={onAnalyze} disabled={analysisState === "loading"}>
              {analysisState === "loading" ? <Loader2 className="spin" size={18} /> : <Sparkles size={18} />}
              <span>Analizar</span>
            </button>
            <button type="button" className="secondary-button" onClick={onRefreshAnalysis} disabled={analysisState === "loading"}>
              <RefreshCw size={18} />
              <span>Regenerar</span>
            </button>
          </div>
        ) : (
          <span className="muted">No disponible todavía</span>
        )}
      </div>
    </section>
  );
}

function InfoTile({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) {
  return (
    <div className="info-tile">
      <span>{icon}</span>
      <div>
        <dt>{label}</dt>
        <dd>{value}</dd>
      </div>
    </div>
  );
}

function formatProxyUsage(usage: ProxyUsageResponse | null, fromCache: boolean) {
  if (fromCache && (!usage || usage.totalBytes === 0)) {
    return "Cache";
  }
  if (!usage || usage.totalBytes === 0) {
    return "-";
  }
  return `${usage.totalMb.toFixed(3)} MB / USD ${usage.estimatedProxyCostUsd.toFixed(6)}`;
}

function summarizeProxyUsage(items: LibraryVideoItem[]) {
  const measured = items
    .map((item) => item.proxyUsage)
    .filter((usage): usage is ProxyUsageResponse => Boolean(usage && usage.totalBytes > 0));
  const totalMb = measured.reduce((sum, usage) => sum + usage.totalMb, 0);
  const totalCost = measured.reduce((sum, usage) => sum + usage.estimatedProxyCostUsd, 0);
  const averageMb = measured.length > 0 ? totalMb / measured.length : 0;
  const estimatedVideosPerGb = averageMb > 0 ? 1024 / averageMb : 0;
  return {
    measuredVideos: measured.length,
    totalMb,
    totalCost,
    averageMb,
    estimatedVideosPerGb
  };
}

function Step({ done, label }: { done: boolean; label: string }) {
  return (
    <div className="step" data-done={done}>
      <span>{done ? <CheckCircle2 size={16} /> : <Search size={16} />}</span>
      <p>{label}</p>
    </div>
  );
}

function LearningAnalysisPanel({ analysis }: { analysis: LearningAnalysisResponse }) {
  return (
    <section className="analysis-section" aria-label="Análisis de aprendizaje">
      <div className="section-title">
        <div>
          <p className="eyebrow">Análisis</p>
          <h2>Aprendizaje en castellano</h2>
        </div>
        <span className="pill">
          <Sparkles size={14} />
          {analysis.fromCache ? "Cache" : "Nuevo"}
        </span>
      </div>

      <div className="analysis-meta">
        <InfoTile icon={<Languages size={20} />} label="Fuente" value={analysis.sourceLanguage} />
        <InfoTile icon={<BookOpenCheck size={20} />} label="Salida" value={analysis.analysisLanguage} />
        <InfoTile icon={<Server size={20} />} label="Provider" value={analysis.provider} />
        <InfoTile icon={<Tags size={20} />} label="Prompt" value={analysis.promptVersion} />
      </div>

      <div className="summary-box">
        <h3>Resumen</h3>
        <p>{analysis.summary}</p>
      </div>

      <ListBlock title="Ideas clave" items={analysis.keyIdeas} />

      <div className="application-grid">
        <h3>Aplicaciones a proyectos</h3>
        {analysis.projectApplications.map((application, index) => (
          <article className="application-card" key={`${application.idea}-${index}`}>
            <strong>{application.idea}</strong>
            <p>{application.whyItMatters}</p>
          </article>
        ))}
      </div>

      <div className="important-segments">
        <h3>Timestamps útiles</h3>
        {analysis.importantSegments.map((segment, index) => (
          <article className="segment-row" key={`${segment.start}-${index}`}>
            <time>{formatTime(segment.start)}</time>
            <p>{segment.reason}</p>
          </article>
        ))}
      </div>

      <ListBlock title="Notas personales" items={analysis.personalLearningNotes} />
      <ListBlock title="Acciones sugeridas" items={analysis.suggestedActions} />
    </section>
  );
}

function ListBlock({ title, items }: { title: string; items: string[] }) {
  return (
    <div className="list-block">
      <h3>{title}</h3>
      <ul>
        {items.map((item, index) => (
          <li key={`${title}-${index}`}>{item}</li>
        ))}
      </ul>
    </div>
  );
}

function TranscriptPanel({ transcript }: { transcript: TranscriptResponse }) {
  const previewSegments = transcript.segments.slice(0, 8);

  return (
    <section className="transcript-section" aria-label="Transcript guardado">
      <div className="section-title">
        <div>
          <p className="eyebrow">Debug</p>
          <h2>Transcript completo</h2>
        </div>
        <span className="pill">
          <Captions size={14} />
          {transcript.segments.length} segmentos
        </span>
      </div>

      {transcript.insight && (
        <div className="insight-band">
          <Sparkles size={20} />
          <div>
            <strong>Contexto {transcript.insight.contextLanguage} → salida {transcript.insight.outputLanguage}</strong>
            <p>{transcript.insight.llmInstructions}</p>
          </div>
        </div>
      )}

      <div className="segment-list">
        {previewSegments.map((segment) => (
          <article className="segment-row" key={segment.position}>
            <time>{formatTime(segment.start)}</time>
            <p>{segment.text}</p>
          </article>
        ))}
      </div>

      <details className="raw-text">
        <summary>
          <FileText size={16} />
          Ver texto completo
        </summary>
        <p>{transcript.fullText}</p>
      </details>
    </section>
  );
}

async function postJson<T>(path: string, payload: unknown): Promise<T> {
  const response = await fetch(path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });

  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.reason ?? `HTTP ${response.status}`);
  }
  return data as T;
}

async function getJson<T>(path: string): Promise<T> {
  const response = await fetch(path);
  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.reason ?? `HTTP ${response.status}`);
  }
  return data as T;
}

function readError(cause: unknown) {
  return cause instanceof Error ? cause.message : "Ocurrió un error inesperado.";
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("es", {
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  }).format(new Date(value));
}

function formatTime(seconds: number) {
  const minutes = Math.floor(seconds / 60);
  const rest = Math.floor(seconds % 60);
  return `${minutes}:${String(rest).padStart(2, "0")}`;
}

createRoot(document.getElementById("app")!).render(<App />);
