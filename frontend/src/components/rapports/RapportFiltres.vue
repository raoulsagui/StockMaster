<script setup>
defineProps({
  onglet:          { type: String, required: true },
  inventaires:     { type: Array, default: () => [] },
  inventaireSelId: { type: [Number, null], default: null },
  filtres:         { type: Object, required: true },
  isLoading:       { type: Boolean, default: false },
})
const emit = defineEmits([
  'update:inventaireSelId',
  'update:filtres',
  'generer',
])
</script>

<template>
  <div class="card p-5 space-y-4">
    <h2 class="text-sm font-semibold text-gray-700">Paramètres du rapport</h2>

    <!-- INVENTAIRE -->
    <div v-if="onglet === 'inventaire'">
      <label class="form-label">Sélectionner un inventaire <span class="text-red-500">*</span></label>
      <select
        :value="inventaireSelId"
        @change="emit('update:inventaireSelId', $event.target.value ? Number($event.target.value) : null)"
        class="form-input max-w-md">
        <option :value="null" disabled>Choisir un inventaire…</option>
        <option v-for="inv in inventaires" :key="inv.reference" :value="inv.id">
          {{ inv.reference }} — {{ inv.entrepotNom }} ({{ inv.statut }})
        </option>
      </select>
      <p v-if="inventaires.length === 0" class="text-xs text-orange-500 mt-1">Aucun inventaire disponible.</p>
    </div>

    <!-- MOUVEMENTS & FOURNISSEURS -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div>
        <label class="form-label">Date début <span class="text-red-500">*</span></label>
        <input
          :value="filtres.dateDebut"
          @input="emit('update:filtres', { ...filtres, dateDebut: $event.target.value })"
          type="date" class="form-input" />
      </div>
      <div>
        <label class="form-label">Date fin <span class="text-red-500">*</span></label>
        <input
          :value="filtres.dateFin"
          @input="emit('update:filtres', { ...filtres, dateFin: $event.target.value })"
          type="date" class="form-input" />
      </div>
      <div v-if="onglet === 'mouvements'">
        <label class="form-label">Type de mouvement</label>
        <select
          :value="filtres.type"
          @change="emit('update:filtres', { ...filtres, type: $event.target.value || null })"
          class="form-input">
          <option :value="null">Tous les types</option>
          <option value="ENTREE">Entrée</option>
          <option value="SORTIE">Sortie</option>
          <option value="TRANSFERT">Transfert</option>
          <option value="AJUSTEMENT">Ajustement</option>
        </select>
      </div>
    </div>

    <button @click="emit('generer')" :disabled="isLoading" class="btn-primary">
      <svg v-if="isLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
      </svg>
      <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
      </svg>
      {{ isLoading ? 'Génération…' : 'Générer le rapport' }}
    </button>
  </div>
</template>
