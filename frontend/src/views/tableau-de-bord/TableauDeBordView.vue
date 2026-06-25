<script setup>
import { ref, computed, onMounted } from 'vue'
import AppLayout        from '@/layout/AppLayout.vue'
import AppChart         from '@/components/commun/AppChart.vue'
import dashboardService from '@/services/dashboardService'

const data      = ref(null)
const isLoading = ref(false)

onMounted(async () => {
  isLoading.value = true
  try { data.value = await dashboardService.getDashboard() }
  catch { /* non bloquant */ }
  finally { isLoading.value = false }
})

// -------------------------------------------------------
// KPI
// -------------------------------------------------------
const kpis = computed(() => {
  if (!data.value) return []
  const d = data.value
  const items = [
    { id: 1, label: 'Entrepôts actifs',     value: d.entrepotsActifs,  sub: `${d.entrepotsTotal} au total`,                                       icon: 'warehouse', gradient: 'from-blue-500 to-blue-700',    shadow: 'shadow-blue-500/25' },
    { id: 2, label: 'Références en stock',  value: d.referencesEnStock, sub: `${d.totalUnitesDispo.toLocaleString('fr-FR')} unités disponibles`,  icon: 'box',       gradient: 'from-emerald-500 to-emerald-700', shadow: 'shadow-emerald-500/25' },
    { id: 3, label: 'Stocks en alerte',     value: d.stocksEnAlerte,   sub: d.stocksEnAlerte > 0 ? 'Stocks faibles détectés' : 'Aucune alerte',  icon: 'alert',     gradient: d.stocksEnAlerte > 0 ? 'from-red-500 to-red-700' : 'from-gray-400 to-gray-500', shadow: d.stocksEnAlerte > 0 ? 'shadow-red-500/25' : 'shadow-gray-400/20' },
    { id: 4, label: 'Entrées du mois',      value: d.entreesduMois,    sub: 'Bons de réception validés',                                          icon: 'arrow-in',  gradient: 'from-indigo-500 to-indigo-700',  shadow: 'shadow-indigo-500/25' },
    { id: 5, label: 'Sorties du mois',      value: d.sortiesDuMois,    sub: 'Bons de sortie validés',                                             icon: 'arrow-out', gradient: 'from-orange-500 to-orange-600',  shadow: 'shadow-orange-500/25' },
  ]
  if (d.utilisateursActifs >= 0) {
    items.push({ id: 6, label: 'Utilisateurs actifs', value: d.utilisateursActifs, sub: 'Comptes actifs', icon: 'users', gradient: 'from-purple-500 to-purple-700', shadow: 'shadow-purple-500/25' })
  }
  return items
})

// -------------------------------------------------------
// GRAPHIQUE 1 — Évolution du stock (Line)
// -------------------------------------------------------
const lineData = computed(() => {
  const pts = data.value?.evolutionStock ?? []
  const labels = pts.map(p => {
    const d = new Date(p.date)
    return d.toLocaleDateString('fr-FR', { day: '2-digit', month: 'short' })
  })
  return {
    labels,
    datasets: [
      {
        label: 'Entrées',
        data: pts.map(p => p.entrees),
        borderColor: '#10b981',
        backgroundColor: 'rgba(16,185,129,0.1)',
        fill: true,
        tension: 0.4,
        pointRadius: 3,
        pointHoverRadius: 5,
      },
      {
        label: 'Sorties',
        data: pts.map(p => p.sorties),
        borderColor: '#f97316',
        backgroundColor: 'rgba(249,115,22,0.08)',
        fill: true,
        tension: 0.4,
        pointRadius: 3,
        pointHoverRadius: 5,
      },
    ],
  }
})

const lineOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: 'top', labels: { boxWidth: 12, font: { size: 12 } } },
    tooltip: { mode: 'index', intersect: false },
  },
  scales: {
    x: { grid: { display: false }, ticks: { maxTicksLimit: 10, font: { size: 11 } } },
    y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' }, ticks: { font: { size: 11 } } },
  },
}

// -------------------------------------------------------
// GRAPHIQUE 2 — Rotation des produits (Bar)
// -------------------------------------------------------
const barData = computed(() => {
  const pts = data.value?.rotationProduits ?? []
  return {
    labels: pts.map(p => p.produitNom),
    datasets: [{
      label: 'Mouvements (30j)',
      data: pts.map(p => p.totalMouvements),
      backgroundColor: ['#3b82f6','#10b981','#f97316','#8b5cf6','#f43f5e'],
      borderRadius: 6,
      borderSkipped: false,
    }],
  }
})

const barOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: { mode: 'index', intersect: false },
  },
  scales: {
    x: { grid: { display: false }, ticks: { font: { size: 11 } } },
    y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' }, ticks: { font: { size: 11 } } },
  },
}

// -------------------------------------------------------
// HELPERS
// -------------------------------------------------------
const typeConfig = {
  ENTREE:           { label: 'Entrée',      dot: 'bg-emerald-500', class: 'bg-emerald-50 text-emerald-700'  },
  SORTIE:           { label: 'Sortie',      dot: 'bg-red-500',     class: 'bg-red-50 text-red-700'          },
  TRANSFERT_SORTIE: { label: 'Transfert →', dot: 'bg-blue-500',    class: 'bg-blue-50 text-blue-700'        },
  TRANSFERT_ENTREE: { label: '← Transfert', dot: 'bg-blue-500',    class: 'bg-blue-50 text-blue-700'        },
  AJUSTEMENT:       { label: 'Ajustement',  dot: 'bg-yellow-500',  class: 'bg-yellow-50 text-yellow-700'    },
}
const getTypeConfig = (type) => typeConfig[type] ?? { label: type, dot: 'bg-gray-400', class: 'bg-gray-50 text-gray-700' }
const formatDate = (d) => d
  ? new Date(d).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' })
  : '—'
const today = new Date().toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })
</script>

<template>
  <AppLayout>
    <div class="space-y-6">

      <!-- EN-TÊTE -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="hidden md:block text-2xl font-bold text-gray-900">Tableau de bord</h1>
          <p class="hidden md:block text-sm text-gray-400 mt-0.5">
            Aperçu de votre activité en temps réel
            <span v-if="data?.filtrePeriemtre"
              class="ml-2 inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-700">
              <span class="w-1.5 h-1.5 rounded-full bg-blue-500 inline-block"></span>
              Mes entrepôts
            </span>
          </p>
        </div>
        <span class="hidden sm:block text-sm text-gray-400 capitalize bg-white border border-gray-100 rounded-xl px-4 py-2 shadow-sm">
          {{ today }}
        </span>
      </div>

      <!-- LOADING -->
      <div v-if="isLoading" class="grid grid-cols-2 md:grid-cols-3 gap-4">
        <div v-for="i in 6" :key="i" class="rounded-2xl h-36 animate-pulse bg-gray-200/60"/>
      </div>

      <!-- KPI 3+3 -->
      <div v-else class="grid grid-cols-2 md:grid-cols-3 gap-4">
        <div
          v-for="kpi in kpis" :key="kpi.id"
          class="card p-0 overflow-hidden hover:shadow-lg hover:-translate-y-1 transition-all duration-200 cursor-default"
        >
          <!-- Zone colorée haute avec icône -->
          <div :class="['bg-gradient-to-br px-6 pt-6 pb-4 flex items-center justify-between', kpi.gradient]">
            <div class="bg-white/20 rounded-2xl p-3">
              <svg class="w-7 h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path v-if="kpi.icon === 'warehouse'" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8"
                  d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
                <path v-else-if="kpi.icon === 'box'" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8"
                  d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
                <path v-else-if="kpi.icon === 'alert'" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8"
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                <path v-else-if="kpi.icon === 'arrow-in'" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8"
                  d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"/>
                <path v-else-if="kpi.icon === 'arrow-out'" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8"
                  d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
                <path v-else-if="kpi.icon === 'users'" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8"
                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/>
              </svg>
            </div>
            <!-- Valeur principale dans la zone colorée -->
            <p class="text-4xl font-extrabold text-white tracking-tight">{{ kpi.value }}</p>
          </div>
          <!-- Zone blanche basse avec label + sous-texte -->
          <div class="px-6 py-4">
            <p class="text-sm font-semibold text-gray-800">{{ kpi.label }}</p>
            <p class="text-xs text-gray-400 mt-0.5 leading-snug">{{ kpi.sub }}</p>
          </div>
        </div>
      </div>

      <!-- MESSAGE aucun entrepôt -->
      <div
        v-if="!isLoading && data?.filtrePeriemtre && data?.entrepotsTotal === 0"
        class="card p-10 text-center text-gray-400"
      >
        <div class="w-14 h-14 rounded-2xl bg-gray-100 flex items-center justify-center mx-auto mb-4">
          <svg class="w-7 h-7 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
              d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5"/>
          </svg>
        </div>
        <p class="text-sm font-semibold text-gray-600">Aucun entrepôt assigné</p>
        <p class="text-xs mt-1">Contactez un administrateur pour être assigné à un entrepôt.</p>
      </div>

      <!-- MOUVEMENTS + ALERTES -->
      <div v-else-if="!isLoading" class="grid grid-cols-1 lg:grid-cols-3 gap-6">

        <!-- Derniers mouvements (2/3) -->
        <div class="card lg:col-span-2">
          <div class="flex items-center justify-between mb-5">
            <div>
              <h3 class="text-base font-semibold text-gray-900">Derniers mouvements</h3>
              <p class="text-xs text-gray-400 mt-0.5">5 dernières opérations</p>
            </div>
            <RouterLink :to="{ name: 'stocks' }" class="text-sm text-blue-600 hover:text-blue-700 font-medium flex items-center gap-1">
              Voir tout
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
              </svg>
            </RouterLink>
          </div>

          <div v-if="!data?.derniersMouvements?.length" class="py-10 text-center text-gray-300 text-sm">
            Aucun mouvement enregistré pour l'instant.
          </div>

          <div v-else class="space-y-2">
            <div v-for="m in data.derniersMouvements" :key="m.id"
              class="flex items-center gap-3 p-3 rounded-xl hover:bg-gray-50 transition-colors">
              <!-- Dot type -->
              <div :class="['w-2 h-2 rounded-full flex-shrink-0', getTypeConfig(m.type).dot]"></div>
              <!-- Badge type -->
              <span :class="['inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium flex-shrink-0', getTypeConfig(m.type).class]">
                {{ getTypeConfig(m.type).label }}
              </span>
              <!-- Produit -->
              <span class="flex-1 text-sm font-medium text-gray-800 truncate">{{ m.produitNom }}</span>
              <!-- Entrepôt -->
              <span class="text-xs text-gray-400 hidden sm:block truncate max-w-[120px]">{{ m.entrepotNom }}</span>
              <!-- Quantité -->
              <span class="text-sm font-semibold text-gray-700 flex-shrink-0">{{ m.quantite }}</span>
              <!-- Date -->
              <span class="text-xs text-gray-400 flex-shrink-0 hidden md:block">{{ formatDate(m.dateCreation) }}</span>
            </div>
          </div>
        </div>

        <!-- Alertes (1/3) -->
        <div class="card">
          <div class="flex items-center justify-between mb-5">
            <div>
              <h3 class="text-base font-semibold text-gray-900">Stocks en alerte</h3>
              <p class="text-xs text-gray-400 mt-0.5">Seuils minimums dépassés</p>
            </div>
            <span v-if="data?.alertesRecentes?.length"
              class="w-6 h-6 rounded-full bg-red-100 text-red-600 text-xs font-bold flex items-center justify-center">
              {{ data.alertesRecentes.length }}
            </span>
          </div>

          <div v-if="!data?.alertesRecentes?.length" class="py-10 text-center text-gray-300 text-sm">
            <svg class="w-8 h-8 mx-auto mb-2 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
            Tous les stocks sont normaux
          </div>

          <div v-else class="space-y-2">
            <div v-for="a in data.alertesRecentes" :key="a.stockId"
              class="p-3 rounded-xl bg-red-50 border border-red-100">
              <div class="flex items-start justify-between gap-2">
                <div class="min-w-0">
                  <p class="text-sm font-medium text-red-800 truncate">{{ a.produitNom }}</p>
                  <p class="text-xs text-red-500 mt-0.5">{{ a.entrepotNom }}</p>
                </div>
                <div class="text-right flex-shrink-0">
                  <p class="text-sm font-bold text-red-700">{{ a.quantiteDisponible }}</p>
                  <p class="text-xs text-red-400">/ {{ a.stockMinimum }} min</p>
                </div>
              </div>
              <!-- Barre de niveau -->
              <div class="mt-2 h-1.5 bg-red-200 rounded-full overflow-hidden">
                <div class="h-full bg-red-500 rounded-full transition-all"
                  :style="{ width: Math.min(100, Math.round((a.quantiteDisponible / a.stockMinimum) * 100)) + '%' }">
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- GRAPHIQUES -->
      <div v-if="!isLoading && data" class="grid grid-cols-1 lg:grid-cols-5 gap-6">

        <!-- Évolution du stock (3/5) -->
        <div class="card lg:col-span-3">
          <div class="mb-4">
            <h3 class="text-base font-semibold text-gray-900">Évolution du stock</h3>
            <p class="text-xs text-gray-400 mt-0.5">Entrées et sorties sur les 30 derniers jours</p>
          </div>
          <div class="h-56">
            <AppChart type="line" :data="lineData" :options="lineOptions" />
          </div>
        </div>

        <!-- Rotation produits (2/5) -->
        <div class="card lg:col-span-2">
          <div class="mb-4">
            <h3 class="text-base font-semibold text-gray-900">Top produits actifs</h3>
            <p class="text-xs text-gray-400 mt-0.5">Produits les plus mouvementés (30j)</p>
          </div>
          <div class="h-56">
            <AppChart type="bar" :data="barData" :options="barOptions" />
          </div>
        </div>
      </div>

    </div>
  </AppLayout>
</template>
