import api from './api'

const alerteService = {
  async findAll(statut = null) {
    const params = statut ? { statut } : {}
    const res = await api.get('/alertes', { params })
    return res.data
  },
  async findActives() {
    const res = await api.get('/alertes/actives')
    return res.data
  },
  async getStats() {
    const res = await api.get('/alertes/stats')
    return res.data
  },
  async countNonLues() {
    const res = await api.get('/alertes/count-non-lues')
    return res.data.count
  },
  async marquerLue(id) {
    const res = await api.patch(`/alertes/${id}/lue`)
    return res.data
  },
  async marquerResolue(id) {
    const res = await api.patch(`/alertes/${id}/resolue`)
    return res.data
  },
  async marquerToutesLues() {
    await api.patch('/alertes/tout-lire')
  },
  async scanner(type = 'TOUS') {
    const res = await api.post('/alertes/scan', null, { params: { type } })
    return res.data
  },
}

export default alerteService
