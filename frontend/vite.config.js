import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    host: "127.0.0.1",
    port: 4173,
    proxy: {
      "/auth": "http://127.0.0.1:8080",
      "/grupos": "http://127.0.0.1:8080",
      "/incidencias": "http://127.0.0.1:8080",
      "/peticiones": "http://127.0.0.1:8080",
      "/admin": "http://127.0.0.1:8080"
    }
  }
});
