<script setup>
// ============================================================
// MODULE 1 : LISTE DES UTILISATEURS — Branchée sur l'API
// ============================================================

import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppLayout from '@/components/layout/AppLayout.vue'
import utilisateurService from '@/services/utilisateurService'

const router = useRouter()

// -------------------------------------------------------
// DONNÉES — Chargées depuis l'API Spring Boot
// -------------------------------------------------------
const utilisateurs  = ref([])
const isLoading     = ref(false)
const erreur        = ref('')

// Charge tous les utilisateurs au montage du composant
// onMounted = exécuté une fois que la vue est affichée
onMounted(async () => {
  await chargerUtilisateurs()
})

async function chargerUtilisateurs() {
  isLoading.value = true
  erreur.value    = ''
  try {
    // GET /api/utilisateurs → retourne la liste complète
    utilisateurs.value = await utilisateurService.findAll()
  } catch (e) {
    erreur.value = 'Impossible de charger les utilisateurs. Vérifiez que le backend est démarré.'
  } finally {
    isLoading.value = false
  }
}

// -------------------------------------------------------
// FILTRES ET RECHERCHE
// -------------------------------------------------------

// Texte de recherche saisi par l'utilisateur
const recherche = ref('')

// Filtre par rôle ('' = tous les rôles)
const filtreRole = ref('')

// Filtre par statut ('' = tous, 'actif', 'inactif')
const filtreStatut = ref('')

// computed() : liste filtrée recalculée automatiquement
// dès que recherche, filtreRole ou filtreStatut changent
const utilisateursFiltres = computed(() => {
  return utilisateurs.value.filter((u) => {
    // Filtre texte : cherche dans nom, prénom et email
    const texte = recherche.value.toLowerCase()
    const matchTexte =
      !texte ||
      u.nom.toLowerCase().includes(texte) ||
      u.prenom.toLowerCase().includes(texte) ||
      u.email.toLowerCase().includes(texte)

    // Filtre rôle exact
    const matchRole = !filtreRole.value || u.role === filtreRole.value

    // Filtre statut
    const matchStatut =
      !filtreStatut.value ||
      (filtreStatut.value === 'actif'   &&  u.actif) ||
      (filtreStatut.value === 'inactif' && !u.actif)

    return matchTexte && matchRole && matchStatut
  })
})

// -------------------------------------------------------
// ACTIONS — Branchées sur l'API
// -------------------------------------------------------

const creerUtilisateur = () => router.push({ name: 'utilisateurs-creer' })
const modifierUtilisateur = (id) => router.push({ name: 'utilisateurs-modifier', params: { id } })

// Active ou désactive un compte via PATCH /utilisateurs/{id}/statut
const toggleStatut = async (utilisateur) => {
  try {
    const updated = await utilisateurService.toggleStatut(utilisateur.id)
    // Met à jour localement sans recharger toute la liste
    utilisateur.actif = updated.actif
  } catch (e) {
    alert('Erreur lors de la mise à jour du statut.')
  }
}

// Déclenche la réinitialisation du mot de passe
const reinitialiserMotDePasse = async (utilisateur) => {
  if (!confirm(`Réinitialiser le mot de passe de ${utilisateur.prenom} ${utilisateur.nom} ?`)) return
  try {
    await utilisateurService.reinitialiserMotDePasse(utilisateur.id)
    alert(`Mot de passe réinitialisé. Un email sera envoyé à ${utilisateur.email}`)
  } catch (e) {
    alert('Erreur lors de la réinitialisation.')
  }
}

// -------------------------------------------------------
// HELPERS D'AFFICHAGE
// -------------------------------------------------------

const rolesLabels = {
  ADMIN:        'Administrateur',
  GESTIONNAIRE: 'Gestionnaire',
  MAGASINIER:   'Magasinier',
  AUDITEUR:     'Auditeur',
}

// Initiales pour l'avatar
const getInitiales = (u) => `${u.prenom[0]}${u.nom[0]}`.toUpperCase()

// Couleur d'avatar selon le rôle
const avatarColors = {
  ADMIN:        'bg-purple-600',
  GESTIONNAIRE: 'bg-blue-600',
  MAGASINIER:   'bg-green-600',
  AUDITEUR:     'bg-yellow-500',
}
</script>

<template>
  <AppLayout>
    <div class="space-y-6">

      <!-- ===== EN-TÊTE ===== -->
      <div class="flex flex-col sm:flex-row sm:items-center gap-4 justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Utilisateurs</h1>
          <p class="text-sm text-gray-500 mt-1">{{ utilisateurs.length }} utilisateurs enregistrés</p>
        </div>
        <button @click="creerUtilisateur" class="btn-primary">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
          </svg>
          Nouvel utilisateur
        </button>
      </div>

      <!-- ===== FILTRES ===== -->
      <div class="card p-4">
        <div class="flex flex-col sm:flex-row gap-3">

          <!-- Recherche texte -->
          <div class="relative flex-1">
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
              fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0"/>
            </svg>
            <input
              v-model="recherche"
              type="text"
              placeholder="Rechercher par nom, prénom ou email…"
              class="form-input pl-9"
            />
          </div>

          <!-- Filtre rôle -->
          <select v-model="filtreRole" class="form-input w-full sm:w-48">
            <option value="">Tous les rôles</option>
            <option value="ADMIN">Administrateur</option>
            <option value="GESTIONNAIRE">Gestionnaire</option>
            <option value="MAGASINIER">Magasinier</option>
            <option value="AUDITEUR">Auditeur</option>
          </select>

          <!-- Filtre statut -->
          <select v-model="filtreStatut" class="form-input w-full sm:w-44">
            <option value="">Tous les statuts</option>
            <option value="actif">Actif</option>
            <option value="inactif">Inactif</option>
          </select>

        </div>
      </div>

      <!-- Message d'erreur API -->
      <div v-if="erreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">
        {{ erreur }}
      </div>

      <!-- ===== TABLEAU ===== -->
      <div class="card p-0 overflow-hidden">
        <!-- Skeleton loader pendant le chargement -->
        <div v-if="isLoading" class="p-12 text-center text-gray-400">
          <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-blue-500" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
          Chargement des utilisateurs…
        </div>
        <div v-if="!isLoading" class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50 border-b border-gray-100">
              <tr>
                <th class="table-header">Utilisateur</th>
                <th class="table-header">Email</th>
                <th class="table-header">Rôle</th>
                <th class="table-header">Statut</th>
                <th class="table-header">Créé le</th>
                <th class="table-header text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">

              <!-- Aucun résultat -->
              <tr v-if="utilisateursFiltres.length === 0">
                <td colspan="6" class="px-6 py-12 text-center text-gray-400 text-sm">
                  Aucun utilisateur ne correspond aux critères.
                </td>
              </tr>

              <!-- Lignes -->
              <tr
                v-for="u in utilisateursFiltres"
                :key="u.id"
                class="hover:bg-gray-50 transition-colors"
              >
                <!-- Avatar + Nom -->
                <td class="table-cell">
                  <div class="flex items-center gap-3">
                    <div :class="['w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0', avatarColors[u.role]]">
                      <span class="text-white text-xs font-bold">{{ getInitiales(u) }}</span>
                    </div>
                    <span class="font-medium text-gray-900">{{ u.prenom }} {{ u.nom }}</span>
                  </div>
                </td>

                <td class="table-cell text-gray-500">{{ u.email }}</td>

                <!-- Badge rôle : classe dynamique .badge-ADMIN etc. -->
                <td class="table-cell">
                  <span :class="`badge-${u.role}`">{{ rolesLabels[u.role] }}</span>
                </td>

                <td class="table-cell">
                  <span :class="u.actif ? 'badge-actif' : 'badge-inactif'">
                    {{ u.actif ? 'Actif' : 'Inactif' }}
                  </span>
                </td>

                <td class="table-cell text-gray-400">{{ u.dateCreation }}</td>

                <!-- Actions -->
                <td class="table-cell">
                  <div class="flex items-center justify-end gap-2">

                    <!-- Modifier -->
                    <button
                      @click="modifierUtilisateur(u.id)"
                      class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                      title="Modifier"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                      </svg>
                    </button>

                    <!-- Réinitialiser mot de passe -->
                    <button
                      @click="reinitialiserMotDePasse(u)"
                      class="p-1.5 text-gray-400 hover:text-yellow-600 hover:bg-yellow-50 rounded-lg transition-colors"
                      title="Réinitialiser le mot de passe"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"/>
                      </svg>
                    </button>

                    <!-- Activer / Désactiver -->
                    <button
                      @click="toggleStatut(u)"
                      :class="[
                        'p-1.5 rounded-lg transition-colors',
                        u.actif
                          ? 'text-gray-400 hover:text-red-600 hover:bg-red-50'
                          : 'text-gray-400 hover:text-green-600 hover:bg-green-50'
                      ]"
                      :title="u.actif ? 'Désactiver' : 'Réactiver'"
                    >
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
      </div>

    </div>
  </AppLayout>
</template>
