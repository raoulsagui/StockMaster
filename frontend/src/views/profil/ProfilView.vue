<script setup>
import { ref, computed } from 'vue'
import AppLayout from '@/layout/AppLayout.vue'
import { useAuthStore } from '@/stores/authStore'
import utilisateurService from '@/services/utilisateurService'

const authStore = useAuthStore()

// -------------------------------------------------------
// SECTION INFOS (prénom + nom)
// -------------------------------------------------------
const formInfos      = ref({ prenom: authStore.utilisateur?.prenom ?? '', nom: authStore.utilisateur?.nom ?? '' })
const erreursInfos   = ref({})
const erreurApiInfos = ref('')
const succesInfos    = ref(false)
const isSavingInfos  = ref(false)

function validerInfos() {
  const e = {}
  if (!formInfos.value.prenom.trim()) e.prenom = 'Le prénom est obligatoire.'
  if (!formInfos.value.nom.trim())    e.nom    = 'Le nom est obligatoire.'
  erreursInfos.value = e
  return Object.keys(e).length === 0
}

async function soumettreInfos() {
  if (!validerInfos()) return
  isSavingInfos.value  = true
  erreurApiInfos.value = ''
  succesInfos.value    = false
  try {
    const updated = await utilisateurService.modifierProfil(authStore.utilisateur.id, {
      prenom: formInfos.value.prenom,
      nom:    formInfos.value.nom,
    })
    authStore.utilisateur.prenom = updated.prenom
    authStore.utilisateur.nom    = updated.nom
    localStorage.setItem('utilisateur', JSON.stringify(authStore.utilisateur))
    succesInfos.value = true
    setTimeout(() => { succesInfos.value = false }, 3000)
  } catch (e) {
    erreurApiInfos.value = e.response?.data || 'Erreur lors de la mise à jour.'
  } finally {
    isSavingInfos.value = false
  }
}

// -------------------------------------------------------
// SECTION MOT DE PASSE
// -------------------------------------------------------
const formMdp       = ref({ ancien: '', nouveau: '', confirmer: '' })
const erreursMdp    = ref({})
const erreurApiMdp  = ref('')
const succesMdp     = ref(false)
const isSavingMdp   = ref(false)
const showAncien    = ref(false)
const showNouveau   = ref(false)
const showConfirmer = ref(false)

const forceMdp = computed(() => {
  const mdp = formMdp.value.nouveau
  if (!mdp) return { label: '', color: '', width: '' }
  let score = 0
  if (mdp.length >= 8)           score++
  if (mdp.length >= 12)          score++
  if (/[A-Z]/.test(mdp))         score++
  if (/[0-9]/.test(mdp))         score++
  if (/[^A-Za-z0-9]/.test(mdp))  score++
  if (score <= 1) return { label: 'Très faible', color: 'bg-red-500',    width: 'w-1/5' }
  if (score === 2) return { label: 'Faible',      color: 'bg-orange-500', width: 'w-2/5' }
  if (score === 3) return { label: 'Moyen',       color: 'bg-yellow-500', width: 'w-3/5' }
  if (score === 4) return { label: 'Fort',        color: 'bg-blue-500',   width: 'w-4/5' }
  return              { label: 'Très fort',   color: 'bg-green-500',  width: 'w-full' }
})

function validerMdp() {
  const e = {}
  if (!formMdp.value.ancien)                             e.ancien    = "L'ancien mot de passe est obligatoire."
  if (!formMdp.value.nouveau)                            e.nouveau   = 'Le nouveau mot de passe est obligatoire.'
  else if (formMdp.value.nouveau.length < 8)             e.nouveau   = 'Minimum 8 caractères.'
  if (formMdp.value.nouveau !== formMdp.value.confirmer) e.confirmer = 'Les mots de passe ne correspondent pas.'
  erreursMdp.value = e
  return Object.keys(e).length === 0
}

async function soumettreMdp() {
  if (!validerMdp()) return
  isSavingMdp.value  = true
  erreurApiMdp.value = ''
  succesMdp.value    = false
  try {
    await utilisateurService.changerMotDePasse(authStore.utilisateur.id, {
      ancienMotDePasse:    formMdp.value.ancien,
      nouveauMotDePasse:   formMdp.value.nouveau,
      confirmerMotDePasse: formMdp.value.confirmer,
    })
    formMdp.value  = { ancien: '', nouveau: '', confirmer: '' }
    succesMdp.value = true
    setTimeout(() => { succesMdp.value = false }, 3000)
  } catch (e) {
    erreurApiMdp.value = e.response?.data || 'Erreur lors du changement de mot de passe.'
  } finally {
    isSavingMdp.value = false
  }
}

// -------------------------------------------------------
// HELPERS
// -------------------------------------------------------
const rolesLabels = {
  ADMIN:        'Administrateur',
  GESTIONNAIRE: 'Gestionnaire',
  MAGASINIER:   'Magasinier',
  AUDITEUR:     'Auditeur',
}

const avatarColors = {
  ADMIN:        'bg-purple-600',
  GESTIONNAIRE: 'bg-blue-600',
  MAGASINIER:   'bg-green-600',
  AUDITEUR:     'bg-yellow-500',
}

const initiales = computed(() => {
  const u = authStore.utilisateur
  if (!u) return '?'
  return `${u.prenom?.[0] ?? ''}${u.nom?.[0] ?? ''}`.toUpperCase()
})

const formatDate = (dateStr) => {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' })
}
</script>

<template>
  <AppLayout>
    <div class="space-y-6">

      <!-- EN-TÊTE -->
      <div>
        <h1 class="hidden md:block text-xl md:text-2xl font-bold text-gray-900">Mon profil</h1>
        <p class="hidden md:block text-sm text-gray-500 mt-0.5">Gérez vos informations personnelles</p>
      </div>

      <!-- LAYOUT DEUX COLONNES -->
      <div class="flex flex-col lg:flex-row gap-6">

        <!-- COLONNE GAUCHE : carte identité + modifier infos -->
        <div class="flex flex-col gap-6 flex-1 w-full">

          <!-- CARTE IDENTITÉ -->
          <div class="card p-6">
            <div class="flex items-center gap-4">
              <div :class="['w-16 h-16 rounded-full flex items-center justify-center flex-shrink-0', avatarColors[authStore.role]]">
                <span class="text-white text-xl font-bold">{{ initiales }}</span>
              </div>
              <div>
                <p class="text-lg font-semibold text-gray-900">{{ authStore.nomComplet }}</p>
                <p class="text-sm text-gray-500">{{ authStore.utilisateur?.email }}</p>
                <span class="inline-block mt-1 text-xs font-medium px-2 py-0.5 rounded-full bg-blue-100 text-blue-700">
                  {{ rolesLabels[authStore.role] }}
                </span>
              </div>
              <div class="ml-auto text-right hidden sm:block">
                <p class="text-xs text-gray-400">Membre depuis</p>
                <p class="text-sm text-gray-600 font-medium">{{ formatDate(authStore.utilisateur?.dateCreation) }}</p>
              </div>
            </div>
            <div class="mt-5 flex gap-3">
              <div class="bg-gray-50 rounded-lg px-4 py-3">
                <p class="text-xs text-gray-400 mb-0.5">Email</p>
                <p class="text-sm text-gray-700 font-medium">{{ authStore.utilisateur?.email }}</p>
              </div>
              <div class="bg-gray-50 rounded-lg px-4 py-3">
                <p class="text-xs text-gray-400 mb-0.5">Rôle</p>
                <p class="text-sm text-gray-700 font-medium">{{ rolesLabels[authStore.role] }}</p>
              </div>
            </div>
          </div>

          <!-- FORMULAIRE INFOS -->
          <div class="card p-6">
            <h2 class="text-base font-semibold text-gray-900 mb-4">Informations personnelles</h2>

            <Transition
              enter-active-class="transition duration-200"
              enter-from-class="opacity-0 -translate-y-1"
              enter-to-class="opacity-100 translate-y-0"
            >
              <div v-if="succesInfos"
                class="flex items-center gap-2 p-3 mb-4 bg-green-50 border border-green-200 rounded-lg text-green-700 text-sm">
                <svg class="w-4 h-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                </svg>
                Profil mis à jour avec succès.
              </div>
            </Transition>

            <div v-if="erreurApiInfos" class="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg p-3 mb-4">
              {{ erreurApiInfos }}
            </div>

            <form @submit.prevent="soumettreInfos" class="space-y-4">
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="form-label">Prénom <span class="text-red-500">*</span></label>
                  <input v-model="formInfos.prenom" type="text"
                    :class="['form-input', erreursInfos.prenom ? 'border-red-400' : '']"/>
                  <p v-if="erreursInfos.prenom" class="form-error">{{ erreursInfos.prenom }}</p>
                </div>
                <div>
                  <label class="form-label">Nom <span class="text-red-500">*</span></label>
                  <input v-model="formInfos.nom" type="text"
                    :class="['form-input', erreursInfos.nom ? 'border-red-400' : '']"/>
                  <p v-if="erreursInfos.nom" class="form-error">{{ erreursInfos.nom }}</p>
                </div>
              </div>
              <div class="flex justify-end">
                <button type="submit" :disabled="isSavingInfos" class="btn-primary">
                  <svg v-if="isSavingInfos" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                  </svg>
                  Enregistrer
                </button>
              </div>
            </form>
          </div>

        </div>

        <!-- COLONNE DROITE : mot de passe -->
        <div class="flex-1 w-full">
          <div class="card p-6">
        <h2 class="text-base font-semibold text-gray-900 mb-4">Changer le mot de passe</h2>

        <Transition
          enter-active-class="transition duration-200"
          enter-from-class="opacity-0 -translate-y-1"
          enter-to-class="opacity-100 translate-y-0"
        >
          <div v-if="succesMdp"
            class="flex items-center gap-2 p-3 mb-4 bg-green-50 border border-green-200 rounded-lg text-green-700 text-sm">
            <svg class="w-4 h-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
            </svg>
            Mot de passe modifié avec succès.
          </div>
        </Transition>

        <div v-if="erreurApiMdp" class="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg p-3 mb-4">
          {{ erreurApiMdp }}
        </div>

        <form @submit.prevent="soumettreMdp" class="space-y-4">

          <!-- Ancien mot de passe -->
          <div>
            <label class="form-label">Ancien mot de passe <span class="text-red-500">*</span></label>
            <div class="relative">
              <input v-model="formMdp.ancien" :type="showAncien ? 'text' : 'password'"
                placeholder="••••••••"
                :class="['form-input pr-10', erreursMdp.ancien ? 'border-red-400' : '']"/>
              <button type="button" @click="showAncien = !showAncien"
                class="absolute inset-y-0 right-3 flex items-center text-gray-400 hover:text-gray-600 transition-colors">
                <svg v-if="!showAncien" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M15 12a3 3 0 11-6 0 3 3 0 016 0zM2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                </svg>
                <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"/>
                </svg>
              </button>
            </div>
            <p v-if="erreursMdp.ancien" class="form-error">{{ erreursMdp.ancien }}</p>
          </div>

          <!-- Nouveau mot de passe -->
          <div>
            <label class="form-label">Nouveau mot de passe <span class="text-red-500">*</span></label>
            <div class="relative">
              <input v-model="formMdp.nouveau" :type="showNouveau ? 'text' : 'password'"
                placeholder="Min. 8 caractères"
                :class="['form-input pr-10', erreursMdp.nouveau ? 'border-red-400' : '']"/>
              <button type="button" @click="showNouveau = !showNouveau"
                class="absolute inset-y-0 right-3 flex items-center text-gray-400 hover:text-gray-600 transition-colors">
                <svg v-if="!showNouveau" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M15 12a3 3 0 11-6 0 3 3 0 016 0zM2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                </svg>
                <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"/>
                </svg>
              </button>
            </div>
            <p v-if="erreursMdp.nouveau" class="form-error">{{ erreursMdp.nouveau }}</p>
            <div v-if="formMdp.nouveau" class="mt-2">
              <div class="h-1.5 bg-gray-100 rounded-full overflow-hidden">
                <div :class="['h-full rounded-full transition-all duration-300', forceMdp.color, forceMdp.width]"></div>
              </div>
              <p class="text-xs mt-1 text-gray-500">{{ forceMdp.label }}</p>
            </div>
          </div>

          <!-- Confirmer -->
          <div>
            <label class="form-label">Confirmer <span class="text-red-500">*</span></label>
            <div class="relative">
              <input v-model="formMdp.confirmer" :type="showConfirmer ? 'text' : 'password'"
                placeholder="Répéter le nouveau mot de passe"
                :class="['form-input pr-10', erreursMdp.confirmer ? 'border-red-400' : '']"/>
              <button type="button" @click="showConfirmer = !showConfirmer"
                class="absolute inset-y-0 right-3 flex items-center text-gray-400 hover:text-gray-600 transition-colors">
                <svg v-if="!showConfirmer" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M15 12a3 3 0 11-6 0 3 3 0 016 0zM2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                </svg>
                <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"/>
                </svg>
              </button>
            </div>
            <p v-if="erreursMdp.confirmer" class="form-error">{{ erreursMdp.confirmer }}</p>
          </div>

          <div class="flex justify-end">
            <button type="submit" :disabled="isSavingMdp" class="btn-primary">
              <svg v-if="isSavingMdp" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
              Changer le mot de passe
            </button>
          </div>
        </form>
          </div>
        </div>
      </div>

    </div>
  </AppLayout>
</template>
