<script setup>
// ============================================================
// COMPOSANT : ZoneStatsBar
//
// Affiche les 3 cartes de métriques globales en haut de la
// page zones. Les métriques sont calculées sur les zones
// filtrées (pas le total absolu) pour refléter la sélection.
//
// Props :
//   zones → tableau des zones actuellement affichées (filtrées)
// ============================================================

import { computed } from 'vue'

const props = defineProps({
  /** Zones actuellement affichées après filtrage */
  zones: {
    type: Array,
    required: true,
  },
  /** Total absolu de zones (avant filtrage) — pour l'info contextuelle */
  totalAbsolu: {
    type: Number,
    default: 0,
  },
})

// -------------------------------------------------------
// MÉTRIQUES
// -------------------------------------------------------

const totalZones = computed(() => props.zones.length)

const totalActives = computed(() => props.zones.filter(z => z.actif).length)

/** Taux d'occupation moyen (moyenne simple des taux de chaque zone) */
const tauxMoyen = computed(() => {
  if (!totalZones.value) return 0
  const total = props.zones.reduce((acc, z) => acc + (z.tauxOccupation || 0), 0)
  return Math.round(total / totalZones.value)
})

function getCouleurTaux(taux) {
  if (taux >= 85) return 'text-red-600'
  if (taux >= 60) return 'text-orange-500'
  return 'text-green-600'
}
</script>

<template>
  <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">

    <!-- Zones affichées -->
    <div class="card p-5">
      <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Zones affichées</p>
      <p class="text-3xl font-bold text-gray-900 mt-1">{{ totalZones }}</p>
      <!-- Sous-titre si filtre actif -->
      <p v-if="totalAbsolu && totalAbsolu !== totalZones" class="text-xs text-gray-400 mt-1">
        sur {{ totalAbsolu }} au total
      </p>
    </div>

    <!-- Zones actives dans la sélection -->
    <div class="card p-5">
      <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Zones actives</p>
      <p class="text-3xl font-bold text-green-600 mt-1">{{ totalActives }}</p>
    </div>

    <!-- Taux d'occupation moyen -->
    <div class="card p-5">
      <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Taux d'occupation moyen</p>
      <p :class="['text-3xl font-bold mt-1', getCouleurTaux(tauxMoyen)]">
        {{ tauxMoyen }} %
      </p>
    </div>

  </div>
</template>
