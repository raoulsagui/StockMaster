<script setup>
// ============================================================
// HEADER — Barre de navigation du haut
//
// Ce composant affiche :
//   - Le bouton burger (pour ouvrir/fermer la sidebar sur mobile)
//   - Le titre de la page courante
//   - La barre de recherche globale
//   - Les notifications
//   - Le menu utilisateur (profil / déconnexion)
//
// Il communique avec le parent (AppLayout) via :
//   - emit('toggle-sidebar') : pour ouvrir/fermer la sidebar
// ============================================================

import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const route     = useRoute()
const router    = useRouter()
const authStore = useAuthStore()

const emit = defineEmits(['toggle-sidebar'])

// État local : menu profil ouvert ou fermé
const isProfileMenuOpen = ref(false)

const closeProfileMenu = () => { isProfileMenuOpen.value = false }

// Déconnexion : vide le store Pinia + localStorage, redirige vers login
const logout = () => {
  authStore.logout()
  router.push({ name: 'login' })
}

// Initiales de l'utilisateur connecté pour l'avatar
const initiales = computed(() => {
  const u = authStore.utilisateur
  if (!u) return '?'
  return `${u.prenom?.[0] ?? ''}${u.nom?.[0] ?? ''}`.toUpperCase()
})

// Libellé du rôle pour l'affichage
const rolesLabels = {
  ADMIN:        'Administrateur',
  GESTIONNAIRE: 'Gestionnaire',
  MAGASINIER:   'Magasinier',
  AUDITEUR:     'Auditeur',
}
const roleLabel = computed(() => rolesLabels[authStore.role] ?? authStore.role)

// Mapping route.name → titre affiché dans le header
// C'est plus propre que de lire route.meta.title partout
const pageTitles = {
  'tableau-de-bord': 'Tableau de bord',
  'utilisateurs':    'Gestion des utilisateurs',
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

// On calcule le titre en fonction de la route active.
// computed() recalcule automatiquement quand route.name change.
const pageTitle = computed(() => pageTitles[route.name] ?? 'StockMaster')
</script>

<template>
  <!--
    HEADER FIXE EN HAUT
    - h-16 : hauteur fixe de 64px
    - shadow-sm : légère ombre pour séparer du contenu
    - z-30 : au-dessus du contenu mais sous la sidebar (z-40)
  -->
  <header class="h-16 bg-white border-b border-gray-200 flex items-center px-4 gap-4 z-30 sticky top-0">

    <!-- ===== BOUTON BURGER (mobile only) ===== -->
    <!--
      Ce bouton n'est visible que sur mobile (lg:hidden).
      Il émet un événement vers AppLayout qui toggle isOpen.
    -->
    <button
      @click="emit('toggle-sidebar')"
      class="lg:hidden p-2 rounded-lg text-gray-500 hover:bg-gray-100 transition-colors"
      aria-label="Ouvrir le menu"
    >
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
      </svg>
    </button>

    <!-- ===== TITRE DE LA PAGE ===== -->
    <!--
      On affiche le titre correspondant à la route active.
      flex-1 pousse les éléments suivants vers la droite.
    -->
    <h2 class="flex-1 text-lg font-semibold text-gray-800 truncate">
      {{ pageTitle }}
    </h2>

    <!-- ===== ZONE DROITE : Notifications + Profil ===== -->
    <div class="flex items-center gap-2">

      <!-- Bouton Notifications -->
      <button
        class="relative p-2 rounded-lg text-gray-500 hover:bg-gray-100 transition-colors"
        aria-label="Notifications"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
        </svg>
        <!-- Badge de compteur de notifications -->
        <span class="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full"></span>
      </button>

      <!-- ===== MENU PROFIL ===== -->
      <!--
        relative + v-click-outside : le menu s'ouvre sous le bouton.
        On utilise @click.stop pour ne pas déclencher closeProfileMenu
        depuis le bouton lui-même.
      -->
      <div class="relative" v-click-outside="closeProfileMenu">

        <!-- Bouton Avatar -->
        <button
          @click.stop="isProfileMenuOpen = !isProfileMenuOpen"
          class="flex items-center gap-2 p-1.5 rounded-lg hover:bg-gray-100 transition-colors"
        >
          <!-- Cercle avec initiales -->
          <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center">
            <span class="text-white text-xs font-bold">{{ initiales }}</span>
          </div>
          <!-- Nom (masqué sur mobile) -->
          <span class="hidden sm:block text-sm font-medium text-gray-700">{{ authStore.nomComplet }}</span>
          <!-- Flèche indicatrice -->
          <svg class="w-4 h-4 text-gray-400 hidden sm:block" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
          </svg>
        </button>

        <!-- Menu déroulant profil -->
        <!--
          Transition : apparition fluide du menu déroulant.
          v-if : le menu n'est rendu dans le DOM que si ouvert.
        -->
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
            <!-- Infos utilisateur -->
            <div class="px-4 py-3 border-b border-gray-100">
              <p class="text-sm font-semibold text-gray-800">{{ authStore.nomComplet }}</p>
              <p class="text-xs text-gray-500">{{ authStore.utilisateur?.email }}</p>
              <p class="text-xs text-blue-600 font-medium mt-0.5">{{ roleLabel }}</p>
            </div>

            <!-- Lien Mon profil -->
            <button class="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 transition-colors">
              <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
              </svg>
              Mon profil
            </button>

            <!-- Lien Paramètres -->
            <button class="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50 transition-colors">
              <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
              </svg>
              Paramètres
            </button>

            <div class="border-t border-gray-100 mt-1"></div>

            <!-- Bouton Déconnexion -->
            <button
              @click="logout"
              class="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 transition-colors"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
              </svg>
              Déconnexion
            </button>
          </div>
        </Transition>
      </div>

    </div>
  </header>
</template>
