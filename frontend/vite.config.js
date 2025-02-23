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
      '/getLogin': 'http://localhost:9000',
      '/getLogout': 'http://localhost:9000',
      '/getWriting': 'http://localhost:9000',
      '/getComentarios': 'http://localhost:9000',
      '/getCommented': 'http://localhost:9000',
      '/getUserIdent': 'http://localhost:9000',
      '/postCommentInserted': 'http://localhost:9000',
      '/getIdComment': 'http://localhost:9000',
      '/getCommentEdit': 'http://localhost:9000',
      '/postCommentEdited': 'http://localhost:9000',
      '/getEmails': 'http://localhost:9000',
      '/postRegistryUser': 'http://localhost:9000',
    },
  },
});

