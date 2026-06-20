<script setup>
// ============================================================
// TABLEAU DE BORD
//
// Les données sont filtrées côté backend selon le rôle :
//   - ADMIN          → toutes les données (tous les entrepôts)
//   - autres rôles   → uniquement leurs entrepôts assignés
// ============================================================

import { ref, computed, onMounted } from 'vue'
import AppLayout       from '@/layout/AppLayout.vue'
import dashboardService from '@/services/dashboardService'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const data      = ref(null)
const isLoading = ref(false)

onMounted(async () => {
  isLoading.value = true
  try {
    data.value = await dashboardService.getDashboard()
  } catch {
    // non bloquant
  } finally {
    isLoading.value = false
  }
})

// -------------------------------------------------------
// KPI
// -------------------------------------------------------
const kpis = computed(() => {
  if (!data.value) return []
  const d = data.value
  const items = [
    {
      id: 1,
      label: 'Entrepôts actifs',
      value: d.entrepotsActifs,
      sub: `${d.entrepotsTotal} au total`,
      icon: 'warehouse',
      color: 'blue',
    },
    {
      id: 2,
      label: 'Références en stock',
      value: d.referencesEnStock,
      sub: `${d.totalUnitesDispo.toLocaleString('fr-FR')} unités disponibles`,
      icon: 'box',
      color: 'green',
    },
    {
      id: 3,
      label: 'Stocks en alerte',
      value: d.stocksEnAlerte,
      sub: d.stocksEnAlerte > 0 ? 'Stocks faibles détectés' : 'Aucune alerte',
      icon: 'alert',
      color: d.stocksEnAlerte > 0 ? 'red' : 'gray',
    },
    {
      id: 4,
      label: 'Entrées du mois',
      value: d.entreesduMois,
      sub: 'Bons de réception validés',
      icon: 'arrow-in',
      color: 'indigo',
    },
    {
      id: 5,
      label: 'Sorties du mois',
      value: d.sortiesDuMois,
      sub: 'Bons de sortie validés',
      icon: 'arrow-out',
      color: 'orange',
    },
  ]

  // KPI utilisateurs uniquement pour l'ADMIN
  if (d.utilisateursActifs >= 0) {
    items.push({
      id: 6,
      label: 'Utilisateurs actifs',
      value: d.utilisateursActifs,
      sub: 'Comptes actifs',
      icon: 'users',
      color: 'purple',
    })
  }

  return items
})

// -------------------------------------------------------
// HELPERS
// -------------------------------------------------------
const colorMap = {
  blue:   'bg-blue-600',
  green:  'bg-green-600',
  red:    'bg-red-600',
  gray:   'bg-gray-400',
  purple: 'bg-purple-600',
  indigo: 'bg-indigo-600',
  orange: 'bg-orange-500',
}

const typeConfig = {
  ENTREE:           { label: 'Entrée',      class: 'bg-green-100 text-green-700'   },
  SORTIE:           { label: 'Sortie',      class: 'bg-red-100 text-red-700'       },
  TRANSFERT_SORTIE: { label: 'Transfert →', class: 'bg-blue-100 text-blue-700'     },
  TRANSFERT_ENTREE: { label: '← Transfert', class: 'bg-blue-100 text-blue-700'     },
  AJUSTEMENT:       { label: 'Ajustement',  class: 'bg-yellow-100 text-yellow-700' },
}

const getTypeConfig = (type) =>
  typeConfig[type] ?? { label: type, class: 'bg-gray-100 text-gray-700' }

const formatDate = (d) => d
  ? new Date(d).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' })
  : '—'

const today = new Date().toLocaleDateString('fr-FR', {
  weekday: 'long', day: 'numeric', month: 'long', year: 'numeric',
})
</script>

<template>
  <AppLayout>
    <div class="space-y-6">

      <!-- EN-TÊTE -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="hidden md:block text-2xl font-bold text-gray-900">Tableau de bord</h1>
          <p class="hidden md:block text-sm text-gray-500 mt-1">
            Voici un aperçu de votre activité en temps réel.
            <span v-if="data?.filtrePeriemtre"
              class="ml-1 inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-700">
              Mes entrepôts
            </span>
          </p>
        </div>
        <span class="hidden sm:block text-sm text-gray-400 capitalize">{{ today }}</span>
      </div>

      <!-- LOADING -->
      <div v-if="isLoading" class="grid grid-cols-2 xl:grid-cols-4 gap-3 md:gap-4">
        <div v-for="i in 4" :key="i" class="card p-5 h-24 animate-pulse bg-gray-50"/>
      </div>

      <!-- KPI -->
      <div v-else class="grid grid-cols-2 xl:grid-cols-4 gap-3 md:gap-4">
        <div
          v-for="kpi in kpis"
          :key="kpi.id"
          class="card p-3 md:p-5 flex items-start gap-3 md:gap-4 hover:shadow-md transition-shadow duration-200"
        >
          <div :class="['w-10 h-10 md:w-12 md:h-12 rounded-xl flex items-center justify-center flex-shrink-0', colorMap[kpi.color]]">
            <svg class="w-5 h-5 md:w-6 md:h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path v-if="kpi.icon === 'warehouse'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
              <path v-else-if="kpi.icon === 'box'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
              <path v-else-if="kpi.icon === 'alert'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
              <path v-else-if="kpi.icon === 'arrow-in'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"/>
              <path v-else-if="kpi.icon === 'arrow-out'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
              <path v-else-if="kpi.icon === 'users'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/>
            </svg>
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-xl md:text-2xl font-bold text-gray-900">{{ kpi.value }}</p>
            <p class="text-xs md:text-sm text-gray-500 leading-snug">{{ kpi.label }}</p>
            <p class="text-xs mt-1 text-gray-400 font-medium leading-snug">{{ kpi.sub }}</p>
          </div>
        </div>
      </div>

      <!-- MESSAGE si aucun entrepôt assigné (non-ADMIN sans périmètre) -->
      <div
        v-if="!isLoading && data?.filtrePeriemtre && data?.entrepotsTotal === 0"
        class="card p-8 text-center text-gray-400"
      >
        <svg class="w-10 h-10 mx-auto mb-3 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
            d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5"/>
        </svg>
        <p class="text-sm font-medium">Aucun entrepôt assigné</p>
        <p class="text-xs mt-1">Contactez un administrateur pour être assigné à un entrepôt.</p>
      </div>

      <!-- MOUVEMENTS + ALERTES -->
      <div v-else-if="!isLoading" class="grid grid-cols-1 lg:grid-cols-3 gap-6">

        <!-- Derniers mouvements (2/3) -->
        <div class="card lg:col-span-2">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-base font-semibold text-gray-900">Derniers mouvements</h3>
            <RouterLink :to="{ name: 'stocks' }" class="text-sm text-blue-600 hover:text-blue-700 font-medium">
              Voir tout →
            </RouterLink>
          </div>

          <div v-if="!data?.derniersMouvements?.length"
            class="py-8 text-center text-gray-400 text-sm">
            Aucun mouvement enregistré pour l'instant.
          </div>

          <div v-else class="overflow-x-auto">
            <table class="w-full">
              <thead>
                <tr class="border-b border-gray-100">
                  <th class="table-header pl-0">Type</th>
                  <th class="table-header">Produit</th>
                  <th class="table-header">Qté</th>
                  <th class="table-header">Entrepôt</th>
                  <th class="table-header">Date</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-50">
                <tr v-for="mvt in data.derniersMouvements" :key="mvt.id"
                  class="hover:bg-gray-50 transition-colors">
                  <td class="table-cell pl-0">
                    <span :class="['inline-flex items-center px-2 py-0.5 rounded text-xs font-medium',
                      getTypeConfig(mvt.type).class]">
                      {{ getTypeConfig(mvt.type).label }}
                    </span>
                  </td>
                  <td class="table-cell font-medium text-gray-900">{{ mvt.produitNom }}</td>
                  <td class="table-cell text-gray-600">{{ mvt.quantite }}</td>
                  <td class="table-cell text-gray-500">{{ mvt.entrepotNom }}</td>
                  <td class="table-cell text-gray-400">{{ formatDate(mvt.dateCreation) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Stocks en alerte (1/3) -->
        <div class="card">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-base font-semibold text-gray-900">Stocks en alerte</h3>
            <RouterLink :to="{ name: 'stocks' }" class="text-sm text-blue-600 hover:text-blue-700 font-medium">
              Voir tout →
            </RouterLink>
          </div>

          <div v-if="!data?.alertesRecentes?.length"
            class="py-8 text-center text-gray-400 text-sm">
            Aucun stock en alerte.
          </div>

          <div v-else class="space-y-2">
            <div
              v-for="alerte in data.alertesRecentes"
              :key="alerte.stockId"
              class="flex items-start gap-3 p-3 rounded-lg bg-red-50"
            >
              <svg class="w-4 h-4 text-red-500 flex-shrink-0 mt-0.5"
                fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
              </svg>
              <div class="min-w-0">
                <p class="text-xs font-medium text-red-800 truncate">{{ alerte.produitNom }}</p>
                <p class="text-xs text-red-600 mt-0.5">
                  {{ alerte.quantiteDisponible }} restant{{ alerte.quantiteDisponible > 1 ? 's' : '' }}
                  <span v-if="alerte.stockMinimum"> (min : {{ alerte.stockMinimum }})</span>
                  — {{ alerte.entrepotNom }}
                </p>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  </AppLayout>
</template>
