// ============================================================
// SERVICE D'AUTHENTIFICATION
//
// Gère les appels HTTP liés à l'authentification.
// Ce service est utilisé par authStore (Pinia).
//
// Séparation des responsabilités :
//   authService  → appels HTTP (axios)
//   authStore    → état global (token, utilisateur connecté)
//   LoginView    → interface utilisateur
// ============================================================

import api from './api'

const authService = {
  /**
   * Connexion : POST /api/auth/login
   *
   * @param {string} email
   * @param {string} motDePasse
   * @returns {Promise<{token, type, utilisateur}>}
   *
   * Le backend retourne :
   * {
   *   "token": "eyJhbGci...",
   *   "type": "Bearer",
   *   "utilisateur": { "id": 1, "prenom": "Admin", "role": "ADMIN", ... }
   * }
   */
  async login(email, motDePasse) {
    // On utilise "motDePasse" car c'est le nom attendu par LoginRequestDTO côté Java
    const response = await api.post('/auth/login', { email, motDePasse })
    return response.data
  },
}

export default authService
