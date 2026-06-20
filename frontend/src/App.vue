<script setup>
// ============================================================
// APP.VUE — Composant racine de l'application
// ============================================================
import AppToast from '@/components/commun/AppToast.vue'
</script>

<template>
  <RouterView />
  <AppToast />
</template>
