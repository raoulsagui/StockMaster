<script setup>
// ============================================================
// VUE : Gestion des utilisateurs (Module 1)
//
// Cette vue est un orchestrateur — elle gère uniquement :
//   - Le chargement des données depuis l'API
//   - La logique de filtres et pagination
//   - Les actions métier (toggle, reset mdp)
//   - L'ouverture/fermeture des modals
//
// Le HTML est délégué aux composants dédiés.
// ============================================================

import { ref, computed, watch, onMounted } from 'vue'
import AppLayout                          from '@/layout/AppLayout.vue'
import utilisateurService                 from '@/services/utilisateurService'
import UtilisateurFiltresComponent        from '@/components/utilisateurs/UtilisateurFiltresComponent.vue'
import UtilisateurFiltresMobileComponent  from '@/components/utilisateurs/UtilisateurFiltresMobileComponent.vue'
import UtilisateurTableComponent          from '@/components/utilisateurs/UtilisateurTableComponent.vue'
import UtilisateurModalCrudComponent      from '@/components/utilisateurs/UtilisateurModalCrudComponent.vue'
import { useToast }                       from '@/composables/useToast'

// -------------------------------------------------------
// DONNÉES
// -------------------------------------------------------
const toast = useToast()
const utilisateurs = ref([])
const isLoading    = ref(false)
const erreur       = ref('')

onMounted(() => chargerUtilisateurs())

async function chargerUtilisateurs() {
  isLoading.value = true
  erreur.value    = ''
  try {
    utilisateurs.value = await utilisateurService.findAll()
  } catch {
    erreur.value = 'Impossible de charger les utilisateurs.'
  } finally {
    isLoading.value = false
  }
}

// -------------------------------------------------------
// FILTRES
// -------------------------------------------------------
const recherche    = ref('')
const filtreRole   = ref('')
const filtreStatut = ref('')
const showFiltresMobile = ref(false)

// -------------------------------------------------------
// PAGINATION
// -------------------------------------------------------
const pageCourante = ref(1)
const parPage      = ref(10)

watch([recherche, filtreRole, filtreStatut], () => { pageCourante.value = 1 })

const utilisateursFiltres = computed(() =>
  utilisateurs.value.filter(u => {
    const texte = recherche.value.toLowerCase()
    const matchTexte =
      !texte ||
      u.nom.toLowerCase().includes(texte) ||
      u.prenom.toLowerCase().includes(texte) ||
      u.email.toLowerCase().includes(texte)
    const matchRole   = !filtreRole.value   || u.role   === filtreRole.value
    const matchStatut =
      !filtreStatut.value ||
      (filtreStatut.value === 'actif'   &&  u.actif) ||
      (filtreStatut.value === 'inactif' && !u.actif)
    return matchTexte && matchRole && matchStatut
  })
)

const totalPages = computed(() =>
  Math.ceil(utilisateursFiltres.value.length / parPage.value) || 1
)

const utilisateursPagines = computed(() => {
  const debut = (pageCourante.value - 1) * parPage.value
  return utilisateursFiltres.value.slice(debut, debut + parPage.value)
})

// -------------------------------------------------------
// MODAL CRUD
// -------------------------------------------------------
const showModal   = ref(false)
const modalMode   = ref('creer')          // 'creer' | 'modifier'
const modalData   = ref(null)             // données pré-remplies en modification
const isSubmitting = ref(false)
const erreurApi   = ref('')

function ouvrirCreer() {
  modalMode.value  = 'creer'
  modalData.value  = null
  erreurApi.value  = ''
  showModal.value  = true
}

function ouvrirModifier(u) {
  modalMode.value  = 'modifier'
  modalData.value  = { id: u.id, prenom: u.prenom, nom: u.nom, email: u.email, role: u.role }
  erreurApi.value  = ''
  showModal.value  = true
}

async function soumettreModal(payload) {
  isSubmitting.value = true
  erreurApi.value    = ''
  try {
    if (modalMode.value === 'creer') {
      const created = await utilisateurService.creer(payload)
      utilisateurs.value.push(created)
    } else {
      const updated = await utilisateurService.modifier(modalData.value.id, payload)
      const idx = utilisateurs.value.findIndex(u => u.id === updated.id)
      if (idx !== -1) utilisateurs.value[idx] = updated
    }
    showModal.value = false
  } catch (e) {
    const msg = e.response?.data
    erreurApi.value = typeof msg === 'string' ? msg : 'Erreur lors de la sauvegarde.'
  } finally {
    isSubmitting.value = false
  }
}

// -------------------------------------------------------
// ACTIONS DIRECTES
// -------------------------------------------------------
async function toggleStatut(u) {
  try {
    const updated = await utilisateurService.toggleStatut(u.id)
    u.actif = updated.actif
  } catch {
    toast.error('Erreur lors de la mise à jour du statut.')
  }
}

async function reinitialiserMotDePasse(u) {
  try {
    await utilisateurService.reinitialiserMotDePasse(u.id)
    toast.success(`Mot de passe de ${u.prenom} ${u.nom} réinitialisé. Un email a été envoyé.`)
  } catch {
    toast.error('Erreur lors de la réinitialisation.')
  }
}
</script>

<template>
  <AppLayout>
    <div class="space-y-5">

      <!-- EN-TÊTE GRADIENT -->
      <div class="page-header bg-gradient-to-r from-purple-600 to-violet-700 shadow-lg shadow-purple-500/20">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-xl font-bold text-white">Utilisateurs</h1>
            <p class="text-purple-100 text-sm mt-0.5">{{ utilisateurs.length }} comptes enregistrés</p>
          </div>
          <button @click="ouvrirCreer"
            class="inline-flex items-center gap-2 bg-white/20 hover:bg-white/30 text-white px-4 py-2 rounded-xl text-sm font-medium transition-colors backdrop-blur-sm border border-white/20">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
            </svg>
            Nouvel utilisateur
          </button>
        </div>
        <div class="absolute -right-8 -top-8 w-32 h-32 rounded-full bg-white/5 pointer-events-none"></div>
        <div class="absolute -right-4 top-8 w-20 h-20 rounded-full bg-white/5 pointer-events-none"></div>
      </div>

      <!-- FILTRES DESKTOP + MOBILE -->
      <UtilisateurFiltresComponent
        v-model:recherche="recherche"
        v-model:filtreRole="filtreRole"
        v-model:filtreStatut="filtreStatut"
        @ouvrir-filtres-mobile="showFiltresMobile = true"
      />

      <!-- BOTTOM SHEET MOBILE -->
      <UtilisateurFiltresMobileComponent
        :visible="showFiltresMobile"
        :filtreRole="filtreRole"
        :filtreStatut="filtreStatut"
        @fermer="showFiltresMobile = false"
        @update:filtreRole="filtreRole = $event"
        @update:filtreStatut="filtreStatut = $event"
        @reinitialiser="filtreRole = ''; filtreStatut = ''"
      />

      <!-- ERREUR API -->
      <div v-if="erreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">
        {{ erreur }}
      </div>

      <!-- TABLEAU + PAGINATION -->
      <UtilisateurTableComponent
        :utilisateurs="utilisateursPagines"
        :isLoading="isLoading"
        :pageCourante="pageCourante"
        :totalPages="totalPages"
        :parPage="parPage"
        :totalFiltres="utilisateursFiltres.length"
        @modifier="ouvrirModifier"
        @toggle-statut="toggleStatut"
        @reinitialiser-mdp="reinitialiserMotDePasse"
        @page-precedente="pageCourante--"
        @page-suivante="pageCourante++"
        @changer-page="pageCourante = $event"
      />

    </div>

    <!-- MODAL CRUD (création + modification) -->
    <UtilisateurModalCrudComponent
      :visible="showModal"
      :mode="modalMode"
      :initialData="modalData"
      :isLoading="isSubmitting"
      :erreurApi="erreurApi"
      @fermer="showModal = false"
      @soumettre="soumettreModal"
    />

  </AppLayout>
</template>
