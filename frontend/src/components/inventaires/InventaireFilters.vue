<script setup>
/**
 * Filtres de la liste des inventaires.
 * Utilise defineModel (Vue 3.4) — même pattern qu'EntrepotFilters et ZoneFilters.
 */
import { computed } from 'vue'

const recherche = defineModel('recherche', { type: String, default: '' })
const statut    = defineModel('statut',    { type: String, default: '' })
const type      = defineModel('type',      { type: String, default: '' })

const emit = defineEmits(['reset'])

const filtreActif = computed(() => !!recherche.value || !!statut.value || !!type.value)
</script>

<template>
  <div class="card p-4 space-y-3">
    <div class="flex flex-col sm:flex-row gap-3">

      <!-- Recherche textuelle -->
      <div class="relative flex-1">
        <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
          fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0"/>
        </svg>
        <input
          v-model="recherche"
          type="text"
          placeholder="Référence, entrepôt…"
          class="form-input pl-9"
        />
      </div>

      <!-- Filtre par statut -->
      <select v-model="statut" class="form-input w-full sm:w-44">
        <option value="">Tous les statuts</option>
        <option value="BROUILLON">Brouillon</option>
        <option value="EN_COURS">En cours</option>
        <option value="VALIDE">Validé</option>
        <option value="ANNULE">Annulé</option>
      </select>

      <!-- Filtre par type -->
      <select v-model="type" class="form-input w-full sm:w-44">
        <option value="">Tous les types</option>
        <option value="COMPLET">Complet</option>
        <option value="PARTIEL">Partiel</option>
      </select>

    </div> 
    <!-- Réinitialiser -->
    <div v-if="filtreActif" class="flex justify-end">
      <button
        @click="recherche = ''; statut = ''; type = ''; emit('reset')"
        class="text-xs text-gray-400 hover:text-gray-600 underline"
      >
        Réinitialiser les filtres
      </button>
    </div>
  </div>
</template>
