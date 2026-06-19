<script setup>
/**
 * Ligne du tableau des commandes fournisseur.
 * Actions contextuelles selon le statut de la commande.
 */
import { useAuthStore } from '@/stores/authStore'

defineProps({
  commande: { type: Object, required: true },
})
defineEmits(['voir', 'valider', 'receptionner', 'annuler'])

const authStore = useAuthStore()

const statutConfig = {
  BROUILLON: { label: 'Brouillon',  bg: 'bg-yellow-100 text-yellow-700' },
  VALIDEE:   { label: 'Validée',    bg: 'bg-blue-100   text-blue-700'   },
  LIVREE:    { label: 'Livrée',     bg: 'bg-green-100  text-green-700'  },
  ANNULEE:   { label: 'Annulée',    bg: 'bg-red-100    text-red-600'    },
}

function formatDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleDateString('fr-FR')
}

function formatMontant(m) {
  if (m == null || m === 0) return '—'
  return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(m)
}
</script>

<template>
  <tr class="hover:bg-gray-50 transition-colors">

    <!-- Référence -->
    <td class="table-cell">
      <span class="font-mono text-sm font-semibold text-gray-900">{{ commande.reference }}</span>
    </td>

    <!-- Fournisseur -->
    <td class="table-cell">
      <p class="text-sm font-medium text-gray-900">{{ commande.fournisseurNom }}</p>
      <p class="text-xs text-gray-400">{{ commande.fournisseurEmail }}</p>
    </td>

    <!-- Entrepôt -->
    <td class="table-cell text-sm text-gray-700">{{ commande.entrepotNom }}</td>

    <!-- Lignes / Unités -->
    <td class="table-cell text-center">
      <p class="text-sm font-medium text-gray-900">{{ commande.nombreLignes }} ligne{{ commande.nombreLignes > 1 ? 's' : '' }}</p>
      <p class="text-xs text-gray-400">{{ commande.nombreUnites }} unité{{ commande.nombreUnites > 1 ? 's' : '' }}</p>
    </td>

    <!-- Montant total -->
    <td class="table-cell text-right">
      <span class="text-sm font-mono font-medium text-gray-800">
        {{ formatMontant(commande.montantTotal) }}
      </span>
    </td>

    <!-- Date livraison prévue -->
    <td class="table-cell text-sm text-gray-500">
      {{ formatDate(commande.dateLivraisonPrevue) }}
    </td>

    <!-- Statut -->
    <td class="table-cell">
      <span :class="[statutConfig[commande.statut]?.bg, 'px-2.5 py-1 rounded-full text-xs font-semibold']">
        {{ statutConfig[commande.statut]?.label ?? commande.statut }}
      </span>
    </td>

    <!-- Actions -->
    <td class="table-cell text-right">
      <div class="flex items-center justify-end gap-1">

        <!-- Voir le détail -->
        <button
          @click="$emit('voir', commande.id)"
          class="btn-icon text-blue-600 hover:bg-blue-50"
          title="Voir le détail"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
          </svg>
        </button>

        <!-- Valider (BROUILLON + ADMIN/GESTIONNAIRE) -->
        <button
          v-if="commande.statut === 'BROUILLON' && ['ADMIN','GESTIONNAIRE'].includes(authStore.role)"
          @click="$emit('valider', commande)"
          class="btn-icon text-blue-600 hover:bg-blue-50"
          title="Valider la commande"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
          </svg>
        </button>

        <!-- Réceptionner (VALIDEE) -->
        <button
          v-if="commande.statut === 'VALIDEE'"
          @click="$emit('receptionner', commande)"
          class="btn-icon text-green-600 hover:bg-green-50"
          title="Réceptionner la livraison"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
          </svg>
        </button>

        <!-- Annuler (BROUILLON ou VALIDEE + ADMIN/GESTIONNAIRE) -->
        <button
          v-if="['BROUILLON','VALIDEE'].includes(commande.statut) && ['ADMIN','GESTIONNAIRE'].includes(authStore.role)"
          @click="$emit('annuler', commande)"
          class="btn-icon text-red-500 hover:bg-red-50"
          title="Annuler la commande"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>

      </div>
    </td>

  </tr>
</template>
