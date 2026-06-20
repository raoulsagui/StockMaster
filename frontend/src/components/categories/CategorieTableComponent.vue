<script setup>
// ============================================================
// COMPOSANT : Tableau des catégories + pagination
//
// Responsabilité : affichage uniquement.
// Reçoit la liste paginée, émet les actions vers la view.
// ============================================================

defineProps({
  categories:   { type: Array,   required: true },
  isLoading:    { type: Boolean, default: false },
  pageCourante: { type: Number,  required: true },
  totalPages:   { type: Number,  required: true },
  parPage:      { type: Number,  required: true },
  totalFiltres: { type: Number,  required: true },
  peutModifier: { type: Boolean, default: false },
})

const emit = defineEmits([
  'modifier',
  'supprimer',
  'page-precedente',
  'page-suivante',
  'changer-page',
])
</script>

<template>
  <div class="card p-0 overflow-hidden">

    <!-- Loader -->
    <div v-if="isLoading" class="p-12 text-center text-gray-400">
      <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-blue-500" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
      </svg>
      Chargement…
    </div>

    <!-- Tableau -->
    <div v-else class="overflow-x-auto">
      <table class="w-full">
        <thead class="bg-gray-50 border-b border-gray-100">
          <tr>
            <th class="table-header">Nom</th>
            <th class="table-header hidden md:table-cell">Description</th>
            <th class="table-header text-right">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">
          <tr v-if="categories.length === 0">
            <td colspan="3" class="px-6 py-12 text-center text-gray-400 text-sm">
              Aucune catégorie ne correspond aux critères.
            </td>
          </tr>
          <tr v-for="c in categories" :key="c.id" class="hover:bg-gray-50 transition-colors">
            <td class="table-cell font-medium text-gray-900">{{ c.nom }}</td>
            <td class="table-cell text-gray-500 hidden md:table-cell">{{ c.description || '—' }}</td>
            <td class="table-cell">
              <div class="flex items-center justify-end gap-1">
                <!-- Modifier -->
                <button
                  v-if="peutModifier"
                  @click="emit('modifier', c)"
                  class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                  title="Modifier"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                  </svg>
                </button>
                <!-- Supprimer -->
                <button
                  v-if="peutModifier"
                  @click="emit('supprimer', c)"
                  class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                  title="Supprimer"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                  </svg>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div
      v-if="!isLoading && totalPages > 1"
      class="flex flex-col sm:flex-row items-center justify-between gap-3 px-4 py-3 border-t border-gray-100"
    >
      <p class="text-sm text-gray-500">
        {{ (pageCourante - 1) * parPage + 1 }}–{{ Math.min(pageCourante * parPage, totalFiltres) }}
        sur {{ totalFiltres }} catégories
      </p>
      <div class="flex items-center gap-1">
        <button @click="emit('page-precedente')" :disabled="pageCourante === 1"
          class="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
          </svg>
        </button>
        <button v-for="p in totalPages" :key="p" @click="emit('changer-page', p)"
          :class="['w-8 h-8 rounded-lg text-sm font-medium transition-colors',
            p === pageCourante ? 'bg-blue-600 text-white' : 'text-gray-600 hover:bg-gray-100']">
          {{ p }}
        </button>
        <button @click="emit('page-suivante')" :disabled="pageCourante === totalPages"
          class="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
          </svg>
        </button>
      </div>
    </div>

  </div>
</template>
