<script setup>
// Sélecteurs hiérarchiques + breadcrumb
// Émet les IDs sélectionnés vers le parent via v-model
defineProps({
  entrepots:  { type: Array, default: () => [] },
  zones:      { type: Array, default: () => [] },
  rayons:     { type: Array, default: () => [] },
  etageres:   { type: Array, default: () => [] },
  entrepotId: { type: Number, default: null },
  zoneId:     { type: Number, default: null },
  rayonId:    { type: Number, default: null },
  etagereId:  { type: Number, default: null },
  breadcrumb: { type: Array, default: () => [] },
  canEdit:    { type: Boolean, default: false },
})

const emit = defineEmits([
  'update:entrepotId',
  'update:zoneId',
  'update:rayonId',
  'update:etagereId',
  'creer-rayon',
  'creer-etagere',
])
</script>

<template>
  <div class="card p-4">
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">

      <!-- Entrepôt -->
      <div>
        <label class="form-label text-xs">Entrepôt</label>
        <select :value="entrepotId" @change="emit('update:entrepotId', $event.target.value ? Number($event.target.value) : null)"
          class="form-input py-2 text-sm">
          <option :value="null">Choisir un entrepôt…</option>
          <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
        </select>
      </div>

      <!-- Zone -->
      <div>
        <label class="form-label text-xs">Zone</label>
        <select :value="zoneId" @change="emit('update:zoneId', $event.target.value ? Number($event.target.value) : null)"
          :disabled="!entrepotId" class="form-input py-2 text-sm disabled:opacity-50">
          <option :value="null">Choisir une zone…</option>
          <option v-for="z in zones" :key="z.id" :value="z.id">{{ z.nom }}</option>
        </select>
      </div>

      <!-- Rayon -->
      <div>
        <label class="form-label text-xs flex items-center justify-between">
          Rayon
          <button v-if="canEdit && zoneId" @click="emit('creer-rayon')"
            class="text-blue-600 hover:text-blue-800 text-xs font-medium">+ Nouveau</button>
        </label>
        <select :value="rayonId" @change="emit('update:rayonId', $event.target.value ? Number($event.target.value) : null)"
          :disabled="!zoneId" class="form-input py-2 text-sm disabled:opacity-50">
          <option :value="null">Choisir un rayon…</option>
          <option v-for="r in rayons" :key="r.id" :value="r.id">
            RAYON-{{ r.code }}<template v-if="r.libelle"> — {{ r.libelle }}</template>
          </option>
        </select>
      </div>

      <!-- Étagère -->
      <div>
        <label class="form-label text-xs flex items-center justify-between">
          Étagère
          <button v-if="canEdit && rayonId" @click="emit('creer-etagere')"
            class="text-blue-600 hover:text-blue-800 text-xs font-medium">+ Nouvelle</button>
        </label>
        <select :value="etagereId" @change="emit('update:etagereId', $event.target.value ? Number($event.target.value) : null)"
          :disabled="!rayonId" class="form-input py-2 text-sm disabled:opacity-50">
          <option :value="null">Choisir une étagère…</option>
          <option v-for="e in etageres" :key="e.id" :value="e.id">
            ETAGERE-{{ e.code }}<template v-if="e.libelle"> — {{ e.libelle }}</template>
          </option>
        </select>
      </div>

    </div>

    <!-- Breadcrumb -->
    <div v-if="breadcrumb.length > 0" class="mt-3 flex items-center gap-1.5 flex-wrap text-xs text-gray-400">
      <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
      </svg>
      <template v-for="(part, i) in breadcrumb" :key="i">
        <span class="text-gray-600 font-medium">{{ part }}</span>
        <span v-if="i < breadcrumb.length - 1" class="text-gray-300">/</span>
      </template>
    </div>
  </div>
</template>
