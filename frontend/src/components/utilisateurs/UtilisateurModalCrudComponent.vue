<script setup>
// ============================================================
// COMPOSANT : Modal CRUD utilisateur (création + modification)
//
// Un seul composant gère les deux modes via la prop "mode".
// mode = 'creer'    → formulaire vide, titre "Nouvel utilisateur"
// mode = 'modifier' → formulaire pré-rempli, titre "Modifier"
//
// Props   : mode, visible, initialData (pour modification)
// Emits   : fermer, soumettre(payload)
// ============================================================

import { ref, watch } from 'vue'

const props = defineProps({
  visible:     { type: Boolean, required: true },
  mode:        { type: String,  default: 'creer' }, // 'creer' | 'modifier'
  initialData: { type: Object,  default: null },
  isLoading:   { type: Boolean, default: false },
  erreurApi:   { type: String,  default: '' },
})

const emit = defineEmits(['fermer', 'soumettre'])

// Formulaire local — réinitialisé à chaque ouverture
const form = ref({ prenom: '', nom: '', email: '', role: '' })
const erreurs = ref({})

// Quand la modal s'ouvre, on pré-remplit si mode modification
watch(() => props.visible, (val) => {
  if (!val) return
  erreurs.value = {}
  form.value = props.mode === 'modifier' && props.initialData
    ? { ...props.initialData }
    : { prenom: '', nom: '', email: '', role: '' }
})

// Validation
const valider = () => {
  const e = {}
  if (!form.value.prenom.trim()) e.prenom = 'Le prénom est obligatoire.'
  if (!form.value.nom.trim())    e.nom    = 'Le nom est obligatoire.'
  const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!form.value.email.trim())              e.email = "L'email est obligatoire."
  else if (!emailRe.test(form.value.email))  e.email = "L'email n'est pas valide."
  if (!form.value.role) e.role = 'Veuillez sélectionner un rôle.'
  erreurs.value = e
  return Object.keys(e).length === 0
}

const soumettre = () => {
  if (!valider()) return
  emit('soumettre', { ...form.value })
}
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="visible"
        class="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4"
      >
        <!-- Overlay -->
        <div class="absolute inset-0 bg-black/50" @click="emit('fermer')" />

        <!-- Panneau -->
        <div class="relative bg-white w-full sm:max-w-lg sm:rounded-2xl rounded-t-2xl shadow-2xl max-h-[90vh] overflow-y-auto">

          <!-- Header -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 sticky top-0 bg-white rounded-t-2xl">
            <h2 class="text-lg font-semibold text-gray-900">
              {{ mode === 'creer' ? 'Nouvel utilisateur' : "Modifier l'utilisateur" }}
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

            <!-- Prénom + Nom -->
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="form-label">Prénom <span class="text-red-500">*</span></label>
                <input
                  v-model="form.prenom" type="text" placeholder="Alice"
                  :class="['form-input', erreurs.prenom ? 'border-red-400' : '']"
                />
                <p v-if="erreurs.prenom" class="form-error">{{ erreurs.prenom }}</p>
              </div>
              <div>
                <label class="form-label">Nom <span class="text-red-500">*</span></label>
                <input
                  v-model="form.nom" type="text" placeholder="Martin"
                  :class="['form-input', erreurs.nom ? 'border-red-400' : '']"
                />
                <p v-if="erreurs.nom" class="form-error">{{ erreurs.nom }}</p>
              </div>
            </div>

            <!-- Email -->
            <div>
              <label class="form-label">Email <span class="text-red-500">*</span></label>
              <input
                v-model="form.email" type="email" placeholder="alice@example.fr"
                :class="['form-input', erreurs.email ? 'border-red-400' : '']"
              />
              <p v-if="erreurs.email" class="form-error">{{ erreurs.email }}</p>
            </div>

            <!-- Rôle -->
            <div>
              <label class="form-label">Rôle <span class="text-red-500">*</span></label>
              <select
                v-model="form.role"
                :class="['form-input', erreurs.role ? 'border-red-400' : '']"
              >
                <option value="" disabled>Sélectionner…</option>
                <option value="ADMIN">Administrateur</option>
                <option value="GESTIONNAIRE">Gestionnaire d'entrepôt</option>
                <option value="MAGASINIER">Magasinier</option>
                <option value="AUDITEUR">Auditeur</option>
              </select>
              <p v-if="erreurs.role" class="form-error">{{ erreurs.role }}</p>
            </div>

            <!-- Boutons -->
            <div class="flex gap-3 pt-2">
              <button type="button" @click="emit('fermer')" class="btn-secondary flex-1">
                Annuler
              </button>
              <button type="submit" :disabled="isLoading" class="btn-primary flex-1 justify-center">
                <svg v-if="isLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                {{ mode === 'creer' ? "Créer l'utilisateur" : 'Enregistrer' }}
              </button>
            </div>

          </form>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
