// ============================================================
// SERVICE ENTREPÔTS
//
// Centralise tous les appels HTTP vers /api/entrepots.
// Même pattern que utilisateurService — instance Axios partagée
// avec intercepteurs (token JWT injecté automatiquement).
// ============================================================

import api from './api'

const entrepotService = {
  /**
   * GET /api/entrepots
   * Récupère la liste de tous les entrepôts avec leurs métriques.
   * @returns {Promise<EntrepotResponseDTO[]>}
   */
  async findAll() {
    const response = await api.get('/entrepots')
    return response.data
  },

  /**
   * GET /api/entrepots/actifs
   * Récupère uniquement les entrepôts actifs.
   * Utilisé lors de la création d'une zone pour peupler le select.
   * @returns {Promise<EntrepotResponseDTO[]>}
   */
  async findActifs() {
    const response = await api.get('/entrepots/actifs')
    return response.data
  },

  /**
   * GET /api/entrepots/{id}
   * Récupère un entrepôt par son ID.
   * @param {number} id
   * @returns {Promise<EntrepotResponseDTO>}
   */
  async findById(id) {
    const response = await api.get(`/entrepots/${id}`)
    return response.data
  },

  /**
   * POST /api/entrepots
   * Crée un nouvel entrepôt.
   * Réservé aux ADMIN et GESTIONNAIRE (vérifié côté backend).
   * @param {Object} data - { nom, adresse, capaciteTotale, capaciteUtilisee?, responsableId? }
   * @returns {Promise<EntrepotResponseDTO>}
   */
  async creer(data) {
    const response = await api.post('/entrepots', data)
    return response.data
  },

  /**
   * PUT /api/entrepots/{id}
   * Modifie un entrepôt existant.
   * @param {number} id
   * @param {Object} data - { nom, adresse, capaciteTotale, capaciteUtilisee?, responsableId? }
   * @returns {Promise<EntrepotResponseDTO>}
   */
  async modifier(id, data) {
    const response = await api.put(`/entrepots/${id}`, data)
    return response.data
  },

  /**
   * PATCH /api/entrepots/{id}/statut
   * Active ou désactive un entrepôt.
   * @param {number} id
   * @returns {Promise<EntrepotResponseDTO>}
   */
  async toggleStatut(id) {
    const response = await api.patch(`/entrepots/${id}/statut`)
    return response.data
  },
}

export default entrepotService
