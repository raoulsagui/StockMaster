<script setup>
// ============================================================
// COMPOSANT : Bottom sheet filtres produits (mobile)
// ============================================================

defineProps({
  visible:         { type: Boolean, required: true },
  filtreCategorie: { type: [String, Number], default: '' },
  filtreStatut:    { type: String, default: '' },
  categories:      { type: Array,  default: () => [] },
})

const emit = defineEmits([
  'fermer',
  'update:filtreCategorie',
  'update:filtreStatut',
  'reinitialiser',
])

const statuts = [['', 'Tous'], ['actif', 'Actif'], ['inactif', 'Inactif']]
</script>

<template>
  <Teleport to="body">
    <Transition enter-active-class="transition ease-out duration-200" enter-from-class="opacity-0" enter-to-class="opacity-100"
      leave-active-class="transition ease-in duration-150" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="visible" class="fixed inset-0 z-50 bg-black/40 md:hidden" @click="emit('fermer')" />
    </Transition>
    <Transition enter-active-class="transition ease-out duration-300" enter-from-class="translate-y-full" enter-to-class="translate-y-0"
      leave-active-class="transition ease-in duration-200" leave-from-class="translate-y-0" leave-to-class="translate-y-full">
      <div v-if="visible" class="fixed bottom-0 inset-x-0 z-50 bg-white rounded-t-2xl shadow-2xl md:hidden">
        <div class="flex justify-center pt-3 pb-1"><div class="w-10 h-1 bg-gray-200 rounded-full" /></div>
        <div class="flex items-center justify-between px-5 py-3 border-b border-gray-100">
          <p class="text-sm font-semibold text-gray-800">Filtres</p>
          <button @click="emit('fermer')" class="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>
        <div class="px-5 py-4 space-y-4">
          <!-- Catégories -->
          <div>
            <label class="form-label">Catégorie</label>
            <div class="grid grid-cols-2 gap-2 mt-1">
              <button @click="emit('update:filtreCategorie', '')"
                :class="['px-3 py-2 rounded-lg text-sm font-medium border transition-colors',
                  filtreCategorie === '' ? 'bg-blue-600 text-white border-blue-600' : 'text-gray-600 border-gray-200']">
                Toutes
              </button>
              <button v-for="c in categories" :key="c.id"
                @click="emit('update:filtreCategorie', c.id)"
                :class="['px-3 py-2 rounded-lg text-sm font-medium border transition-colors',
                  String(filtreCategorie) === String(c.id) ? 'bg-blue-600 text-white border-blue-600' : 'text-gray-600 border-gray-200']">
                {{ c.nom }}
              </button>
            </div>
          </div>
          <!-- Statut -->
          <div>
            <label class="form-label">Statut</label>
            <div class="grid grid-cols-3 gap-2 mt-1">
              <button v-for="[val, label] in statuts" :key="val"
                @click="emit('update:filtreStatut', val)"
                :class="['px-3 py-2 rounded-lg text-sm font-medium border transition-colors',
                  filtreStatut === val ? 'bg-blue-600 text-white border-blue-600' : 'text-gray-600 border-gray-200']">
                {{ label }}
              </button>
            </div>
          </div>
          <button @click="emit('reinitialiser')"
            class="w-full py-2 text-sm text-red-500 hover:bg-red-50 rounded-lg transition-colors">
            Réinitialiser les filtres
          </button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
