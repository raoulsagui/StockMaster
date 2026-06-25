<script setup>
// ============================================================
// VUE : Gestion des catégories (Module 5)
//
// Orchestrateur — gère uniquement :
//   - Chargement API
//   - Filtres + pagination
//   - Logique des modals (ouvrir, soumettre, supprimer)
//
// Le HTML est délégué aux composants dédiés.
// ============================================================

import { ref, computed, onMounted } from 'vue'
import AppLayout                          from '@/layout/AppLayout.vue'
import categorieService                   from '@/services/categorieService'
import CategorieTableComponent            from '@/components/categories/CategorieTableComponent.vue'
import CategorieModalCrudComponent        from '@/components/categories/CategorieModalCrudComponent.vue'
import CategorieModalSupprimerComponent   from '@/components/categories/CategorieModalSupprimerComponent.vue'
import { usePermissions }                 from '@/composables/usePermissions'

// -------------------------------------------------------
// DONNÉES
// -------------------------------------------------------
const { peutGererCatalogue } = usePermissions()
const categories = ref([])
const isLoading  = ref(false)
const erreur     = ref('')

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
    !recherche.value ||
    c.nom.toLowerCase().includes(recherche.value.toLowerCase())
  )
)

const totalPages = computed(() =>
  Math.ceil(categoriesFiltrees.value.length / parPage.value) || 1
)

const categoriesPagines = computed(() => {
  const debut = (pageCourante.value - 1) * parPage.value
  return categoriesFiltrees.value.slice(debut, debut + parPage.value)
})

// -------------------------------------------------------
// MODAL CRUD (création + modification)
// -------------------------------------------------------
const showModal    = ref(false)
const modalMode    = ref('creer')
const modalData    = ref(null)
const isSubmitting = ref(false)
const erreurApi    = ref('')

function ouvrirCreer() {
  modalMode.value = 'creer'
  modalData.value = null
  erreurApi.value = ''
  showModal.value = true
}

function ouvrirModifier(c) {
  modalMode.value = 'modifier'
  modalData.value = { id: c.id, nom: c.nom, description: c.description ?? '' }
  erreurApi.value = ''
  showModal.value = true
}

async function soumettreModal(payload) {
  isSubmitting.value = true
  erreurApi.value    = ''
  try {
    if (modalMode.value === 'creer') {
      const created = await categorieService.creer(payload)
      categories.value.push(created)
    } else {
      const updated = await categorieService.modifier(modalData.value.id, payload)
      const idx = categories.value.findIndex(c => c.id === updated.id)
      if (idx !== -1) categories.value[idx] = updated
    }
    showModal.value = false
  } catch (e) {
    const msg = e.response?.data
    erreurApi.value = typeof msg === 'string' ? msg : 'Erreur lors de la sauvegarde.'
  } finally {
    isSubmitting.value = false
  }
}

// -------------------------------------------------------
// MODAL SUPPRESSION
// -------------------------------------------------------
const showModalSupprimer  = ref(false)
const categorieASupprimer = ref(null)
const isDeleting          = ref(false)
const erreurApiSupprimer  = ref('')

function ouvrirSupprimer(c) {
  categorieASupprimer.value = c
  erreurApiSupprimer.value  = ''
  showModalSupprimer.value  = true
}

async function confirmerSupprimer() {
  isDeleting.value         = true
  erreurApiSupprimer.value = ''
  try {
    await categorieService.supprimer(categorieASupprimer.value.id)
    categories.value         = categories.value.filter(c => c.id !== categorieASupprimer.value.id)
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
    <div class="space-y-5">

      <!-- EN-TÊTE GRADIENT -->
      <div class="page-header bg-gradient-to-r from-pink-500 to-rose-600 shadow-lg shadow-pink-500/20">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-xl font-bold text-white">Catégories</h1>
            <p class="text-pink-100 text-sm mt-0.5">{{ categories.length }} catégorie(s) enregistrée(s)</p>
          </div>
          <button v-if="peutGererCatalogue" @click="ouvrirCreer"
            class="inline-flex items-center gap-2 bg-white/20 hover:bg-white/30 text-white px-4 py-2 rounded-xl text-sm font-medium transition-colors backdrop-blur-sm border border-white/20">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
            </svg>
            Nouvelle catégorie
          </button>
        </div>
        <div class="absolute -right-8 -top-8 w-32 h-32 rounded-full bg-white/5 pointer-events-none"></div>
        <div class="absolute -right-4 top-8 w-20 h-20 rounded-full bg-white/5 pointer-events-none"></div>
      </div>

      <!-- BARRE DE RECHERCHE -->
      <div class="relative">
        <svg class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
          fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0"/>
        </svg>
        <input v-model="recherche" type="text"
          placeholder="Rechercher une catégorie…"
          class="form-input pl-10 bg-white shadow-sm"/>
      </div>

      <!-- ERREUR -->
      <div v-if="erreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">
        {{ erreur }}
      </div>

      <!-- TABLEAU + PAGINATION -->
      <CategorieTableComponent
        :categories="categoriesPagines"
        :isLoading="isLoading"
        :pageCourante="pageCourante"
        :totalPages="totalPages"
        :parPage="parPage"
        :totalFiltres="categoriesFiltrees.length"
        :peut-modifier="peutGererCatalogue"
        @modifier="ouvrirModifier"
        @supprimer="ouvrirSupprimer"
        @page-precedente="pageCourante--"
        @page-suivante="pageCourante++"
        @changer-page="pageCourante = $event"
      />

    </div>

    <!-- MODAL CRUD -->
    <CategorieModalCrudComponent
      :visible="showModal"
      :mode="modalMode"
      :initialData="modalData"
      :isLoading="isSubmitting"
      :erreurApi="erreurApi"
      @fermer="showModal = false"
      @soumettre="soumettreModal"
    />

    <!-- MODAL SUPPRESSION -->
    <CategorieModalSupprimerComponent
      :visible="showModalSupprimer"
      :categorie="categorieASupprimer"
      :isLoading="isDeleting"
      :erreurApi="erreurApiSupprimer"
      @fermer="showModalSupprimer = false"
      @confirmer="confirmerSupprimer"
    />

  </AppLayout>
</template>
