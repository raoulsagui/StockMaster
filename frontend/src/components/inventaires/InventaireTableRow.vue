<script setup>
/**
 * Ligne du tableau d'inventaires.
 * Affiche les informations clés et les actions disponibles selon le statut.
 */
defineProps({
  inventaire: { type: Object, required: true },
})

defineEmits(['voir', 'demarrer', 'annuler'])

const statutConfig = {
  BROUILLON: { label: 'Brouillon',  classes: 'badge-yellow' },
  EN_COURS:  { label: 'En cours',   classes: 'badge-blue'   },
  VALIDE:    { label: 'Validé',     classes: 'badge-green'  },
  ANNULE:    { label: 'Annulé',     classes: 'badge-red'    },
}

const typeConfig = {
  COMPLET: { label: 'Complet',  classes: 'badge-gray'  },
  PARTIEL: { label: 'Partiel',  classes: 'badge-purple' },
}
</script>

<template>
  <tr class="hover:bg-gray-50 transition-colors">

    <!-- Référence -->
    <td class="table-cell">
      <span class="font-mono text-sm font-medium text-gray-900">{{ inventaire.reference }}</span>
    </td>

    <!-- Type -->
    <td class="table-cell">
      <span :class="typeConfig[inventaire.type]?.classes ?? 'badge-gray'" class="badge">
        {{ typeConfig[inventaire.type]?.label ?? inventaire.type }}
      </span>
    </td>

    <!-- Entrepôt -->
    <td class="table-cell text-sm text-gray-700">{{ inventaire.entrepotNom }}</td>

    <!-- Progression -->
    <td class="table-cell">
      <span class="text-sm text-gray-700 font-mono">
        {{ inventaire.nombreLignesComptees }}/{{ inventaire.nombreLignes }}
      </span>
      <p class="text-xs text-gray-400 mt-0.5">lignes</p>
    </td>

    <!-- Écarts -->
    <td class="table-cell text-sm">
      <div v-if="inventaire.nombreLignesAvecEcart > 0" class="space-y-0.5">
        <div v-if="inventaire.nombreLignesEcartPositif > 0"
          class="flex items-center gap-1 text-green-600 text-xs">
          <span>▲</span>
          <span>{{ inventaire.nombreLignesEcartPositif }} surplus</span>
        </div>
        <div v-if="inventaire.nombreLignesEcartNegatif > 0"
          class="flex items-center gap-1 text-red-600 text-xs">
          <span>▼</span>
          <span>{{ inventaire.nombreLignesEcartNegatif }} manque(s)</span>
        </div>
      </div>
      <span v-else class="text-xs text-gray-400">—</span>
    </td>

    <!-- Date prévue -->
    <td class="table-cell text-sm text-gray-500">
      {{ new Date(inventaire.datePrevue).toLocaleDateString('fr-FR') }}
    </td>

    <!-- Statut -->
    <td class="table-cell">
      <span :class="statutConfig[inventaire.statut]?.classes ?? 'badge-gray'" class="badge">
        {{ statutConfig[inventaire.statut]?.label ?? inventaire.statut }}
      </span>
    </td>

    <!-- Actions -->
    <td class="table-cell text-right">
      <div class="flex items-center justify-end gap-1">

        <!-- Voir le détail -->
        <button
          @click="$emit('voir', inventaire.id)"
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

        <!-- Démarrer (si brouillon) -->
        <button
          v-if="inventaire.statut === 'BROUILLON'"
          @click="$emit('demarrer', inventaire)"
          class="btn-icon text-blue-600 hover:bg-blue-50"
          title="Démarrer le comptage"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"/>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
        </button>

        <!-- Annuler (si brouillon ou en cours) -->
        <button
          v-if="inventaire.statut === 'BROUILLON' || inventaire.statut === 'EN_COURS'"
          @click="$emit('annuler', inventaire)"
          class="btn-icon text-red-500 hover:bg-red-50"
          title="Annuler l'inventaire"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>

      </div>
    </td>

  </tr>
</template>
