<script setup>
/**
 * Barre de statistiques du module Commandes.
 * 5 KPI : total, brouillons, validées, livrées, annulées.
 */
defineProps({
  stats: { type: Object, default: () => ({}) },
})
</script>

<template>
  <div class="grid grid-cols-2 lg:grid-cols-5 gap-4">

    <div class="card p-4">
      <p class="text-xs font-medium text-gray-500 uppercase tracking-wider">Total</p>
      <p class="text-2xl font-bold text-gray-900 mt-1">{{ stats.totalCommandes ?? 0 }}</p>
      <p class="text-xs text-gray-400 mt-1">commandes créées</p>
    </div>

    <div class="card p-4 border-l-4 border-yellow-400">
      <p class="text-xs font-medium text-yellow-600 uppercase tracking-wider">Brouillons</p>
      <p class="text-2xl font-bold text-yellow-700 mt-1">{{ stats.brouillons ?? 0 }}</p>
      <p class="text-xs text-gray-400 mt-1">en préparation</p>
    </div>

    <div class="card p-4 border-l-4 border-blue-500">
      <p class="text-xs font-medium text-blue-600 uppercase tracking-wider">Validées</p>
      <p class="text-2xl font-bold text-blue-700 mt-1">{{ stats.validees ?? 0 }}</p>
      <p class="text-xs text-gray-400 mt-1">en attente livraison</p>
    </div>

    <div class="card p-4 border-l-4 border-green-500">
      <p class="text-xs font-medium text-green-600 uppercase tracking-wider">Livrées</p>
      <p class="text-2xl font-bold text-green-700 mt-1">{{ stats.livrees ?? 0 }}</p>
      <p class="text-xs text-gray-400 mt-1">stock mis à jour</p>
    </div>

    <div class="card p-4 border-l-4 border-red-400">
      <p class="text-xs font-medium text-red-500 uppercase tracking-wider">Annulées</p>
      <p class="text-2xl font-bold text-red-600 mt-1">{{ stats.annulees ?? 0 }}</p>
      <p class="text-xs text-gray-400 mt-1">abandonnées</p>
    </div>

  </div>
</template>
