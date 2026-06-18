<script setup>
const recherche = defineModel('recherche', { type: String, default: '' })
const statut    = defineModel('statut',    { type: String, default: '' })
</script>

<template>
  <div class="card p-4">
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
          placeholder="Rechercher par nom, email ou contact…"
          class="form-input pl-9"
        />
      </div>

      <!-- Filtre statut -->
      <select v-model="statut" class="form-input w-full sm:w-44">
        <option value="">Tous les statuts</option>
        <option value="actif">Actif</option>
        <option value="inactif">Inactif</option>
      </select>

    </div>
  </div>
</template>
