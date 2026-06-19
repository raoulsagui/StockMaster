import api from '@/services/api'

export default {
  findAll:       ()        => api.get('/sorties').then(r => r.data),
  findById:      (id)      => api.get(`/sorties/${id}`).then(r => r.data),
  creer:         (payload) => api.post('/sorties', payload).then(r => r.data),
  valider:       (id)      => api.patch(`/sorties/${id}/valider`).then(r => r.data),
  annuler:       (id)      => api.patch(`/sorties/${id}/annuler`).then(r => r.data),
}
