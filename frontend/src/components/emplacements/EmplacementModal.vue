<script setup>
// Modal unique pour créer/modifier Rayon, Étagère ou Emplacement
import { ref, watch } from 'vue'

const props = defineProps({
  visible: { type: Boolean, required: true },
  mode:    { type: String, default: 'creer' }, // 'creer' | 'modifier'
  type:    { type: String, default: null },     // 'rayon' | 'etagere' | 'emplacement'
  item:    { type: Object, default: null },
  loading: { type: Boolean, default: false },
  erreur:  { type: String, default: '' },
})

const emit = defineEmits(['fermer', 'soumettre'])

const form = ref({})

watch(() => [props.visible, props.item], () => {
  if (!props.visible) return
  if (props.mode === 'modifier' && props.item) {
    form.value = { ...props.item }
  } else {
    form.value = props.type === 'emplacement'
      ? { code: '', type: 'PALETTE', capaciteMax: null, description: '' }
      : { code: '', libelle: '', ...(props.type === 'etagere' ? { niveaux: null } : {}) }
  }
}, { immediate: true })

const titreModal = () => {
  const action = props.mode === 'creer' ? 'Nouveau' : 'Modifier'
  const label  = props.type === 'rayon' ? 'rayon' : props.type === 'etagere' ? 'étagère' : 'emplacement'
  return `${action} ${label}`
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible"
        class="fixed inset-0 z-50 flex items-center justify-center p-4"
        @click.self="emit('fermer')">
        <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="emit('fermer')"></div>

        <div class="relative bg-white rounded-2xl shadow-2xl w-full max-w-md">

          <!-- En-tête -->
          <div class="flex items-center justify-between p-5 border-b border-gray-100">
            <h2 class="text-base font-semibold text-gray-900">{{ titreModal() }}</h2>
            <button @click="emit('fermer')" class="p-2 text-gray-400 hover:text-gray-600 rounded-lg">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <!-- Formulaire -->
          <form @submit.prevent="emit('soumettre', form)" class="p-5 space-y-4">
            <div v-if="erreur" class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg p-3">
              {{ erreur }}
            </div>

            <!-- Rayon / Étagère : code + libellé -->
            <template v-if="type !== 'emplacement'">
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="form-label">Code <span class="text-red-500">*</span></label>
                  <input v-model="form.code" type="text" placeholder="Ex: 01, A, B2"
                    class="form-input uppercase" required />
                  <p class="text-xs text-gray-400 mt-1">Majuscules, chiffres, tirets</p>
                </div>
                <div>
                  <label class="form-label">Libellé</label>
                  <input v-model="form.libelle" type="text" placeholder="Description optionnelle" class="form-input"/>
                </div>
              </div>
              <div v-if="type === 'etagere'">
                <label class="form-label">Nombre de niveaux</label>
                <input v-model.number="form.niveaux" type="number" min="1" max="50" class="form-input" placeholder="Ex : 5"/>
              </div>
            </template>

            <!-- Emplacement : code + type + capacité + description -->
            <template v-else>
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="form-label">Code <span class="text-red-500">*</span></label>
                  <input v-model="form.code" type="text" placeholder="Ex: 12, A3" class="form-input uppercase" required/>
                </div>
                <div>
                  <label class="form-label">Type <span class="text-red-500">*</span></label>
                  <select v-model="form.type" class="form-input" required>
                    <option value="PALETTE">🔲 Palette</option>
                    <option value="BAC">📦 Bac</option>
                    <option value="ETAGERE_OUVERTE">🗄️ Étagère ouverte</option>
                    <option value="ARMOIRE">🚪 Armoire</option>
                    <option value="SOL">⬛ Sol</option>
                  </select>
                </div>
              </div>
              <div>
                <label class="form-label">Capacité maximale (unités)</label>
                <input v-model.number="form.capaciteMax" type="number" min="1"
                  placeholder="Laisser vide = illimitée" class="form-input"/>
              </div>
              <div>
                <label class="form-label">Description / contrainte</label>
                <input v-model="form.description" type="text"
                  placeholder="Ex : Fragile uniquement, Froid -18°C" class="form-input"/>
              </div>
            </template>

            <div class="flex items-center justify-end gap-3 pt-2 border-t border-gray-100">
              <button type="button" @click="emit('fermer')" class="btn-secondary">Annuler</button>
              <button type="submit" :disabled="loading" class="btn-primary">
                <svg v-if="loading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
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

<style scoped>
.modal-enter-active, .modal-leave-active { transition: opacity 0.2s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
</style>
