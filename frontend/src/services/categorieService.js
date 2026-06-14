import api from './api'

const categorieService = {
  async findAll() {
    const response = await api.get('/categories')
    return response.data
  },

  async creer(data) {
    const response = await api.post('/categories', data)
    return response.data
  },

  async modifier(id, data) {
    const response = await api.put(`/categories/${id}`, data)
    return response.data
  },

  async supprimer(id) {
    await api.delete(`/categories/${id}`)
  },
}

export default categorieService
