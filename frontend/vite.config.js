import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  base: '/Controle_Medicamentos/',
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
