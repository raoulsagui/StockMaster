<script setup>
// ============================================================
// COMPOSANT : Modal CRUD produit (création + modification)
//
// mode = 'creer'    → formulaire vide, titre "Nouveau produit"
// mode = 'modifier' → formulaire pré-rempli, titre "Modifier"
//
// Props   : mode, visible, initialData, categories
// Emits   : fermer, soumettre(payload), generer-reference
//
// Fonctionnalité inline : création de catégorie à la volée
// depuis le select, sans quitter le formulaire produit.
// ============================================================

import { ref, watch } from 'vue'
import categorieService from '@/services/categorieService'

const props = defineProps({
  visible:     { type: Boolean, required: true },
  mode:        { type: String,  default: 'creer' },
  initialData: { type: Object,  default: null },
  isLoading:   { type: Boolean, default: false },
  erreurApi:   { type: String,  default: '' },
  categories:  { type: Array,   default: () => [] },
})

const emit = defineEmits(['fermer', 'soumettre', 'generer-reference', 'categorie-creee'])

const formVide = () => ({
  reference: '', codeBarres: '', nom: '', description: '',
  categorieId: '', prixAchat: '', prixVente: '', poids: '', volume: '',
})

const form    = ref(formVide())
const erreurs = ref({})

// Réinitialise/pré-remplit à chaque ouverture
watch(() => props.visible, (val) => {
  if (!val) { showNouvelleCategorie.value = false; return }
  erreurs.value = {}
  if (props.mode === 'modifier' && props.initialData) {
    form.value = {
      reference:   props.initialData.reference   ?? '',
      codeBarres:  props.initialData.codeBarres  ?? '',
      nom:         props.initialData.nom         ?? '',
      description: props.initialData.description ?? '',
      categorieId: props.initialData.categorieId != null ? String(props.initialData.categorieId) : '',
      prixAchat:   props.initialData.prixAchat   ?? '',
      prixVente:   props.initialData.prixVente   ?? '',
      poids:       props.initialData.poids       ?? '',
      volume:      props.initialData.volume      ?? '',
    }
  } else {
    form.value = formVide()
  }
})

watch(() => props.initialData?.reference, (val) => {
  if (val && props.visible) form.value.reference = val
})

// -------------------------------------------------------
// CRÉATION DE CATÉGORIE À LA VOLÉE
// Permet de créer une catégorie sans quitter la modal produit.
// La nouvelle catégorie est ajoutée à la liste et sélectionnée.
// -------------------------------------------------------
const showNouvelleCategorie = ref(false)
const nomNouvelleCategorie  = ref('')
const erreurNouvelleCategorie = ref('')
const isCreatingCategorie   = ref(false)

function toggleNouvelleCategorie() {
  showNouvelleCategorie.value = !showNouvelleCategorie.value
  if (showNouvelleCategorie.value) {
    nomNouvelleCategorie.value   = ''
    erreurNouvelleCategorie.value = ''
  }
}

async function creerCategorieInline() {
  erreurNouvelleCategorie.value = ''

  if (!nomNouvelleCategorie.value.trim()) {
    erreurNouvelleCategorie.value = 'Le nom est obligatoire.'
    return
  }

  isCreatingCategorie.value = true
  try {
    // Crée la catégorie via l'API
    const created = await categorieService.creer({ nom: nomNouvelleCategorie.value.trim() })

    // Notifie la view parente pour qu'elle mette à jour sa liste de catégories
    emit('categorie-creee', created)

    // Sélectionne automatiquement la nouvelle catégorie dans le formulaire
    form.value.categorieId = String(created.id)

    // Referme le mini-formulaire
    showNouvelleCategorie.value = false
    nomNouvelleCategorie.value  = ''
  } catch (e) {
    const msg = e.response?.data
    erreurNouvelleCategorie.value = typeof msg === 'string' ? msg : 'Erreur lors de la création.'
  } finally {
    isCreatingCategorie.value = false
  }
}

// -------------------------------------------------------
// VALIDATION + SOUMISSION
// -------------------------------------------------------
const valider = () => {
  const e = {}
  if (!form.value.nom.trim()) e.nom = 'Le nom est obligatoire.'
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
        <div class="relative bg-white w-full sm:max-w-2xl sm:rounded-2xl rounded-t-2xl shadow-2xl max-h-[90vh] overflow-y-auto">

          <!-- Header -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 sticky top-0 bg-white rounded-t-2xl">
            <h2 class="text-lg font-semibold text-gray-900">
              {{ mode === 'creer' ? 'Nouveau produit' : 'Modifier le produit' }}
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

            <!-- Référence + Code-barres -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label class="form-label">Référence</label>
                <div class="flex gap-2">
                  <input
                    v-model="form.reference"
                    type="text"
                    placeholder="REF-XXXXX"
                    class="form-input flex-1"
                  />
                  <button
                    type="button"
                    @click="emit('generer-reference')"
                    class="px-3 py-2 rounded-lg border border-gray-200 text-xs text-gray-500 hover:bg-gray-50 transition-colors whitespace-nowrap"
                    title="Générer automatiquement"
                  >
                    Générer
                  </button>
                </div>
              </div>
              <div>
                <label class="form-label">Code-barres</label>
                <input
                  v-model="form.codeBarres"
                  type="text"
                  placeholder="EAN-13…"
                  class="form-input"
                />
              </div>
            </div>

            <!-- Nom -->
            <div>
              <label class="form-label">Nom <span class="text-red-500">*</span></label>
              <input
                v-model="form.nom"
                type="text"
                placeholder="Nom du produit"
                :class="['form-input', erreurs.nom ? 'border-red-400' : '']"
              />
              <p v-if="erreurs.nom" class="form-error">{{ erreurs.nom }}</p>
            </div>

            <!-- Catégorie -->
            <div>
              <div class="flex items-center justify-between mb-1">
                <label class="form-label mb-0">Catégorie</label>
                <!-- Bouton pour afficher/masquer le mini-formulaire de création -->
                <button
                  type="button"
                  @click="toggleNouvelleCategorie"
                  class="text-xs text-blue-600 hover:text-blue-700 font-medium flex items-center gap-1 transition-colors"
                >
                  <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      :d="showNouvelleCategorie ? 'M6 18L18 6M6 6l12 12' : 'M12 4v16m8-8H4'"/>
                  </svg>
                  {{ showNouvelleCategorie ? 'Annuler' : 'Nouvelle catégorie' }}
                </button>
              </div>

              <!-- Select catégorie existante -->
              <select v-model="form.categorieId" class="form-input">
                <option value="">Aucune catégorie</option>
                <option v-for="cat in categories" :key="cat.id" :value="String(cat.id)">
                  {{ cat.nom }}
                </option>
              </select>

              <!-- Mini-formulaire création catégorie à la volée -->
              <Transition
                enter-active-class="transition ease-out duration-150"
                enter-from-class="opacity-0 -translate-y-1"
                enter-to-class="opacity-100 translate-y-0"
                leave-active-class="transition ease-in duration-100"
                leave-from-class="opacity-100 translate-y-0"
                leave-to-class="opacity-0 -translate-y-1"
              >
                <div v-if="showNouvelleCategorie"
                  class="mt-2 p-3 bg-blue-50 border border-blue-200 rounded-lg space-y-2">
                  <p class="text-xs font-medium text-blue-700">Créer une nouvelle catégorie</p>
                  <div class="flex gap-2">
                    <input
                      v-model="nomNouvelleCategorie"
                      type="text"
                      placeholder="Nom de la catégorie"
                      class="form-input flex-1 text-sm py-1.5"
                      @keydown.enter.prevent="creerCategorieInline"
                    />
                    <button
                      type="button"
                      @click="creerCategorieInline"
                      :disabled="isCreatingCategorie"
                      class="px-3 py-1.5 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors flex items-center gap-1.5"
                    >
                      <svg v-if="isCreatingCategorie" class="w-3.5 h-3.5 animate-spin" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                      </svg>
                      Créer
                    </button>
                  </div>
                  <p v-if="erreurNouvelleCategorie" class="text-xs text-red-600">
                    {{ erreurNouvelleCategorie }}
                  </p>
                </div>
              </Transition>
            </div>

            <!-- Description -->
            <div>
              <label class="form-label">Description</label>
              <textarea
                v-model="form.description"
                rows="3"
                placeholder="Description optionnelle…"
                class="form-input resize-none"
              />
            </div>

            <!-- Prix achat + Prix vente -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label class="form-label">Prix d'achat (€)</label>
                <input
                  v-model="form.prixAchat"
                  type="number"
                  min="0"
                  step="0.01"
                  placeholder="0.00"
                  class="form-input"
                />
              </div>
              <div>
                <label class="form-label">Prix de vente (€)</label>
                <input
                  v-model="form.prixVente"
                  type="number"
                  min="0"
                  step="0.01"
                  placeholder="0.00"
                  class="form-input"
                />
              </div>
            </div>

            <!-- Poids + Volume -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label class="form-label">Poids (kg)</label>
                <input
                  v-model="form.poids"
                  type="number"
                  min="0"
                  step="0.001"
                  placeholder="0.000"
                  class="form-input"
                />
              </div>
              <div>
                <label class="form-label">Volume (m³)</label>
                <input
                  v-model="form.volume"
                  type="number"
                  min="0"
                  step="0.001"
                  placeholder="0.000"
                  class="form-input"
                />
              </div>
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
                {{ mode === 'creer' ? 'Créer le produit' : 'Enregistrer' }}
              </button>
            </div>

          </form>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
