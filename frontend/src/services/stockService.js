// ============================================================
// SERVICE STOCKS — Module 7
//
// Centralise tous les appels HTTP vers /api/stocks.
// ============================================================

import api from './api'

const stockService = {
  /** GET /api/stocks — tous les stocks */
  async findAll() {
    const response = await api.get('/stocks')
    return response.data
  },

  /** GET /api/stocks/entrepot/{id} — stocks d'un entrepôt */
  async findByEntrepot(entrepotId) {
    const response = await api.get(`/stocks/entrepot/${entrepotId}`)
    return response.data
  },

  /** GET /api/stocks/produit/{id} — stocks d'un produit */
  async findByProduit(produitId) {
    const response = await api.get(`/stocks/produit/${produitId}`)
    return response.data
  },

  /** GET /api/stocks/alertes — stocks en dessous du seuil */
  async findEnAlerte() {
    const response = await api.get('/stocks/alertes')
    return response.data
  },

  /** GET /api/stocks/{id}/mouvements?page=0&size=20 */
  async getMouvements(stockId, page = 0, size = 20) {
    const response = await api.get(`/stocks/${stockId}/mouvements`, {
      params: { page, size },
    })
    return response.data // Page Spring (content, totalPages, etc.)
  },

  /** PATCH /api/stocks/{id}/seuils */
  async mettreAJourSeuils(stockId, data) {
    const response = await api.patch(`/stocks/${stockId}/seuils`, data)
    return response.data
  },

  /** GET /api/stocks/mouvements/derniers?limit=5 — pour le tableau de bord */
  async getDerniersMouvements(limit = 5) {
    const response = await api.get('/stocks/mouvements/derniers', { params: { limit } })
    return response.data
  },
}

export default stockService
