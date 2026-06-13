<script setup>
// ============================================================
// VUE : EntrepotsView — Liste des entrepôts
//
// Cette vue orchestre les composants :
//   EntrepotStatsBar → métriques globales
//   EntrepotFilters  → filtres de recherche
//   EntrepotCard     → carte de chaque entrepôt
//
// La vue gère :
//   - Le chargement des données depuis l'API
//   - Le filtrage de la liste
//   - La navigation (vers formulaire, vers zones)
//   - Les actions (toggle statut)
//
// Elle ne contient plus de markup de rendu — tout est délégué
// aux composants enfants.
// ============================================================

import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppLayout from '@/components/layout/AppLayout.vue'
import EntrepotStatsBar from '@/components/entrepots/EntrepotStatsBar.vue'
import EntrepotFilters  from '@/components/entrepots/EntrepotFilters.vue'
import EntrepotCard     from '@/components/entrepots/EntrepotCard.vue'
import entrepotService  from '@/services/entrepotService'

const router = useRouter()

// -------------------------------------------------------
// DONNÉES
// -------------------------------------------------------
const entrepots = ref([])
const isLoading = ref(false)
const erreur    = ref('')

onMounted(async () => {
  await chargerEntrepots()
})

async function chargerEntrepots() {
  isLoading.value = true
  erreur.value    = ''
  try {
    entrepots.value = await entrepotService.findAll()
  } catch {
    erreur.value = 'Impossible de charger les entrepôts. Vérifiez que le backend est démarré.'
  } finally {
    isLoading.value = false
  }
}

// -------------------------------------------------------
// FILTRES — modèles liés aux composants via v-model
// -------------------------------------------------------
const recherche    = ref('')
const filtreStatut = ref('')

/** Liste filtrée recalculée à chaque changement de filtre */
const entrepotsFiltres = computed(() => {
  return entrepots.value.filter((e) => {
    const texte = recherche.value.toLowerCase()
    const matchTexte =
      !texte ||
      e.nom.toLowerCase().includes(texte) ||
      e.adresse.toLowerCase().includes(texte)

    const matchStatut =
      !filtreStatut.value ||
      (filtreStatut.value === 'actif'   &&  e.actif) ||
      (filtreStatut.value === 'inactif' && !e.actif)

    return matchTexte && matchStatut
  })
})

// -------------------------------------------------------
// ACTIONS — Déclenchées par les événements des composants
// -------------------------------------------------------

/** Navigation vers la page de création */
const creerEntrepot = () => router.push({ name: 'entrepots-creer' })

/** Navigation vers la page de modification */
const modifierEntrepot = (id) => router.push({ name: 'entrepots-modifier', params: { id } })

/** Navigation vers les zones en pré-filtrant sur cet entrepôt */
const voirZones = (id) => router.push({ name: 'zones', query: { entrepotId: id } })

/** Appel API toggle + mise à jour locale du statut */
const toggleStatut = async (entrepot) => {
  try {
    const updated = await entrepotService.toggleStatut(entrepot.id)
    entrepot.actif = updated.actif
  } catch {
    alert('Erreur lors de la mise à jour du statut.')
  }
}
</script>

<template>
  <AppLayout>
    <div class="space-y-6">

      <!-- ===== EN-TÊTE ===== -->
      <div class="flex flex-col sm:flex-row sm:items-center gap-4 justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Entrepôts</h1>
          <p class="text-sm text-gray-500 mt-1">
            {{ entrepots.length }} entrepôt{{ entrepots.length > 1 ? 's' : '' }} enregistré{{ entrepots.length > 1 ? 's' : '' }}
          </p>
        </div>
        <button @click="creerEntrepot" class="btn-primary">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
          </svg>
          Nouvel entrepôt
        </button>
      </div>

      <!-- ===== MÉTRIQUES GLOBALES ===== -->
      <!-- Passe la liste complète (non filtrée) pour des stats sur tout le parc -->
      <EntrepotStatsBar :entrepots="entrepots" />

      <!-- ===== FILTRES ===== -->
      <EntrepotFilters
        v-model:recherche="recherche"
        v-model:statut="filtreStatut"
      />

      <!-- Erreur API -->
      <div v-if="erreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">
        {{ erreur }}
      </div>

      <!-- ===== LOADING ===== -->
      <div v-if="isLoading" class="card p-12 text-center text-gray-400">
        <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-blue-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
        Chargement des entrepôts…
      </div>

      <!-- ===== GRILLE DES CARTES ===== -->
      <div v-if="!isLoading">

        <!-- Aucun résultat -->
        <div v-if="entrepotsFiltres.length === 0" class="card p-12 text-center text-gray-400 text-sm">
          Aucun entrepôt ne correspond aux critères de recherche.
        </div>

        <!-- Grille 2 colonnes sur desktop -->
        <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-5">
          <!--
            EntrepotCard émet 3 événements :
              @voir-zones → voirZones(id)
              @modifier   → modifierEntrepot(id)
              @toggle     → toggleStatut(entrepot)
          -->
          <EntrepotCard
            v-for="entrepot in entrepotsFiltres"
            :key="entrepot.id"
            :entrepot="entrepot"
            @voir-zones="voirZones"
            @modifier="modifierEntrepot"
            @toggle="toggleStatut"
          />
        </div>

      </div>

    </div>
  </AppLayout>
</template>
