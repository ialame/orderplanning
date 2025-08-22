import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 3000
  },
  build: {
    outDir: 'dist',
    rollupOptions: {
      // Assurer que vue-router est inclus dans le bundle
      external: [],
      output: {
        globals: {}
      }
    }
  },
  resolve: {
    alias: {
      '@': '/src'
    }
  }
})
