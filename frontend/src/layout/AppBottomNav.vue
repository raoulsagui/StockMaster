<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route    = useRoute()
const isActive = (name) => route.name === name

// Menu "Plus" — panneau du bas
const showMore = ref(false)

const mainItems = [
  { label: 'Accueil',      to: 'tableau-de-bord', icon: 'dashboard'  },
  { label: 'Entrepôts',    to: 'entrepots',        icon: 'warehouse'  },
  { label: 'Produits',     to: 'produits',         icon: 'produits'   },
  { label: 'Stocks',       to: 'stocks',           icon: 'stocks'     },
  { label: 'Commandes',    to: 'commandes',        icon: 'commandes'  },
]

// Tous les autres liens dans le panneau "Plus"
const moreItems = [
  { label: 'Zones',        to: 'zones',        icon: 'zones'        },
  { label: 'Emplacements', to: 'emplacements', icon: 'emplacements' },
  { label: 'Catégories',   to: 'categories',   icon: 'categories'   },
  { label: 'Fournisseurs', to: 'fournisseurs',  icon: 'fournisseurs' },
  { label: 'Entrées',      to: 'entrees',       icon: 'entrees'      },
  { label: 'Sorties',      to: 'sorties',       icon: 'sorties'      },
  { label: 'Transferts',   to: 'transferts',    icon: 'transferts'   },
  { label: 'Inventaires',  to: 'inventaires',   icon: 'inventaires'  },
  { label: 'Alertes',      to: 'alertes',       icon: 'alertes'      },
  { label: 'Rapports',     to: 'rapports',      icon: 'rapports'     },
  { label: 'Utilisateurs', to: 'utilisateurs',  icon: 'utilisateurs' },
]

const iconPaths = {
  dashboard:    "M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6",
  warehouse:    "M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4",
  produits:     "M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4",
  commandes:    "M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01",
  stocks:       "M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z",
  zones:        "M4 5a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1V5zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1V5zM4 15a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1v-4zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z",
  categories:   "M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z",
  fournisseurs: "M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z",
  entrees:      "M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1",
  sorties:      "M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1",
  transferts:   "M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4",
  inventaires:  "M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4",
  alertes:      "M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9",
  rapports:     "M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z",
  utilisateurs: "M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z",
  emplacements1: "M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z",
  emplacements2: "M15 11a3 3 0 11-6 0 3 3 0 016 0z",
}
</script>

<template>
  <!-- Overlay -->
  <Transition
    enter-active-class="transition ease-out duration-200"
    enter-from-class="opacity-0"
    enter-to-class="opacity-100"
    leave-active-class="transition ease-in duration-150"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
  >
    <div v-if="showMore" class="lg:hidden fixed inset-0 z-40 bg-black/40" @click="showMore = false"></div>
  </Transition>

  <!-- Bottom sheet "Plus" -->
  <Transition
    enter-active-class="transition ease-out duration-300"
    enter-from-class="translate-y-full"
    enter-to-class="translate-y-0"
    leave-active-class="transition ease-in duration-200"
    leave-from-class="translate-y-0"
    leave-to-class="translate-y-full"
  >
    <div
      v-if="showMore"
      class="lg:hidden fixed bottom-16 inset-x-0 z-50 bg-white rounded-t-2xl shadow-2xl"
    >
      <!-- Handle -->
      <div class="flex justify-center pt-3 pb-1">
        <div class="w-10 h-1 bg-gray-200 rounded-full"></div>
      </div>

      <!-- Header -->
      <div class="flex items-center justify-between px-5 py-3 border-b border-gray-100">
        <p class="text-sm font-semibold text-gray-800">Plus</p>
        <button @click="showMore = false" class="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 transition-colors">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>
      </div>

      <!-- Items en grille 4 colonnes -->
      <div class="grid grid-cols-4 gap-1 px-4 py-4">
        <RouterLink
          v-for="item in moreItems"
          :key="item.to"
          :to="{ name: item.to }"
          @click="showMore = false"
          :class="[
            'flex flex-col items-center gap-1.5 p-3 rounded-xl transition-colors',
            isActive(item.to) ? 'bg-blue-50 text-blue-600' : 'text-gray-500 hover:bg-gray-50',
          ]"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <template v-if="item.icon === 'emplacements'">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="iconPaths.emplacements1"/>
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="iconPaths.emplacements2"/>
            </template>
            <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="iconPaths[item.icon]"/>
          </svg>
          <span class="text-xs font-medium text-center leading-tight">{{ item.label }}</span>
        </RouterLink>
      </div>
    </div>
  </Transition>

  <!-- BARRE DE NAVIGATION PRINCIPALE -->
  <nav class="lg:hidden fixed bottom-0 inset-x-0 z-50 bg-white border-t border-gray-200
              flex items-center justify-around h-16 safe-bottom">

    <!-- Liens principaux -->
    <RouterLink
      v-for="item in mainItems"
      :key="item.to"
      :to="{ name: item.to }"
      :class="[
        'flex flex-col items-center justify-center gap-0.5 flex-1 h-full transition-colors',
        isActive(item.to) ? 'text-blue-600' : 'text-gray-400',
      ]"
    >
      <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="iconPaths[item.icon]"/>
      </svg>
      <span class="text-xs font-medium">{{ item.label }}</span>
    </RouterLink>

    <!-- Bouton "Plus" -->
    <button
      @click="showMore = !showMore"
      :class="[
        'flex flex-col items-center justify-center gap-0.5 flex-1 h-full transition-colors',
        showMore ? 'text-blue-600' : 'text-gray-400',
      ]"
    >
      <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M5 12h.01M12 12h.01M19 12h.01M6 12a1 1 0 11-2 0 1 1 0 012 0zm7 0a1 1 0 11-2 0 1 1 0 012 0zm7 0a1 1 0 11-2 0 1 1 0 012 0z"/>
      </svg>
      <span class="text-xs font-medium">Plus</span>
    </button>
  </nav>
</template>
