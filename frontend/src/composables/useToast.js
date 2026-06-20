// ============================================================
// COMPOSABLE : useToast
//
// Système de notifications toast global.
// Usage dans n'importe quel composant :
//
//   const toast = useToast()
//   toast.success('Utilisateur créé avec succès.')
//   toast.error('Stock insuffisant.')
//   toast.warning('Aucun entrepôt assigné.')
//   toast.info('Réinitialisation en cours…')
//
// Le composant AppToast dans App.vue écoute et affiche les toasts.
// ============================================================

import { reactive } from 'vue'

// État global partagé entre tous les composants
const toasts = reactive([])
let nextId = 0

function ajouter(message, type = 'info', duree = 4000) {
  const id = ++nextId
  toasts.push({ id, message, type })

  // Auto-suppression après `duree` ms
  setTimeout(() => supprimer(id), duree)
}

function supprimer(id) {
  const idx = toasts.findIndex(t => t.id === id)
  if (idx !== -1) toasts.splice(idx, 1)
}

export function useToast() {
  return {
    toasts,
    supprimer,
    success: (message, duree) => ajouter(message, 'success', duree),
    error:   (message, duree) => ajouter(message, 'error',   duree ?? 6000),
    warning: (message, duree) => ajouter(message, 'warning', duree),
    info:    (message, duree) => ajouter(message, 'info',    duree),
  }
}
