<script setup>
// ============================================================
// COMPOSANT : Filtres des produits (desktop + mobile)
// ============================================================

defineProps({
  recherche:       { type: String, default: '' },
  filtreCategorie: { type: [String, Number], default: '' },
  filtreStatut:    { type: String, default: '' },
  categories:      { type: Array,  default: () => [] },
})

const emit = defineEmits([
  'update:recherche',
  'update:filtreCategorie',
  'update:filtreStatut',
  'ouvrir-filtres-mobile',
])
</script>

<template>
  <div class="card p-3 md:p-4">

    <!-- Mobile -->
    <div class="flex gap-3 md:hidden">
      <div class="relative flex-1">
        <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
          fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0"/>
        </svg>
        <input :value="recherche" @input="emit('update:recherche', $event.target.value)"
          type="text" placeholder="Rechercher…" class="form-input pl-9"/>
      </div>
      <button @click="emit('ouvrir-filtres-mobile')"
        :class="['relative p-2.5 rounded-lg border transition-colors flex-shrink-0',
          (filtreCategorie || filtreStatut)
            ? 'border-blue-500 bg-blue-50 text-blue-600'
            : 'border-gray-200 text-gray-500 hover:bg-gray-50']">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2a1 1 0 01-.293.707L13 13.414V19a1 1 0 01-.553.894l-4 2A1 1 0 017 21v-7.586L3.293 6.707A1 1 0 013 6V4z"/>
        </svg>
        <span v-if="filtreCategorie || filtreStatut"
          class="absolute -top-1 -right-1 w-4 h-4 bg-blue-600 text-white text-xs rounded-full flex items-center justify-center">
          {{ (filtreCategorie ? 1 : 0) + (filtreStatut ? 1 : 0) }}
        </span>
      </button>
    </div>

    <!-- Desktop -->
    <div class="hidden md:flex gap-3">
      <div class="relative flex-1">
        <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
          fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0"/>
        </svg>
        <input :value="recherche" @input="emit('update:recherche', $event.target.value)"
          type="text" placeholder="Rechercher…" class="form-input pl-9"/>
      </div>
      <select :value="filtreCategorie"
        @change="emit('update:filtreCategorie', $event.target.value)"
        class="form-input w-48">
        <option value="">Toutes les catégories</option>
        <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.nom }}</option>
      </select>
      <select :value="filtreStatut"
        @change="emit('update:filtreStatut', $event.target.value)"
        class="form-input w-40">
        <option value="">Tous les statuts</option>
        <option value="actif">Actif</option>
        <option value="inactif">Inactif</option>
      </select>
    </div>

  </div>
</template>
