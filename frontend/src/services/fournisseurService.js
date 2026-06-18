import api from './api'

const fournisseurService = {
  async findAll() {
    const response = await api.get('/fournisseurs')
    return response.data
  },

  async findActifs() {
    const response = await api.get('/fournisseurs/actifs')
    return response.data
  },

  async findById(id) {
    const response = await api.get(`/fournisseurs/${id}`)
    return response.data
  },

  async creer(data) {
    const response = await api.post('/fournisseurs', data)
    return response.data
  },

  async modifier(id, data) {
    const response = await api.put(`/fournisseurs/${id}`, data)
    return response.data
  },

  async toggleStatut(id) {
    const response = await api.patch(`/fournisseurs/${id}/statut`)
    return response.data
  },
}

export default fournisseurService
