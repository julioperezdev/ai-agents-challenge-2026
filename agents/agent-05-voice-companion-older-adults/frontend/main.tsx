import React, { useRef, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  FileText,
  HeartHandshake,
  Loader2,
  Mic,
  MicOff,
  Play,
  RefreshCw,
  ShieldCheck,
  Square,
  Unplug,
  Volume2,
  Wifi
} from "lucide-react";
import "./style.css";

type Speaker = "user" | "assistant";
type RealtimeState = "idle" | "connecting" | "connected" | "error";

interface TranscriptTurn {
  speaker: Speaker;
  text: string;
}

interface Reminder {
  id: string;
  title: string;
  date: string;
  time: string;
  type: string;
}

interface DemoResponse {
  summary: {
    user: string;
    date: string;
    generalState: string;
    topics: string[];
    remindersConsulted: Reminder[];
    suggestedActions: string[];
    transcript: TranscriptTurn[];
  };
  markdown: string;
}

interface RealtimeServerEvent {
  type: string;
  delta?: string;
  transcript?: string;
  text?: string;
  error?: { message?: string };
}

function App() {
  const [demo, setDemo] = useState<DemoResponse | null>(null);
  const [loadingDemo, setLoadingDemo] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [realtimeState, setRealtimeState] = useState<RealtimeState>("idle");
  const [realtimeTranscript, setRealtimeTranscript] = useState<TranscriptTurn[]>([]);
  const [assistantDraft, setAssistantDraft] = useState("");
  const [micLevel, setMicLevel] = useState(0);
  const [micStatus, setMicStatus] = useState("Sin probar");
  const [audioInputs, setAudioInputs] = useState<MediaDeviceInfo[]>([]);
  const [selectedAudioInputId, setSelectedAudioInputId] = useState("");
  const [realtimeEvents, setRealtimeEvents] = useState<string[]>([]);
  const [audioUnlocked, setAudioUnlocked] = useState(false);
  const [browserHint] = useState(() => browserCompatibilityHint());

  const peerConnectionRef = useRef<RTCPeerConnection | null>(null);
  const dataChannelRef = useRef<RTCDataChannel | null>(null);
  const mediaStreamRef = useRef<MediaStream | null>(null);
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const audioContextRef = useRef<AudioContext | null>(null);
  const micSourceRef = useRef<MediaStreamAudioSourceNode | null>(null);
  const animationFrameRef = useRef<number | null>(null);
  const assistantDraftRef = useRef("");

  async function runDemo() {
    setLoadingDemo(true);
    setError(null);
    try {
      const response = await fetch("/api/demo/default");
      if (!response.ok) {
        throw new Error(`Backend responded with ${response.status}`);
      }
      setDemo((await response.json()) as DemoResponse);
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "No se pudo ejecutar la demo.");
    } finally {
      setLoadingDemo(false);
    }
  }

  async function startRealtime() {
    setRealtimeState("connecting");
    setError(null);
    setRealtimeTranscript([]);
    setAssistantDraft("");
    assistantDraftRef.current = "";
    setRealtimeEvents([]);

    try {
      await unlockAudio();
      const peerConnection = new RTCPeerConnection();
      peerConnectionRef.current = peerConnection;

      peerConnection.ontrack = (event) => {
        if (audioRef.current) {
          audioRef.current.srcObject = event.streams[0];
          void audioRef.current.play().catch((playError: unknown) => {
            setError(
              playError instanceof Error
                ? `Safari/Browser bloqueo la reproduccion de audio remoto: ${playError.message}`
                : "Safari/Browser bloqueo la reproduccion de audio remoto."
            );
          });
        }
      };

      peerConnection.onconnectionstatechange = () => {
        logRealtimeEvent(`peer_connection.${peerConnection.connectionState}`);
        if (peerConnection.connectionState === "connected") {
          setRealtimeState("connected");
        }
        if (["failed", "closed", "disconnected"].includes(peerConnection.connectionState)) {
          setRealtimeState(peerConnection.connectionState === "closed" ? "idle" : "error");
        }
      };

      const mediaStream = await openMicrophone();
      mediaStreamRef.current = mediaStream;
      attachTrackDiagnostics(mediaStream);
      startMicMeter(mediaStream);
      mediaStream.getAudioTracks().forEach((track) => peerConnection.addTrack(track, mediaStream));

      const dataChannel = peerConnection.createDataChannel("oai-events");
      dataChannelRef.current = dataChannel;
      dataChannel.addEventListener("open", () => {
        logRealtimeEvent("data_channel.open");
        setRealtimeState("connected");
        dataChannel.send(
          JSON.stringify({
            type: "conversation.item.create",
            item: {
              type: "message",
              role: "user",
              content: [
                {
                  type: "input_text",
                  text: "Hola, soy Don Roberto. Saludame brevemente y preguntame como estoy."
                }
              ]
            }
          })
        );
        dataChannel.send(JSON.stringify({ type: "response.create" }));
      });
      dataChannel.addEventListener("message", (event) => handleRealtimeEvent(JSON.parse(event.data)));

      const offer = await peerConnection.createOffer();
      await peerConnection.setLocalDescription(offer);

      const sdpResponse = await fetch("/api/realtime/session", {
        method: "POST",
        headers: { "Content-Type": "application/sdp" },
        body: offer.sdp ?? ""
      });

      if (!sdpResponse.ok) {
        const detail = await sdpResponse.text();
        throw new Error(readApiError(detail) || `Backend responded with ${sdpResponse.status}`);
      }

      await peerConnection.setRemoteDescription({
        type: "answer",
        sdp: await sdpResponse.text()
      });
    } catch (caught) {
      stopRealtime();
      setRealtimeState("error");
      setError(caught instanceof Error ? caught.message : "No se pudo conectar con OpenAI Realtime.");
    }
  }

  function stopRealtime() {
    dataChannelRef.current?.close();
    dataChannelRef.current = null;
    peerConnectionRef.current?.close();
    peerConnectionRef.current = null;
    mediaStreamRef.current?.getTracks().forEach((track) => track.stop());
    mediaStreamRef.current = null;
    stopMicMeter();
    if (audioRef.current) {
      audioRef.current.srcObject = null;
    }
    setAssistantDraft("");
    assistantDraftRef.current = "";
    setRealtimeState("idle");
  }

  async function testMicrophone() {
    setError(null);
    try {
      await unlockAudio();
      const stream = await openMicrophone();
      const previousStream = mediaStreamRef.current;
      mediaStreamRef.current = stream;
      attachTrackDiagnostics(stream);
      startMicMeter(stream);
      if (previousStream && previousStream !== stream) {
        previousStream.getTracks().forEach((track) => track.stop());
      }
      setMicStatus(trackLabel(stream));
      logRealtimeEvent("microphone.test_started");
      await refreshAudioInputs();
    } catch (caught) {
      setMicStatus("No disponible");
      setError(caught instanceof Error ? caught.message : "No se pudo abrir el microfono.");
    }
  }

  async function refreshAudioInputs() {
    const devices = await navigator.mediaDevices.enumerateDevices();
    const inputs = devices.filter((device) => device.kind === "audioinput");
    setAudioInputs(inputs);
    if (!selectedAudioInputId && inputs[0]?.deviceId) {
      setSelectedAudioInputId(inputs[0].deviceId);
    }
  }

  async function openMicrophone() {
    if (!navigator.mediaDevices?.getUserMedia) {
      throw new Error("Este navegador no expone getUserMedia. Use Chrome en http://localhost:3000.");
    }

    const constraints = safariLike()
      ? { echoCancellation: true }
      : {
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true,
          channelCount: 1
        };
    const audio: MediaTrackConstraints = selectedAudioInputId
      ? { ...constraints, deviceId: { exact: selectedAudioInputId } }
      : constraints;

    const stream = await navigator.mediaDevices.getUserMedia({ audio });
    setMicStatus(trackLabel(stream));
    return stream;
  }

  function releaseMicrophone() {
    mediaStreamRef.current?.getTracks().forEach((track) => track.stop());
    mediaStreamRef.current = null;
    stopMicMeter();
    setMicStatus("Liberado");
    logRealtimeEvent("microphone.released");
  }

  function attachTrackDiagnostics(stream: MediaStream) {
    const track = stream.getAudioTracks()[0];
    if (!track) return;
    track.onmute = () => {
      setMicStatus(trackLabel(stream));
      logRealtimeEvent("microphone.track_muted");
    };
    track.onunmute = () => {
      setMicStatus(trackLabel(stream));
      logRealtimeEvent("microphone.track_unmuted");
    };
    track.onended = () => {
      setMicStatus(trackLabel(stream));
      logRealtimeEvent("microphone.track_ended");
    };
  }

  function handleRealtimeEvent(event: RealtimeServerEvent) {
    if (!event.type.includes(".delta")) {
      logRealtimeEvent(event.type);
    }

    if (event.type === "error") {
      setError(event.error?.message ?? "Realtime API returned an error.");
      setRealtimeState("error");
      return;
    }

    if (event.type === "conversation.item.input_audio_transcription.completed" && event.transcript) {
      appendRealtimeTurn("user", event.transcript);
      return;
    }

    if (event.type === "response.output_audio_transcript.delta" && event.delta) {
      setAssistantDraft((current) => {
        const next = current + event.delta;
        assistantDraftRef.current = next;
        return next;
      });
      return;
    }

    if (event.type === "response.output_audio_transcript.done") {
      const text = event.transcript || assistantDraftRef.current;
      if (text.trim()) appendRealtimeTurn("assistant", text);
      setAssistantDraft("");
      assistantDraftRef.current = "";
      return;
    }

    if (event.type === "response.output_text.done" && event.text) {
      appendRealtimeTurn("assistant", event.text);
    }
  }

  function appendRealtimeTurn(speaker: Speaker, text: string) {
    setRealtimeTranscript((current) => [...current, { speaker, text }]);
  }

  function forceResponse() {
    const dataChannel = dataChannelRef.current;
    if (!dataChannel || dataChannel.readyState !== "open") {
      setError("La sesion Realtime no tiene un data channel abierto.");
      return;
    }

    logRealtimeEvent("client.force_commit_and_response");
    dataChannel.send(JSON.stringify({ type: "input_audio_buffer.commit" }));
    dataChannel.send(JSON.stringify({ type: "response.create" }));
  }

  async function unlockAudio() {
    const AudioContextClass =
      window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
    const audioContext = audioContextRef.current ?? new AudioContextClass();
    audioContextRef.current = audioContext;
    if (audioContext.state !== "running") {
      await audioContext.resume();
    }
    if (audioRef.current) {
      audioRef.current.muted = false;
      audioRef.current.volume = 1;
    }
    setAudioUnlocked(true);
    logRealtimeEvent("browser.audio_unlocked");
  }

  function logRealtimeEvent(type: string) {
    setRealtimeEvents((current) => [`${new Date().toLocaleTimeString()} ${type}`, ...current].slice(0, 12));
  }

  function startMicMeter(stream: MediaStream) {
    stopMicMeter();
    const AudioContextClass =
      window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
    const audioContext = audioContextRef.current ?? new AudioContextClass();
    void audioContext.resume();
    const analyser = audioContext.createAnalyser();
    analyser.fftSize = 256;
    const source = audioContext.createMediaStreamSource(stream);
    source.connect(analyser);
    micSourceRef.current = source;
    audioContextRef.current = audioContext;

    const track = stream.getAudioTracks()[0];
    const samples = new Uint8Array(analyser.fftSize);
    const tick = () => {
      analyser.getByteTimeDomainData(samples);
      let sum = 0;
      for (const sample of samples) {
        const normalized = (sample - 128) / 128;
        sum += normalized * normalized;
      }
      const rms = Math.sqrt(sum / samples.length);
      setMicLevel(Math.min(100, Math.max(0, Math.round(rms * 320))));
      if (track) setMicStatus(trackLabel(stream));
      animationFrameRef.current = requestAnimationFrame(tick);
    };
    tick();
  }

  function stopMicMeter() {
    if (animationFrameRef.current) {
      cancelAnimationFrame(animationFrameRef.current);
      animationFrameRef.current = null;
    }
    micSourceRef.current?.disconnect();
    micSourceRef.current = null;
    setMicLevel(0);
  }

  function trackLabel(stream: MediaStream) {
    const track = stream.getAudioTracks()[0];
    if (!track) return "Sin pista de audio";
    const settings = track.getSettings();
    return `${track.label || "Microfono"} (${track.readyState}, ${track.enabled ? "enabled" : "disabled"}, ${track.muted ? "muted" : "unmuted"}, ${settings.sampleRate ?? "?"} Hz)`;
  }

  const realtimeConnected = realtimeState === "connected";
  const realtimeBusy = realtimeState === "connecting";

  return (
    <main className="app-shell">
      <audio ref={audioRef} autoPlay />
      {browserHint ? <p className="notice">{browserHint}</p> : null}
      <section className="toolbar">
        <div>
          <p className="eyebrow">Agente 05</p>
          <h1>Acompanante de voz</h1>
        </div>
        <div className="button-row">
          <button type="button" onClick={runDemo} disabled={loadingDemo || realtimeBusy}>
            {loadingDemo ? <Loader2 className="spin" size={18} /> : <Play size={18} />}
            Demo local
          </button>
          <button type="button" onClick={testMicrophone} disabled={realtimeBusy}>
            <Mic size={18} />
            Probar micro
          </button>
          <button type="button" onClick={unlockAudio} disabled={realtimeBusy}>
            <Volume2 size={18} />
            Habilitar audio
          </button>
          <button type="button" onClick={refreshAudioInputs} disabled={realtimeBusy}>
            <RefreshCw size={18} />
            Dispositivos
          </button>
          <button type="button" onClick={releaseMicrophone} disabled={realtimeBusy || realtimeConnected}>
            <Unplug size={18} />
            Soltar micro
          </button>
          <button
            type="button"
            className={realtimeConnected ? "danger-button" : "primary-button"}
            onClick={realtimeConnected || realtimeBusy ? stopRealtime : startRealtime}
          >
            {realtimeBusy ? <Loader2 className="spin" size={18} /> : realtimeConnected ? <Square size={18} /> : <Mic size={18} />}
            {realtimeBusy ? "Conectando" : realtimeConnected ? "Cortar charla" : "Hablar con IA"}
          </button>
        </div>
      </section>

      {error ? <p className="error">Aviso: {error}</p> : null}

      <section className="summary-strip" aria-label="Resumen">
        <InfoBlock icon={<HeartHandshake size={20} />} label="Demo" value="Local sin credenciales" />
        <InfoBlock icon={<Wifi size={20} />} label="Realtime" value={realtimeLabel(realtimeState)} />
        <InfoBlock icon={<ShieldCheck size={20} />} label="Seguridad" value="No medico, no emergencias" />
      </section>

      <section className="workspace">
        <div className="panel transcript-panel">
          <div className="panel-header">
            <h2>Charla en tiempo real</h2>
            <span>{realtimeConnected ? "Microfono activo" : "Desconectado"}</span>
          </div>
          <div className="call-surface">
            {realtimeConnected ? <Mic size={34} /> : <MicOff size={34} />}
            <div className="call-copy">
              <p>
                {realtimeConnected
                  ? "Hable con naturalidad. El modelo responde por audio y registra una transcripcion parcial."
                  : "Inicie una charla Realtime. Spring Boot negocia la sesion con OpenAI sin exponer la API key."}
              </p>
              <div className="mic-meter" aria-label="Nivel de microfono">
                <span style={{ width: `${micLevel}%` }} />
              </div>
              <small>{realtimeConnected ? `Nivel de microfono: ${micLevel}%` : "El medidor se activa al conectar."}</small>
              <small>Estado: {micStatus}</small>
              <small>Audio remoto: {audioUnlocked ? "habilitado" : "pendiente"}</small>
            </div>
          </div>
          <label className="device-picker">
            <span>Microfono</span>
            <select
              value={selectedAudioInputId}
              onChange={(event) => setSelectedAudioInputId(event.target.value)}
              disabled={realtimeBusy || realtimeConnected}
            >
              <option value="">Predeterminado del navegador</option>
              {audioInputs.map((device) => (
                <option value={device.deviceId} key={device.deviceId}>
                  {device.label || `Microfono ${device.deviceId.slice(0, 6)}`}
                </option>
              ))}
            </select>
          </label>
          {realtimeConnected ? (
            <div className="realtime-actions">
              <button type="button" onClick={forceResponse}>Forzar respuesta</button>
              <span>Usalo si hablaste, el medidor se movio y el agente no respondio.</span>
            </div>
          ) : null}
          <div className="transcript realtime-transcript">
            {realtimeTranscript.map((turn, index) => (
              <article className={`turn ${turn.speaker}`} key={`${turn.speaker}-${index}`}>
                <strong>{turn.speaker === "user" ? "Persona" : "Agente"}</strong>
                <p>{turn.text}</p>
              </article>
            ))}
            {assistantDraft ? (
              <article className="turn assistant">
                <strong>Agente</strong>
                <p>{assistantDraft}</p>
              </article>
            ) : null}
            {!realtimeTranscript.length && !assistantDraft ? (
              <p className="empty">La transcripcion aparece aca cuando la sesion Realtime esta activa.</p>
            ) : null}
          </div>
        </div>

        <aside className="panel">
          <div className="panel-header">
            <h2>Resumen demo</h2>
            <span>{demo?.summary.date ?? "Pendiente"}</span>
          </div>
          {demo ? (
            <div className="summary">
              <p className="state">{demo.summary.generalState}</p>
              <h3>Recordatorios</h3>
              <ul>
                {demo.summary.remindersConsulted.map((reminder) => (
                  <li key={reminder.id}>
                    {reminder.title} a las {reminder.time}
                  </li>
                ))}
              </ul>
              <h3>Acciones</h3>
              <ul>
                {demo.summary.suggestedActions.map((action) => (
                  <li key={action}>{action}</li>
                ))}
              </ul>
            </div>
          ) : (
            <p className="empty">El backend Spring Boot tambien mantiene la demo deterministica versionada.</p>
          )}
        </aside>
      </section>

      <section className="workspace lower-grid">
        <div className="panel transcript-panel">
          <div className="panel-header">
            <h2>Transcripcion demo local</h2>
            <span>{demo ? `${demo.summary.transcript.length} turnos` : "Sin ejecutar"}</span>
          </div>
          <div className="transcript">
            {demo ? (
              demo.summary.transcript.map((turn, index) => (
                <article className={`turn ${turn.speaker}`} key={`${turn.speaker}-${index}`}>
                  <strong>{turn.speaker === "user" ? "Persona" : "Agente"}</strong>
                  <p>{turn.text}</p>
                </article>
              ))
            ) : (
              <p className="empty">Ejecute la demo local para comparar contra el flujo Realtime.</p>
            )}
          </div>
        </div>

        <aside className="panel">
          <div className="panel-header">
            <h2>Notas</h2>
            <FileText size={18} />
          </div>
          <p className="empty">
            Para Realtime real, levante Spring Boot con <code>OPENAI_API_KEY</code>. El navegador necesita permisos de microfono y usa el proxy de Vite hacia <code>/api/realtime/session</code>.
          </p>
          <h3>Eventos Realtime</h3>
          <ol className="event-list">
            {realtimeEvents.length ? realtimeEvents.map((event) => <li key={event}>{event}</li>) : <li>Sin eventos todavia.</li>}
          </ol>
        </aside>
      </section>

      {demo ? (
        <section className="markdown-panel">
          <div className="panel-header">
            <h2>Markdown generado</h2>
            <span>{demo.summary.user}</span>
          </div>
          <pre>{demo.markdown}</pre>
        </section>
      ) : null}
    </main>
  );
}

function InfoBlock({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) {
  return (
    <div className="info-block">
      {icon}
      <div>
        <span>{label}</span>
        <strong>{value}</strong>
      </div>
    </div>
  );
}

function realtimeLabel(state: RealtimeState) {
  if (state === "connected") return "Conectado a OpenAI";
  if (state === "connecting") return "Conectando";
  if (state === "error") return "Error de conexion";
  return "Listo";
}

function readApiError(detail: string) {
  try {
    const parsed = JSON.parse(detail) as { message?: string };
    return parsed.message ?? detail;
  } catch {
    return detail;
  }
}

function safariLike() {
  return /^((?!chrome|android).)*safari/i.test(navigator.userAgent);
}

function browserCompatibilityHint() {
  if (!window.isSecureContext) {
    return "Safari requiere HTTPS o localhost para usar microfono. Abra http://localhost:3000.";
  }
  if (!navigator.mediaDevices?.getUserMedia) {
    return "Este navegador no permite capturar microfono en este contexto.";
  }
  if (safariLike()) {
    return "Safari puede bloquear audio remoto hasta que pulses Habilitar audio o Probar micro.";
  }
  return "";
}

createRoot(document.getElementById("app")!).render(<App />);
