<script setup>
// ============================================================
// VUE : Gestion des stocks (Module 7)
//
// Orchestrateur — gère :
//   - Chargement des stocks depuis l'API
//   - Filtres (produit, entrepôt, statut alerte)
//   - Pagination
//   - Ouverture du panneau historique
//   - Ouverture de la modal seuils
// ============================================================

import { ref, computed, watch, onMounted } from 'vue'
import AppLayout                   from '@/layout/AppLayout.vue'
import stockService                from '@/services/stockService'
import StockTableComponent         from '@/components/stocks/StockTableComponent.vue'
import StockSeuilsModalComponent   from '@/components/stocks/StockSeuilsModalComponent.vue'
import StockMouvementsComponent    from '@/components/stocks/StockMouvementsComponent.vue'

// -------------------------------------------------------
// DONNÉES
// -------------------------------------------------------
const stocks    = ref([])
const isLoading = ref(false)
const erreur    = ref('')

onMounted(() => chargerStocks())

async function chargerStocks() {
  isLoading.value = true
  erreur.value    = ''
  try {
    stocks.value = await stockService.findAll()
  } catch {
    erreur.value = 'Impossible de charger les stocks.'
  } finally {
    isLoading.value = false
  }
}

// -------------------------------------------------------
// FILTRES
// -------------------------------------------------------
const recherche     = ref('')
const filtreAlerte  = ref('') // '' | 'faible' | 'sursock' | 'normal'

// -------------------------------------------------------
// PAGINATION
// -------------------------------------------------------
const pageCourante = ref(1)
const parPage      = ref(15)

watch([recherche, filtreAlerte], () => { pageCourante.value = 1 })

const stocksFiltres = computed(() =>
  stocks.value.filter(s => {
    const texte = recherche.value.toLowerCase()
    const matchTexte =
      !texte ||
      s.produitNom.toLowerCase().includes(texte) ||
      s.produitReference?.toLowerCase().includes(texte) ||
      s.entrepotNom.toLowerCase().includes(texte)

    const matchAlerte =
      !filtreAlerte.value ||
      (filtreAlerte.value === 'faible'  &&  s.enStockFaible) ||
      (filtreAlerte.value === 'sursock' &&  s.enSurStock) ||
      (filtreAlerte.value === 'normal'  && !s.enStockFaible && !s.enSurStock)

    return matchTexte && matchAlerte
  })
)

const totalPages = computed(() =>
  Math.ceil(stocksFiltres.value.length / parPage.value) || 1
)

const stocksPagines = computed(() => {
  const debut = (pageCourante.value - 1) * parPage.value
  return stocksFiltres.value.slice(debut, debut + parPage.value)
})

// KPI calculés depuis les données chargées
const nbReferences   = computed(() => stocks.value.length)
const nbEnAlerte     = computed(() => stocks.value.filter(s => s.enStockFaible).length)
const nbEnSurStock   = computed(() => stocks.value.filter(s => s.enSurStock).length)
const nbNormaux      = computed(() => stocks.value.filter(s => !s.enStockFaible && !s.enSurStock).length)

// Total des unités disponibles tous entrepôts confondus
const totalDisponible = computed(() =>
  stocks.value.reduce((acc, s) => acc + (s.quantiteDisponible ?? 0), 0)
)

// Total des unités réservées
const totalReserve = computed(() =>
  stocks.value.reduce((acc, s) => acc + (s.quantiteReservee ?? 0), 0)
)

// Taux de santé du stock = % de références en état normal
const tauxSante = computed(() => {
  if (!nbReferences.value) return 100
  return Math.round((nbNormaux.value / nbReferences.value) * 100)
})

// -------------------------------------------------------
// PANNEAU HISTORIQUE
// -------------------------------------------------------
const showMouvements  = ref(false)
const stockMouvements = ref(null)

function ouvrirMouvements(stock) {
  stockMouvements.value = stock
  showMouvements.value  = true
}

// -------------------------------------------------------
// MODAL SEUILS
// -------------------------------------------------------
const showSeuils    = ref(false)
const stockSeuils   = ref(null)
const isSaving      = ref(false)
const erreurSeuils  = ref('')

function ouvrirSeuils(stock) {
  stockSeuils.value  = stock
  erreurSeuils.value = ''
  showSeuils.value   = true
}

async function soumettreSeuilsModal(payload) {
  isSaving.value     = true
  erreurSeuils.value = ''
  try {
    const updated = await stockService.mettreAJourSeuils(stockSeuils.value.id, payload)
    // Mise à jour locale sans recharger toute la liste
    const idx = stocks.value.findIndex(s => s.id === updated.id)
    if (idx !== -1) stocks.value[idx] = updated
    showSeuils.value = false
  } catch (e) {
    erreurSeuils.value = e.response?.data ?? 'Erreur lors de la sauvegarde.'
  } finally {
    isSaving.value = false
  }
}
</script>

<template>
  <AppLayout>
    <div class="space-y-4 md:space-y-6">

      <!-- EN-TÊTE -->
      <div class="flex items-center justify-between">
        <div class="hidden md:block">
          <h1 class="text-2xl font-bold text-gray-900">Stocks</h1>
          <p class="text-sm text-gray-500 mt-0.5">
            {{ stocks.length }} références · {{ nbEnAlerte }} en alerte
          </p>
        </div>
        <!-- Bouton rafraîchir -->
        <button @click="chargerStocks" class="btn-secondary">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
          </svg>
          Actualiser
        </button>
      </div>

      <!-- KPI -->
      <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">

        <!-- Références en stock -->
        <div class="card flex items-center gap-4 hover:shadow-md transition-shadow">
          <div class="w-12 h-12 rounded-xl bg-blue-600 flex items-center justify-center flex-shrink-0">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
            </svg>
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-2xl font-bold text-gray-900">{{ nbReferences }}</p>
            <p class="text-sm text-gray-500">Références suivies</p>
            <p class="text-xs text-blue-600 font-medium mt-0.5">
              {{ stocks.length }} combinaisons produit/entrepôt
            </p>
          </div>
        </div>

        <!-- Unités disponibles -->
        <div class="card flex items-center gap-4 hover:shadow-md transition-shadow">
          <div class="w-12 h-12 rounded-xl bg-green-600 flex items-center justify-center flex-shrink-0">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-2xl font-bold text-gray-900">{{ totalDisponible.toLocaleString('fr-FR') }}</p>
            <p class="text-sm text-gray-500">Unités disponibles</p>
            <p class="text-xs text-gray-400 mt-0.5">
              {{ totalReserve.toLocaleString('fr-FR') }} réservées
            </p>
          </div>
        </div>

        <!-- Taux de santé du stock -->
        <div class="card flex items-center gap-4 hover:shadow-md transition-shadow">
          <div :class="['w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0',
            tauxSante >= 80 ? 'bg-green-600' : tauxSante >= 50 ? 'bg-yellow-500' : 'bg-red-600']">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
            </svg>
          </div>
          <div class="flex-1 min-w-0">
            <p :class="['text-2xl font-bold', tauxSante >= 80 ? 'text-green-600' : tauxSante >= 50 ? 'text-yellow-600' : 'text-red-600']">
              {{ tauxSante }}%
            </p>
            <p class="text-sm text-gray-500">Santé globale du stock</p>
            <p class="text-xs text-gray-400 mt-0.5">{{ nbNormaux }} références en état normal</p>
          </div>
        </div>

        <!-- Stocks faibles -->
        <div :class="['card flex items-center gap-4 hover:shadow-md transition-shadow cursor-pointer',
          nbEnAlerte > 0 ? 'border-red-200' : '']"
          @click="filtreAlerte = filtreAlerte === 'faible' ? '' : 'faible'"
        >
          <div :class="['w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0',
            nbEnAlerte > 0 ? 'bg-red-600' : 'bg-gray-400']">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
            </svg>
          </div>
          <div class="flex-1 min-w-0">
            <p :class="['text-2xl font-bold', nbEnAlerte > 0 ? 'text-red-600' : 'text-gray-900']">
              {{ nbEnAlerte }}
            </p>
            <p class="text-sm text-gray-500">Stocks faibles</p>
            <p class="text-xs mt-0.5" :class="nbEnAlerte > 0 ? 'text-red-500 font-medium' : 'text-gray-400'">
              {{ nbEnAlerte > 0 ? 'Cliquer pour filtrer' : 'Aucune alerte' }}
            </p>
          </div>
        </div>

        <!-- Sur-stocks -->
        <div :class="['card flex items-center gap-4 hover:shadow-md transition-shadow cursor-pointer',
          nbEnSurStock > 0 ? 'border-orange-200' : '']"
          @click="filtreAlerte = filtreAlerte === 'sursock' ? '' : 'sursock'"
        >
          <div :class="['w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0',
            nbEnSurStock > 0 ? 'bg-orange-500' : 'bg-gray-400']">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"/>
            </svg>
          </div>
          <div class="flex-1 min-w-0">
            <p :class="['text-2xl font-bold', nbEnSurStock > 0 ? 'text-orange-600' : 'text-gray-900']">
              {{ nbEnSurStock }}
            </p>
            <p class="text-sm text-gray-500">Sur-stocks</p>
            <p class="text-xs mt-0.5" :class="nbEnSurStock > 0 ? 'text-orange-500 font-medium' : 'text-gray-400'">
              {{ nbEnSurStock > 0 ? 'Cliquer pour filtrer' : 'Aucun sur-stock' }}
            </p>
          </div>
        </div>

        <!-- Stocks normaux -->
        <div class="card flex items-center gap-4 hover:shadow-md transition-shadow cursor-pointer"
          @click="filtreAlerte = filtreAlerte === 'normal' ? '' : 'normal'"
        >
          <div class="w-12 h-12 rounded-xl bg-indigo-600 flex items-center justify-center flex-shrink-0">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M5 13l4 4L19 7"/>
            </svg>
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-2xl font-bold text-gray-900">{{ nbNormaux }}</p>
            <p class="text-sm text-gray-500">Stocks normaux</p>
            <p class="text-xs text-gray-400 mt-0.5">Cliquer pour filtrer</p>
          </div>
        </div>

      </div>

      <!-- FILTRES -->
      <div class="card p-3 md:p-4">
        <div class="flex flex-col sm:flex-row gap-3">
          <div class="relative flex-1">
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
              fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0"/>
            </svg>
            <input
              v-model="recherche"
              type="text"
              placeholder="Rechercher par produit ou entrepôt…"
              class="form-input pl-9"
            />
          </div>
          <select v-model="filtreAlerte" class="form-input w-full sm:w-48">
            <option value="">Tous les statuts</option>
            <option value="faible">Stock faible</option>
            <option value="sursock">Sur-stock</option>
            <option value="normal">Normal</option>
          </select>
        </div>
      </div>

      <!-- ERREUR API -->
      <div v-if="erreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">
        {{ erreur }}
      </div>

      <!-- TABLEAU -->
      <StockTableComponent
        :stocks="stocksPagines"
        :isLoading="isLoading"
        :pageCourante="pageCourante"
        :totalPages="totalPages"
        :parPage="parPage"
        :totalFiltres="stocksFiltres.length"
        @voir-mouvements="ouvrirMouvements"
        @configurer-seuils="ouvrirSeuils"
        @page-precedente="pageCourante--"
        @page-suivante="pageCourante++"
        @changer-page="pageCourante = $event"
      />

    </div>

    <!-- PANNEAU HISTORIQUE (side panel) -->
    <StockMouvementsComponent
      :visible="showMouvements"
      :stock="stockMouvements"
      @fermer="showMouvements = false"
    />

    <!-- MODAL SEUILS -->
    <StockSeuilsModalComponent
      :visible="showSeuils"
      :stock="stockSeuils"
      :isLoading="isSaving"
      :erreurApi="erreurSeuils"
      @fermer="showSeuils = false"
      @soumettre="soumettreSeuilsModal"
    />

  </AppLayout>
</template>
