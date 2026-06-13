<script setup>
// ============================================================
// COMPOSANT : EntrepotStatsBar
//
// Affiche les 4 cartes de métriques globales en haut de la
// page entrepôts : total, actifs, capacité cumulée, taux moyen.
//
// Props :
//   entrepots → tableau complet des entrepôts (non filtrés)
//               pour que les métriques reflètent toujours le parc entier
// ============================================================

import { computed } from 'vue'

const props = defineProps({
  /** Liste complète des entrepôts */
  entrepots: {
    type: Array,
    required: true,
  },
})

// -------------------------------------------------------
// MÉTRIQUES CALCULÉES
// -------------------------------------------------------

const totalEntrepots = computed(() => props.entrepots.length)

const totalActifs = computed(() => props.entrepots.filter(e => e.actif).length)

const capaciteTotaleCumulee = computed(() =>
  props.entrepots.reduce((acc, e) => acc + (e.capaciteTotale || 0), 0)
)

const tauxOccupationMoyen = computed(() => {
  if (!capaciteTotaleCumulee.value) return 0
  const totalUtilise = props.entrepots.reduce((acc, e) => acc + (e.capaciteUtilisee || 0), 0)
  return Math.round((totalUtilise / capaciteTotaleCumulee.value) * 100)
})

// Couleur du taux d'occupation
function getCouleurTaux(taux) {
  if (taux >= 85) return 'text-red-600'
  if (taux >= 60) return 'text-orange-500'
  return 'text-green-600'
}

// Formate une valeur en m² avec séparateur de milliers
function formatCapacite(val) {
  return val != null ? val.toLocaleString('fr-FR') + ' m²' : '—'
}
</script>

<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">

    <!-- Total entrepôts -->
    <div class="card p-5">
      <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Total entrepôts</p>
      <p class="text-3xl font-bold text-gray-900 mt-1">{{ totalEntrepots }}</p>
    </div>

    <!-- Entrepôts actifs -->
    <div class="card p-5">
      <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Actifs</p>
      <p class="text-3xl font-bold text-green-600 mt-1">{{ totalActifs }}</p>
    </div>

    <!-- Capacité totale cumulée -->
    <div class="card p-5">
      <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Capacité totale</p>
      <p class="text-3xl font-bold text-gray-900 mt-1">{{ formatCapacite(capaciteTotaleCumulee) }}</p>
    </div>

    <!-- Taux d'occupation moyen -->
    <div class="card p-5">
      <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Taux moyen d'occupation</p>
      <p :class="['text-3xl font-bold mt-1', getCouleurTaux(tauxOccupationMoyen)]">
        {{ tauxOccupationMoyen }} %
      </p>
    </div>

  </div>
</template>
