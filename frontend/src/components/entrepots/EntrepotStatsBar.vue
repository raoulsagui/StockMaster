<script setup>
import { computed } from 'vue'

const props = defineProps({
  entrepots: { type: Array, required: true },
})

const totalEntrepots = computed(() => props.entrepots.length)
const totalActifs    = computed(() => props.entrepots.filter(e => e.actif).length)
const totalInactifs  = computed(() => totalEntrepots.value - totalActifs.value)

const capaciteTotaleCumulee = computed(() =>
  props.entrepots.reduce((acc, e) => acc + (e.capaciteTotale || 0), 0)
)

const tauxOccupationMoyen = computed(() => {
  if (!capaciteTotaleCumulee.value) return 0
  const totalUtilise = props.entrepots.reduce((acc, e) => acc + (e.capaciteUtilisee || 0), 0)
  return Math.round((totalUtilise / capaciteTotaleCumulee.value) * 100)
})

function formatCapacite(val) {
  return val != null ? val.toLocaleString('fr-FR') + ' m³' : '—'
}

const kpis = computed(() => [
  {
    label:   'Total entrepôts',
    value:   totalEntrepots.value,
    sub:     `${totalActifs.value} actif${totalActifs.value > 1 ? 's' : ''}`,
    color:   'bg-blue-600',
    subColor: 'text-gray-400',
    icon:    'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4',
    valueColor: 'text-gray-900',
  },
  {
    label:   'Entrepôts actifs',
    value:   totalActifs.value,
    sub:     `${totalInactifs.value} inactif${totalInactifs.value > 1 ? 's' : ''}`,
    color:   'bg-green-600',
    subColor: 'text-gray-400',
    icon:    'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z',
    valueColor: 'text-green-600',
  },
  {
    label:   'Capacité totale',
    value:   formatCapacite(capaciteTotaleCumulee.value),
    sub:     'Cumulée sur tous les entrepôts',
    color:   'bg-indigo-600',
    subColor: 'text-gray-400',
    icon:    'M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4',
    valueColor: 'text-gray-900',
  },
  {
    label:   "Taux d'occupation moyen",
    value:   tauxOccupationMoyen.value + ' %',
    sub:     tauxOccupationMoyen.value >= 85 ? 'Saturation critique' : tauxOccupationMoyen.value >= 60 ? 'Charge élevée' : 'Capacité disponible',
    color:   tauxOccupationMoyen.value >= 85 ? 'bg-red-600' : tauxOccupationMoyen.value >= 60 ? 'bg-orange-500' : 'bg-green-600',
    subColor: tauxOccupationMoyen.value >= 85 ? 'text-red-500' : tauxOccupationMoyen.value >= 60 ? 'text-orange-400' : 'text-green-500',
    icon:    'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z',
    valueColor: tauxOccupationMoyen.value >= 85 ? 'text-red-600' : tauxOccupationMoyen.value >= 60 ? 'text-orange-500' : 'text-green-600',
  },
])
</script>

<template>
  <div class="grid grid-cols-2 xl:grid-cols-4 gap-3 md:gap-4">
    <div
      v-for="kpi in kpis"
      :key="kpi.label"
      class="card p-3 md:p-5 flex items-start gap-3 md:gap-4 hover:shadow-md transition-shadow duration-200"
    >
      <!-- Icône -->
      <div :class="['w-10 h-10 md:w-12 md:h-12 rounded-xl flex items-center justify-center flex-shrink-0', kpi.color]">
        <svg class="w-5 h-5 md:w-6 md:h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="kpi.icon"/>
        </svg>
      </div>
      <!-- Texte -->
      <div class="flex-1 min-w-0">
        <p :class="['text-xl md:text-2xl font-bold', kpi.valueColor]">{{ kpi.value }}</p>
        <p class="text-xs md:text-sm text-gray-500 leading-snug">{{ kpi.label }}</p>
        <p :class="['text-xs mt-1 font-medium', kpi.subColor]">{{ kpi.sub }}</p>
      </div>
    </div>
  </div>
</template>
