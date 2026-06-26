<script setup>
// ============================================================
// VUE : Gestion des stocks (Module 7) — version sobre
// ============================================================

import { ref, computed, watch, onMounted } from 'vue'
import AppLayout                   from '@/layout/AppLayout.vue'
import stockService                from '@/services/stockService'
import StockTableComponent         from '@/components/stocks/StockTableComponent.vue'
import StockSeuilsModalComponent   from '@/components/stocks/StockSeuilsModalComponent.vue'
import StockMouvementsComponent    from '@/components/stocks/StockMouvementsComponent.vue'
import { usePermissions }          from '@/composables/usePermissions'

const { peutConfigurerSeuils } = usePermissions()
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

const recherche     = ref('')
const filtreAlerte  = ref('')
const pageCourante  = ref(1)
const parPage       = ref(15)

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

const nbReferences   = computed(() => stocks.value.length)
const nbEnAlerte     = computed(() => stocks.value.filter(s => s.enStockFaible).length)
const nbEnSurStock   = computed(() => stocks.value.filter(s => s.enSurStock).length)
const nbNormaux      = computed(() => stocks.value.filter(s => !s.enStockFaible && !s.enSurStock).length)
const totalDisponible = computed(() =>
  stocks.value.reduce((acc, s) => acc + (s.quantiteDisponible ?? 0), 0)
)
const totalReserve = computed(() =>
  stocks.value.reduce((acc, s) => acc + (s.quantiteReservee ?? 0), 0)
)

const showMouvements  = ref(false)
const stockMouvements = ref(null)
function ouvrirMouvements(stock) {
  stockMouvements.value = stock
  showMouvements.value  = true
}

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
    <div class="space-y-5">

      <!-- EN-TÊTE sobre -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-xl font-semibold text-gray-900">Stocks</h1>
          <p class="text-sm text-gray-400 mt-0.5">{{ stocks.length }} références suivies</p>
        </div>
        <button @click="chargerStocks"
          class="inline-flex items-center gap-1.5 bg-white border border-gray-200 text-gray-600 hover:bg-gray-50 px-3.5 py-2 rounded-lg text-sm font-medium transition-colors">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
          </svg>
          Actualiser
        </button>
      </div>

      <!-- KPI sobres -->
      <div class="grid grid-cols-2 sm:grid-cols-3 xl:grid-cols-6 gap-3">

        <div class="bg-white rounded-xl border border-gray-200 p-3.5">
          <p class="text-lg font-semibold text-gray-900">{{ nbReferences }}</p>
          <p class="text-xs text-gray-500">Références</p>
        </div>

        <div class="bg-white rounded-xl border border-gray-200 p-3.5">
          <p class="text-lg font-semibold text-gray-900">{{ totalDisponible.toLocaleString('fr-FR') }}</p>
          <p class="text-xs text-gray-500">Unités dispo.</p>
          <p class="text-xs text-gray-400">{{ totalReserve.toLocaleString('fr-FR') }} réservées</p>
        </div>

        <div class="bg-white rounded-xl border border-gray-200 p-3.5">
          <p class="text-lg font-semibold text-gray-900">{{ nbNormaux }}</p>
          <p class="text-xs text-gray-500">Normaux</p>
        </div>

        <div
          :class="['bg-white rounded-xl border p-3.5 cursor-pointer transition-colors', nbEnAlerte > 0 ? 'border-red-200 bg-red-50/50' : 'border-gray-200']"
          @click="filtreAlerte = filtreAlerte === 'faible' ? '' : 'faible'">
          <p :class="['text-lg font-semibold', nbEnAlerte > 0 ? 'text-red-600' : 'text-gray-900']">{{ nbEnAlerte }}</p>
          <p class="text-xs text-gray-500">Stock faible</p>
        </div>

        <div
          :class="['bg-white rounded-xl border p-3.5 cursor-pointer transition-colors', nbEnSurStock > 0 ? 'border-amber-200 bg-amber-50/50' : 'border-gray-200']"
          @click="filtreAlerte = filtreAlerte === 'sursock' ? '' : 'sursock'">
          <p :class="['text-lg font-semibold', nbEnSurStock > 0 ? 'text-amber-600' : 'text-gray-900']">{{ nbEnSurStock }}</p>
          <p class="text-xs text-gray-500">Sur-stock</p>
        </div>

        <div
          class="bg-white rounded-xl border border-gray-200 p-3.5 cursor-pointer transition-colors hover:bg-gray-50"
          @click="filtreAlerte = filtreAlerte === 'normal' ? '' : 'normal'">
          <p class="text-lg font-semibold text-gray-900">{{ nbNormaux }}</p>
          <p class="text-xs text-gray-500">Normaux</p>
        </div>

      </div>

      <!-- FILTRES -->
      <div class="bg-white rounded-xl border border-gray-200 p-3">
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
              placeholder="Rechercher par produit ou entrepôt..."
              class="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-300"
            />
          </div>
          <select v-model="filtreAlerte"
            class="w-full sm:w-44 text-sm border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-300">
            <option value="">Tous les statuts</option>
            <option value="faible">Stock faible</option>
            <option value="sursock">Sur-stock</option>
            <option value="normal">Normal</option>
          </select>
        </div>
      </div>

      <!-- ERREUR -->
      <div v-if="erreur" class="rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm p-3">{{ erreur }}</div>

      <!-- TABLEAU -->
      <StockTableComponent
        :stocks="stocksPagines"
        :isLoading="isLoading"
        :pageCourante="pageCourante"
        :totalPages="totalPages"
        :parPage="parPage"
        :totalFiltres="stocksFiltres.length"
        :peut-configurer-seuils="peutConfigurerSeuils"
        @voir-mouvements="ouvrirMouvements"
        @configurer-seuils="ouvrirSeuils"
        @page-precedente="pageCourante--"
        @page-suivante="pageCourante++"
        @changer-page="pageCourante = $event"
      />

    </div>

    <StockMouvementsComponent
      :visible="showMouvements"
      :stock="stockMouvements"
      @fermer="showMouvements = false"
    />

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
