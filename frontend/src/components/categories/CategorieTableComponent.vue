<script setup>
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
  'modifier', 'supprimer',
  'page-precedente', 'page-suivante', 'changer-page',
])

// Génère une couleur pastel déterministe à partir du nom
const palettes = [
  { bg: 'bg-blue-100',   icon: 'bg-blue-500',   text: 'text-blue-700'   },
  { bg: 'bg-emerald-100',icon: 'bg-emerald-500', text: 'text-emerald-700'},
  { bg: 'bg-violet-100', icon: 'bg-violet-500',  text: 'text-violet-700' },
  { bg: 'bg-orange-100', icon: 'bg-orange-500',  text: 'text-orange-700' },
  { bg: 'bg-pink-100',   icon: 'bg-pink-500',    text: 'text-pink-700'   },
  { bg: 'bg-cyan-100',   icon: 'bg-cyan-500',    text: 'text-cyan-700'   },
  { bg: 'bg-amber-100',  icon: 'bg-amber-500',   text: 'text-amber-700'  },
  { bg: 'bg-indigo-100', icon: 'bg-indigo-500',  text: 'text-indigo-700' },
]

function getPalette(nom) {
  let hash = 0
  for (let i = 0; i < nom.length; i++) hash = nom.charCodeAt(i) + ((hash << 5) - hash)
  return palettes[Math.abs(hash) % palettes.length]
}

function getInitiales(nom) {
  return nom.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2)
}
</script>

<template>
  <div>
    <!-- Loader -->
    <div v-if="isLoading" class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
      <div v-for="i in 8" :key="i" class="h-36 rounded-2xl animate-pulse bg-gray-200/60"/>
    </div>

    <!-- Vide -->
    <div v-else-if="categories.length === 0"
      class="card py-16 text-center text-gray-300">
      <svg class="w-10 h-10 mx-auto mb-3 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
          d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/>
      </svg>
      <p class="text-sm">Aucune catégorie trouvée.</p>
    </div>

    <!-- Grille de cards -->
    <div v-else class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
      <div
        v-for="c in categories" :key="c.id"
        class="card p-0 overflow-hidden group hover:shadow-md hover:-translate-y-0.5 transition-all duration-200"
      >
        <!-- Zone colorée haute -->
        <div :class="['px-5 pt-5 pb-4 flex items-center gap-3', getPalette(c.nom).bg]">
          <div :class="['w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0 text-sm font-bold text-white shadow-sm', getPalette(c.nom).icon]">
            {{ getInitiales(c.nom) }}
          </div>
          <h3 :class="['font-semibold text-sm leading-snug flex-1 min-w-0 truncate', getPalette(c.nom).text]">
            {{ c.nom }}
          </h3>
        </div>

        <!-- Corps -->
        <div class="px-5 py-3 flex-1">
          <p class="text-xs text-gray-400 leading-relaxed line-clamp-2 min-h-[2.5rem]">
            {{ c.description || 'Aucune description.' }}
          </p>
        </div>

        <!-- Actions -->
        <div v-if="peutModifier" class="px-4 py-3 border-t border-gray-50 flex items-center justify-end gap-1">
          <button @click="emit('modifier', c)"
            class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
            title="Modifier">
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
            </svg>
            Modifier
          </button>
          <button @click="emit('supprimer', c)"
            class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
            title="Supprimer">
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
            </svg>
            Supprimer
          </button>
        </div>
        <!-- Auditeur : pas d'actions, juste un espaceur -->
        <div v-else class="pb-4"/>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="!isLoading && totalPages > 1"
      class="flex items-center justify-between mt-4 px-1">
      <p class="text-sm text-gray-400">
        {{ (pageCourante - 1) * parPage + 1 }}–{{ Math.min(pageCourante * parPage, totalFiltres) }}
        <span class="text-gray-300">/ {{ totalFiltres }}</span>
      </p>
      <div class="flex items-center gap-1">
        <button @click="emit('page-precedente')" :disabled="pageCourante === 1"
          class="p-1.5 rounded-lg text-gray-400 hover:bg-white hover:shadow-sm disabled:opacity-30 disabled:cursor-not-allowed transition-all">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
          </svg>
        </button>
        <button v-for="p in totalPages" :key="p" @click="emit('changer-page', p)"
          :class="['w-8 h-8 rounded-lg text-sm font-medium transition-all',
            p === pageCourante ? 'bg-white shadow-sm text-gray-900 font-semibold' : 'text-gray-400 hover:bg-white hover:shadow-sm']">
          {{ p }}
        </button>
        <button @click="emit('page-suivante')" :disabled="pageCourante === totalPages"
          class="p-1.5 rounded-lg text-gray-400 hover:bg-white hover:shadow-sm disabled:opacity-30 disabled:cursor-not-allowed transition-all">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>
