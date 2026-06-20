<script setup>
import { computed, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const route     = useRoute()
const router    = useRouter()
const authStore = useAuthStore()

const logout = () => {
  authStore.logout()
  router.push({ name: 'login' })
}

const rolesLabels = {
  ADMIN:        'Administrateur',
  GESTIONNAIRE: 'Gestionnaire',
  MAGASINIER:   'Magasinier',
  AUDITEUR:     'Auditeur',
}

const props = defineProps({
  collapsed: { type: Boolean, default: false },
})

const menuItems = [
  { section: null,             label: 'Tableau de bord', icon: 'dashboard',    to: 'tableau-de-bord', roles: null },
  { section: 'Organisation',   label: 'Entrepôts',       icon: 'warehouse',    to: 'entrepots',       roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER', 'AUDITEUR'] },
  { section: 'Organisation',   label: 'Zones',           icon: 'zones',        to: 'zones',           roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER', 'AUDITEUR'] },
  { section: 'Organisation',   label: 'Emplacements',    icon: 'emplacements', to: 'emplacements',    roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER'] },
  { section: 'Catalogue',      label: 'Produits',        icon: 'produits',     to: 'produits',        roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER', 'AUDITEUR'] },
  { section: 'Catalogue',      label: 'Catégories',      icon: 'categories',   to: 'categories',      roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER', 'AUDITEUR'] },
  { section: 'Catalogue',      label: 'Fournisseurs',    icon: 'fournisseurs', to: 'fournisseurs',    roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER', 'AUDITEUR'] },
  { section: 'Mouvements',     label: 'Stocks',          icon: 'stocks',       to: 'stocks',          roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER', 'AUDITEUR'] },
  { section: 'Mouvements',     label: 'Entrées',         icon: 'entrees',      to: 'entrees',         roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER'] },
  { section: 'Mouvements',     label: 'Sorties',         icon: 'sorties',      to: 'sorties',         roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER'] },
  { section: 'Mouvements',     label: 'Transferts',      icon: 'transferts',   to: 'transferts',      roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER'] },
  { section: 'Gestion',        label: 'Commandes',       icon: 'commandes',    to: 'commandes',       roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER'] },
  { section: 'Gestion',        label: 'Inventaires',     icon: 'inventaires',  to: 'inventaires',     roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER'] },
  { section: 'Gestion',        label: 'Alertes',         icon: 'alertes',      to: 'alertes',         roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER', 'AUDITEUR'] },
  { section: 'Gestion',        label: 'Rapports',        icon: 'rapports',     to: 'rapports',        roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER', 'AUDITEUR'] },
  { section: 'Administration', label: 'Utilisateurs',    icon: 'utilisateurs', to: 'utilisateurs',    roles: ['ADMIN'] },
]

// Filtre les items selon le rôle de l'utilisateur connecté
const userRole = authStore.role
const menuItemsFiltres = computed(() =>
  menuItems.filter(item => !item.roles || item.roles.includes(userRole))
)

const sections = computed(() => {
  const seen = new Set()
  return menuItemsFiltres.value
    .filter(i => i.section && !seen.has(i.section) && seen.add(i.section))
    .map(i => i.section)
})

const getItemsBySection = (section) => menuItemsFiltres.value.filter(i => i.section === section)
const topItems = computed(() => menuItemsFiltres.value.filter(i => !i.section))
const isActive = (name) => route.name === name

// Tooltip via Teleport — position fixed pour échapper au overflow de la nav
const tooltip = ref({ visible: false, label: '', top: 0 })

const showTooltip = (event, label) => {
  if (!props.collapsed) return
  const rect = event.currentTarget.getBoundingClientRect()
  tooltip.value = {
    visible: true,
    label,
    top: rect.top + rect.height / 2,
  }
}
const hideTooltip = () => { tooltip.value.visible = false }

const iconPaths = {
  dashboard:    "M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6",
  warehouse:    "M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4",
  zones:        "M4 5a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1V5zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1V5zM4 15a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1v-4zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z",
  produits:     "M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4",
  categories:   "M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z",
  fournisseurs: "M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z",
  stocks:       "M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z",
  entrees:      "M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1",
  sorties:      "M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1",
  transferts:   "M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4",
  commandes:    "M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01",
  inventaires:  "M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4",
  alertes:      "M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9",
  rapports:     "M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z",
  utilisateurs: "M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z",
}

const emplacementsPath1 = "M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
const emplacementsPath2 = "M15 11a3 3 0 11-6 0 3 3 0 016 0z"
</script>

<template>
  <!-- Tooltip global via Teleport — position:fixed pour échapper aux overflow -->
  <Teleport to="body">
    <Transition
      enter-active-class="transition-opacity duration-150"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-100"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="tooltip.visible"
        class="fixed z-[9999] pointer-events-none"
        :style="{ top: tooltip.top + 'px', left: '72px', transform: 'translateY(-50%)' }"
      >
        <div class="px-3 py-1.5 bg-slate-900 text-white text-xs rounded-lg whitespace-nowrap shadow-lg border border-slate-700">
          {{ tooltip.label }}
          <!-- Petite flèche gauche -->
          <div class="absolute right-full top-1/2 -translate-y-1/2 border-4 border-transparent border-r-slate-900"></div>
        </div>
      </div>
    </Transition>
  </Teleport>

  <aside
    :class="[
      'hidden lg:flex flex-col fixed inset-y-0 left-0 z-40',
      'bg-slate-800 text-white transition-all duration-300 ease-in-out',
      collapsed ? 'w-16' : 'w-64',
    ]"
  >
    <!-- EN-TÊTE : Logo -->
    <div class="flex items-center gap-3 px-4 border-b border-slate-700 flex-shrink-0 h-16">
      <div class="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center flex-shrink-0">
        <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
        </svg>
      </div>
      <div v-show="!collapsed" class="overflow-hidden whitespace-nowrap min-w-0">
        <h1 class="text-white font-bold text-base leading-tight">StockMaster</h1>
        <p class="text-slate-400 text-xs">Gestion des stocks</p>
      </div>
    </div>

    <!-- NAVIGATION -->
    <nav class="flex-1 overflow-y-scroll overflow-x-hidden py-2 px-2 space-y-0.5 scrollbar-hide">

      <!-- Items sans section -->
      <template v-for="item in topItems" :key="item.to">
        <RouterLink
          :to="{ name: item.to }"
          active-class=""
          :class="[
            'flex items-center gap-3 rounded-lg text-sm font-medium transition-colors duration-150',
            collapsed ? 'justify-center px-0 py-2.5' : 'px-3 py-2',
            isActive(item.to) ? 'bg-blue-600 text-white' : 'text-slate-300 hover:bg-slate-700 hover:text-white',
          ]"
          @mouseenter="showTooltip($event, item.label)"
          @mouseleave="hideTooltip"
        >
          <svg class="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="iconPaths[item.icon]" />
          </svg>
          <span v-show="!collapsed" class="truncate">{{ item.label }}</span>
        </RouterLink>
      </template>

      <div v-if="topItems.length" class="pt-1"></div>

      <!-- Sections groupées -->
      <template v-for="section in sections" :key="section">
        <p v-show="!collapsed"
          class="px-3 pt-3 pb-0.5 text-xs font-semibold text-slate-500 uppercase tracking-wider whitespace-nowrap">
          {{ section }}
        </p>
        <div v-show="collapsed" class="border-t border-slate-700 my-2"></div>

        <template v-for="item in getItemsBySection(section)" :key="item.to">
          <RouterLink
            :to="{ name: item.to }"
            active-class=""
            :class="[
              'flex items-center gap-3 rounded-lg text-sm font-medium transition-colors duration-150',
              collapsed ? 'justify-center px-0 py-2.5' : 'px-3 py-2',
              isActive(item.to) ? 'bg-blue-600 text-white' : 'text-slate-300 hover:bg-slate-700 hover:text-white',
            ]"
            @mouseenter="showTooltip($event, item.label)"
            @mouseleave="hideTooltip"
          >
            <svg v-if="item.icon === 'emplacements'"
              class="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="emplacementsPath1" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="emplacementsPath2" />
            </svg>
            <svg v-else class="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="iconPaths[item.icon]" />
            </svg>
            <span v-show="!collapsed" class="truncate">{{ item.label }}</span>
          </RouterLink>
        </template>
      </template>
    </nav>

    <!-- PIED : bouton déconnexion -->
    <div class="border-t border-slate-700 px-3 py-4 flex-shrink-0">
      <button
        @click="logout"
        :class="[
          'flex items-center gap-3 rounded-lg text-sm font-medium transition-colors duration-150 w-full',
          collapsed ? 'justify-center px-0 py-2.5' : 'px-3 py-2.5',
          'text-slate-300 hover:bg-red-600/20 hover:text-red-400',
        ]"
        @mouseenter="showTooltip($event, 'Déconnexion')"
        @mouseleave="hideTooltip"
      >
        <svg class="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
        </svg>
        <span v-show="!collapsed">Déconnexion</span>
      </button>
    </div>
  </aside>
</template>
