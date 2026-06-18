<script setup>
import { computed } from 'vue'

const props = defineProps({
  fournisseurs: { type: Array, required: true },
})

const total       = computed(() => props.fournisseurs.length)
const totalActifs = computed(() => props.fournisseurs.filter(f => f.actif).length)
const totalInactifs = computed(() => total.value - totalActifs.value)
</script>

<template>
  <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">

    <div class="card p-5">
      <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Total fournisseurs</p>
      <p class="text-3xl font-bold text-gray-900 mt-1">{{ total }}</p>
    </div>

    <div class="card p-5">
      <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Actifs</p>
      <p class="text-3xl font-bold text-green-600 mt-1">{{ totalActifs }}</p>
    </div>

    <div class="card p-5">
      <p class="text-xs font-medium text-gray-500 uppercase tracking-wide">Inactifs</p>
      <p class="text-3xl font-bold text-gray-400 mt-1">{{ totalInactifs }}</p>
    </div>

  </div>
</template>
