<script setup>
// ============================================================
// PAGE DE CONNEXION — Branchée sur l'API Spring Boot
//
// Flux complet :
//   1. L'utilisateur saisit email + mot de passe
//   2. On appelle authStore.login() → authService → POST /api/auth/login
//   3. Le backend valide, retourne un token JWT
//   4. authStore stocke le token dans localStorage + Pinia
//   5. On redirige vers le tableau de bord
// ============================================================

import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router  = useRouter()
const route   = useRoute()
const authStore = useAuthStore()

const form = ref({
  email:      '',
  motDePasse: '',
})

const rememberMe        = ref(false)
const erreurConnexion   = ref('')
const isLoading         = ref(false)

// -------------------------------------------------------
// CONNEXION — Appel réel à l'API
// -------------------------------------------------------
const seConnecter = async () => {
  erreurConnexion.value = ''

  if (!form.value.email || !form.value.motDePasse) {
    erreurConnexion.value = 'Veuillez remplir tous les champs.'
    return
  }

  isLoading.value = true
  try {
    // authStore.login() appelle l'API et stocke le token
    await authStore.login(form.value.email, form.value.motDePasse)

    // Redirige vers la page demandée avant la connexion,
    // ou vers le tableau de bord par défaut
    const redirectTo = route.query.redirect || '/tableau-de-bord'
    router.push(redirectTo)

  } catch (error) {
    // Gestion des erreurs HTTP retournées par Spring Boot
    const status = error.response?.status

    if (status === 401) {
      erreurConnexion.value = 'Email ou mot de passe incorrect.'
    } else if (status === 403) {
      erreurConnexion.value = 'Ce compte est désactivé. Contactez un administrateur.'
    } else if (status === 400) {
      erreurConnexion.value = 'Données invalides. Vérifiez votre email.'
    } else {
      erreurConnexion.value = 'Impossible de contacter le serveur. Vérifiez que le backend est démarré.'
    }
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-800 to-slate-900 p-4">
    <div class="w-full max-w-md">

      <!-- Logo + Titre -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-blue-600 rounded-2xl mb-4 shadow-lg">
          <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
          </svg>
        </div>
        <h1 class="text-3xl font-bold text-white">StockMaster</h1>
        <p class="text-slate-400 mt-1 text-sm">Gestion de stocks multi-entrepôts</p>
      </div>

      <!-- Carte de connexion -->
      <div class="bg-white rounded-2xl shadow-xl p-8">
        <h2 class="text-xl font-bold text-gray-900 mb-6">Connexion</h2>

        <form @submit.prevent="seConnecter" class="space-y-5">

          <!-- Bannière d'erreur -->
          <Transition
            enter-active-class="transition ease-out duration-200"
            enter-from-class="opacity-0 -translate-y-2"
            enter-to-class="opacity-100 translate-y-0"
          >
            <div
              v-if="erreurConnexion"
              class="flex items-start gap-3 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm"
            >
              <svg class="w-4 h-4 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
              </svg>
              {{ erreurConnexion }}
            </div>
          </Transition>

          <!-- Email -->
          <div>
            <label for="email" class="form-label">Adresse email</label>
            <input
              id="email"
              v-model="form.email"
              type="email"
              placeholder="admin@stockmaster.fr"
              class="form-input"
              autocomplete="email"
            />
          </div>

          <!-- Mot de passe -->
          <div>
            <div class="flex items-center justify-between mb-1">
              <label for="motDePasse" class="form-label mb-0">Mot de passe</label>
            </div>
            <input
              id="motDePasse"
              v-model="form.motDePasse"
              type="password"
              placeholder="••••••••"
              class="form-input"
              autocomplete="current-password"
            />
          </div>

          <!-- Se souvenir de moi -->
          <div class="flex items-center gap-2">
            <input
              id="rememberMe"
              v-model="rememberMe"
              type="checkbox"
              class="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
            />
            <label for="rememberMe" class="text-sm text-gray-600">Se souvenir de moi</label>
          </div>

          <!-- Bouton connexion -->
          <button type="submit" :disabled="isLoading" class="w-full btn-primary justify-center py-2.5">
            <svg v-if="isLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
            {{ isLoading ? 'Connexion…' : 'Se connecter' }}
          </button>

        </form>

        <!-- Hint dev -->
        <p class="text-xs text-gray-400 text-center mt-5 border-t pt-4">
          Compte dev : <strong>admin@stockmaster.fr</strong> / <strong>Admin@1234</strong>
        </p>
      </div>

      <p class="text-center text-slate-500 text-xs mt-6">
        StockMaster © 2026 — Gestion de stocks entreprise
      </p>
    </div>
  </div>
</template>
