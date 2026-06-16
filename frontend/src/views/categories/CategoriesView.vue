<script setup>
import { ref, computed, onMounted } from 'vue'
import AppLayout from '@/layout/AppLayout.vue'
import categorieService from '@/services/categorieService'

const categories  = ref([])
const isLoading   = ref(false)
const erreur      = ref('')

onMounted(() => chargerCategories())

async function chargerCategories() {
  isLoading.value = true
  erreur.value    = ''
  try {
    categories.value = await categorieService.findAll()
  } catch {
    erreur.value = 'Impossible de charger les catégories.'
  } finally {
    isLoading.value = false
  }
}

// -------------------------------------------------------
// FILTRES + PAGINATION
// -------------------------------------------------------
const recherche    = ref('')
const pageCourante = ref(1)
const parPage      = ref(10)

const categoriesFiltrees = computed(() =>
  categories.value.filter(c =>
    !recherche.value || c.nom.toLowerCase().includes(recherche.value.toLowerCase())
  )
)

const totalPages        = computed(() => Math.ceil(categoriesFiltrees.value.length / parPage.value) || 1)
const categoriesPagines = computed(() => {
  const debut = (pageCourante.value - 1) * parPage.value
  return categoriesFiltrees.value.slice(debut, debut + parPage.value)
})

// -------------------------------------------------------
// MODAL CRÉATION
// -------------------------------------------------------
const showModalCreer  = ref(false)
const formCreer       = ref({ nom: '', description: '' })
const erreursCreer    = ref({})
const erreurApiCreer  = ref('')
const isCreating      = ref(false)

function ouvrirModalCreer() {
  formCreer.value      = { nom: '', description: '' }
  erreursCreer.value   = {}
  erreurApiCreer.value = ''
  showModalCreer.value = true
}

function validerCreer() {
  const e = {}
  if (!formCreer.value.nom.trim()) e.nom = 'Le nom est obligatoire.'
  erreursCreer.value = e
  return Object.keys(e).length === 0
}

async function soumettreCreer() {
  if (!validerCreer()) return
  isCreating.value     = true
  erreurApiCreer.value = ''
  try {
    const created = await categorieService.creer(formCreer.value)
    categories.value.push(created)
    showModalCreer.value = false
  } catch (e) {
    const msg = e.response?.data
    erreurApiCreer.value = typeof msg === 'string' ? msg : 'Erreur lors de la création.'
  } finally {
    isCreating.value = false
  }
}

// -------------------------------------------------------
// MODAL MODIFICATION
// -------------------------------------------------------
const showModalModifier  = ref(false)
const formModifier       = ref({ id: null, nom: '', description: '' })
const erreursModifier    = ref({})
const erreurApiModifier  = ref('')
const isModifying        = ref(false)

function ouvrirModalModifier(c) {
  formModifier.value      = { id: c.id, nom: c.nom, description: c.description ?? '' }
  erreursModifier.value   = {}
  erreurApiModifier.value = ''
  showModalModifier.value = true
}

function validerModifier() {
  const e = {}
  if (!formModifier.value.nom.trim()) e.nom = 'Le nom est obligatoire.'
  erreursModifier.value = e
  return Object.keys(e).length === 0
}

async function soumettreModifier() {
  if (!validerModifier()) return
  isModifying.value       = true
  erreurApiModifier.value = ''
  try {
    const updated = await categorieService.modifier(formModifier.value.id, {
      nom:         formModifier.value.nom,
      description: formModifier.value.description,
    })
    const idx = categories.value.findIndex(c => c.id === updated.id)
    if (idx !== -1) categories.value[idx] = updated
    showModalModifier.value = false
  } catch (e) {
    const msg = e.response?.data
    erreurApiModifier.value = typeof msg === 'string' ? msg : 'Erreur lors de la modification.'
  } finally {
    isModifying.value = false
  }
}

// -------------------------------------------------------
// SUPPRESSION
// -------------------------------------------------------
const showModalSupprimer = ref(false)
const categorieASupprimer = ref(null)
const isDeleting          = ref(false)
const erreurApiSupprimer  = ref('')

function ouvrirModalSupprimer(c) {
  categorieASupprimer.value = c
  erreurApiSupprimer.value  = ''
  showModalSupprimer.value  = true
}

async function confirmerSupprimer() {
  isDeleting.value          = true
  erreurApiSupprimer.value  = ''
  try {
    await categorieService.supprimer(categorieASupprimer.value.id)
    categories.value = categories.value.filter(c => c.id !== categorieASupprimer.value.id)
    showModalSupprimer.value = false
  } catch (e) {
    const msg = e.response?.data
    erreurApiSupprimer.value = typeof msg === 'string' ? msg : 'Erreur lors de la suppression.'
  } finally {
    isDeleting.value = false
  }
}
</script>

<template>
  <AppLayout>
    <div class="space-y-4 md:space-y-6">

      <!-- EN-TÊTE -->
      <div class="flex items-center justify-between">
        <div class="hidden md:block">
          <h1 class="text-xl md:text-2xl font-bold text-gray-900">Catégories</h1>
          <p class="text-sm text-gray-500 mt-0.5">{{ categories.length }} catégorie(s) enregistrée(s)</p>
        </div>
        <button @click="ouvrirModalCreer" class="btn-primary justify-center self-end sm:self-auto">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
          </svg>
          Nouvelle catégorie
        </button>
      </div>

      <!-- RECHERCHE -->
      <div class="card p-3 md:p-4">
        <div class="relative">
          <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0"/>
          </svg>
          <input v-model="recherche" type="text" placeholder="Rechercher…" class="form-input pl-9"/>
        </div>
      </div>

      <!-- ERREUR -->
      <div v-if="erreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">{{ erreur }}</div>

      <!-- TABLEAU -->
      <div class="card p-0 overflow-hidden">
        <div v-if="isLoading" class="p-12 text-center text-gray-400">
          <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-blue-500" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
          Chargement…
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50 border-b border-gray-100">
              <tr>
                <th class="table-header">Nom</th>
                <th class="table-header hidden md:table-cell">Description</th>
                <th class="table-header text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <tr v-if="categoriesFiltrees.length === 0">
                <td colspan="3" class="px-6 py-12 text-center text-gray-400 text-sm">
                  Aucune catégorie ne correspond aux critères.
                </td>
              </tr>
              <tr v-for="c in categoriesPagines" :key="c.id" class="hover:bg-gray-50 transition-colors">
                <td class="table-cell font-medium text-gray-900">{{ c.nom }}</td>
                <td class="table-cell text-gray-500 hidden md:table-cell">{{ c.description || '—' }}</td>
                <td class="table-cell">
                  <div class="flex items-center justify-end gap-1">
                    <button @click="ouvrirModalModifier(c)"
                      class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors" title="Modifier">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                      </svg>
                    </button>
                    <button @click="ouvrirModalSupprimer(c)"
                      class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors" title="Supprimer">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                      </svg>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- PAGINATION -->
        <div v-if="!isLoading && totalPages > 1"
          class="flex flex-col sm:flex-row items-center justify-between gap-3 px-4 py-3 border-t border-gray-100">
          <p class="text-sm text-gray-500">
            {{ (pageCourante - 1) * parPage + 1 }}–{{ Math.min(pageCourante * parPage, categoriesFiltrees.length) }}
            sur {{ categoriesFiltrees.length }} catégories
          </p>
          <div class="flex items-center gap-1">
            <button @click="pageCourante--" :disabled="pageCourante === 1"
              class="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
              </svg>
            </button>
            <button v-for="p in totalPages" :key="p" @click="pageCourante = p"
              :class="['w-8 h-8 rounded-lg text-sm font-medium transition-colors',
                p === pageCourante ? 'bg-blue-600 text-white' : 'text-gray-600 hover:bg-gray-100']">
              {{ p }}
            </button>
            <button @click="pageCourante++" :disabled="pageCourante === totalPages"
              class="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL CRÉATION -->
    <Teleport to="body">
      <Transition enter-active-class="transition duration-200" enter-from-class="opacity-0" enter-to-class="opacity-100"
        leave-active-class="transition duration-150" leave-from-class="opacity-100" leave-to-class="opacity-0">
        <div v-if="showModalCreer" class="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
          <div class="absolute inset-0 bg-black/50" @click="showModalCreer = false"></div>
          <div class="relative bg-white w-full sm:max-w-md sm:rounded-2xl rounded-t-2xl shadow-2xl max-h-[90vh] overflow-y-auto">
            <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 sticky top-0 bg-white rounded-t-2xl">
              <h2 class="text-lg font-semibold text-gray-900">Nouvelle catégorie</h2>
              <button @click="showModalCreer = false" class="p-2 rounded-lg text-gray-400 hover:bg-gray-100 transition-colors">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            </div>
            <form @submit.prevent="soumettreCreer" class="px-6 py-5 space-y-4">
              <div v-if="erreurApiCreer" class="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg p-3">{{ erreurApiCreer }}</div>
              <div>
                <label class="form-label">Nom <span class="text-red-500">*</span></label>
                <input v-model="formCreer.nom" type="text" placeholder="Ex : Informatique"
                  :class="['form-input', erreursCreer.nom ? 'border-red-400' : '']"/>
                <p v-if="erreursCreer.nom" class="form-error">{{ erreursCreer.nom }}</p>
              </div>
              <div>
                <label class="form-label">Description</label>
                <textarea v-model="formCreer.description" rows="3" placeholder="Description optionnelle…" class="form-input resize-none"/>
              </div>
              <div class="flex gap-3 pt-2">
                <button type="button" @click="showModalCreer = false" class="btn-secondary flex-1">Annuler</button>
                <button type="submit" :disabled="isCreating" class="btn-primary flex-1 justify-center">
                  <svg v-if="isCreating" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                  </svg>
                  Créer
                </button>
              </div>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- MODAL MODIFICATION -->
    <Teleport to="body">
      <Transition enter-active-class="transition duration-200" enter-from-class="opacity-0" enter-to-class="opacity-100"
        leave-active-class="transition duration-150" leave-from-class="opacity-100" leave-to-class="opacity-0">
        <div v-if="showModalModifier" class="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
          <div class="absolute inset-0 bg-black/50" @click="showModalModifier = false"></div>
          <div class="relative bg-white w-full sm:max-w-md sm:rounded-2xl rounded-t-2xl shadow-2xl max-h-[90vh] overflow-y-auto">
            <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 sticky top-0 bg-white rounded-t-2xl">
              <h2 class="text-lg font-semibold text-gray-900">Modifier la catégorie</h2>
              <button @click="showModalModifier = false" class="p-2 rounded-lg text-gray-400 hover:bg-gray-100 transition-colors">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            </div>
            <form @submit.prevent="soumettreModifier" class="px-6 py-5 space-y-4">
              <div v-if="erreurApiModifier" class="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg p-3">{{ erreurApiModifier }}</div>
              <div>
                <label class="form-label">Nom <span class="text-red-500">*</span></label>
                <input v-model="formModifier.nom" type="text"
                  :class="['form-input', erreursModifier.nom ? 'border-red-400' : '']"/>
                <p v-if="erreursModifier.nom" class="form-error">{{ erreursModifier.nom }}</p>
              </div>
              <div>
                <label class="form-label">Description</label>
                <textarea v-model="formModifier.description" rows="3" class="form-input resize-none"/>
              </div>
              <div class="flex gap-3 pt-2">
                <button type="button" @click="showModalModifier = false" class="btn-secondary flex-1">Annuler</button>
                <button type="submit" :disabled="isModifying" class="btn-primary flex-1 justify-center">
                  <svg v-if="isModifying" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
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

    <!-- MODAL SUPPRESSION -->
    <Teleport to="body">
      <Transition enter-active-class="transition duration-200" enter-from-class="opacity-0" enter-to-class="opacity-100"
        leave-active-class="transition duration-150" leave-from-class="opacity-100" leave-to-class="opacity-0">
        <div v-if="showModalSupprimer" class="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
          <div class="absolute inset-0 bg-black/50" @click="showModalSupprimer = false"></div>
          <div class="relative bg-white w-full sm:max-w-sm sm:rounded-2xl rounded-t-2xl shadow-2xl">
            <div class="p-6">
              <div class="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg class="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                </svg>
              </div>
              <h3 class="text-base font-semibold text-gray-900 text-center mb-1">Supprimer la catégorie</h3>
              <p class="text-sm text-gray-500 text-center mb-6">
                Voulez-vous vraiment supprimer <span class="font-medium text-gray-800">{{ categorieASupprimer?.nom }}</span> ?
                Cette action est irréversible.
              </p>
              <div v-if="erreurApiSupprimer" class="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg p-3 mb-4">{{ erreurApiSupprimer }}</div>
              <div class="flex gap-3">
                <button @click="showModalSupprimer = false" class="btn-secondary flex-1">Annuler</button>
                <button @click="confirmerSupprimer" :disabled="isDeleting"
                  class="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-red-600 hover:bg-red-700 text-white text-sm font-medium rounded-lg transition-colors disabled:opacity-50">
                  <svg v-if="isDeleting" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                  </svg>
                  Supprimer
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

  </AppLayout>
</template>
