import api from '@/services/api'

export default {
  findAll:       ()        => api.get('/entrees').then(r => r.data),
  findById:      (id)      => api.get(`/entrees/${id}`).then(r => r.data),
  creer:         (payload) => api.post('/entrees', payload).then(r => r.data),
  valider:       (id)      => api.patch(`/entrees/${id}/valider`).then(r => r.data),
  annuler:       (id)      => api.patch(`/entrees/${id}/annuler`).then(r => r.data),
}
