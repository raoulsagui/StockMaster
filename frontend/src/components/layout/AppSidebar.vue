<script setup>
// ============================================================
// SIDEBAR — Navigation latérale de l'application
//
// Ce composant affiche le menu de navigation à gauche.
// On utilise useRoute() pour détecter la route active
// et mettre en évidence le lien correspondant.
// ============================================================

import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const route     = useRoute()
const authStore = useAuthStore()

const rolesLabels = {
  ADMIN:        'Administrateur',
  GESTIONNAIRE: 'Gestionnaire',
  MAGASINIER:   'Magasinier',
  AUDITEUR:     'Auditeur',
}

// Définition des props :
// - isOpen : contrôlé par le parent (AppLayout) pour
//   afficher/masquer la sidebar sur mobile
defineProps({
  isOpen: {
    type: Boolean,
    default: true,
  },
})

// -------------------------------------------------------
// STRUCTURE DU MENU DE NAVIGATION
// Chaque entrée contient :
//   - label   : texte affiché
//   - icon    : emoji ou icône SVG (on utilise des SVG inline)
//   - to      : nom de la route Vue Router
//   - section : regroupement visuel dans le menu
// -------------------------------------------------------
const menuItems = [
  // --- Tableau de bord ---
  {
    section: null, // pas de section = affiché directement
    label: 'Tableau de bord',
    icon: 'dashboard',
    to: 'tableau-de-bord',
  },

  // --- Section : Organisation ---
  {
    section: 'Organisation',
    label: 'Entrepôts',
    icon: 'warehouse',
    to: 'entrepots',
  },
  {
    section: 'Organisation',
    label: 'Zones',
    icon: 'zones',
    to: 'zones',
  },
  {
    section: 'Organisation',
    label: 'Emplacements',
    icon: 'emplacements',
    to: 'emplacements',
  },

  // --- Section : Catalogue ---
  {
    section: 'Catalogue',
    label: 'Produits',
    icon: 'produits',
    to: 'produits',
  },
  {
    section: 'Catalogue',
    label: 'Catégories',
    icon: 'categories',
    to: 'categories',
  },
  {
    section: 'Catalogue',
    label: 'Fournisseurs',
    icon: 'fournisseurs',
    to: 'fournisseurs',
  },

  // --- Section : Mouvements ---
  {
    section: 'Mouvements',
    label: 'Stocks',
    icon: 'stocks',
    to: 'stocks',
  },
  {
    section: 'Mouvements',
    label: 'Entrées',
    icon: 'entrees',
    to: 'entrees',
  },
  {
    section: 'Mouvements',
    label: 'Sorties',
    icon: 'sorties',
    to: 'sorties',
  },
  {
    section: 'Mouvements',
    label: 'Transferts',
    icon: 'transferts',
    to: 'transferts',
  },

  // --- Section : Gestion ---
  {
    section: 'Gestion',
    label: 'Commandes',
    icon: 'commandes',
    to: 'commandes',
  },
  {
    section: 'Gestion',
    label: 'Inventaires',
    icon: 'inventaires',
    to: 'inventaires',
  },
  {
    section: 'Gestion',
    label: 'Alertes',
    icon: 'alertes',
    to: 'alertes',
  },
  {
    section: 'Gestion',
    label: 'Rapports',
    icon: 'rapports',
    to: 'rapports',
  },

  // --- Section : Administration ---
  {
    section: 'Administration',
    label: 'Utilisateurs',
    icon: 'utilisateurs',
    to: 'utilisateurs',
  },
]

// On calcule les sections uniques pour construire le menu groupé.
// computed() met en cache le résultat et ne recalcule que si
// menuItems change (ici jamais, mais c'est la bonne pratique).
const sections = computed(() => {
  // Récupère les sections sans doublons, en gardant l'ordre
  const seen = new Set()
  const result = []
  for (const item of menuItems) {
    if (item.section && !seen.has(item.section)) {
      seen.add(item.section)
      result.push(item.section)
    }
  }
  return result
})

// Retourne les items d'une section donnée
const getItemsBySection = (section) =>
  menuItems.filter((item) => item.section === section)

// Items sans section (= tableau de bord)
const topItems = computed(() => menuItems.filter((item) => !item.section))

// Vérifie si un lien est actif en comparant son "to" avec le nom de la route courante
const isActive = (routeName) => route.name === routeName
</script>

<template>
  <!--
    SIDEBAR PRINCIPALE
    - Sur desktop : toujours visible (translate-x-0)
    - Sur mobile : cachée par défaut, affichée si isOpen = true
    La transition CSS permet un glissement fluide.
  -->
  <aside
    :class="[
      'fixed inset-y-0 left-0 z-40 w-64 flex flex-col',
      'bg-slate-800 text-white',
      'transition-transform duration-300 ease-in-out',
      // Sur mobile : décale la sidebar hors écran si fermée
      isOpen ? 'translate-x-0' : '-translate-x-full',
      // Sur desktop (lg+) : toujours visible
      'lg:translate-x-0',
    ]"
  >
    <!-- ===== EN-TÊTE DE LA SIDEBAR : Logo + Nom ===== -->
    <div class="flex items-center gap-3 px-6 py-5 border-b border-slate-700">
      <!-- Icône du logo -->
      <div class="w-9 h-9 bg-blue-600 rounded-lg flex items-center justify-center flex-shrink-0">
        <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
        </svg>
      </div>
      <!-- Nom de l'application -->
      <div>
        <h1 class="text-white font-bold text-lg leading-tight">StockMaster</h1>
        <p class="text-slate-400 text-xs">Gestion des stocks</p>
      </div>
    </div>

    <!-- ===== NAVIGATION : Zone scrollable ===== -->
    <!-- overflow-y-auto : scroll si le menu dépasse la hauteur -->
    <nav class="flex-1 overflow-y-auto py-4 px-3 space-y-1">

      <!-- Items du haut (tableau de bord) — sans section -->
      <RouterLink
        v-for="item in topItems"
        :key="item.to"
        :to="{ name: item.to }"
        :class="[
          'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors duration-150',
          isActive(item.to)
            ? 'bg-blue-600 text-white'          // style lien actif
            : 'text-slate-300 hover:bg-slate-700 hover:text-white' // style lien inactif
        ]"
      >
        <!-- Icône dynamique selon le type -->
        <SidebarIcon :icon="item.icon" />
        {{ item.label }}
      </RouterLink>

      <!-- Séparation entre les items du haut et les sections -->
      <div v-if="topItems.length" class="pt-2"></div>

      <!-- Boucle sur chaque section du menu -->
      <div v-for="section in sections" :key="section">

        <!-- Titre de la section (non cliquable) -->
        <p class="px-3 pt-3 pb-1 text-xs font-semibold text-slate-500 uppercase tracking-wider">
          {{ section }}
        </p>

        <!-- Liens de la section -->
        <RouterLink
          v-for="item in getItemsBySection(section)"
          :key="item.to"
          :to="{ name: item.to }"
          :class="[
            'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors duration-150',
            isActive(item.to)
              ? 'bg-blue-600 text-white'
              : 'text-slate-300 hover:bg-slate-700 hover:text-white'
          ]"
        >
          <SidebarIcon :icon="item.icon" />
          {{ item.label }}
        </RouterLink>
      </div>
    </nav>

    <!-- ===== PIED DE SIDEBAR : Infos utilisateur connecté ===== -->
    <div class="border-t border-slate-700 px-4 py-4">
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center flex-shrink-0">
          <span class="text-white text-xs font-bold">{{ authStore.utilisateur ? `${authStore.utilisateur.prenom?.[0]}${authStore.utilisateur.nom?.[0]}`.toUpperCase() : '?' }}</span>
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-white truncate">{{ authStore.nomComplet }}</p>
          <p class="text-xs text-slate-400 truncate">{{ rolesLabels[authStore.role] ?? authStore.role }}</p>
        </div>
      </div>
    </div>
  </aside>
</template>

<script>
// -------------------------------------------------------
// COMPOSANT INLINE : SidebarIcon
// Centralise toutes les icônes SVG du menu.
// On passe le type via la prop "icon" et on affiche
// le bon SVG. Cela évite de répéter du SVG partout.
// -------------------------------------------------------
const SidebarIcon = {
  props: ['icon'],
  template: `
    <svg class="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <!-- Tableau de bord -->
      <template v-if="icon === 'dashboard'">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/>
      </template>
      <!-- Entrepôts -->
      <template v-else-if="icon === 'warehouse'">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
      </template>
      <!-- Zones -->
      <template v-else-if="icon === 'zones'">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M4 5a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1V5zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1V5zM4 15a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1v-4zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z"/>
      </template>
      <!-- Emplacements -->
      <template v-else-if="icon === 'emplacements'">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
      </template>
      <!-- Produits -->
      <template v-else-if="icon === 'produits'">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
      </template>
      <!-- Catégories -->
      <template v-else-if="icon === 'categories'">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/>
      </template>
      <!-- Fournisseurs -->
      <template v-else-if="icon === 'fournisseurs'">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/>
      </template>
      <!-- Stocks -->
      <template v-else-if="icon === 'stocks'">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
      </template>
      <!-- Entrées -->
      <template v-else-if="icon === 'entrees'">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"/>
      </template>
      <!-- Sorties -->
      <template v-else-if="icon === 'sorties'">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
      </template>
      <!-- Transferts -->
      <template v-else-if="icon === 'transferts'">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"/>
      </template>
      <!-- Commandes -->
      <template v-else-if="icon === 'commandes'">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"/>
      </template>
      <!-- Inventaires -->
      <template v-else-if="icon === 'inventaires'">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"/>
      </template>
      <!-- Alertes -->
      <template v-else-if="icon === 'alertes'">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
      </template>
      <!-- Rapports -->
      <template v-else-if="icon === 'rapports'">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
      </template>
      <!-- Utilisateurs -->
      <template v-else-if="icon === 'utilisateurs'">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/>
      </template>
      <!-- Icône par défaut si le type n'est pas reconnu -->
      <template v-else>
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M4 6h16M4 10h16M4 14h16M4 18h16"/>
      </template>
    </svg>
  `,
}
</script>
