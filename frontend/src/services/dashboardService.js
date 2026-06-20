import api from '@/services/api'

export default {
  /** GET /api/dashboard — KPI filtrés selon l'utilisateur connecté */
  getDashboard: () => api.get('/dashboard').then(r => r.data),

  /** PATCH /api/entrepots/{id}/membres — assigner des membres à un entrepôt */
  mettreAJourMembres: (entrepotId, membresIds) =>
    api.patch(`/entrepots/${entrepotId}/membres`, membresIds).then(r => r.data),
}
