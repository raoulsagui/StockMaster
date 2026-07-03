<script setup>
import { computed } from 'vue'

const props = defineProps({
  zones:       { type: Array,  required: true },
  totalAbsolu: { type: Number, default: 0 },
})

const totalZones   = computed(() => props.zones.length)
const totalActives = computed(() => props.zones.filter(z => z.actif).length)
const totalInactives = computed(() => totalZones.value - totalActives.value)

const capaciteUtiliseeTotale = computed(() => {
  return props.zones.reduce((acc, z) => acc + (parseFloat(z.capaciteUtilisee) || 0), 0)
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
    label:      'Capacité utilisée totale',
    value:      capaciteUtiliseeTotale.value.toLocaleString('fr-FR') + ' m³',
    sub:        'Somme des capacités occupées par zone',
    color:      'bg-indigo-600',
    subColor:   'text-gray-400',
    valueColor: 'text-indigo-600',
    icon:       'M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4',
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
