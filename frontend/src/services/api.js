// ============================================================
// INSTANCE AXIOS PARTAGÉE — Configuration HTTP globale
//
// On crée une instance Axios configurée une seule fois ici.
// Tous les services (authService, utilisateurService…) importent
// cette instance plutôt que d'utiliser axios directement.
//
// Avantages :
//   - L'URL de base est définie en un seul endroit
//   - Les intercepteurs (ajout du token, gestion des erreurs)
//     s'appliquent automatiquement à TOUTES les requêtes
// ============================================================

import axios from 'axios'

// Création de l'instance avec l'URL de base du backend Spring Boot
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

// -------------------------------------------------------
// INTERCEPTEUR DE REQUÊTE
// Exécuté AVANT chaque appel HTTP sortant.
// On y injecte le token JWT dans le header Authorization.
//
// Sans ça, on devrait ajouter le header manuellement
// dans chaque appel → code dupliqué partout.
// -------------------------------------------------------
api.interceptors.request.use(
  (config) => {
    // Récupère le token depuis localStorage
    // (stocké par authStore après connexion)
    const token = localStorage.getItem('token')

    if (token) {
      // Format attendu par Spring Security : "Bearer <token>"
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => Promise.reject(error),
)

// -------------------------------------------------------
// INTERCEPTEUR DE RÉPONSE
// Exécuté APRÈS chaque réponse HTTP reçue.
// On gère ici les erreurs communes à toute l'application.
// -------------------------------------------------------
api.interceptors.response.use(
  // Réponse OK (2xx) → on laisse passer sans modification
  (response) => response,

  (error) => {
    // 401 Unauthorized : token expiré ou invalide
    // On utilise le router Vue au lieu de window.location pour éviter
    // un rechargement complet de la page qui causerait une page blanche.
    // On importe le router dynamiquement pour éviter les dépendances circulaires.
    if (error.response?.status === 401) {
      const surLoginPage = window.location.pathname === '/login'
      if (!surLoginPage) {
        localStorage.removeItem('token')
        localStorage.removeItem('utilisateur')
        // Import dynamique du router pour éviter les cycles
        import('@/router').then(({ default: router }) => {
          router.push({ name: 'login' })
        })
      }
    }

    return Promise.reject(error)
  },
)

export default api
