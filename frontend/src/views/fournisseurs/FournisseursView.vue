<script setup>
// ============================================================
// VUE : FournisseursView — Liste des fournisseurs
//
// Composants utilisés :
//   FournisseurStatsBar  → métriques globales (total, actifs, inactifs)
//   FournisseurFilters   → recherche texte + filtre statut
//   FournisseurTableRow  → une ligne du tableau
//   FournisseurModal     → modal création / modification
// ============================================================

import { ref, computed, onMounted } from 'vue'
import AppLayout              from '@/layout/AppLayout.vue'
import FournisseurStatsBar    from '@/components/fournisseurs/FournisseurStatsBar.vue'
import FournisseurFilters     from '@/components/fournisseurs/FournisseurFilters.vue'
import FournisseurTableRow    from '@/components/fournisseurs/FournisseurTableRow.vue'
import FournisseurModal       from '@/components/fournisseurs/FournisseurModal.vue'
import fournisseurService     from '@/services/fournisseurService'
import { usePermissions }     from '@/composables/usePermissions'

const { peutGererFournisseurs } = usePermissions()

// -------------------------------------------------------
// DONNÉES
// -------------------------------------------------------
const fournisseurs = ref([])
const isLoading    = ref(false)
const erreur       = ref('')

onMounted(async () => { await chargerFournisseurs() })

async function chargerFournisseurs() {
  isLoading.value = true
  erreur.value    = ''
  try {
    fournisseurs.value = await fournisseurService.findAll()
  } catch {
    erreur.value = 'Impossible de charger les fournisseurs. Vérifiez que le backend est démarré.'
  } finally {
    isLoading.value = false
  }
}

// -------------------------------------------------------
// FILTRES
// -------------------------------------------------------
const recherche    = ref('')
const filtreStatut = ref('')

const fournisseursFiltres = computed(() => {
  return fournisseurs.value.filter((f) => {
    const texte = recherche.value.toLowerCase()
    const matchTexte =
      !texte ||
      f.nom.toLowerCase().includes(texte) ||
      f.email.toLowerCase().includes(texte) ||
      f.contactPrincipal.toLowerCase().includes(texte)
    const matchStatut =
      !filtreStatut.value ||
      (filtreStatut.value === 'actif'   &&  f.actif) ||
      (filtreStatut.value === 'inactif' && !f.actif)
    return matchTexte && matchStatut
  })
})

// -------------------------------------------------------
// MODAL
// -------------------------------------------------------
const modalVisible      = ref(false)
const fournisseurEditeId = ref(null)

const ouvrirCreation = () => { fournisseurEditeId.value = null; modalVisible.value = true }
const ouvrirEdition  = (id) => { fournisseurEditeId.value = id; modalVisible.value = true }
const fermerModal    = () => { modalVisible.value = false }
const apresEnregistrement = async () => { await chargerFournisseurs() }

// -------------------------------------------------------
// TOGGLE STATUT
// -------------------------------------------------------
const toggleStatut = async (fournisseur) => {
  try {
    const updated = await fournisseurService.toggleStatut(fournisseur.id)
    fournisseur.actif = updated.actif
  } catch {
    alert('Erreur lors de la mise à jour du statut.')
  }
}
</script>

<template>
  <AppLayout>
    <div class="space-y-6">

      <!-- EN-TÊTE -->
      <div class="flex flex-col sm:flex-row sm:items-center gap-4 justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Fournisseurs</h1>
          <p class="text-sm text-gray-500 mt-1">
            {{ fournisseurs.length }} fournisseur{{ fournisseurs.length > 1 ? 's' : '' }} enregistré{{ fournisseurs.length > 1 ? 's' : '' }}
          </p>
        </div>
        <button v-if="peutGererFournisseurs" @click="ouvrirCreation" class="btn-primary">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
          </svg>
          Nouveau fournisseur
        </button>
      </div>

      <!-- MÉTRIQUES -->
      <FournisseurStatsBar :fournisseurs="fournisseurs" />

      <!-- FILTRES -->
      <FournisseurFilters v-model:recherche="recherche" v-model:statut="filtreStatut" />

      <!-- Erreur API -->
      <div v-if="erreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">{{ erreur }}</div>

      <!-- LOADING -->
      <div v-if="isLoading" class="card p-12 text-center text-gray-400">
        <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-blue-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
        Chargement des fournisseurs…
      </div>

      <!-- TABLEAU -->
      <div v-if="!isLoading" class="card p-0 overflow-hidden">
        <div v-if="fournisseursFiltres.length === 0" class="p-12 text-center text-gray-400 text-sm">
          Aucun fournisseur ne correspond aux critères de recherche.
        </div>
        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50 border-b border-gray-100">
              <tr>
                <th class="table-header">Nom</th>
                <th class="table-header">Adresse</th>
                <th class="table-header">Téléphone</th>
                <th class="table-header">Email</th>
                <th class="table-header">Contact principal</th>
                <th class="table-header">Statut</th>
                <th class="table-header text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <FournisseurTableRow
                v-for="fournisseur in fournisseursFiltres"
                :key="fournisseur.id"
                :fournisseur="fournisseur"
                :peut-modifier="peutGererFournisseurs"
                @modifier="ouvrirEdition"
                @toggle="toggleStatut"
              />
            </tbody>
          </table>
        </div>
      </div>

    </div>

    <!-- MODAL -->
    <FournisseurModal
      :visible="modalVisible"
      :fournisseur-id="fournisseurEditeId"
      @fermer="fermerModal"
      @sauvegarde="apresEnregistrement"
    />

  </AppLayout>
</template>
