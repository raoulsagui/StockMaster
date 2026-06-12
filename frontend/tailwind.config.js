/** @type {import('tailwindcss').Config} */

// On dit à Tailwind de scanner tous nos fichiers Vue et JS
// pour détecter quelles classes CSS on utilise.
// Ainsi il ne génère que ce qu'on utilise réellement (tree-shaking CSS).
export default {
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}', // tous les fichiers sous src/
  ],
  theme: {
    extend: {
      // On définit nos couleurs personnalisées pour le projet StockMaster
      colors: {
        primary: {
          50:  '#eff6ff',
          100: '#dbeafe',
          200: '#bfdbfe',
          300: '#93c5fd',
          400: '#60a5fa',
          500: '#3b82f6',  // couleur principale
          600: '#2563eb',
          700: '#1d4ed8',
          800: '#1e40af',
          900: '#1e3a8a',
        },
        sidebar: {
          bg:     '#1e293b', // fond de la sidebar (slate-800)
          hover:  '#334155', // hover sur les liens (slate-700)
          active: '#2563eb', // lien actif (blue-600)
          text:   '#94a3b8', // texte des liens inactifs (slate-400)
        },
      },
      // Police de caractères principale
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
