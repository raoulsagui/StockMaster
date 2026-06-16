<script setup>
// ============================================================
// COMPOSANT : Barre de recherche des catégories
//
// Responsabilité unique : afficher le champ de recherche et
// émettre les changements vers la vue parente.
//
// Props : valeur actuelle de la recherche
// Emits : mise à jour de la recherche (pattern v-model)
// ============================================================

defineProps({
  recherche: { type: String, default: '' },
})

const emit = defineEmits(['update:recherche'])
</script>

<template>
  <div class="card p-3 md:p-4">
    <div class="relative">
      <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
        fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0"/>
      </svg>
      <input
        :value="recherche"
        @input="emit('update:recherche', $event.target.value)"
        type="text"
        placeholder="Rechercher une catégorie…"
        class="form-input pl-9"
      />
    </div>
  </div>
</template>
