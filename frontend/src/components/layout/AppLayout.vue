<script setup>
// ============================================================
// APP LAYOUT — Structure principale de l'application
//
// Ce composant est le "squelette" de toutes les pages
// authentifiées. Il assemble :
//   - AppSidebar : menu latéral gauche
//   - AppHeader  : barre du haut
//   - <slot />   : contenu de la page courante
//
// Principe du slot : AppLayout ne sait pas ce qui sera affiché
// à l'intérieur. Chaque vue (tableau de bord, utilisateurs…)
// s'y insère via <AppLayout><MonContenu /></AppLayout>.
//
// Structure visuelle :
// ┌──────────┬─────────────────────────────┐
// │          │  HEADER                     │
// │ SIDEBAR  ├─────────────────────────────┤
// │  (fixe)  │                             │
// │          │  CONTENU (slot)             │
// │          │                             │
// └──────────┴─────────────────────────────┘
// ============================================================

import { ref } from 'vue'
import AppSidebar from './AppSidebar.vue'
import AppHeader from './AppHeader.vue'

// État local : la sidebar est-elle ouverte ?
// Utile surtout sur mobile (sur desktop, elle est toujours visible via CSS).
const isSidebarOpen = ref(false)

// Méthode pour basculer l'état de la sidebar
const toggleSidebar = () => {
  isSidebarOpen.value = !isSidebarOpen.value
}

// Méthode pour fermer la sidebar (ex: après clic sur un lien mobile)
const closeSidebar = () => {
  isSidebarOpen.value = false
}
</script>

<template>
  <!--
    CONTENEUR PRINCIPAL : occupe toute la hauteur de l'écran
    flex : disposition en ligne (sidebar | main)
  -->
  <div class="min-h-screen flex bg-gray-100">

    <!-- ===== OVERLAY MOBILE =====
      Sur mobile, quand la sidebar est ouverte, on affiche un
      fond semi-transparent derrière elle. Un clic dessus la ferme.
      lg:hidden : invisible sur desktop (inutile car sidebar toujours visible).
    -->
    <Transition
      enter-active-class="transition-opacity duration-300"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-300"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="isSidebarOpen"
        class="fixed inset-0 bg-black/50 z-30 lg:hidden"
        @click="closeSidebar"
      ></div>
    </Transition>

    <!-- ===== SIDEBAR =====
      On passe isOpen pour que la sidebar sache si elle doit
      se décaler ou non (gestion mobile).
    -->
    <AppSidebar :isOpen="isSidebarOpen" />

    <!-- ===== ZONE PRINCIPALE (header + contenu) =====
      flex-1 : prend tout l'espace restant (à droite de la sidebar)
      lg:ml-64 : marge gauche = largeur de la sidebar sur desktop
      flex flex-col : empile header + contenu verticalement
    -->
    <div class="flex-1 flex flex-col min-w-0 lg:ml-64">

      <!-- Header fixe en haut -->
      <!-- On écoute toggle-sidebar émis par AppHeader -->
      <AppHeader @toggle-sidebar="toggleSidebar" />

      <!-- ===== CONTENU DE LA PAGE =====
        flex-1 : prend tout l'espace vertical restant
        overflow-y-auto : scroll si le contenu dépasse
        p-6 : padding autour du contenu
        Le <slot /> est remplacé par le contenu de la vue courante.
      -->
      <main class="flex-1 overflow-y-auto p-6">
        <slot />
      </main>
    </div>

  </div>
</template>
