<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const route     = useRoute()
const router    = useRouter()
const authStore = useAuthStore()

const props = defineProps({
  sidebarCollapsed: { type: Boolean, default: false },
})

const emit = defineEmits(['toggle-sidebar'])

const isProfileMenuOpen = ref(false)
const closeProfileMenu  = () => { isProfileMenuOpen.value = false }

const logout = () => {
  authStore.logout()
  router.push({ name: 'login' })
}

const initiales = computed(() => {
  const u = authStore.utilisateur
  if (!u) return '?'
  return `${u.prenom?.[0] ?? ''}${u.nom?.[0] ?? ''}`.toUpperCase()
})

const rolesLabels = {
  ADMIN:        'Administrateur',
  GESTIONNAIRE: 'Gestionnaire',
  MAGASINIER:   'Magasinier',
  AUDITEUR:     'Auditeur',
}
const roleLabel = computed(() => rolesLabels[authStore.role] ?? authStore.role)

const pageTitles = {
  'tableau-de-bord': 'Tableau de bord',
  'utilisateurs':    'Gestion des utilisateurs',
  'profil':          'Mon profil',
  'entrepots':       'Gestion des entrepôts',
  'zones':           'Zones de stockage',
  'emplacements':    'Emplacements',
  'produits':        'Produits',
  'categories':      'Catégories',
  'fournisseurs':    'Fournisseurs',
  'stocks':          'Stocks',
  'entrees':         'Entrées de stock',
  'sorties':         'Sorties de stock',
  'transferts':      'Transferts',
  'commandes':       'Commandes fournisseurs',
  'inventaires':     'Inventaires',
  'alertes':         'Alertes',
  'rapports':        'Rapports',
}

const pageTitle = computed(() => pageTitles[route.name] ?? 'StockMaster')
</script>

<template>
  <header class="h-16 bg-white border-b border-gray-200 flex items-center px-4 gap-3 z-30 sticky top-0 flex-shrink-0">

    <!-- PROFIL : à droite sur mobile et desktop -->
    <div class="relative order-3 lg:order-4" v-click-outside="closeProfileMenu">
      <button
        @click.stop="isProfileMenuOpen = !isProfileMenuOpen"
        class="flex items-center gap-2 p-1.5 rounded-lg hover:bg-gray-100 transition-colors"
      >
        <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center flex-shrink-0">
          <span class="text-white text-xs font-bold">{{ initiales }}</span>
        </div>
        <span class="hidden sm:block text-sm font-medium text-gray-700">{{ authStore.nomComplet }}</span>
        <svg class="w-4 h-4 text-gray-400 hidden sm:block" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
        </svg>
      </button>

      <Transition
        enter-active-class="transition ease-out duration-100"
        enter-from-class="opacity-0 scale-95"
        enter-to-class="opacity-100 scale-100"
        leave-active-class="transition ease-in duration-75"
        leave-from-class="opacity-100 scale-100"
        leave-to-class="opacity-0 scale-95"
      >
        <div
          v-if="isProfileMenuOpen"
          class="absolute right-0 mt-2 w-52 bg-white rounded-xl shadow-lg border border-gray-100 py-1 z-50"
        >
          <div class="px-4 py-3 border-b border-gray-100">
            <p class="text-sm font-semibold text-gray-800">{{ authStore.nomComplet }}</p>
            <p class="text-xs text-gray-500">{{ authStore.utilisateur?.email }}</p>
            <p class="text-xs text-blue-600 font-medium mt-0.5">{{ roleLabel }}</p>
          </div>
          <button @click="router.push({ name: 'profil' }); closeProfileMenu()"
            class="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 transition-colors">
            <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
            </svg>
            Mon profil
          </button>
          <div class="border-t border-gray-100 mt-1"></div>
          <button @click="logout"
            class="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 transition-colors">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
            </svg>
            Déconnexion
          </button>
        </div>
      </Transition>
    </div>

    <!-- BOUTON TOGGLE SIDEBAR (desktop uniquement) -->
    <button
      @click="emit('toggle-sidebar')"
      class="hidden lg:flex lg:order-1 p-2 rounded-lg text-gray-500 hover:bg-gray-100 transition-colors flex-shrink-0"
      :aria-label="sidebarCollapsed ? 'Développer la sidebar' : 'Réduire la sidebar'"
    >
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <template v-if="sidebarCollapsed">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h8M4 18h16"/>
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 9l3 3-3 3"/>
        </template>
        <template v-else>
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h8M4 18h16"/>
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9l-3 3 3 3"/>
        </template>
      </svg>
    </button>

    <!-- TITRE DE LA PAGE (mobile uniquement, centré) -->
    <h2 class="flex-1 text-base font-semibold text-gray-800 text-center order-2 lg:hidden lg:order-2">
      {{ pageTitle }}
    </h2>

    <!-- Espaceur desktop — pousse notif + profil vers la droite -->
    <div class="hidden lg:block lg:order-2 flex-1"></div>

    <!-- NOTIFICATIONS -->
    <button
      class="relative order-1 lg:order-3 p-2 rounded-lg text-gray-500 hover:bg-gray-100 transition-colors"
      aria-label="Notifications"
    >
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
      </svg>
      <span class="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
    </button>

  </header>
</template>
