<script setup>
/**
 * Barre de statistiques du module Inventaires.
 * Affiche 4 KPI : total, en cours, brouillons, validés.
 */
const props = defineProps({
  stats: { type: Object, default: () => ({}) },
})
</script>

<template>
  <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">

    <!-- Total -->
    <div class="card p-4">
      <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">Total</p>
      <p class="text-2xl font-bold text-gray-900 mt-1">{{ stats.totalInventaires ?? 0 }}</p>
      <p class="text-xs text-gray-400 mt-1">inventaires créés</p>
    </div>

    <!-- En cours -->
    <div class="card p-4 border-l-4 border-blue-500">
      <p class="text-xs font-medium text-blue-600 uppercase tracking-wider">En cours</p>
      <p class="text-2xl font-bold text-blue-700 mt-1">{{ stats.enCours ?? 0 }}</p>
      <p class="text-xs text-gray-400 mt-1">comptage actif</p>
    </div>

    <!-- Brouillons -->
    <div class="card p-4 border-l-4 border-yellow-400">
      <p class="text-xs font-medium text-yellow-600 uppercase tracking-wider">Brouillons</p>
      <p class="text-2xl font-bold text-yellow-700 mt-1">{{ stats.brouillons ?? 0 }}</p>
      <p class="text-xs text-gray-400 mt-1">non démarrés</p>
    </div>

    <!-- Validés -->
    <div class="card p-4 border-l-4 border-green-500">
      <p class="text-xs font-medium text-green-600 uppercase tracking-wider">Validés</p>
      <p class="text-2xl font-bold text-green-700 mt-1">{{ stats.valides ?? 0 }}</p>
      <p class="text-xs text-gray-400 mt-1">ajustements appliqués</p>
    </div>

  </div>
</template>
