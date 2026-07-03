<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AppLayout     from '@/layout/AppLayout.vue'
import ZoneStatsBar  from '@/components/zones/ZoneStatsBar.vue'
import ZoneFilters   from '@/components/zones/ZoneFilters.vue'
import ZoneTableRow  from '@/components/zones/ZoneTableRow.vue'
import ZoneModal     from '@/components/zones/ZoneModal.vue'
import zoneService    from '@/services/zoneService'
import entrepotService from '@/services/entrepotService'
import { usePermissions } from '@/composables/usePermissions'
import { useToast }       from '@/composables/useToast'

const router = useRouter()
const route  = useRoute()
const { peutGererEntrepots } = usePermissions()
const toast = useToast()

// -------------------------------------------------------
// DONNÉES
// -------------------------------------------------------
const zones     = ref([])
const entrepots = ref([])
const isLoading = ref(false)
const erreur    = ref('')

// -------------------------------------------------------
// FILTRES
// -------------------------------------------------------
const filtreEntrepot = ref(route.query.entrepotId ? Number(route.query.entrepotId) : '')
const filtreType     = ref('')
const filtreStatut   = ref('')
const recherche      = ref('')

watch(() => route.query.entrepotId, (val) => {
  filtreEntrepot.value = val ? Number(val) : ''
})

onMounted(async () => {
  await Promise.all([chargerZones(), chargerEntrepots()])
})

async function chargerZones() {
  isLoading.value = true
  erreur.value    = ''
  try {
    zones.value = await zoneService.findAll()
  } catch {
    erreur.value = 'Impossible de charger les zones.'
  } finally {
    isLoading.value = false
  }
}

async function chargerEntrepots() {
  try {
    entrepots.value = await entrepotService.findAll()
  } catch { /* non bloquant */ }
}

const zonesFiltrees = computed(() => {
  return zones.value.filter((z) => {
    const texte = recherche.value.toLowerCase()
    const matchTexte    = !texte || z.nom.toLowerCase().includes(texte)
    const matchEntrepot = !filtreEntrepot.value || z.entrepot?.id === filtreEntrepot.value
    const matchType     = !filtreType.value     || z.type === filtreType.value
    const matchStatut   =
      !filtreStatut.value ||
      (filtreStatut.value === 'actif'   &&  z.actif) ||
      (filtreStatut.value === 'inactif' && !z.actif)
    return matchTexte && matchEntrepot && matchType && matchStatut
  })
})

// -------------------------------------------------------
// MODAL
// -------------------------------------------------------
const modalVisible = ref(false)
const zoneEditeeId = ref(null)

const ouvrirCreation = () => { zoneEditeeId.value = null; modalVisible.value = true }
const ouvrirEdition  = (id) => { zoneEditeeId.value = id; modalVisible.value = true }
const fermerModal    = () => { modalVisible.value = false }
const apresEnregistrement = async () => { await chargerZones() }

// -------------------------------------------------------
// AUTRES ACTIONS
// -------------------------------------------------------
const toggleStatut = async (zone) => {
  try {
    const updated = await zoneService.toggleStatut(zone.id)
    zone.actif = updated.actif
  } catch {
    toast.error('Erreur lors de la mise à jour du statut.')
  }
}

const reinitialiserFiltres = () => {
  recherche.value      = ''
  filtreEntrepot.value = ''
  filtreType.value     = ''
  filtreStatut.value   = ''
  router.replace({ name: 'zones' })
}
</script>

<template>
  <AppLayout>
    <div class="space-y-6">

      <!-- EN-TÊTE -->
      <div class="flex flex-col sm:flex-row sm:items-center gap-4 justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Zones de stockage</h1>
          <p class="text-sm text-gray-500 mt-1">
            {{ zones.length }} zone{{ zones.length > 1 ? 's' : '' }} au total
          </p>
        </div>
        <button v-if="peutGererEntrepots" @click="ouvrirCreation" class="btn-primary">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
          </svg>
          Nouvelle zone
        </button>
      </div>

      <!-- MÉTRIQUES -->
      <ZoneStatsBar :zones="zonesFiltrees" :total-absolu="zones.length" />

      <!-- FILTRES -->
      <ZoneFilters
        v-model:recherche="recherche"
        v-model:entrepot-id="filtreEntrepot"
        v-model:type="filtreType"
        v-model:statut="filtreStatut"
        :entrepots="entrepots"
        @reset="reinitialiserFiltres"
      />

      <!-- Erreur API -->
      <div v-if="erreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">{{ erreur }}</div>

      <!-- LOADING -->
      <div v-if="isLoading" class="card p-12 text-center text-gray-400">
        <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-blue-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
        Chargement des zones…
      </div>

      <!-- TABLEAU -->
      <div v-if="!isLoading" class="card p-0 overflow-hidden">
        <div v-if="zonesFiltrees.length === 0" class="p-12 text-center text-gray-400 text-sm">
          Aucune zone ne correspond aux critères de recherche.
        </div>
        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50 border-b border-gray-100">
              <tr>
                <th class="table-header">Zone</th>
                <th class="table-header">Entrepôt</th>
                <th class="table-header">Type</th>
                <th class="table-header">Capacité</th>
                <th class="table-header">Statut</th>
                <th class="table-header text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <ZoneTableRow
                v-for="zone in zonesFiltrees"
                :key="zone.id"
                :zone="zone"
                :peut-modifier="peutGererEntrepots"
                @modifier="ouvrirEdition"
                @toggle="toggleStatut"
              />
            </tbody>
          </table>
        </div>
      </div>

    </div>

    <!-- MODAL -->
    <ZoneModal
      :visible="modalVisible"
      :zone-id="zoneEditeeId"
      :entrepot-id="filtreEntrepot || null"
      @fermer="fermerModal"
      @sauvegarde="apresEnregistrement"
    />

  </AppLayout>
</template>
