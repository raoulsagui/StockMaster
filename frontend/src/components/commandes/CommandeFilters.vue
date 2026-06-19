<script setup>
/**
 * Filtres de la liste des commandes.
 * v-model sur recherche, statut et fournisseur.
 */
defineProps({
  recherche:   { type: String, default: '' },
  statut:      { type: String, default: '' },
})
defineEmits(['update:recherche', 'update:statut'])
</script>

<template>
  <div class="card p-4">
    <div class="flex flex-col sm:flex-row gap-3">

      <!-- Recherche textuelle -->
      <div class="flex-1 relative">
        <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
          fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0"/>
        </svg>
        <input
          type="text"
          :value="recherche"
          @input="$emit('update:recherche', $event.target.value)"
          placeholder="Référence, fournisseur, entrepôt…"
          class="form-input pl-9 w-full"
        />
      </div>

      <!-- Filtre statut -->
      <select
        :value="statut"
        @change="$emit('update:statut', $event.target.value)"
        class="form-input w-full sm:w-48"
      >
        <option value="">Tous les statuts</option>
        <option value="BROUILLON">Brouillon</option>
        <option value="VALIDEE">Validée</option>
        <option value="LIVREE">Livrée</option>
        <option value="ANNULEE">Annulée</option>
      </select>

    </div>
  </div>
</template>
