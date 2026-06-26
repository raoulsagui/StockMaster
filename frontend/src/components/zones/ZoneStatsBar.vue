<script setup>
import { computed } from 'vue'

const props = defineProps({
  zones:       { type: Array,  required: true },
  totalAbsolu: { type: Number, default: 0 },
})

const totalZones   = computed(() => props.zones.length)
const totalActives = computed(() => props.zones.filter(z => z.actif).length)
const totalInactives = computed(() => totalZones.value - totalActives.value)

const tauxMoyen = computed(() => {
  // On ne considère que les zones ayant une capacité définie
  const zonesAvecCapacite = props.zones.filter(z => z.capaciteTotale != null && z.capaciteTotale > 0)
  if (!zonesAvecCapacite.length) return null
  const total = zonesAvecCapacite.reduce((acc, z) => acc + (z.tauxOccupation || 0), 0)
  return Math.round(total / zonesAvecCapacite.length)
})

const kpis = computed(() => [
  {
    label:      'Zones affichées',
    value:      totalZones.value,
    sub:        props.totalAbsolu && props.totalAbsolu !== totalZones.value
                  ? `sur ${props.totalAbsolu} au total`
                  : `${totalActives.value} active${totalActives.value > 1 ? 's' : ''}`,
    color:      'bg-blue-600',
    subColor:   'text-gray-400',
    valueColor: 'text-gray-900',
    icon:       'M4 5a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1V5zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1V5zM4 15a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1v-4zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z',
  },
  {
    label:      'Zones actives',
    value:      totalActives.value,
    sub:        `${totalInactives.value} inactive${totalInactives.value > 1 ? 's' : ''}`,
    color:      'bg-green-600',
    subColor:   'text-gray-400',
    valueColor: 'text-green-600',
    icon:       'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z',
  },
  {
    label:      "Taux d'occupation moyen",
    value:      tauxMoyen.value !== null ? tauxMoyen.value + ' %' : '—',
    sub:        tauxMoyen.value === null ? 'Aucune capacité configurée'
                : tauxMoyen.value >= 85 ? 'Saturation critique'
                : tauxMoyen.value >= 60 ? 'Charge élevée'
                : 'Capacité disponible',
    color:      tauxMoyen.value === null ? 'bg-gray-400'
                : tauxMoyen.value >= 85 ? 'bg-red-600'
                : tauxMoyen.value >= 60 ? 'bg-orange-500'
                : 'bg-green-600',
    subColor:   tauxMoyen.value === null ? 'text-gray-400'
                : tauxMoyen.value >= 85 ? 'text-red-500'
                : tauxMoyen.value >= 60 ? 'text-orange-400'
                : 'text-green-500',
    valueColor: tauxMoyen.value === null ? 'text-gray-400'
                : tauxMoyen.value >= 85 ? 'text-red-600'
                : tauxMoyen.value >= 60 ? 'text-orange-500'
                : 'text-green-600',
    icon:       'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z',
  },
])
</script>

<template>
  <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 md:gap-4">
    <div
      v-for="kpi in kpis"
      :key="kpi.label"
      class="card p-3 md:p-5 flex items-start gap-3 md:gap-4 hover:shadow-md transition-shadow duration-200"
    >
      <div :class="['w-10 h-10 md:w-12 md:h-12 rounded-xl flex items-center justify-center flex-shrink-0', kpi.color]">
        <svg class="w-5 h-5 md:w-6 md:h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="kpi.icon"/>
        </svg>
      </div>
      <div class="flex-1 min-w-0">
        <p :class="['text-xl md:text-2xl font-bold', kpi.valueColor]">{{ kpi.value }}</p>
        <p class="text-xs md:text-sm text-gray-500 leading-snug">{{ kpi.label }}</p>
        <p :class="['text-xs mt-1 font-medium', kpi.subColor]">{{ kpi.sub }}</p>
      </div>
    </div>
  </div>
</template>
