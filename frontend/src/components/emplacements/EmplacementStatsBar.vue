<script setup>
defineProps({
  stats: { type: Object, required: true },
})
</script>

<template>
  <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
    <div class="card p-4 text-center">
      <p class="text-2xl font-bold text-gray-900">{{ stats.total }}</p>
      <p class="text-xs text-gray-500 mt-1">Total emplacements</p>
    </div>
    <div class="card p-4 text-center">
      <p class="text-2xl font-bold text-green-600">{{ stats.libres }}</p>
      <p class="text-xs text-gray-500 mt-1">Libres</p>
    </div>
    <div class="card p-4 text-center">
      <p class="text-2xl font-bold text-red-600">{{ stats.parStatut?.OCCUPE || 0 }}</p>
      <p class="text-xs text-gray-500 mt-1">Occupés</p>
    </div>
    <div class="card p-4 text-center">
      <p class="text-2xl font-bold text-blue-600">{{ stats.tauxOccupation }} %</p>
      <p class="text-xs text-gray-500 mt-1">Taux d'occupation</p>
    </div>
  </div>
</template>
