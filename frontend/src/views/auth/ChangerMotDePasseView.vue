<script setup>
// ============================================================
// PAGE CHANGEMENT DE MOT DE PASSE OBLIGATOIRE
//
// Affichée automatiquement à la première connexion ou après
// une réinitialisation de mot de passe par un admin.
//
// L'utilisateur ne peut PAS naviguer ailleurs tant qu'il
// n'a pas changé son mot de passe (bloqué par le guard router).
// ============================================================

import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import api from '@/services/api'

const router    = useRouter()
const authStore = useAuthStore()

const form = ref({
  nouveauMotDePasse:    '',
  confirmerMotDePasse:  '',
})

const erreurs   = ref({})
const erreurApi = ref('')
const isLoading = ref(false)
const succes    = ref(false)

// Validation locale
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

    // POST /api/utilisateurs/{id}/changer-mot-de-passe
    // ancienMotDePasse = null car c'est un changement forcé
    await api.post(`/utilisateurs/${userId}/changer-mot-de-passe`, {
      ancienMotDePasse:   null,
      nouveauMotDePasse:  form.value.nouveauMotDePasse,
      confirmerMotDePasse: form.value.confirmerMotDePasse,
    })

    // Met à jour le flag dans le store Pinia
    // pour que le guard ne redirige plus ici
    if (authStore.utilisateur) {
      authStore.utilisateur.doitChangerMotDePasse = false
      localStorage.setItem('utilisateur', JSON.stringify(authStore.utilisateur))
    }

    succes.value = true

    // Redirige vers le tableau de bord après 1.5s
    setTimeout(() => {
      router.push({ name: 'tableau-de-bord' })
    }, 1500)

  } catch (e) {
    erreurApi.value = e.response?.data || 'Erreur lors du changement de mot de passe.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <!--
    Page plein écran — même style que LoginView.
    Pas de sidebar/header : l'utilisateur est bloqué ici
    jusqu'à ce qu'il ait changé son mot de passe.
  -->
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-800 to-slate-900 p-4">
    <div class="w-full max-w-md">

      <!-- Logo -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-orange-500 rounded-2xl mb-4 shadow-lg">
          <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"/>
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-white">Changement de mot de passe</h1>
        <p class="text-slate-400 mt-1 text-sm">
          Pour des raisons de sécurité, vous devez définir un nouveau mot de passe.
        </p>
      </div>

      <div class="bg-white rounded-2xl shadow-xl p-8">

        <!-- Message de succès -->
        <div v-if="succes" class="text-center py-4">
          <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
            </svg>
          </div>
          <p class="text-green-700 font-semibold">Mot de passe changé avec succès !</p>
          <p class="text-gray-400 text-sm mt-1">Redirection en cours…</p>
        </div>

        <!-- Formulaire -->
        <form v-else @submit.prevent="soumettre" class="space-y-5">

          <!-- Info utilisateur -->
          <div class="bg-blue-50 border border-blue-200 rounded-lg p-3 flex items-center gap-3">
            <div class="w-9 h-9 rounded-full bg-blue-600 flex items-center justify-center flex-shrink-0">
              <span class="text-white text-xs font-bold">
                {{ authStore.utilisateur?.prenom?.[0] }}{{ authStore.utilisateur?.nom?.[0] }}
              </span>
            </div>
            <div>
              <p class="text-sm font-semibold text-blue-900">{{ authStore.nomComplet }}</p>
              <p class="text-xs text-blue-600">{{ authStore.utilisateur?.email }}</p>
            </div>
          </div>

          <!-- Erreur API -->
          <div v-if="erreurApi" class="p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
            {{ erreurApi }}
          </div>

          <!-- Nouveau mot de passe -->
          <div>
            <label for="nouveauMotDePasse" class="form-label">
              Nouveau mot de passe <span class="text-red-500">*</span>
            </label>
            <input
              id="nouveauMotDePasse"
              v-model="form.nouveauMotDePasse"
              type="password"
              placeholder="Min. 8 caractères"
              :class="['form-input', erreurs.nouveauMotDePasse ? 'border-red-400' : '']"
            />
            <p v-if="erreurs.nouveauMotDePasse" class="form-error">{{ erreurs.nouveauMotDePasse }}</p>
          </div>

          <!-- Confirmer -->
          <div>
            <label for="confirmerMotDePasse" class="form-label">
              Confirmer le mot de passe <span class="text-red-500">*</span>
            </label>
            <input
              id="confirmerMotDePasse"
              v-model="form.confirmerMotDePasse"
              type="password"
              placeholder="Répéter le mot de passe"
              :class="['form-input', erreurs.confirmerMotDePasse ? 'border-red-400' : '']"
            />
            <p v-if="erreurs.confirmerMotDePasse" class="form-error">{{ erreurs.confirmerMotDePasse }}</p>
          </div>

          <button type="submit" :disabled="isLoading" class="w-full btn-primary justify-center py-2.5">
            <svg v-if="isLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
            {{ isLoading ? 'Enregistrement…' : 'Définir mon mot de passe' }}
          </button>

        </form>
      </div>
    </div>
  </div>
</template>
