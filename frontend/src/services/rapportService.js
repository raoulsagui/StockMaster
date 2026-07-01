// ============================================================
// SERVICE RAPPORTS
// Centralise tous les appels HTTP vers /api/rapports.
// Les exports (PDF/Excel/CSV) déclenchent un téléchargement
// côté navigateur via un blob.
// ============================================================

import api from './api'

const rapportService = {

  // -------------------------------------------------------
  // DONNÉES JSON
  // -------------------------------------------------------

  /** Liste des inventaires (filtrables par statut) */
  async listerInventaires(statut = null) {
    const params = statut ? { statut } : {}
    const res = await api.get('/rapports/inventaires', { params })
    return res.data
  },

  /** Rapport complet d'un inventaire par ID */
  async getRapportInventaire(id) {
    const res = await api.get(`/rapports/inventaires/${id}`)
    return res.data
  },

  /** Rapport des mouvements sur une période */
  async getRapportMouvements({ dateDebut, dateFin, entrepotId = null, type = null }) {
    const params = { dateDebut, dateFin }
    if (entrepotId) params.entrepotId = entrepotId
    if (type)       params.type       = type
    const res = await api.get('/rapports/mouvements', { params })
    return res.data
  },

  /** Rapport des fournisseurs sur une période */
  async getRapportFournisseurs({ dateDebut, dateFin }) {
    const res = await api.get('/rapports/fournisseurs', { params: { dateDebut, dateFin } })
    return res.data
  },

  // -------------------------------------------------------
  // EXPORTS — télécharge le fichier côté navigateur
  // -------------------------------------------------------

  async exportInventaire(id, format = 'pdf') {
    const res = await api.get(`/rapports/inventaires/${id}/export`, {
      params: { format },
      responseType: 'blob',
    })
    telecharger(res, `inventaire-${id}.${ext(format)}`)
  },

  async exportMouvements({ dateDebut, dateFin, entrepotId = null, type = null }, format = 'pdf') {
    const params = { dateDebut, dateFin, format }
    if (entrepotId) params.entrepotId = entrepotId
    if (type)       params.type       = type
    const res = await api.get('/rapports/mouvements/export', {
      params,
      responseType: 'blob',
    })
    telecharger(res, `rapport-mouvements.${ext(format)}`)
  },

  async exportFournisseurs({ dateDebut, dateFin }, format = 'pdf') {
    const res = await api.get('/rapports/fournisseurs/export', {
      params: { dateDebut, dateFin, format },
      responseType: 'blob',
    })
    telecharger(res, `rapport-fournisseurs.${ext(format)}`)
  },
}

// -------------------------------------------------------
// HELPERS
// -------------------------------------------------------

function ext(format) {
  const map = { pdf: 'pdf', excel: 'xlsx', xlsx: 'xlsx', csv: 'csv' }
  return map[format] ?? 'pdf'
}

function telecharger(response, nomFichier) {
  const url  = URL.createObjectURL(new Blob([response.data]))
  const link = document.createElement('a')
  link.href  = url
  link.setAttribute('download', nomFichier)
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}

export default rapportService
