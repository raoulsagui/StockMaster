<script setup>
/**
 * Ligne du tableau d'inventaires.
 * Actions disponibles : Détail (modal), Annuler.
 */
defineProps({
  inventaire: { type: Object, required: true },
})

defineEmits(['detail', 'annuler'])

const statutConfig = {
  BROUILLON: { label: 'Brouillon', classes: 'bg-yellow-100 text-yellow-700' },
  EN_COURS:  { label: 'En cours',  classes: 'bg-blue-100 text-blue-700'     },
  VALIDE:    { label: 'Validé',    classes: 'bg-green-100 text-green-700'   },
  ANNULE:    { label: 'Annulé',    classes: 'bg-red-100 text-red-600'       },
}

const typeConfig = {
  COMPLET: { label: 'Complet', classes: 'bg-gray-100 text-gray-600'    },
  PARTIEL: { label: 'Partiel', classes: 'bg-purple-100 text-purple-700' },
}

/**
 * Retourne la date pertinente selon le statut de l'inventaire.
 *   VALIDE   → dateValidation (date réelle de clôture)
 *   Autres   → dateCreation   (date de création / démarrage)
 */
function dateInventaire(inv) {
  const raw = inv.statut === 'VALIDE' ? inv.dateValidation : inv.dateCreation
  if (!raw) return '—'
  return new Date(raw).toLocaleDateString('fr-FR')
}

/**
 * Libellé de la colonne date selon le statut.
 */
function libelleDate(statut) {
  return statut === 'VALIDE' ? 'Validé le' : 'Créé le'
}
</script>

<template>
  <tr class="hover:bg-gray-50 transition-colors">

    <!-- Référence -->
    <td class="table-cell">
      <span class="font-mono text-sm font-semibold text-gray-900">{{ inventaire.reference }}</span>
    </td>

    <!-- Type -->
    <td class="table-cell">
      <span :class="[typeConfig[inventaire.type]?.classes ?? 'bg-gray-100 text-gray-600',
        'px-2.5 py-0.5 rounded-full text-xs font-medium']">
        {{ typeConfig[inventaire.type]?.label ?? inventaire.type }}
      </span>
    </td>

    <!-- Entrepôt -->
    <td class="table-cell text-sm text-gray-700">{{ inventaire.entrepotNom }}</td>

    <!-- Surplus (écart positif) -->
    <td class="table-cell text-center">
      <span
        v-if="inventaire.nombreLignesEcartPositif > 0"
        class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-green-100 text-green-700"
      >
        ▲ {{ inventaire.nombreLignesEcartPositif }}
      </span>
      <span v-else class="text-xs text-gray-300">—</span>
    </td>

    <!-- Manque (écart négatif) -->
    <td class="table-cell text-center">
      <span
        v-if="inventaire.nombreLignesEcartNegatif > 0"
        class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-red-100 text-red-700"
      >
        ▼ {{ inventaire.nombreLignesEcartNegatif }}
      </span>
      <span v-else class="text-xs text-gray-300">—</span>
    </td>

    <!-- Date réelle de l'inventaire -->
    <td class="table-cell text-sm text-gray-500">
      <p>{{ dateInventaire(inventaire) }}</p>
      <p class="text-xs text-gray-400">{{ libelleDate(inventaire.statut) }}</p>
    </td>

    <!-- Statut -->
    <td class="table-cell">
      <span :class="[statutConfig[inventaire.statut]?.classes ?? 'bg-gray-100 text-gray-600',
        'px-2.5 py-0.5 rounded-full text-xs font-semibold']">
        {{ statutConfig[inventaire.statut]?.label ?? inventaire.statut }}
      </span>
    </td>

    <!-- Actions -->
    <td class="table-cell text-right">
      <div class="flex items-center justify-end gap-1">

        <!-- Détail -->
        <button
          @click="$emit('detail', inventaire.id)"
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

        <!-- Annuler -->
        <button
          v-if="['BROUILLON', 'EN_COURS'].includes(inventaire.statut)"
          @click="$emit('annuler', inventaire)"
          class="btn-icon text-red-500 hover:bg-red-50"
          title="Annuler l'inventaire"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>

      </div>
    </td>

  </tr>
</template>
