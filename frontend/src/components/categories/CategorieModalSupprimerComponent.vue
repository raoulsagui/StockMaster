<script setup>
// ============================================================
// COMPOSANT : Modal de confirmation de suppression
//
// Affiche le nom de la catégorie à supprimer et attend
// une confirmation explicite avant d'émettre 'confirmer'.
// ============================================================

defineProps({
  visible:   { type: Boolean, required: true },
  categorie: { type: Object,  default: null },
  isLoading: { type: Boolean, default: false },
  erreurApi: { type: String,  default: '' },
})

const emit = defineEmits(['fermer', 'confirmer'])
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition duration-200" enter-from-class="opacity-0" enter-to-class="opacity-100"
      leave-active-class="transition duration-150" leave-from-class="opacity-100" leave-to-class="opacity-0"
    >
      <div v-if="visible" class="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
        <div class="absolute inset-0 bg-black/50" @click="emit('fermer')" />
        <div class="relative bg-white w-full sm:max-w-sm sm:rounded-2xl rounded-t-2xl shadow-2xl">
          <div class="p-6">
            <!-- Icône danger -->
            <div class="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
              </svg>
            </div>
            <h3 class="text-base font-semibold text-gray-900 text-center mb-1">Supprimer la catégorie</h3>
            <p class="text-sm text-gray-500 text-center mb-6">
              Voulez-vous vraiment supprimer
              <span class="font-medium text-gray-800">{{ categorie?.nom }}</span> ?
              Cette action est irréversible.
            </p>
            <div v-if="erreurApi" class="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg p-3 mb-4">
              {{ erreurApi }}
            </div>
            <div class="flex gap-3">
              <button @click="emit('fermer')" class="btn-secondary flex-1">Annuler</button>
              <button @click="emit('confirmer')" :disabled="isLoading"
                class="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-red-600 hover:bg-red-700 text-white text-sm font-medium rounded-lg transition-colors disabled:opacity-50">
                <svg v-if="isLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                Supprimer
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
