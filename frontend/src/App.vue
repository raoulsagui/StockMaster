<script setup>
// ============================================================
// APP.VUE — Composant racine de l'application
//
// Ce composant est le point d'entrée de toute l'UI Vue.
// Il contient uniquement <RouterView /> qui est remplacé
// dynamiquement par la vue correspondant à la route active.
//
// Pourquoi ne pas mettre le layout ici ?
// Parce que certaines pages (login) n'utilisent PAS AppLayout.
// Chaque vue décide elle-même si elle enveloppe son contenu
// dans AppLayout ou non.
//
// Exemple :
//   LoginView.vue        → rendu direct (pas de sidebar/header)
//   TableauDeBordView    → enveloppé dans AppLayout
// ============================================================
</script>

<template>
  <!-- RouterView : la vue active est injectée ici -->
  <RouterView />
</template>
