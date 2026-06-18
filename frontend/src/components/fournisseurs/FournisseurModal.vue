<script setup>
// ============================================================
// COMPOSANT : FournisseurModal
// Modal de création / modification d'un fournisseur.
//
// Props :
//   visible      (Boolean) → affiche ou cache le modal
//   fournisseurId (Number) → si fourni, mode édition ; sinon création
//
// Événements :
//   @fermer     → le parent ferme le modal
//   @sauvegarde → création/modification réussie, le parent recharge
// ============================================================

import { ref, computed, watch } from 'vue'
import fournisseurService from '@/services/fournisseurService'

const props = defineProps({
  visible:       { type: Boolean, required: true },
  fournisseurId: { type: [Number, String], default: null },
})

const emit = defineEmits(['fermer', 'sauvegarde'])

const isEditing = computed(() => props.fournisseurId !== null)

// -------------------------------------------------------
// ÉTAT
// -------------------------------------------------------
const form = ref({
  nom:              '',
  adresse:          '',
  telephone:        '',
  email:            '',
  contactPrincipal: '',
})

const erreurs   = ref({})
const erreurApi = ref('')
const isLoading = ref(false)

// Recharge les données à chaque ouverture
watch(() => props.visible, async (val) => {
  if (!val) return
  erreurs.value   = {}
  erreurApi.value = ''

  if (isEditing.value) {
    try {
      const data = await fournisseurService.findById(props.fournisseurId)
      form.value = {
        nom:              data.nom,
        adresse:          data.adresse,
        telephone:        data.telephone,
        email:            data.email,
        contactPrincipal: data.contactPrincipal,
      }
    } catch {
      erreurApi.value = 'Impossible de charger ce fournisseur.'
    }
  } else {
    form.value = { nom: '', adresse: '', telephone: '', email: '', contactPrincipal: '' }
  }
})

// -------------------------------------------------------
// VALIDATION
// -------------------------------------------------------
const valider = () => {
  erreurs.value = {}

  if (!form.value.nom.trim())
    erreurs.value.nom = 'Le nom est obligatoire.'
  else if (form.value.nom.trim().length < 2)
    erreurs.value.nom = 'Le nom doit contenir au moins 2 caractères.'

  if (!form.value.adresse.trim())
    erreurs.value.adresse = "L'adresse est obligatoire."

  if (!form.value.telephone.trim())
    erreurs.value.telephone = 'Le téléphone est obligatoire.'

  if (!form.value.email.trim())
    erreurs.value.email = "L'email est obligatoire."
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email))
    erreurs.value.email = "L'email n'est pas valide."

  if (!form.value.contactPrincipal.trim())
    erreurs.value.contactPrincipal = 'Le contact principal est obligatoire.'

  return Object.keys(erreurs.value).length === 0
}

// -------------------------------------------------------
// SOUMISSION
// -------------------------------------------------------
const soumettre = async () => {
  if (!valider()) return
  isLoading.value = true
  erreurApi.value = ''

  try {
    const payload = {
      nom:              form.value.nom.trim(),
      adresse:          form.value.adresse.trim(),
      telephone:        form.value.telephone.trim(),
      email:            form.value.email.trim(),
      contactPrincipal: form.value.contactPrincipal.trim(),
    }

    if (isEditing.value) {
      await fournisseurService.modifier(props.fournisseurId, payload)
    } else {
      await fournisseurService.creer(payload)
    }

    emit('sauvegarde')
    emit('fermer')
  } catch (e) {
    const msg = e.response?.data
    erreurApi.value = typeof msg === 'string'
      ? msg
      : (isEditing.value ? 'Erreur lors de la modification.' : 'Erreur lors de la création.')
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div
        v-if="visible"
        class="fixed inset-0 z-50 flex items-center justify-center p-4"
      >
        <!-- Fond sombre -->
        <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="emit('fermer')"></div>

        <!-- Boîte du modal -->
        <div class="relative bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">

          <!-- En-tête -->
          <div class="flex items-center justify-between p-6 border-b border-gray-100">
            <div>
              <h2 class="text-lg font-semibold text-gray-900">
                {{ isEditing ? 'Modifier le fournisseur' : 'Nouveau fournisseur' }}
              </h2>
            </div>
            <button
              @click="emit('fermer')"
              class="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <!-- Formulaire -->
          <form @submit.prevent="soumettre" class="p-6 space-y-4">

            <!-- Erreur API -->
            <div v-if="erreurApi" class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg p-3">
              {{ erreurApi }}
            </div>

            <!-- Nom -->
            <div>
              <label class="form-label">Nom <span class="text-red-500">*</span></label>
              <input
                v-model="form.nom" type="text"
                placeholder="Ex : Fournisseur ABC"
                :class="['form-input', erreurs.nom ? 'border-red-400 focus:ring-red-400' : '']"
              />
              <p v-if="erreurs.nom" class="form-error">{{ erreurs.nom }}</p>
            </div>

            <!-- Adresse -->
            <div>
              <label class="form-label">Adresse <span class="text-red-500">*</span></label>
              <input
                v-model="form.adresse" type="text"
                placeholder="Ex : 12 Rue du Commerce, 75000 Paris"
                :class="['form-input', erreurs.adresse ? 'border-red-400 focus:ring-red-400' : '']"
              />
              <p v-if="erreurs.adresse" class="form-error">{{ erreurs.adresse }}</p>
            </div>

            <!-- Téléphone + Email sur 2 colonnes -->
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="form-label">Téléphone <span class="text-red-500">*</span></label>
                <input
                  v-model="form.telephone" type="tel"
                  placeholder="Ex : +299 01 85 65 25 15"
                  :class="['form-input', erreurs.telephone ? 'border-red-400 focus:ring-red-400' : '']"
                />
                <p v-if="erreurs.telephone" class="form-error">{{ erreurs.telephone }}</p>
              </div>
              <div>
                <label class="form-label">Email <span class="text-red-500">*</span></label>
                <input
                  v-model="form.email" type="email"
                  placeholder="Ex : contact@fournisseur.fr"
                  :class="['form-input', erreurs.email ? 'border-red-400 focus:ring-red-400' : '']"
                />
                <p v-if="erreurs.email" class="form-error">{{ erreurs.email }}</p>
              </div>
            </div>

            <!-- Contact principal -->
            <div>
              <label class="form-label">Contact principal <span class="text-red-500">*</span></label>
              <input
                v-model="form.contactPrincipal" type="text"
                placeholder="Ex : Houéfa Houéfa"
                :class="['form-input', erreurs.contactPrincipal ? 'border-red-400 focus:ring-red-400' : '']"
              />
              <p v-if="erreurs.contactPrincipal" class="form-error">{{ erreurs.contactPrincipal }}</p>
            </div>

            <!-- Boutons -->
            <div class="flex items-center justify-end gap-3 pt-4 border-t border-gray-100">
              <button type="button" @click="emit('fermer')" class="btn-secondary">Annuler</button>
              <button type="submit" :disabled="isLoading" class="btn-primary">
                <svg v-if="isLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                {{ isEditing ? 'Enregistrer' : 'Ajouter le fournisseur' }}
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
