import api from './api'

const produitService = {
  async findAll() {
    const response = await api.get('/produits')
    return response.data
  },

  async findById(id) {
    const response = await api.get(`/produits/${id}`)
    return response.data
  },

  async creer(data) {
    const response = await api.post('/produits', data)
    return response.data
  },

  async modifier(id, data) {
    const response = await api.put(`/produits/${id}`, data)
    return response.data
  },

  async toggleActif(id) {
    const response = await api.patch(`/produits/${id}/statut`)
    return response.data
  },
}

export default produitService
