/*
 * tw-config.js
 *
 * Configurazione del tema Tailwind.
 * Con il CDN di sviluppo si usa questa variabile globale.
 * Quando migreremo ad Angular + PostCSS, questo diventa
 * il classico tailwind.config.js nella root del progetto.
 */

tailwind.config = {
  theme: {
    extend: {

      fontFamily: {
        sans:    ['DM Sans', 'sans-serif'],
        display: ['Sora', 'sans-serif'],
        mono:    ['JetBrains Mono', 'monospace'],
      },

      colors: {
        /* Neutri caldi — base di tutta l'interfaccia */
        ink: {
          50:  '#f7f6f3',
          100: '#eeecea',
          200: '#dddad6',
          300: '#c6c2bb',
          400: '#a09990',
          500: '#7d7570',
          600: '#655e59',
          700: '#524d49',
          800: '#2e2b28',
          900: '#1a1816',
        },
        /* Accento teal — usato con parsimonia */
        brand: {
          50:  '#effefb',
          100: '#c8fef4',
          200: '#92fae8',
          400: '#22d9c3',
          500: '#0bbdaa',
          600: '#089888',
          700: '#0c7a6e',
        },
      },

    },
  },
};
