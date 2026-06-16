<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppLayout        from '@/layout/AppLayout.vue'
import EntrepotStatsBar from '@/components/entrepots/EntrepotStatsBar.vue'
import EntrepotFilters  from '@/components/entrepots/EntrepotFilters.vue'
import EntrepotTableRow from '@/components/entrepots/EntrepotTableRow.vue'
import EntrepotModal    from '@/components/entrepots/EntrepotModal.vue'
import entrepotService  from '@/services/entrepotService'

const router = useRouter()

// -------------------------------------------------------
// DONNÉES
// -------------------------------------------------------
const entrepots = ref([])
const isLoading = ref(false)
const erreur    = ref('')

onMounted(async () => { await chargerEntrepots() })

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
// FILTRES
// -------------------------------------------------------
const recherche    = ref('')
const filtreStatut = ref('')

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
// MODAL
// -------------------------------------------------------
const modalVisible    = ref(false)
const entrepotEditeId = ref(null)

const ouvrirCreation  = () => { entrepotEditeId.value = null; modalVisible.value = true }
const ouvrirEdition   = (id) => { entrepotEditeId.value = id; modalVisible.value = true }
const fermerModal     = () => { modalVisible.value = false }
const apresEnregistrement = async () => { await chargerEntrepots() }

// -------------------------------------------------------
// AUTRES ACTIONS
// -------------------------------------------------------
const voirZones = (id) => router.push({ name: 'zones', query: { entrepotId: id } })

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

      <!-- EN-TÊTE -->
      <div class="flex flex-col sm:flex-row sm:items-center gap-4 justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Entrepôts</h1>
          <p class="text-sm text-gray-500 mt-1">
            {{ entrepots.length }} entrepôt{{ entrepots.length > 1 ? 's' : '' }} enregistré{{ entrepots.length > 1 ? 's' : '' }}
          </p>
        </div>
        <button @click="ouvrirCreation" class="btn-primary">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
          </svg>
          Nouvel entrepôt
        </button>
      </div>

      <!-- MÉTRIQUES -->
      <EntrepotStatsBar :entrepots="entrepots" />

      <!-- FILTRES -->
      <EntrepotFilters v-model:recherche="recherche" v-model:statut="filtreStatut" />

      <!-- Erreur API -->
      <div v-if="erreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">{{ erreur }}</div>

      <!-- LOADING -->
      <div v-if="isLoading" class="card p-12 text-center text-gray-400">
        <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-blue-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
        Chargement des entrepôts…
      </div>

      <!-- TABLEAU -->
      <div v-if="!isLoading">
        <div v-if="entrepotsFiltres.length === 0" class="card p-12 text-center text-gray-400 text-sm">
          Aucun entrepôt ne correspond aux critères de recherche.
        </div>
        <div v-else class="card p-0 overflow-hidden">
          <div class="overflow-x-auto">
            <table class="w-full">
              <thead class="bg-gray-50 border-b border-gray-100">
                <tr>
                  <th class="table-header">Entrepôt</th>
                  <th class="table-header">Adresse</th>
                  <th class="table-header">Responsable</th>
                  <th class="table-header">Occupation</th>
                  <th class="table-header">Cap. totale</th>
                  <th class="table-header">Cap. utilisée</th>
                  <th class="table-header">Zones</th>
                  <th class="table-header">Statut</th>
                  <th class="table-header text-right">Actions</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-50">
                <EntrepotTableRow
                  v-for="entrepot in entrepotsFiltres"
                  :key="entrepot.id"
                  :entrepot="entrepot"
                  @voir-zones="voirZones"
                  @modifier="ouvrirEdition"
                  @toggle="toggleStatut"
                />
              </tbody>
            </table>
          </div>
        </div>
      </div>

    </div>

    <!-- MODAL -->
    <EntrepotModal
      :visible="modalVisible"
      :entrepot-id="entrepotEditeId"
      @fermer="fermerModal"
      @sauvegarde="apresEnregistrement"
    />

  </AppLayout>
</template>
