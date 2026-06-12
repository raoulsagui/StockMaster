// ============================================================
// SERVICE UTILISATEURS
//
// Centralise tous les appels HTTP vers /api/utilisateurs.
// Chaque méthode correspond à un endpoint du backend Spring Boot.
//
// Toutes les méthodes sont async/await :
//   - await fait attendre la réponse du serveur
//   - On retourne response.data (le JSON, sans les métadonnées HTTP)
//   - Les erreurs remontent au composant appelant via try/catch
// ============================================================

import api from './api'

const utilisateurService = {
  /**
   * GET /api/utilisateurs
   * Récupère la liste de tous les utilisateurs.
   * @returns {Promise<UtilisateurResponseDTO[]>}
   */
  async findAll() {
    const response = await api.get('/utilisateurs')
    return response.data
  },

  /**
   * GET /api/utilisateurs/{id}
   * Récupère un utilisateur par son ID.
   * @param {number} id
   * @returns {Promise<UtilisateurResponseDTO>}
   */
  async findById(id) {
    const response = await api.get(`/utilisateurs/${id}`)
    return response.data
  },

  /**
   * POST /api/utilisateurs
   * Crée un nouvel utilisateur.
   * Réservé aux ADMIN (Spring Security vérifie côté backend).
   * @param {Object} data - { prenom, nom, email, motDePasse, role }
   * @returns {Promise<UtilisateurResponseDTO>}
   */
  async creer(data) {
    const response = await api.post('/utilisateurs', data)
    return response.data
  },

  /**
   * PUT /api/utilisateurs/{id}
   * Modifie un utilisateur existant.
   * @param {number} id
   * @param {Object} data - { prenom, nom, email, motDePasse?, role }
   * @returns {Promise<UtilisateurResponseDTO>}
   */
  async modifier(id, data) {
    const response = await api.put(`/utilisateurs/${id}`, data)
    return response.data
  },

  /**
   * PATCH /api/utilisateurs/{id}/statut
   * Active ou désactive un compte utilisateur.
   * @param {number} id
   * @returns {Promise<UtilisateurResponseDTO>}
   */
  async toggleStatut(id) {
    const response = await api.patch(`/utilisateurs/${id}/statut`)
    return response.data
  },

  /**
   * POST /api/utilisateurs/{id}/reinitialiser-mdp
   * Déclenche la réinitialisation du mot de passe.
   * @param {number} id
   * @returns {Promise<void>}
   */
  async reinitialiserMotDePasse(id) {
    await api.post(`/utilisateurs/${id}/reinitialiser-mdp`)
  },
}

export default utilisateurService
