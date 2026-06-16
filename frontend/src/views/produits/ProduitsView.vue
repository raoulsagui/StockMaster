<script setup>
// ============================================================
// VUE : Gestion des produits
//
// Cette vue est un orchestrateur — elle gère uniquement :
//   - Le chargement des données depuis l'API (en parallèle)
//   - La logique de filtres et pagination
//   - Les actions métier (créer, modifier, toggle statut)
//   - L'ouverture/fermeture des modals
//
// Le HTML est délégué aux composants dédiés.
// ============================================================

import { ref, computed, watch, onMounted }   from 'vue'
import AppLayout                              from '@/layout/AppLayout.vue'
import produitService                         from '@/services/produitService'
import categorieService                       from '@/services/categorieService'
import ProduitFiltresComponent                from '@/components/produits/ProduitFiltresComponent.vue'
import ProduitFiltresMobileComponent          from '@/components/produits/ProduitFiltresMobileComponent.vue'
import ProduitTableComponent                  from '@/components/produits/ProduitTableComponent.vue'
import ProduitModalCrudComponent              from '@/components/produits/ProduitModalCrudComponent.vue'

// -------------------------------------------------------
// DONNÉES
// -------------------------------------------------------
const produits   = ref([])
const categories = ref([])
const isLoading  = ref(false)
const erreur     = ref('')

onMounted(() => chargerDonnees())

async function chargerDonnees() {
  isLoading.value = true
  erreur.value    = ''
  try {
    const [p, c] = await Promise.all([
      produitService.findAll(),
      categorieService.findAll(),
    ])
    produits.value   = p
    categories.value = c
  } catch {
    erreur.value = 'Impossible de charger les données.'
  } finally {
    isLoading.value = false
  }
}

// -------------------------------------------------------
// FILTRES
// -------------------------------------------------------
const recherche       = ref('')
const filtreCategorie = ref('')
const filtreStatut    = ref('')
const showFiltresMobile = ref(false)

// -------------------------------------------------------
// PAGINATION
// -------------------------------------------------------
const pageCourante = ref(1)
const parPage      = ref(10)

watch([recherche, filtreCategorie, filtreStatut], () => { pageCourante.value = 1 })

const produitsFiltres = computed(() =>
  produits.value.filter(p => {
    const texte = recherche.value.toLowerCase()
    const matchTexte =
      !texte ||
      p.nom.toLowerCase().includes(texte) ||
      (p.reference || '').toLowerCase().includes(texte)
    const matchCategorie =
      !filtreCategorie.value ||
      String(p.categorieId) === filtreCategorie.value
    const matchStatut =
      !filtreStatut.value ||
      (filtreStatut.value === 'actif'   &&  p.actif) ||
      (filtreStatut.value === 'inactif' && !p.actif)
    return matchTexte && matchCategorie && matchStatut
  })
)

const totalPages = computed(() =>
  Math.ceil(produitsFiltres.value.length / parPage.value) || 1
)

const produitsPagines = computed(() => {
  const debut = (pageCourante.value - 1) * parPage.value
  return produitsFiltres.value.slice(debut, debut + parPage.value)
})

// -------------------------------------------------------
// HELPERS
// -------------------------------------------------------
const toPayload = (form) => ({
  reference:   form.reference   || null,
  codeBarres:  form.codeBarres  || null,
  nom:         form.nom,
  description: form.description || null,
  categorieId: form.categorieId ? Number(form.categorieId) : null,
  prixAchat:   form.prixAchat   !== '' ? Number(form.prixAchat)  : null,
  prixVente:   form.prixVente   !== '' ? Number(form.prixVente)  : null,
  poids:       form.poids       !== '' ? Number(form.poids)      : null,
  volume:      form.volume      !== '' ? Number(form.volume)     : null,
})

const genererReference = () => {
  const ts    = Date.now().toString(36).toUpperCase()
  const rand  = Math.random().toString(36).substring(2, 5).toUpperCase()
  return `REF-${ts}-${rand}`
}

// -------------------------------------------------------
// MODAL CRUD
// -------------------------------------------------------
const showModal    = ref(false)
const modalMode    = ref('creer')   // 'creer' | 'modifier'
const modalData    = ref(null)
const isSubmitting = ref(false)
const erreurApi    = ref('')

function ouvrirCreer() {
  modalMode.value  = 'creer'
  modalData.value  = null
  erreurApi.value  = ''
  showModal.value  = true
}

function ouvrirModifier(p) {
  modalMode.value  = 'modifier'
  modalData.value  = {
    id:          p.id,
    reference:   p.reference   ?? '',
    codeBarres:  p.codeBarres  ?? '',
    nom:         p.nom         ?? '',
    description: p.description ?? '',
    categorieId: p.categorieId != null ? String(p.categorieId) : '',
    prixAchat:   p.prixAchat   ?? '',
    prixVente:   p.prixVente   ?? '',
    poids:       p.poids       ?? '',
    volume:      p.volume      ?? '',
  }
  erreurApi.value  = ''
  showModal.value  = true
}

function onGenererReference() {
  if (!modalData.value) modalData.value = {}
  modalData.value = { ...modalData.value, reference: genererReference() }
}

async function soumettreModal(form) {
  isSubmitting.value = true
  erreurApi.value    = ''
  try {
    const payload = toPayload(form)
    if (modalMode.value === 'creer') {
      const created = await produitService.creer(payload)
      produits.value.push(created)
    } else {
      const updated = await produitService.modifier(modalData.value.id, payload)
      const idx = produits.value.findIndex(p => p.id === updated.id)
      if (idx !== -1) produits.value[idx] = updated
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
// ACTIONS DIRECTES
// -------------------------------------------------------
async function toggleStatut(p) {
  try {
    const updated = await produitService.toggleActif(p.id)
    p.actif = updated.actif
  } catch {
    alert('Erreur lors de la mise à jour du statut.')
  }
}
</script>

<template>
  <AppLayout>
    <div class="space-y-4 md:space-y-6">

      <!-- EN-TÊTE -->
      <div class="flex items-center justify-between">
        <div class="hidden md:block">
          <h1 class="text-2xl font-bold text-gray-900">Produits</h1>
          <p class="text-sm text-gray-500 mt-0.5">{{ produits.length }} produits enregistrés</p>
        </div>
        <button @click="ouvrirCreer" class="btn-primary justify-center self-end sm:self-auto">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
          </svg>
          Nouveau produit
        </button>
      </div>

      <!-- FILTRES DESKTOP + MOBILE -->
      <ProduitFiltresComponent
        v-model:recherche="recherche"
        v-model:filtreCategorie="filtreCategorie"
        v-model:filtreStatut="filtreStatut"
        :categories="categories"
        @ouvrir-filtres-mobile="showFiltresMobile = true"
      />

      <!-- BOTTOM SHEET MOBILE -->
      <ProduitFiltresMobileComponent
        :visible="showFiltresMobile"
        :filtreCategorie="filtreCategorie"
        :filtreStatut="filtreStatut"
        :categories="categories"
        @fermer="showFiltresMobile = false"
        @update:filtreCategorie="filtreCategorie = $event"
        @update:filtreStatut="filtreStatut = $event"
        @reinitialiser="filtreCategorie = ''; filtreStatut = ''"
      />

      <!-- ERREUR API -->
      <div v-if="erreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">
        {{ erreur }}
      </div>

      <!-- TABLEAU + PAGINATION -->
      <ProduitTableComponent
        :produits="produitsPagines"
        :isLoading="isLoading"
        :pageCourante="pageCourante"
        :totalPages="totalPages"
        :parPage="parPage"
        :totalFiltres="produitsFiltres.length"
        @modifier="ouvrirModifier"
        @toggle-statut="toggleStatut"
        @page-precedente="pageCourante--"
        @page-suivante="pageCourante++"
        @changer-page="pageCourante = $event"
      />

    </div>

    <!-- MODAL CRUD (création + modification) -->
    <ProduitModalCrudComponent
      :visible="showModal"
      :mode="modalMode"
      :initialData="modalData"
      :isLoading="isSubmitting"
      :erreurApi="erreurApi"
      :categories="categories"
      @fermer="showModal = false"
      @soumettre="soumettreModal"
      @generer-reference="onGenererReference"
      @categorie-creee="(c) => categories.push(c)"
    />

  </AppLayout>
</template>
