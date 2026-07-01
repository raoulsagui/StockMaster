import api from './api'

const emplacementService = {
  // ── RAYONS ──────────────────────────────────────────────────────
  async getRayonsByZone(zoneId)       { return (await api.get('/emplacements/rayons', { params: { zoneId } })).data },
  async getRayonById(id)              { return (await api.get(`/emplacements/rayons/${id}`)).data },
  async creerRayon(data)              { return (await api.post('/emplacements/rayons', data)).data },
  async modifierRayon(id, data)       { return (await api.put(`/emplacements/rayons/${id}`, data)).data },
  async toggleRayonStatut(id)         { return (await api.patch(`/emplacements/rayons/${id}/statut`)).data },

  // ── ÉTAGÈRES ─────────────────────────────────────────────────────
  async getEtageresByRayon(rayonId)   { return (await api.get('/emplacements/etageres', { params: { rayonId } })).data },
  async getEtagereById(id)            { return (await api.get(`/emplacements/etageres/${id}`)).data },
  async creerEtagere(data)            { return (await api.post('/emplacements/etageres', data)).data },
  async modifierEtagere(id, data)     { return (await api.put(`/emplacements/etageres/${id}`, data)).data },
  async toggleEtagereStatut(id)       { return (await api.patch(`/emplacements/etageres/${id}/statut`)).data },

  // ── EMPLACEMENTS ─────────────────────────────────────────────────
  async getByEtagere(etagereId)       { return (await api.get('/emplacements', { params: { etagereId } })).data },
  async getByEntrepot(entrepotId)     { return (await api.get('/emplacements', { params: { entrepotId } })).data },
  async getByZone(zoneId)             { return (await api.get('/emplacements', { params: { zoneId } })).data },
  async getLibres(entrepotId)         { return (await api.get('/emplacements/libres', { params: { entrepotId } })).data },
  async getStats(entrepotId)          { return (await api.get('/emplacements/stats', { params: { entrepotId } })).data },
  async getById(id)                   { return (await api.get(`/emplacements/${id}`)).data },
  async creer(data)                   { return (await api.post('/emplacements', data)).data },
  async modifier(id, data)            { return (await api.put(`/emplacements/${id}`, data)).data },
  async changerStatut(id, statut)     { return (await api.patch(`/emplacements/${id}/statut`, null, { params: { statut } })).data },
}

export default emplacementService
