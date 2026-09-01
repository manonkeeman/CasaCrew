import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // Vast, uniek poortnummer (i.p.v. het standaard 5173, dat ook door
    // andere lokale projecten wordt gebruikt) zodat `localhost:<poort>`
    // altijd naar déze app wijst. strictPort voorkomt dat Vite stilzwijgend
    // uitwijkt naar een andere poort als 5190 al bezet is.
    port: 5190,
    strictPort: true,
  },
})
