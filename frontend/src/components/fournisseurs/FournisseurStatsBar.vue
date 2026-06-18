<script setup>
import { computed } from 'vue'

const props = defineProps({
  fournisseurs: { type: Array, required: true },
})

const total         = computed(() => props.fournisseurs.length)
const totalActifs   = computed(() => props.fournisseurs.filter(f => f.actif).length)
const totalInactifs = computed(() => total.value - totalActifs.value)

const kpis = computed(() => [
  {
    label:      'Total fournisseurs',
    value:      total.value,
    sub:        `${totalActifs.value} actif${totalActifs.value > 1 ? 's' : ''}`,
    color:      'bg-blue-600',
    subColor:   'text-gray-400',
    valueColor: 'text-gray-900',
    icon:       'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4',
  },
  {
    label:      'Fournisseurs actifs',
    value:      totalActifs.value,
    sub:        'Disponibles pour commande',
    color:      'bg-green-600',
    subColor:   'text-green-500',
    valueColor: 'text-green-600',
    icon:       'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z',
  },
  {
    label:      'Fournisseurs inactifs',
    value:      totalInactifs.value,
    sub:        totalInactifs.value > 0 ? 'Non disponibles' : 'Aucun inactif',
    color:      totalInactifs.value > 0 ? 'bg-gray-400' : 'bg-gray-300',
    subColor:   totalInactifs.value > 0 ? 'text-red-400' : 'text-gray-400',
    valueColor: totalInactifs.value > 0 ? 'text-gray-600' : 'text-gray-400',
    icon:       'M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636',
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
