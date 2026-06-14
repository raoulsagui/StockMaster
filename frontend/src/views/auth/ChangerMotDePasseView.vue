<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import api from '@/services/api'

const router    = useRouter()
const authStore = useAuthStore()

const form = ref({ nouveauMotDePasse: '', confirmerMotDePasse: '' })
const showNew     = ref(false)
const showConfirm = ref(false)
const erreurs     = ref({})
const erreurApi   = ref('')
const isLoading   = ref(false)
const succes      = ref(false)

// Indicateur de force du mot de passe
const forceMotDePasse = computed(() => {
  const mdp = form.value.nouveauMotDePasse
  if (!mdp) return { score: 0, label: '', color: '' }
  let score = 0
  if (mdp.length >= 8)  score++
  if (mdp.length >= 12) score++
  if (/[A-Z]/.test(mdp)) score++
  if (/[0-9]/.test(mdp)) score++
  if (/[^A-Za-z0-9]/.test(mdp)) score++

  if (score <= 1) return { score, label: 'Très faible', color: 'bg-red-500',    width: 'w-1/5' }
  if (score === 2) return { score, label: 'Faible',      color: 'bg-orange-500', width: 'w-2/5' }
  if (score === 3) return { score, label: 'Moyen',       color: 'bg-yellow-500', width: 'w-3/5' }
  if (score === 4) return { score, label: 'Fort',        color: 'bg-blue-500',   width: 'w-4/5' }
  return              { score, label: 'Très fort',   color: 'bg-green-500',  width: 'w-full' }
})

const valider = () => {
  erreurs.value = {}
  if (!form.value.nouveauMotDePasse)
    erreurs.value.nouveauMotDePasse = 'Le mot de passe est obligatoire.'
  else if (form.value.nouveauMotDePasse.length < 8)
    erreurs.value.nouveauMotDePasse = 'Minimum 8 caractères.'
  if (form.value.nouveauMotDePasse !== form.value.confirmerMotDePasse)
    erreurs.value.confirmerMotDePasse = 'Les mots de passe ne correspondent pas.'
  return Object.keys(erreurs.value).length === 0
}

const soumettre = async () => {
  if (!valider()) return
  isLoading.value = true
  erreurApi.value = ''
  try {
    const userId = authStore.utilisateur.id
    await api.post(`/utilisateurs/${userId}/changer-mot-de-passe`, {
      ancienMotDePasse:    null,
      nouveauMotDePasse:   form.value.nouveauMotDePasse,
      confirmerMotDePasse: form.value.confirmerMotDePasse,
    })
    if (authStore.utilisateur) {
      authStore.utilisateur.doitChangerMotDePasse = false
      localStorage.setItem('utilisateur', JSON.stringify(authStore.utilisateur))
    }
    succes.value = true
    setTimeout(() => router.push({ name: 'tableau-de-bord' }), 1500)
  } catch (e) {
    erreurApi.value = e.response?.data || 'Erreur lors du changement de mot de passe.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex">

    <!-- CÔTÉ GAUCHE — Branding -->
    <div class="hidden lg:flex lg:w-1/2 relative bg-gradient-to-br from-slate-900 via-blue-950 to-slate-900 flex-col items-center justify-center p-12 overflow-hidden">
      <div class="absolute top-0 left-0 w-96 h-96 bg-blue-600/10 rounded-full -translate-x-1/2 -translate-y-1/2"></div>
      <div class="absolute bottom-0 right-0 w-80 h-80 bg-blue-500/10 rounded-full translate-x-1/3 translate-y-1/3"></div>

      <div class="relative z-10 text-center">
        <div class="inline-flex items-center justify-center w-20 h-20 bg-orange-500 rounded-3xl mb-8 shadow-2xl shadow-orange-500/30">
          <svg class="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
              d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
          </svg>
        </div>
        <h1 class="text-4xl font-bold text-white mb-3">Sécurisez</h1>
        <p class="text-blue-300 text-lg mb-12">votre compte StockMaster</p>

        <!-- Conseils mot de passe -->
        <div class="bg-white/5 backdrop-blur-sm rounded-2xl p-6 border border-white/10 text-left">
          <p class="text-white font-semibold mb-4 text-sm">Conseils pour un bon mot de passe</p>
          <ul class="space-y-3">
            <li v-for="tip in tips" :key="tip"
              class="flex items-center gap-3 text-slate-300 text-sm">
              <div class="w-5 h-5 rounded-full bg-blue-600/30 flex items-center justify-center flex-shrink-0">
                <svg class="w-3 h-3 text-blue-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                </svg>
              </div>
              {{ tip }}
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- CÔTÉ DROIT — Formulaire -->
    <div class="w-full lg:w-1/2 flex items-center justify-center bg-gray-50 p-6">
      <div class="w-full max-w-md">

        <!-- Logo mobile -->
        <div class="lg:hidden text-center mb-8">
          <div class="inline-flex items-center justify-center w-14 h-14 bg-orange-500 rounded-2xl mb-3">
            <svg class="w-7 h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
            </svg>
          </div>
          <h1 class="text-2xl font-bold text-gray-900">StockMaster</h1>
        </div>

        <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">

          <!-- Succès -->
          <div v-if="succes" class="text-center py-6">
            <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
              </svg>
            </div>
            <h3 class="text-lg font-bold text-gray-900 mb-1">Mot de passe défini !</h3>
            <p class="text-gray-500 text-sm">Redirection vers votre tableau de bord…</p>
          </div>

          <!-- Formulaire -->
          <template v-else>
            <div class="mb-7">
              <h2 class="text-2xl font-bold text-gray-900">Nouveau mot de passe</h2>
              <p class="text-gray-500 text-sm mt-1">Pour des raisons de sécurité, définissez un mot de passe personnel.</p>
            </div>

            <!-- Info utilisateur -->
            <div class="flex items-center gap-3 bg-orange-50 border border-orange-100 rounded-xl p-3.5 mb-6">
              <div class="w-9 h-9 rounded-full bg-orange-500 flex items-center justify-center flex-shrink-0">
                <span class="text-white text-xs font-bold">
                  {{ authStore.utilisateur?.prenom?.[0] }}{{ authStore.utilisateur?.nom?.[0] }}
                </span>
              </div>
              <div>
                <p class="text-sm font-semibold text-gray-800">{{ authStore.nomComplet }}</p>
                <p class="text-xs text-gray-500">{{ authStore.utilisateur?.email }}</p>
              </div>
            </div>

            <form @submit.prevent="soumettre" class="space-y-5">

              <div v-if="erreurApi" class="p-3.5 bg-red-50 border border-red-200 rounded-xl text-red-700 text-sm">
                {{ erreurApi }}
              </div>

              <!-- Nouveau mot de passe -->
              <div>
                <label for="nouveauMotDePasse" class="form-label">
                  Nouveau mot de passe <span class="text-red-500">*</span>
                </label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-3 flex items-center pointer-events-none">
                    <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
                    </svg>
                  </div>
                  <input
                    id="nouveauMotDePasse"
                    v-model="form.nouveauMotDePasse"
                    :type="showNew ? 'text' : 'password'"
                    placeholder="Min. 8 caractères"
                    :class="['form-input pl-9 pr-10', erreurs.nouveauMotDePasse ? 'border-red-400' : '']"
                  />
                  <button type="button" @click="showNew = !showNew"
                    class="absolute inset-y-0 right-3 flex items-center text-gray-400 hover:text-gray-600 transition-colors">
                    <svg v-if="!showNew" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M15 12a3 3 0 11-6 0 3 3 0 016 0zM2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                    </svg>
                    <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"/>
                    </svg>
                  </button>
                </div>
                <p v-if="erreurs.nouveauMotDePasse" class="form-error">{{ erreurs.nouveauMotDePasse }}</p>

                <!-- Indicateur de force -->
                <div v-if="form.nouveauMotDePasse" class="mt-2">
                  <div class="h-1.5 bg-gray-100 rounded-full overflow-hidden">
                    <div :class="['h-full rounded-full transition-all duration-300', forceMotDePasse.color, forceMotDePasse.width]"></div>
                  </div>
                  <p :class="['text-xs mt-1', forceMotDePasse.score <= 2 ? 'text-red-500' : forceMotDePasse.score === 3 ? 'text-yellow-600' : 'text-green-600']">
                    {{ forceMotDePasse.label }}
                  </p>
                </div>
              </div>

              <!-- Confirmer -->
              <div>
                <label for="confirmerMotDePasse" class="form-label">
                  Confirmer <span class="text-red-500">*</span>
                </label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-3 flex items-center pointer-events-none">
                    <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/>
                    </svg>
                  </div>
                  <input
                    id="confirmerMotDePasse"
                    v-model="form.confirmerMotDePasse"
                    :type="showConfirm ? 'text' : 'password'"
                    placeholder="Répéter le mot de passe"
                    :class="['form-input pl-9 pr-10', erreurs.confirmerMotDePasse ? 'border-red-400' : '']"
                  />
                  <button type="button" @click="showConfirm = !showConfirm"
                    class="absolute inset-y-0 right-3 flex items-center text-gray-400 hover:text-gray-600 transition-colors">
                    <svg v-if="!showConfirm" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M15 12a3 3 0 11-6 0 3 3 0 016 0zM2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                    </svg>
                    <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"/>
                    </svg>
                  </button>
                </div>
                <p v-if="erreurs.confirmerMotDePasse" class="form-error">{{ erreurs.confirmerMotDePasse }}</p>
              </div>

              <button type="submit" :disabled="isLoading" class="w-full btn-primary justify-center py-3 text-base mt-2 !bg-orange-500 hover:!bg-orange-600 active:!bg-orange-700">
                <svg v-if="isLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                {{ isLoading ? 'Enregistrement…' : 'Définir mon mot de passe' }}
              </button>

            </form>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      tips: [
        'Au moins 8 caractères',
        'Une majuscule et une minuscule',
        'Un chiffre',
        'Un caractère spécial (@, #, !…)',
        'Ne pas réutiliser un ancien mot de passe',
      ],
    }
  },
}
</script>
