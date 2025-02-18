import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/sugerencias': 'http://localhost:9000', // Redirige todas las peticiones a /sugerencias al backend
      '/getUserRole': 'http://localhost:9000', // Redirige todas las peticiones a /getUserRole al backend
      '/getUserAvatar': 'http://localhost:9000', // Redirige todas las peticiones a /getUserAvatar al backend
    },
  },
});

