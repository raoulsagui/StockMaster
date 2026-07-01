<script setup>
defineProps({
  rayons:    { type: Array, default: () => [] },
  rayonId:   { type: Number, default: null },
  canEdit:   { type: Boolean, default: false },
  canToggle: { type: Boolean, default: false },
})
const emit = defineEmits(['select', 'modifier', 'toggle'])
</script>

<template>
  <div class="card p-0 overflow-hidden">
    <div class="px-4 py-3 border-b border-gray-100 bg-gray-50 flex items-center justify-between">
      <h3 class="text-sm font-semibold text-gray-700">
        Rayons <span class="text-gray-400 font-normal ml-1">({{ rayons.length }})</span>
      </h3>
    </div>

    <div v-if="rayons.length === 0" class="p-6 text-center text-gray-400 text-sm">
      Aucun rayon dans cette zone.
    </div>

    <div v-else class="divide-y divide-gray-50">
      <div v-for="r in rayons" :key="r.id"
        :class="['flex items-center gap-3 px-4 py-3 cursor-pointer hover:bg-gray-50 transition-colors',
          rayonId === r.id ? 'bg-blue-50 border-l-2 border-blue-500' : '']"
        @click="emit('select', r.id)">

        <div :class="['w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold flex-shrink-0',
          r.actif ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-400']">
          {{ r.code }}
        </div>

        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-gray-800">RAYON-{{ r.code }}</p>
          <p v-if="r.libelle" class="text-xs text-gray-400 truncate">{{ r.libelle }}</p>
          <p class="text-xs text-gray-400">{{ r.nombreEtageres }} étagère{{ r.nombreEtageres > 1 ? 's' : '' }}</p>
        </div>

        <div class="flex items-center gap-1.5 flex-shrink-0">
          <span :class="['text-xs px-1.5 py-0.5 rounded', r.actif ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500']">
            {{ r.actif ? 'Actif' : 'Inactif' }}
          </span>
          <button v-if="canEdit" @click.stop="emit('modifier', r)"
            class="p-1 text-gray-300 hover:text-blue-600 rounded transition-colors" title="Modifier">
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
            </svg>
          </button>
          <button v-if="canToggle" @click.stop="emit('toggle', r)"
            class="p-1 text-gray-300 hover:text-orange-600 rounded transition-colors" title="Toggle statut">
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
