// ============================================================
// SERVICE COMMANDES FOURNISSEUR
//
// Centralise tous les appels HTTP vers /api/commandes.
// Même pattern axios que les autres services du projet.
// ============================================================

import api from './api'

const commandeService = {

  // -------------------------------------------------------
  // CONSULTATION
  // -------------------------------------------------------

  /**
   * GET /api/commandes?page=0&size=20
   * Liste paginée de toutes les commandes.
   * @param {number} page
   * @param {number} size
   * @returns {Promise<Page<CommandeResponseDTO>>}
   */
  async findAll(page = 0, size = 20) {
    const response = await api.get('/commandes', { params: { page, size } })
    return response.data
  },

  /**
   * GET /api/commandes/stats
   * Statistiques globales (total, brouillons, validées, livrées, annulées).
   * @returns {Promise<CommandeStatsDTO>}
   */
  async getStats() {
    const response = await api.get('/commandes/stats')
    return response.data
  },

  /**
   * GET /api/commandes/fournisseur/{fournisseurId}
   * Commandes d'un fournisseur spécifique.
   * @param {number} fournisseurId
   * @returns {Promise<CommandeResponseDTO[]>}
   */
  async findByFournisseur(fournisseurId) {
    const response = await api.get(`/commandes/fournisseur/${fournisseurId}`)
    return response.data
  },

  /**
   * GET /api/commandes/entrepot/{entrepotId}
   * Commandes destinées à un entrepôt.
   * @param {number} entrepotId
   * @returns {Promise<CommandeResponseDTO[]>}
   */
  async findByEntrepot(entrepotId) {
    const response = await api.get(`/commandes/entrepot/${entrepotId}`)
    return response.data
  },

  /**
   * GET /api/commandes/{id}
   * Détail complet d'une commande avec toutes ses lignes.
   * @param {number} id
   * @returns {Promise<CommandeResponseDTO>}
   */
  async findById(id) {
    const response = await api.get(`/commandes/${id}`)
    return response.data
  },

  // -------------------------------------------------------
  // CRÉATION / MODIFICATION
  // -------------------------------------------------------

  /**
   * POST /api/commandes
   * Crée une nouvelle commande fournisseur (statut BROUILLON).
   *
   * @param {Object}   data
   * @param {number}   data.fournisseurId
   * @param {number}   data.entrepotId
   * @param {string}   [data.dateLivraisonPrevue]  - format YYYY-MM-DD
   * @param {string}   [data.note]
   * @param {Array}    data.lignes
   * @param {number}   data.lignes[].produitId
   * @param {number}   data.lignes[].quantiteCommandee
   * @param {number}   [data.lignes[].prixUnitaire]
   * @param {string}   [data.lignes[].note]
   * @returns {Promise<CommandeResponseDTO>}
   */
  async creer(data) {
    const response = await api.post('/commandes', data)
    return response.data
  },

  /**
   * PUT /api/commandes/{id}
   * Modifie une commande en BROUILLON (en-tête + lignes remplacées).
   * @param {number} id
   * @param {Object} data - même structure que creer()
   * @returns {Promise<CommandeResponseDTO>}
   */
  async modifier(id, data) {
    const response = await api.put(`/commandes/${id}`, data)
    return response.data
  },

  // -------------------------------------------------------
  // CYCLE DE VIE
  // -------------------------------------------------------

  /**
   * PATCH /api/commandes/{id}/valider
   * Valide la commande : BROUILLON → VALIDEE.
   * @param {number} id
   * @returns {Promise<CommandeResponseDTO>}
   */
  async valider(id) {
    const response = await api.patch(`/commandes/${id}/valider`)
    return response.data
  },

  /**
   * PATCH /api/commandes/{id}/receptionner
   * Réceptionne la livraison : VALIDEE → LIVREE.
   * Met à jour le stock en entrée.
   *
   * @param {number} id
   * @param {Object} data
   * @param {Array}  data.lignes
   * @param {number} data.lignes[].ligneId
   * @param {number} data.lignes[].quantiteRecue
   * @param {string} [data.note]
   * @returns {Promise<CommandeResponseDTO>}
   */
  async receptionner(id, data) {
    const response = await api.patch(`/commandes/${id}/receptionner`, data)
    return response.data
  },

  /**
   * PATCH /api/commandes/{id}/annuler
   * Annule la commande sans générer de mouvement de stock.
   * @param {number} id
   * @returns {Promise<CommandeResponseDTO>}
   */
  async annuler(id) {
    const response = await api.patch(`/commandes/${id}/annuler`)
    return response.data
  },
}

export default commandeService
