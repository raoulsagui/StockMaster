// ============================================================
// POINT D'ENTRÉE DE L'APPLICATION VUE
// C'est ici que Vue est initialisé et monté dans le DOM.
// Le fichier index.html contient un <div id="app"> qui est
// le point d'ancrage de toute l'application.
// ============================================================

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// Import du CSS global (Tailwind + nos composants personnalisés)
import './assets/styles/main.css'

// Création de l'instance Vue
const app = createApp(App)

// Pinia : gestionnaire d'état global (comme Vuex mais plus simple)
// Utilisé pour stocker l'utilisateur connecté, les données partagées, etc.
app.use(createPinia())

// Vue Router : gestion de la navigation entre les pages
app.use(router)

// Montage de l'application dans le <div id="app"> de index.html
app.mount('#app')
