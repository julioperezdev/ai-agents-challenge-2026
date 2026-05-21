import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  root: "frontend",
  build: {
    outDir: "../dist/web",
    emptyOutDir: true
  },
  server: {
    port: Number(process.env.APP_PORT ?? 3000),
    proxy: {
      "/api": {
        target: process.env.BACKEND_URL ?? "http://localhost:8080",
        changeOrigin: true
      }
    }
  }
});
