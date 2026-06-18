<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex items-center justify-center p-4">
    <div class="text-center max-w-md">
      <!-- Icône -->
      <div class="w-20 h-20 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-6">
        <svg class="w-10 h-10 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M12 9v2m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
        </svg>
      </div>

      <h1 class="text-3xl font-bold text-gray-900 mb-2">Accès refusé</h1>
      <p class="text-gray-500 mb-8">
        Vous n'avez pas les droits nécessaires pour accéder à cette page.
        Contactez votre administrateur si vous pensez que c'est une erreur.
      </p>

      <button
        @click="router.push({ name: 'tableau-de-bord' })"
        class="btn-primary"
      >
        Retour au tableau de bord
      </button>
    </div>
  </div>
</template>
