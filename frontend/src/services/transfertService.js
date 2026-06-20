import api from '@/services/api'

export default {
  findAll:       ()        => api.get('/transferts').then(r => r.data),
  findById:      (id)      => api.get(`/transferts/${id}`).then(r => r.data),
  creer:         (payload) => api.post('/transferts', payload).then(r => r.data),
  expedier:      (id)      => api.patch(`/transferts/${id}/expedier`).then(r => r.data),
  receptionner:  (id)      => api.patch(`/transferts/${id}/receptionner`).then(r => r.data),
  annuler:       (id)      => api.patch(`/transferts/${id}/annuler`).then(r => r.data),
}
