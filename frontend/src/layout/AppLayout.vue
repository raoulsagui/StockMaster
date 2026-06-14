<script setup>
// ============================================================
// APP LAYOUT — Structure principale de l'application
// ============================================================

import { ref } from 'vue'
import AppSidebar from './AppSidebar.vue'
import AppHeader from './AppHeader.vue'
import AppBottomNav from './AppBottomNav.vue'

// Sur desktop : sidebar élargie par défaut (false = élargie)
const sidebarCollapsed = ref(false)

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}
</script>

<template>
  <div class="min-h-screen flex bg-gray-100">

    <!-- SIDEBAR DESKTOP (hidden on mobile via CSS in component) -->
    <AppSidebar :collapsed="sidebarCollapsed" />

    <!-- ZONE PRINCIPALE -->
    <div
      :class="[
        'flex-1 flex flex-col min-w-0 transition-all duration-300',
        // Sur desktop : marge gauche = largeur sidebar
        sidebarCollapsed ? 'lg:ml-16' : 'lg:ml-64',
        // Sur mobile : pas de marge gauche, on a la bottom nav
        'pb-16 lg:pb-0',
      ]"
    >
      <AppHeader
        :sidebar-collapsed="sidebarCollapsed"
        @toggle-sidebar="toggleSidebar"
      />

      <main class="flex-1 overflow-y-auto p-4 md:p-6">
        <slot />
      </main>
    </div>

    <!-- BOTTOM NAV MOBILE (visible seulement sur mobile) -->
    <AppBottomNav />
  </div>
</template>
