<script setup>
// ============================================================
// COMPOSANT : Modal de configuration des seuils stock
//
// Permet de définir le stock minimum (alerte) et maximum
// pour une combinaison produit/entrepôt.
// ============================================================

import { ref, watch } from 'vue'

const props = defineProps({
  visible:   { type: Boolean, required: true },
  stock:     { type: Object,  default: null },
  isLoading: { type: Boolean, default: false },
  erreurApi: { type: String,  default: '' },
})

const emit = defineEmits(['fermer', 'soumettre'])

const form    = ref({ stockMinimum: '', stockMaximum: '' })
const erreurs = ref({})

// Pré-remplit avec les seuils existants à l'ouverture
watch(() => props.visible, (val) => {
  if (!val || !props.stock) return
  erreurs.value = {}
  form.value = {
    stockMinimum: props.stock.stockMinimum ?? '',
    stockMaximum: props.stock.stockMaximum ?? '',
  }
})

const valider = () => {
  const e = {}
  const min = form.value.stockMinimum !== '' ? Number(form.value.stockMinimum) : null
  const max = form.value.stockMaximum !== '' ? Number(form.value.stockMaximum) : null

  if (min !== null && min < 0) e.stockMinimum = 'Le minimum ne peut pas être négatif.'
  if (max !== null && max < 0) e.stockMaximum = 'Le maximum ne peut pas être négatif.'
  if (min !== null && max !== null && min > max)
    e.stockMinimum = 'Le minimum ne peut pas être supérieur au maximum.'

  erreurs.value = e
  return Object.keys(e).length === 0
}

const soumettre = () => {
  if (!valider()) return
  emit('soumettre', {
    stockMinimum: form.value.stockMinimum !== '' ? Number(form.value.stockMinimum) : null,
    stockMaximum: form.value.stockMaximum !== '' ? Number(form.value.stockMaximum) : null,
  })
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
        <div class="relative bg-white w-full sm:max-w-md sm:rounded-2xl rounded-t-2xl shadow-2xl">

          <!-- Header -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <div>
              <h2 class="text-lg font-semibold text-gray-900">Configurer les seuils</h2>
              <p class="text-sm text-gray-500 mt-0.5" v-if="stock">
                {{ stock.produitNom }} — {{ stock.entrepotNom }}
              </p>
            </div>
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

            <!-- Info stock actuel -->
            <div v-if="stock" class="bg-blue-50 rounded-lg p-3 text-sm">
              <div class="flex justify-between text-blue-800">
                <span>Stock actuel disponible :</span>
                <span class="font-bold">{{ stock.quantiteDisponible }}</span>
              </div>
            </div>

            <!-- Stock minimum -->
            <div>
              <label class="form-label">
                Stock minimum
                <span class="text-gray-400 font-normal">(laisser vide = pas d'alerte)</span>
              </label>
              <input
                v-model="form.stockMinimum"
                type="number"
                min="0"
                step="1"
                placeholder="Ex : 10"
                :class="['form-input', erreurs.stockMinimum ? 'border-red-400' : '']"
              />
              <p class="text-xs text-gray-400 mt-1">
                Une alerte sera déclenchée quand le stock passe en dessous de ce seuil.
              </p>
              <p v-if="erreurs.stockMinimum" class="form-error">{{ erreurs.stockMinimum }}</p>
            </div>

            <!-- Stock maximum -->
            <div>
              <label class="form-label">
                Stock maximum
                <span class="text-gray-400 font-normal">(laisser vide = pas de limite)</span>
              </label>
              <input
                v-model="form.stockMaximum"
                type="number"
                min="0"
                step="1"
                placeholder="Ex : 500"
                :class="['form-input', erreurs.stockMaximum ? 'border-red-400' : '']"
              />
              <p v-if="erreurs.stockMaximum" class="form-error">{{ erreurs.stockMaximum }}</p>
            </div>

            <!-- Boutons -->
            <div class="flex gap-3 pt-2">
              <button type="button" @click="emit('fermer')" class="btn-secondary flex-1">Annuler</button>
              <button type="submit" :disabled="isLoading" class="btn-primary flex-1 justify-center">
                <svg v-if="isLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                Enregistrer
              </button>
            </div>
          </form>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
