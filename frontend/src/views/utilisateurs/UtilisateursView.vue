<script setup>
// ============================================================
// MODULE 1 : LISTE DES UTILISATEURS avec modals CRUD
// ============================================================

import { ref, computed, onMounted, watch } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import utilisateurService from '@/services/utilisateurService'

// -------------------------------------------------------
// DONNÉES
// -------------------------------------------------------
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
const showFiltres  = ref(false)

const nbFiltresActifs = computed(() =>
  (filtreRole.value ? 1 : 0) + (filtreStatut.value ? 1 : 0)
)

// -------------------------------------------------------
// PAGINATION
// -------------------------------------------------------
const parPage    = ref(10)
const pageCourante = ref(1)

// Remettre à la page 1 quand les filtres changent
watch([recherche, filtreRole, filtreStatut], () => { pageCourante.value = 1 })

const utilisateursFiltres = computed(() =>
  utilisateurs.value.filter((u) => {
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

const totalPages       = computed(() => Math.ceil(utilisateursFiltres.value.length / parPage.value) || 1)
const utilisateursPagines = computed(() => {
  const debut = (pageCourante.value - 1) * parPage.value
  return utilisateursFiltres.value.slice(debut, debut + parPage.value)
})

// -------------------------------------------------------
// MODAL CRÉATION
// -------------------------------------------------------
const showModalCreer = ref(false)
const formCreer = ref({ prenom: '', nom: '', email: '', role: '' })
const erreursCreer  = ref({})
const erreurApiCreer = ref('')
const isCreating    = ref(false)

function ouvrirModalCreer() {
  formCreer.value   = { prenom: '', nom: '', email: '', role: '' }
  erreursCreer.value = {}
  erreurApiCreer.value = ''
  showModalCreer.value = true
}

function validerCreer() {
  const e = {}
  if (!formCreer.value.prenom.trim()) e.prenom = 'Le prénom est obligatoire.'
  if (!formCreer.value.nom.trim())    e.nom    = 'Le nom est obligatoire.'
  const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!formCreer.value.email.trim())          e.email = "L'email est obligatoire."
  else if (!emailRe.test(formCreer.value.email)) e.email = "L'email n'est pas valide."
  if (!formCreer.value.role) e.role = 'Veuillez sélectionner un rôle.'
  erreursCreer.value = e
  return Object.keys(e).length === 0
}

async function soumettreCreer() {
  if (!validerCreer()) return
  isCreating.value = true
  erreurApiCreer.value = ''
  try {
    const created = await utilisateurService.creer({
      prenom: formCreer.value.prenom,
      nom:    formCreer.value.nom,
      email:  formCreer.value.email,
      role:   formCreer.value.role,
    })
    utilisateurs.value.push(created)
    showModalCreer.value = false
  } catch (e) {
    const msg = e.response?.data
    erreurApiCreer.value = typeof msg === 'string' ? msg : 'Erreur lors de la création.'
  } finally {
    isCreating.value = false
  }
}

// -------------------------------------------------------
// MODAL MODIFICATION
// -------------------------------------------------------
const showModalModifier = ref(false)
const formModifier      = ref({ id: null, prenom: '', nom: '', email: '', role: '' })
const erreursModifier   = ref({})
const erreurApiModifier = ref('')
const isModifying       = ref(false)

function ouvrirModalModifier(u) {
  formModifier.value   = { id: u.id, prenom: u.prenom, nom: u.nom, email: u.email, role: u.role }
  erreursModifier.value = {}
  erreurApiModifier.value = ''
  showModalModifier.value = true
}

function validerModifier() {
  const e = {}
  if (!formModifier.value.prenom.trim()) e.prenom = 'Le prénom est obligatoire.'
  if (!formModifier.value.nom.trim())    e.nom    = 'Le nom est obligatoire.'
  const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!formModifier.value.email.trim())           e.email = "L'email est obligatoire."
  else if (!emailRe.test(formModifier.value.email)) e.email = "L'email n'est pas valide."
  if (!formModifier.value.role) e.role = 'Veuillez sélectionner un rôle.'
  erreursModifier.value = e
  return Object.keys(e).length === 0
}

async function soumettreModifier() {
  if (!validerModifier()) return
  isModifying.value = true
  erreurApiModifier.value = ''
  try {
    const updated = await utilisateurService.modifier(formModifier.value.id, {
      prenom: formModifier.value.prenom,
      nom:    formModifier.value.nom,
      email:  formModifier.value.email,
      role:   formModifier.value.role,
    })
    const idx = utilisateurs.value.findIndex(u => u.id === updated.id)
    if (idx !== -1) utilisateurs.value[idx] = updated
    showModalModifier.value = false
  } catch (e) {
    const msg = e.response?.data
    erreurApiModifier.value = typeof msg === 'string' ? msg : 'Erreur lors de la modification.'
  } finally {
    isModifying.value = false
  }
}

// -------------------------------------------------------
// ACTIONS DIRECTES
// -------------------------------------------------------
const toggleStatut = async (u) => {
  try {
    const updated = await utilisateurService.toggleStatut(u.id)
    u.actif = updated.actif
  } catch {
    alert('Erreur lors de la mise à jour du statut.')
  }
}

const reinitialiserMotDePasse = async (u) => {
  if (!confirm(`Réinitialiser le mot de passe de ${u.prenom} ${u.nom} ?\nUn email lui sera envoyé.`)) return
  try {
    await utilisateurService.reinitialiserMotDePasse(u.id)
  } catch {
    alert('Erreur lors de la réinitialisation.')
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

const getInitiales = (u) => `${u.prenom[0]}${u.nom[0]}`.toUpperCase()

// Formater la date
const formatDate = (dateStr) => {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' })
}
</script>

<template>
  <AppLayout>
    <div class="space-y-4 md:space-y-6">

      <!-- EN-TÊTE -->
      <div class="flex flex-col sm:flex-row sm:items-center gap-4 justify-between">
        <div class="hidden md:block">
          <h1 class="text-xl md:text-2xl font-bold text-gray-900">Utilisateurs</h1>
          <p class="text-sm text-gray-500 mt-0.5">{{ utilisateurs.length }} utilisateurs enregistrés</p>
        </div>
        <button @click="ouvrirModalCreer" class="btn-primary justify-center self-end sm:self-auto">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
          </svg>
          Nouvel utilisateur
        </button>
      </div>

      <!-- FILTRES -->
      <div class="card p-3 md:p-4">
        <!-- Mobile : recherche + bouton filtres -->
        <div class="flex gap-3 md:hidden">
          <div class="relative flex-1">
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0"/>
            </svg>
            <input v-model="recherche" type="text" placeholder="Rechercher…" class="form-input pl-9"/>
          </div>
          <button @click="showFiltres = true"
            :class="['relative p-2.5 rounded-lg border transition-colors flex-shrink-0',
              nbFiltresActifs ? 'border-blue-500 bg-blue-50 text-blue-600' : 'border-gray-200 text-gray-500 hover:bg-gray-50']">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2a1 1 0 01-.293.707L13 13.414V19a1 1 0 01-.553.894l-4 2A1 1 0 017 21v-7.586L3.293 6.707A1 1 0 013 6V4z"/>
            </svg>
            <span v-if="nbFiltresActifs"
              class="absolute -top-1 -right-1 w-4 h-4 bg-blue-600 text-white text-xs rounded-full flex items-center justify-center">
              {{ nbFiltresActifs }}
            </span>
          </button>
        </div>

        <!-- Desktop : tous les filtres en ligne -->
        <div class="hidden md:flex gap-3">
          <div class="relative flex-1">
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0"/>
            </svg>
            <input v-model="recherche" type="text" placeholder="Rechercher…" class="form-input pl-9"/>
          </div>
          <select v-model="filtreRole" class="form-input w-44">
            <option value="">Tous les rôles</option>
            <option value="ADMIN">Administrateur</option>
            <option value="GESTIONNAIRE">Gestionnaire</option>
            <option value="MAGASINIER">Magasinier</option>
            <option value="AUDITEUR">Auditeur</option>
          </select>
          <select v-model="filtreStatut" class="form-input w-40">
            <option value="">Tous les statuts</option>
            <option value="actif">Actif</option>
            <option value="inactif">Inactif</option>
          </select>
        </div>
      </div>

      <!-- BOTTOM SHEET FILTRES (mobile) -->
      <Teleport to="body">
        <Transition enter-active-class="transition ease-out duration-200" enter-from-class="opacity-0" enter-to-class="opacity-100"
          leave-active-class="transition ease-in duration-150" leave-from-class="opacity-100" leave-to-class="opacity-0">
          <div v-if="showFiltres" class="fixed inset-0 z-50 bg-black/40 md:hidden" @click="showFiltres = false"></div>
        </Transition>
        <Transition enter-active-class="transition ease-out duration-300" enter-from-class="translate-y-full" enter-to-class="translate-y-0"
          leave-active-class="transition ease-in duration-200" leave-from-class="translate-y-0" leave-to-class="translate-y-full">
          <div v-if="showFiltres" class="fixed bottom-0 inset-x-0 z-50 bg-white rounded-t-2xl shadow-2xl md:hidden">
            <div class="flex justify-center pt-3 pb-1">
              <div class="w-10 h-1 bg-gray-200 rounded-full"></div>
            </div>
            <div class="flex items-center justify-between px-5 py-3 border-b border-gray-100">
              <p class="text-sm font-semibold text-gray-800">Filtres</p>
              <button @click="showFiltres = false" class="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            </div>
            <div class="px-5 py-4 space-y-4">
              <div>
                <label class="form-label">Rôle</label>
                <div class="grid grid-cols-2 gap-2 mt-1">
                  <button v-for="r in [['', 'Tous'], ['ADMIN', 'Administrateur'], ['GESTIONNAIRE', 'Gestionnaire'], ['MAGASINIER', 'Magasinier'], ['AUDITEUR', 'Auditeur']]" :key="r[0]"
                    @click="filtreRole = r[0]"
                    :class="['px-3 py-2 rounded-lg text-sm font-medium border transition-colors',
                      filtreRole === r[0] ? 'bg-blue-600 text-white border-blue-600' : 'text-gray-600 border-gray-200 hover:bg-gray-50']">
                    {{ r[1] }}
                  </button>
                </div>
              </div>
              <div>
                <label class="form-label">Statut</label>
                <div class="grid grid-cols-3 gap-2 mt-1">
                  <button v-for="s in [['', 'Tous'], ['actif', 'Actif'], ['inactif', 'Inactif']]" :key="s[0]"
                    @click="filtreStatut = s[0]"
                    :class="['px-3 py-2 rounded-lg text-sm font-medium border transition-colors',
                      filtreStatut === s[0] ? 'bg-blue-600 text-white border-blue-600' : 'text-gray-600 border-gray-200 hover:bg-gray-50']">
                    {{ s[1] }}
                  </button>
                </div>
              </div>
              <button @click="filtreRole = ''; filtreStatut = ''"
                class="w-full py-2 text-sm text-red-500 hover:bg-red-50 rounded-lg transition-colors">
                Réinitialiser les filtres
              </button>
            </div>
          </div>
        </Transition>
      </Teleport>

      <!-- ERREUR -->
      <div v-if="erreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">{{ erreur }}</div>

      <!-- TABLEAU -->
      <div class="card p-0 overflow-hidden">
        <div v-if="isLoading" class="p-12 text-center text-gray-400">
          <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-blue-500" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
          Chargement…
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50 border-b border-gray-100">
              <tr>
                <th class="table-header">Utilisateur</th>
                <th class="table-header hidden md:table-cell">Email</th>
                <th class="table-header">Rôle</th>
                <th class="table-header hidden sm:table-cell">Statut</th>
                <th class="table-header hidden lg:table-cell">Créé le</th>
                <th class="table-header text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <tr v-if="utilisateursFiltres.length === 0">
                <td colspan="6" class="px-6 py-12 text-center text-gray-400 text-sm">
                  Aucun utilisateur ne correspond aux critères.
                </td>
              </tr>
              <tr
                v-for="u in utilisateursPagines"
                :key="u.id"
                class="hover:bg-gray-50 transition-colors"
              >
                <!-- Avatar + Nom -->
                <td class="table-cell">
                  <div class="flex items-center gap-3">
                    <div :class="['w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0', avatarColors[u.role]]">
                      <span class="text-white text-xs font-bold">{{ getInitiales(u) }}</span>
                    </div>
                    <div>
                      <span class="font-medium text-gray-900 block">{{ u.prenom }} {{ u.nom }}</span>
                      <!-- Email visible sur mobile sous le nom -->
                      <span class="text-xs text-gray-400 md:hidden">{{ u.email }}</span>
                    </div>
                  </div>
                </td>
                <td class="table-cell text-gray-500 hidden md:table-cell">{{ u.email }}</td>
                <td class="table-cell">
                  <span :class="`badge-${u.role}`">{{ rolesLabels[u.role] }}</span>
                </td>
                <td class="table-cell hidden sm:table-cell">
                  <span :class="u.actif ? 'badge-actif' : 'badge-inactif'">
                    {{ u.actif ? 'Actif' : 'Inactif' }}
                  </span>
                </td>
                <td class="table-cell text-gray-400 hidden lg:table-cell">{{ formatDate(u.dateCreation) }}</td>
                <!-- Actions -->
                <td class="table-cell">
                  <div class="flex items-center justify-end gap-1">
                    <!-- Modifier -->
                    <button @click="ouvrirModalModifier(u)"
                      class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                      title="Modifier">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                      </svg>
                    </button>
                    <!-- Reset mdp -->
                    <button @click="reinitialiserMotDePasse(u)"
                      class="p-1.5 text-gray-400 hover:text-yellow-600 hover:bg-yellow-50 rounded-lg transition-colors"
                      title="Réinitialiser le mot de passe">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"/>
                      </svg>
                    </button>
                    <!-- Toggle statut -->
                    <button @click="toggleStatut(u)"
                      :class="['p-1.5 rounded-lg transition-colors', u.actif
                        ? 'text-gray-400 hover:text-red-600 hover:bg-red-50'
                        : 'text-gray-400 hover:text-green-600 hover:bg-green-50']"
                      :title="u.actif ? 'Désactiver' : 'Réactiver'">
                      <svg v-if="u.actif" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"/>
                      </svg>
                      <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                      </svg>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- PAGINATION -->
        <div v-if="!isLoading && totalPages > 1"
          class="flex flex-col sm:flex-row items-center justify-between gap-3 px-4 py-3 border-t border-gray-100">
          <p class="text-sm text-gray-500">
            {{ (pageCourante - 1) * parPage + 1 }}–{{ Math.min(pageCourante * parPage, utilisateursFiltres.length) }}
            sur {{ utilisateursFiltres.length }} utilisateurs
          </p>
          <div class="flex items-center gap-1">
            <button @click="pageCourante--" :disabled="pageCourante === 1"
              class="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
              </svg>
            </button>
            <button v-for="p in totalPages" :key="p" @click="pageCourante = p"
              :class="['w-8 h-8 rounded-lg text-sm font-medium transition-colors',
                p === pageCourante ? 'bg-blue-600 text-white' : 'text-gray-600 hover:bg-gray-100']">
              {{ p }}
            </button>
            <button @click="pageCourante++" :disabled="pageCourante === totalPages"
              class="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== MODAL CRÉATION ===== -->
    <Teleport to="body">
      <Transition
        enter-active-class="transition duration-200"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition duration-150"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div v-if="showModalCreer"
          class="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4"
          @click.self="showModalCreer = false"
        >
          <!-- Overlay -->
          <div class="absolute inset-0 bg-black/50" @click="showModalCreer = false"></div>

          <!-- Panneau modal -->
          <div class="relative bg-white w-full sm:max-w-lg sm:rounded-2xl rounded-t-2xl
                      shadow-2xl max-h-[90vh] overflow-y-auto">
            <!-- Header modal -->
            <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 sticky top-0 bg-white rounded-t-2xl">
              <h2 class="text-lg font-semibold text-gray-900">Nouvel utilisateur</h2>
              <button @click="showModalCreer = false"
                class="p-2 rounded-lg text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            </div>

            <!-- Corps -->
            <form @submit.prevent="soumettreCreer" class="px-6 py-5 space-y-4">
              <div v-if="erreurApiCreer" class="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg p-3">
                {{ erreurApiCreer }}
              </div>

              <!-- Prénom + Nom -->
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="form-label">Prénom <span class="text-red-500">*</span></label>
                  <input v-model="formCreer.prenom" type="text" placeholder="Alice"
                    :class="['form-input', erreursCreer.prenom ? 'border-red-400' : '']"/>
                  <p v-if="erreursCreer.prenom" class="form-error">{{ erreursCreer.prenom }}</p>
                </div>
                <div>
                  <label class="form-label">Nom <span class="text-red-500">*</span></label>
                  <input v-model="formCreer.nom" type="text" placeholder="Martin"
                    :class="['form-input', erreursCreer.nom ? 'border-red-400' : '']"/>
                  <p v-if="erreursCreer.nom" class="form-error">{{ erreursCreer.nom }}</p>
                </div>
              </div>

              <!-- Email -->
              <div>
                <label class="form-label">Email <span class="text-red-500">*</span></label>
                <input v-model="formCreer.email" type="email" placeholder="alice@example.fr"
                  :class="['form-input', erreursCreer.email ? 'border-red-400' : '']"/>
                <p v-if="erreursCreer.email" class="form-error">{{ erreursCreer.email }}</p>
              </div>

              <!-- Rôle -->
              <div>
                <label class="form-label">Rôle <span class="text-red-500">*</span></label>
                <select v-model="formCreer.role"
                  :class="['form-input', erreursCreer.role ? 'border-red-400' : '']">
                  <option value="" disabled>Sélectionner…</option>
                  <option value="ADMIN">Administrateur</option>
                  <option value="GESTIONNAIRE">Gestionnaire d'entrepôt</option>
                  <option value="MAGASINIER">Magasinier</option>
                  <option value="AUDITEUR">Auditeur</option>
                </select>
                <p v-if="erreursCreer.role" class="form-error">{{ erreursCreer.role }}</p>
              </div>

              <!-- Actions -->
              <div class="flex gap-3 pt-2">
                <button type="button" @click="showModalCreer = false" class="btn-secondary flex-1">Annuler</button>
                <button type="submit" :disabled="isCreating" class="btn-primary flex-1 justify-center">
                  <svg v-if="isCreating" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                  </svg>
                  Créer l'utilisateur
                </button>
              </div>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ===== MODAL MODIFICATION ===== -->
    <Teleport to="body">
      <Transition
        enter-active-class="transition duration-200"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition duration-150"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div v-if="showModalModifier"
          class="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4"
          @click.self="showModalModifier = false"
        >
          <div class="absolute inset-0 bg-black/50" @click="showModalModifier = false"></div>

          <div class="relative bg-white w-full sm:max-w-lg sm:rounded-2xl rounded-t-2xl
                      shadow-2xl max-h-[90vh] overflow-y-auto">
            <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 sticky top-0 bg-white rounded-t-2xl">
              <h2 class="text-lg font-semibold text-gray-900">Modifier l'utilisateur</h2>
              <button @click="showModalModifier = false"
                class="p-2 rounded-lg text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            </div>

            <form @submit.prevent="soumettreModifier" class="px-6 py-5 space-y-4">
              <div v-if="erreurApiModifier" class="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg p-3">
                {{ erreurApiModifier }}
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="form-label">Prénom <span class="text-red-500">*</span></label>
                  <input v-model="formModifier.prenom" type="text"
                    :class="['form-input', erreursModifier.prenom ? 'border-red-400' : '']"/>
                  <p v-if="erreursModifier.prenom" class="form-error">{{ erreursModifier.prenom }}</p>
                </div>
                <div>
                  <label class="form-label">Nom <span class="text-red-500">*</span></label>
                  <input v-model="formModifier.nom" type="text"
                    :class="['form-input', erreursModifier.nom ? 'border-red-400' : '']"/>
                  <p v-if="erreursModifier.nom" class="form-error">{{ erreursModifier.nom }}</p>
                </div>
              </div>

              <div>
                <label class="form-label">Email <span class="text-red-500">*</span></label>
                <input v-model="formModifier.email" type="email"
                  :class="['form-input', erreursModifier.email ? 'border-red-400' : '']"/>
                <p v-if="erreursModifier.email" class="form-error">{{ erreursModifier.email }}</p>
              </div>

              <div>
                <label class="form-label">Rôle <span class="text-red-500">*</span></label>
                <select v-model="formModifier.role"
                  :class="['form-input', erreursModifier.role ? 'border-red-400' : '']">
                  <option value="" disabled>Sélectionner…</option>
                  <option value="ADMIN">Administrateur</option>
                  <option value="GESTIONNAIRE">Gestionnaire d'entrepôt</option>
                  <option value="MAGASINIER">Magasinier</option>
                  <option value="AUDITEUR">Auditeur</option>
                </select>
                <p v-if="erreursModifier.role" class="form-error">{{ erreursModifier.role }}</p>
              </div>

              <div class="flex gap-3 pt-2">
                <button type="button" @click="showModalModifier = false" class="btn-secondary flex-1">Annuler</button>
                <button type="submit" :disabled="isModifying" class="btn-primary flex-1 justify-center">
                  <svg v-if="isModifying" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                  </svg>
                  Enregistrer
                </button>
              </div>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>

  </AppLayout>
</template>
