// ============================================================
// SERVICE ZONES
//
// Centralise tous les appels HTTP vers /api/zones.
// ============================================================

import api from './api'

const zoneService = {
  /**
   * GET /api/zones
   * Récupère la liste de toutes les zones.
   * @returns {Promise<ZoneResponseDTO[]>}
   */
  async findAll() {
    const response = await api.get('/zones')
    return response.data
  },

  /**
   * GET /api/zones/{id}
   * Récupère une zone par son ID.
   * @param {number} id
   * @returns {Promise<ZoneResponseDTO>}
   */
  async findById(id) {
    const response = await api.get(`/zones/${id}`)
    return response.data
  },

  /**
   * GET /api/zones/entrepot/{entrepotId}
   * Récupère toutes les zones d'un entrepôt donné.
   * Utilisé sur la page de détail d'un entrepôt.
   * @param {number} entrepotId
   * @returns {Promise<ZoneResponseDTO[]>}
   */
  async findByEntrepot(entrepotId) {
    const response = await api.get(`/zones/entrepot/${entrepotId}`)
    return response.data
  },

  /**
   * POST /api/zones
   * Crée une nouvelle zone dans un entrepôt.
   * @param {Object} data - { nom, type, description?, capaciteUtilisee?, entrepotId }
   * @returns {Promise<ZoneResponseDTO>}
   */
  async creer(data) {
    const response = await api.post('/zones', data)
    return response.data
  },

  /**
   * PUT /api/zones/{id}
   * Modifie une zone existante.
   * @param {number} id
   * @param {Object} data
   * @returns {Promise<ZoneResponseDTO>}
   */
  async modifier(id, data) {
    const response = await api.put(`/zones/${id}`, data)
    return response.data
  },

  /**
   * PATCH /api/zones/{id}/statut
   * Active ou désactive une zone.
   * @param {number} id
   * @returns {Promise<ZoneResponseDTO>}
   */
  async toggleStatut(id) {
    const response = await api.patch(`/zones/${id}/statut`)
    return response.data
  },
}

export default zoneService
