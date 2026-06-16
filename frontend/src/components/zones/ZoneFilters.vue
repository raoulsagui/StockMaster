<script setup>
// ============================================================
// COMPOSANT : ZoneFilters
//
// Barre de filtres de la liste des zones.
// Quatre filtres : texte, entrepôt, type, statut.
//
// Les valeurs sont synchronisées avec le parent via defineModel.
// Le bouton "Réinitialiser" est visible dès qu'un filtre est actif
// et émet un événement @reset pour que le parent nettoie aussi
// le query param de l'URL.
//
// Usage :
//   <ZoneFilters
//     v-model:recherche="recherche"
//     v-model:entrepotId="filtreEntrepot"
//     v-model:type="filtreType"
//     v-model:statut="filtreStatut"
//     :entrepots="entrepots"
//     @reset="reinitialiserFiltres"
//   />
// ============================================================

import { computed } from 'vue'

const recherche  = defineModel('recherche',  { type: String, default: '' })
const entrepotId = defineModel('entrepotId', { default: '' })
const type       = defineModel('type',       { type: String, default: '' })
const statut     = defineModel('statut',     { type: String, default: '' })

const props = defineProps({
  /** Liste des entrepôts pour peupler le select */
  entrepots: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['reset'])

/** Vrai si au moins un filtre est actif → affiche le bouton réinitialiser */
const filtreActif = computed(() =>
  !!recherche.value || !!entrepotId.value || !!type.value || !!statut.value
)
</script>

<template>
  <div class="card p-4 space-y-3">

    <div class="flex flex-col sm:flex-row gap-3">

      <!-- Recherche texte -->
      <div class="relative flex-1">
        <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
          fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0"/>
        </svg>
        <input
          v-model="recherche"
          type="text"
          placeholder="Rechercher par nom…"
          class="form-input pl-9"
        />
      </div>

      <!-- Filtre entrepôt -->
      <select v-model="entrepotId" class="form-input w-full sm:w-52">
        <option value="">Tous les entrepôts</option>
        <option v-for="e in entrepots" :key="e.id" :value="e.id">
          {{ e.nom }}
        </option>
      </select>

      <!-- Filtre type de zone -->
      <select v-model="type" class="form-input w-full sm:w-44">
        <option value="">Tous les types</option>
        <option value="RECEPTION">Réception</option>
        <option value="STOCKAGE">Stockage</option>
        <option value="EXPEDITION">Expédition</option>
      </select>

      <!-- Filtre statut -->
      <select v-model="statut" class="form-input w-full sm:w-44">
        <option value="">Tous les statuts</option>
        <option value="actif">Actif</option>
        <option value="inactif">Inactif</option>
      </select>

    </div>

    <!-- Bouton réinitialiser — visible uniquement si un filtre est actif -->
    <div v-if="filtreActif" class="flex justify-end">
      <button
        @click="emit('reset')"
        class="text-xs text-gray-400 hover:text-gray-600 underline"
      >
        Réinitialiser les filtres
      </button>
    </div>

  </div>
</template>
