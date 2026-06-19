// ============================================================
// SERVICE INVENTAIRES — Module 11
//
// Centralise tous les appels HTTP vers /api/inventaires.
// Suit le même pattern que les autres services du projet.
// ============================================================

import api from './api'

const inventaireService = {

  // -------------------------------------------------------
  // CONSULTATION
  // -------------------------------------------------------

  /**
   * GET /api/inventaires?page=0&size=20
   * Liste paginée de tous les inventaires.
   * @param {number} page
   * @param {number} size
   * @returns {Promise<Page<InventaireResponseDTO>>}
   */
  async findAll(page = 0, size = 20) {
    const response = await api.get('/inventaires', { params: { page, size } })
    return response.data
  },

  /**
   * GET /api/inventaires/stats
   * Statistiques globales (total, en cours, brouillons, validés).
   * @returns {Promise<InventaireStatsDTO>}
   */
  async getStats() {
    const response = await api.get('/inventaires/stats')
    return response.data
  },

  /**
   * GET /api/inventaires/entrepot/{entrepotId}
   * Tous les inventaires d'un entrepôt.
   * @param {number} entrepotId
   * @returns {Promise<InventaireResponseDTO[]>}
   */
  async findByEntrepot(entrepotId) {
    const response = await api.get(`/inventaires/entrepot/${entrepotId}`)
    return response.data
  },

  /**
   * GET /api/inventaires/{id}
   * Détail complet d'un inventaire avec toutes ses lignes.
   * @param {number} id
   * @returns {Promise<InventaireResponseDTO>}
   */
  async findById(id) {
    const response = await api.get(`/inventaires/${id}`)
    return response.data
  },

  // -------------------------------------------------------
  // CRÉATION
  // -------------------------------------------------------

  /**
   * POST /api/inventaires
   * Crée un nouvel inventaire (statut BROUILLON).
   *
   * @param {Object} data
   * @param {'COMPLET'|'PARTIEL'} data.type
   * @param {number}              data.entrepotId
   * @param {string}              data.datePrevue  - format ISO (YYYY-MM-DD)
   * @param {string}              [data.note]
   * @param {number[]}            [data.produitsIds] - requis si type=PARTIEL
   * @returns {Promise<InventaireResponseDTO>}
   */
  async creer(data) {
    const response = await api.post('/inventaires', data)
    return response.data
  },

  // -------------------------------------------------------
  // CYCLE DE VIE
  // -------------------------------------------------------

  /**
   * PATCH /api/inventaires/{id}/demarrer
   * Passe l'inventaire de BROUILLON à EN_COURS.
   * @param {number} id
   * @returns {Promise<InventaireResponseDTO>}
   */
  async demarrer(id) {
    const response = await api.patch(`/inventaires/${id}/demarrer`)
    return response.data
  },

  /**
   * PUT /api/inventaires/{inventaireId}/lignes/{ligneId}
   * Saisit la quantité réelle comptée pour une ligne.
   *
   * @param {number} inventaireId
   * @param {number} ligneId
   * @param {Object} data
   * @param {number} data.quantiteComptee
   * @param {string} [data.note]
   * @returns {Promise<LigneInventaireResponseDTO>}
   */
  async saisirQuantite(inventaireId, ligneId, data) {
    const response = await api.put(`/inventaires/${inventaireId}/lignes/${ligneId}`, data)
    return response.data
  },

  /**
   * PATCH /api/inventaires/{id}/valider
   * Valide l'inventaire et applique les ajustements de stock.
   * @param {number} id
   * @returns {Promise<InventaireResponseDTO>}
   */
  async valider(id) {
    const response = await api.patch(`/inventaires/${id}/valider`)
    return response.data
  },

  /**
   * PATCH /api/inventaires/{id}/annuler
   * Annule l'inventaire sans appliquer d'ajustements.
   * @param {number} id
   * @returns {Promise<InventaireResponseDTO>}
   */
  async annuler(id) {
    const response = await api.patch(`/inventaires/${id}/annuler`)
    return response.data
  },
}

export default inventaireService
