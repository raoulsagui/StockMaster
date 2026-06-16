<script setup>
// ============================================================
// COMPOSANT : Modal CRUD catégorie (création + modification)
//
// mode = 'creer'    → formulaire vide
// mode = 'modifier' → pré-rempli avec initialData
// ============================================================

import { ref, watch } from 'vue'

const props = defineProps({
  visible:     { type: Boolean, required: true },
  mode:        { type: String,  default: 'creer' },
  initialData: { type: Object,  default: null },
  isLoading:   { type: Boolean, default: false },
  erreurApi:   { type: String,  default: '' },
})

const emit = defineEmits(['fermer', 'soumettre'])

const form    = ref({ nom: '', description: '' })
const erreurs = ref({})

// Réinitialise ou pré-remplit à chaque ouverture
watch(() => props.visible, (val) => {
  if (!val) return
  erreurs.value = {}
  form.value = props.mode === 'modifier' && props.initialData
    ? { nom: props.initialData.nom, description: props.initialData.description ?? '' }
    : { nom: '', description: '' }
})

const valider = () => {
  const e = {}
  if (!form.value.nom.trim()) e.nom = 'Le nom est obligatoire.'
  erreurs.value = e
  return Object.keys(e).length === 0
}

const soumettre = () => {
  if (!valider()) return
  emit('soumettre', { nom: form.value.nom, description: form.value.description })
}
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition duration-200" enter-from-class="opacity-0" enter-to-class="opacity-100"
      leave-active-class="transition duration-150" leave-from-class="opacity-100" leave-to-class="opacity-0"
    >
      <div v-if="visible" class="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
        <div class="absolute inset-0 bg-black/50" @click="emit('fermer')" />
        <div class="relative bg-white w-full sm:max-w-md sm:rounded-2xl rounded-t-2xl shadow-2xl max-h-[90vh] overflow-y-auto">

          <!-- Header -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 sticky top-0 bg-white rounded-t-2xl">
            <h2 class="text-lg font-semibold text-gray-900">
              {{ mode === 'creer' ? 'Nouvelle catégorie' : 'Modifier la catégorie' }}
            </h2>
            <button @click="emit('fermer')" class="p-2 rounded-lg text-gray-400 hover:bg-gray-100 transition-colors">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <!-- Formulaire -->
          <form @submit.prevent="soumettre" class="px-6 py-5 space-y-4">
            <div v-if="erreurApi" class="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg p-3">
              {{ erreurApi }}
            </div>
            <div>
              <label class="form-label">Nom <span class="text-red-500">*</span></label>
              <input v-model="form.nom" type="text" placeholder="Ex : Informatique"
                :class="['form-input', erreurs.nom ? 'border-red-400' : '']"/>
              <p v-if="erreurs.nom" class="form-error">{{ erreurs.nom }}</p>
            </div>
            <div>
              <label class="form-label">Description</label>
              <textarea v-model="form.description" rows="3" placeholder="Description optionnelle…"
                class="form-input resize-none"/>
            </div>
            <div class="flex gap-3 pt-2">
              <button type="button" @click="emit('fermer')" class="btn-secondary flex-1">Annuler</button>
              <button type="submit" :disabled="isLoading" class="btn-primary flex-1 justify-center">
                <svg v-if="isLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                {{ mode === 'creer' ? 'Créer' : 'Enregistrer' }}
              </button>
            </div>
          </form>

        </div>
      </div>
    </Transition>
  </Teleport>
</template>
