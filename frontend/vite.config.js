import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    host: "0.0.0.0",
    proxy: {
      "/api/auth": "http://localhost:8081",
      "/api/disposals": "http://localhost:8082",
    },
  },
});
