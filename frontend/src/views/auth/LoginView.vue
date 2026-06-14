<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router    = useRouter()
const route     = useRoute()
const authStore = useAuthStore()

const form = ref({ email: '', motDePasse: '' })
const showPassword      = ref(false)
const erreurConnexion   = ref('')
const isLoading         = ref(false)

const seConnecter = async () => {
  erreurConnexion.value = ''
  if (!form.value.email || !form.value.motDePasse) {
    erreurConnexion.value = 'Veuillez remplir tous les champs.'
    return
  }
  isLoading.value = true
  try {
    await authStore.login(form.value.email, form.value.motDePasse)
    const redirectTo = route.query.redirect || '/tableau-de-bord'
    router.push(redirectTo)
  } catch (error) {
    const status = error.response?.status
    if (status === 401)      erreurConnexion.value = 'Email ou mot de passe incorrect.'
    else if (status === 403) erreurConnexion.value = 'Ce compte est désactivé. Contactez un administrateur.'
    else if (status === 400) erreurConnexion.value = 'Données invalides. Vérifiez votre email.'
    else                     erreurConnexion.value = 'Impossible de contacter le serveur.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex">

    <!-- ===== CÔTÉ GAUCHE — Branding ===== -->
    <div class="hidden lg:flex lg:w-1/2 relative bg-gradient-to-br from-slate-900 via-blue-950 to-slate-900 flex-col items-center justify-center p-12 overflow-hidden">

      <!-- Cercles décoratifs en arrière-plan -->
      <div class="absolute top-0 left-0 w-96 h-96 bg-blue-600/10 rounded-full -translate-x-1/2 -translate-y-1/2"></div>
      <div class="absolute bottom-0 right-0 w-80 h-80 bg-blue-500/10 rounded-full translate-x-1/3 translate-y-1/3"></div>
      <div class="absolute top-1/2 left-1/2 w-64 h-64 bg-indigo-600/5 rounded-full -translate-x-1/2 -translate-y-1/2"></div>

      <!-- Contenu branding -->
      <div class="relative z-10 text-center">
        <!-- Logo -->
        <div class="inline-flex items-center justify-center w-20 h-20 bg-blue-600 rounded-3xl mb-8 shadow-2xl shadow-blue-500/30">
          <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
              d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
          </svg>
        </div>

        <h1 class="text-4xl font-bold text-white mb-3">StockMaster</h1>
        <p class="text-blue-300 text-lg mb-12">Gestion de stocks multi-entrepôts</p>

        <!-- Fonctionnalités clés -->
        <div class="space-y-4 text-left">
          <div v-for="feature in features" :key="feature.text"
            class="flex items-center gap-4 bg-white/5 backdrop-blur-sm rounded-xl px-5 py-3.5 border border-white/10">
            <div class="w-9 h-9 rounded-lg bg-blue-600/30 flex items-center justify-center flex-shrink-0">
              <svg class="w-5 h-5 text-blue-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="feature.icon"/>
              </svg>
            </div>
            <div>
              <p class="text-white text-sm font-medium">{{ feature.title }}</p>
              <p class="text-slate-400 text-xs mt-0.5">{{ feature.text }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer branding -->
      <p class="absolute bottom-6 text-slate-600 text-xs">StockMaster © 2026</p>
    </div>

    <!-- ===== CÔTÉ DROIT — Formulaire ===== -->
    <div class="w-full lg:w-1/2 flex items-center justify-center bg-gray-50 p-6">
      <div class="w-full max-w-md">

        <!-- Logo mobile (visible seulement sur mobile) -->
        <div class="lg:hidden text-center mb-8">
          <div class="inline-flex items-center justify-center w-14 h-14 bg-blue-600 rounded-2xl mb-3 shadow-lg">
            <svg class="w-7 h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
            </svg>
          </div>
          <h1 class="text-2xl font-bold text-gray-900">StockMaster</h1>
        </div>

        <!-- Carte formulaire -->
        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          <div class="mb-7 text-center">
            <h2 class="text-2xl font-bold text-gray-900">Connexion</h2>
            <p class="text-gray-500 text-sm mt-1">Connectez-vous à votre espace de gestion</p>
          </div>

          <form @submit.prevent="seConnecter" class="space-y-5">

            <!-- Erreur -->
            <Transition
              enter-active-class="transition ease-out duration-200"
              enter-from-class="opacity-0 -translate-y-1"
              enter-to-class="opacity-100 translate-y-0"
            >
              <div v-if="erreurConnexion"
                class="flex items-start gap-2.5 p-3.5 bg-red-50 border border-red-200 rounded-xl text-red-700 text-sm">
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
              <div class="relative">
                <div class="absolute inset-y-0 left-3 flex items-center pointer-events-none">
                  <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                  </svg>
                </div>
                <input
                  id="email"
                  v-model="form.email"
                  type="email"
                  placeholder="votre@email.fr"
                  class="form-input pl-9"
                  autocomplete="email"
                />
              </div>
            </div>

            <!-- Mot de passe -->
            <div>
              <label for="motDePasse" class="form-label">Mot de passe</label>
              <div class="relative">
                <div class="absolute inset-y-0 left-3 flex items-center pointer-events-none">
                  <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
                  </svg>
                </div>
                <input
                  id="motDePasse"
                  v-model="form.motDePasse"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="••••••••"
                  class="form-input pl-9 pr-10"
                  autocomplete="current-password"
                />
                <!-- Bouton afficher/masquer -->
                <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute inset-y-0 right-3 flex items-center text-gray-400 hover:text-gray-600 transition-colors"
                >
                  <svg v-if="!showPassword" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                  </svg>
                  <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"/>
                  </svg>
                </button>
              </div>
            </div>

            <!-- Bouton connexion -->
            <button
              type="submit"
              :disabled="isLoading"
              class="w-full btn-primary justify-center py-3 text-base mt-2"
            >
              <svg v-if="isLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
              {{ isLoading ? 'Connexion en cours…' : 'Se connecter' }}
            </button>

          </form>
        </div>

        <p class="text-center text-gray-400 text-xs mt-5">
          StockMaster © 2026 — Gestion de stocks entreprise
        </p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      features: [
        {
          title: 'Suivi en temps réel',
          text: 'Visualisez vos stocks sur tous vos entrepôts',
          icon: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z',
        },
        {
          title: 'Gestion multi-entrepôts',
          text: 'Centralisez toutes vos opérations logistiques',
          icon: 'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4',
        },
        {
          title: 'Alertes automatiques',
          text: 'Soyez notifié en cas de stock critique',
          icon: 'M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9',
        },
      ],
    }
  },
}
</script>
