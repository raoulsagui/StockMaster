<script setup>
// ============================================================
// COMPOSANT : Barre de filtres des utilisateurs
//
// Responsabilité unique : afficher les filtres et émettre
// les changements vers la vue parente.
//
// Props  : valeurs actuelles des filtres
// Emits  : mise à jour de chaque filtre (pattern v-model multiple)
// ============================================================

defineProps({
  recherche:    { type: String,  default: '' },
  filtreRole:   { type: String,  default: '' },
  filtreStatut: { type: String,  default: '' },
})

// On émet une mise à jour pour chaque filtre séparément
const emit = defineEmits([
  'update:recherche',
  'update:filtreRole',
  'update:filtreStatut',
  'ouvrir-filtres-mobile', // demande au parent d'ouvrir le bottom sheet
])
</script>

<template>
  <div class="card p-3 md:p-4">

    <!-- Mobile : recherche + bouton filtres -->
    <div class="flex gap-3 md:hidden">
      <div class="relative flex-1">
        <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
          fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0"/>
        </svg>
        <input
          :value="recherche"
          @input="emit('update:recherche', $event.target.value)"
          type="text"
          placeholder="Rechercher…"
          class="form-input pl-9"
        />
      </div>
      <!-- Bouton filtres avec badge si filtres actifs -->
      <button
        @click="emit('ouvrir-filtres-mobile')"
        :class="['relative p-2.5 rounded-lg border transition-colors flex-shrink-0',
          (filtreRole || filtreStatut)
            ? 'border-blue-500 bg-blue-50 text-blue-600'
            : 'border-gray-200 text-gray-500 hover:bg-gray-50']"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2a1 1 0 01-.293.707L13 13.414V19a1 1 0 01-.553.894l-4 2A1 1 0 017 21v-7.586L3.293 6.707A1 1 0 013 6V4z"/>
        </svg>
        <span
          v-if="filtreRole || filtreStatut"
          class="absolute -top-1 -right-1 w-4 h-4 bg-blue-600 text-white text-xs rounded-full flex items-center justify-center"
        >
          {{ (filtreRole ? 1 : 0) + (filtreStatut ? 1 : 0) }}
        </span>
      </button>
    </div>

    <!-- Desktop : tous les filtres en ligne -->
    <div class="hidden md:flex gap-3">
      <div class="relative flex-1">
        <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
          fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0"/>
        </svg>
        <input
          :value="recherche"
          @input="emit('update:recherche', $event.target.value)"
          type="text"
          placeholder="Rechercher par nom, prénom ou email…"
          class="form-input pl-9"
        />
      </div>
      <select
        :value="filtreRole"
        @change="emit('update:filtreRole', $event.target.value)"
        class="form-input w-44"
      >
        <option value="">Tous les rôles</option>
        <option value="ADMIN">Administrateur</option>
        <option value="GESTIONNAIRE">Gestionnaire</option>
        <option value="MAGASINIER">Magasinier</option>
        <option value="AUDITEUR">Auditeur</option>
      </select>
      <select
        :value="filtreStatut"
        @change="emit('update:filtreStatut', $event.target.value)"
        class="form-input w-40"
      >
        <option value="">Tous les statuts</option>
        <option value="actif">Actif</option>
        <option value="inactif">Inactif</option>
      </select>
    </div>

  </div>
</template>
