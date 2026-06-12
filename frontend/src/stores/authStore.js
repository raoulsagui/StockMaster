// ============================================================
// STORE PINIA — Authentification
//
// Pinia est le gestionnaire d'état global de Vue 3.
// Ce store centralise tout ce qui concerne l'utilisateur connecté :
//   - Son token JWT
//   - Ses informations (nom, rôle…)
//   - Son état de connexion
//
// Pourquoi un store plutôt que du localStorage seul ?
//   - Réactivité : les composants Vue se mettent à jour automatiquement
//     quand le store change (ex: header affiche le bon nom)
//   - Logique centralisée : login/logout en un seul endroit
//   - Persistance contrôlée : on sync avec localStorage manuellement
// ============================================================

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authService from '@/services/authService'

// defineStore('auth', ...) → crée un store nommé 'auth'
// On utilise la "Setup Store" syntax (plus proche de la Composition API)
export const useAuthStore = defineStore('auth', () => {
  // -------------------------------------------------------
  // STATE — Les données réactives du store
  // ref() → valeur réactive (comme dans un composant)
  // -------------------------------------------------------

  // Token JWT — null si non connecté
  const token = ref(localStorage.getItem('token') || null)

  // Infos de l'utilisateur connecté — null si non connecté
  // On parse le JSON stocké en localStorage pour restaurer la session
  const utilisateur = ref(
    localStorage.getItem('utilisateur')
      ? JSON.parse(localStorage.getItem('utilisateur'))
      : null,
  )

  // -------------------------------------------------------
  // GETTERS — Valeurs calculées à partir du state
  // computed() → recalculé automatiquement quand le state change
  // -------------------------------------------------------

  // true si l'utilisateur est connecté (token présent)
  const isAuthenticated = computed(() => !!token.value)

  // Rôle de l'utilisateur connecté (ex: "ADMIN")
  const role = computed(() => utilisateur.value?.role || null)

  // Nom complet pour affichage dans le header/sidebar
  const nomComplet = computed(() => {
    if (!utilisateur.value) return ''
    return `${utilisateur.value.prenom} ${utilisateur.value.nom}`
  })

  // Vérifie si l'utilisateur a un rôle spécifique
  const hasRole = (r) => role.value === r

  // -------------------------------------------------------
  // ACTIONS — Méthodes qui modifient le state
  // -------------------------------------------------------

  /**
   * Connexion : appelle authService, stocke le token et l'utilisateur.
   *
   * @param {string} email
   * @param {string} motDePasse
   * @throws {Error} Si les identifiants sont invalides (propagé depuis authService)
   */
  async function login(email, motDePasse) {
    // Appel HTTP → backend Spring Boot POST /api/auth/login
    const data = await authService.login(email, motDePasse)

    // Stocke le token dans le state réactif ET dans localStorage
    // localStorage → persiste la session si l'utilisateur rafraîchit la page
    token.value = data.token
    utilisateur.value = data.utilisateur

    localStorage.setItem('token', data.token)
    localStorage.setItem('utilisateur', JSON.stringify(data.utilisateur))
  }

  /**
   * Déconnexion : vide le state et le localStorage.
   * Le router redirigera vers /login.
   */
  function logout() {
    token.value = null
    utilisateur.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('utilisateur')
  }

  // On expose ce qu'on veut rendre accessible depuis les composants
  return {
    token,
    utilisateur,
    isAuthenticated,
    role,
    nomComplet,
    hasRole,
    login,
    logout,
  }
})
