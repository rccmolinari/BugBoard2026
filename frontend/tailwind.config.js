/*
 * tailwind.config.js
 *
 * Traduzione del vecchio tw-config.js (che agganciava il tema al CDN).
 * Ora che si usa Vite + PostCSS, il tema vive qui.
 */

/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}',
  ],
  theme: {
    extend: {

      fontFamily: {
        sans:    ['DM Sans', 'sans-serif'],
        display: ['Sora', 'sans-serif'],
        mono:    ['JetBrains Mono', 'monospace'],
      },

      colors: {
        /* Neutri dark — base di tutta l'interfaccia */
        ink: {
          50:  '#0b0f14',
          100: '#121821',
          200: '#1b2430',
          300: '#273244',
          400: '#3b4a61',
          500: '#5b6a83',
          600: '#7b89a0',
          700: '#a1aec2',
          800: '#c9d2df',
          900: '#e8edf4',
        },
        /* Accento ciano — ottimizzato per sfondi scuri */
        brand: {
          50:  '#092028',
          100: '#0f3341',
          200: '#17556c',
          400: '#2094ba',
          500: '#29add8',
          600: '#4bc0e6',
          700: '#86daf2',
        },
      },

    },
  },
  plugins: [],
}
