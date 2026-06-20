<script setup>
// ============================================================
// COMPOSANT : EntrepotTableRow
//
// Représente une ligne dans le tableau des entrepôts.
//
// Événements émis :
//   @voir-zones  → l'utilisateur veut voir les zones de cet entrepôt
//   @modifier    → l'utilisateur veut modifier cet entrepôt
//   @toggle      → l'utilisateur veut activer/désactiver l'entrepôt
// ============================================================

defineProps({
  entrepot: {
    type: Object,
    required: true,
  },
  peutModifier: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['voir-zones', 'modifier', 'toggle'])

function getCouleurBarre(taux) {
  if (taux >= 85) return 'bg-red-500'
  if (taux >= 60) return 'bg-orange-400'
  return 'bg-green-500'
}

function getCouleurTaux(taux) {
  if (taux >= 85) return 'text-red-600'
  if (taux >= 60) return 'text-orange-500'
  return 'text-green-600'
}

function formatCapacite(val) {
  return val != null ? val.toLocaleString('fr-FR') + ' m³' : '—'
}
</script>

<template>
  <tr class="hover:bg-gray-50 transition-colors">

    <!-- Nom -->
    <td class="table-cell">
      <p class="font-medium text-gray-900">{{ entrepot.nom }}</p>
    </td>

    <!-- Adresse -->
    <td class="table-cell">
      <div class="flex items-center gap-1 text-sm text-gray-500">
        <svg class="w-3.5 h-3.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
        </svg>
        <span class="truncate max-w-xs">{{ entrepot.adresse }}</span>
      </div>
    </td>

    <!-- Responsable -->
    <td class="table-cell">
      <div v-if="entrepot.responsable" class="flex items-center gap-1.5">
        <div class="w-6 h-6 rounded-full bg-blue-100 flex items-center justify-center flex-shrink-0">
          <svg class="w-3.5 h-3.5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
          </svg>
        </div>
        <span class="text-sm text-gray-600">{{ entrepot.responsable.nomComplet }}</span>
      </div>
      <span v-else class="text-sm text-gray-300">—</span>
    </td>

    <!-- Taux d'occupation (barre + %) -->
    <td class="table-cell w-44">
      <div class="flex items-center gap-2">
        <div class="flex-1 h-1.5 bg-gray-100 rounded-full overflow-hidden">
          <div
            :class="['h-1.5 rounded-full transition-all duration-500', getCouleurBarre(entrepot.tauxOccupation)]"
            :style="{ width: Math.min(entrepot.tauxOccupation, 100) + '%' }"
          ></div>
        </div>
        <span :class="['text-xs font-semibold w-10 text-right', getCouleurTaux(entrepot.tauxOccupation)]">
          {{ entrepot.tauxOccupation }} %
        </span>
      </div>
    </td>

    <!-- Capacité totale -->
    <td class="table-cell text-sm text-gray-600">
      {{ formatCapacite(entrepot.capaciteTotale) }}
    </td>

    <!-- Capacité utilisée -->
    <td class="table-cell text-sm text-gray-600">
      {{ formatCapacite(entrepot.capaciteUtilisee) }}
    </td>

    <!-- Nombre de zones -->
    <td class="table-cell">
      <span class="text-sm font-semibold text-blue-600">{{ entrepot.nombreZones }}</span>
    </td>

    <!-- Statut -->
    <td class="table-cell">
      <span :class="entrepot.actif ? 'badge-actif' : 'badge-inactif'">
        {{ entrepot.actif ? 'Actif' : 'Inactif' }}
      </span>
    </td>

    <!-- Actions -->
    <td class="table-cell">
      <div class="flex items-center justify-end gap-2">

        <!-- Voir les zones -->
        <button
          @click="emit('voir-zones', entrepot.id)"
          class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
          title="Voir les zones de cet entrepôt"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M4 5a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1V5zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1V5zM4 15a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1v-4zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z"/>
          </svg>
        </button>

        <!-- Modifier -->
        <button
          v-if="peutModifier"
          @click="emit('modifier', entrepot.id)"
          class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
          title="Modifier cet entrepôt"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
          </svg>
        </button>

        <!-- Activer / Désactiver -->
        <button
          v-if="peutModifier"
          @click="emit('toggle', entrepot)""
          :class="[
            'p-1.5 rounded-lg transition-colors',
            entrepot.actif
              ? 'text-gray-400 hover:text-red-600 hover:bg-red-50'
              : 'text-gray-400 hover:text-green-600 hover:bg-green-50'
          ]"
          :title="entrepot.actif ? 'Désactiver cet entrepôt' : 'Réactiver cet entrepôt'"
        >
          <svg v-if="entrepot.actif" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"/>
          </svg>
          <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
        </button>

      </div>
    </td>

  </tr>
</template>
