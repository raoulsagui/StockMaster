<script setup>
/**
 * Vue principale du module Inventaires.
 *
 * Affiche :
 *   - Statistiques (total, en cours, brouillons, validés)
 *   - Filtres (statut, type, texte)
 *   - Tableau paginé de tous les inventaires
 *   - Modal de création
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppLayout            from '@/layout/AppLayout.vue'
import InventaireStatsBar   from '@/components/inventaires/InventaireStatsBar.vue'
import InventaireFilters    from '@/components/inventaires/InventaireFilters.vue'
import InventaireTableRow   from '@/components/inventaires/InventaireTableRow.vue'
import InventaireModal      from '@/components/inventaires/InventaireModal.vue'
import inventaireService    from '@/services/inventaireService'

const router = useRouter()

// -------------------------------------------------------
// ÉTAT
// -------------------------------------------------------
const inventaires  = ref([])
const stats        = ref({})
const isLoading    = ref(false)
const erreur       = ref('')

// Pagination
const page         = ref(0)
const totalPages   = ref(0)
const totalItems   = ref(0)
const pageSize     = 20

// Filtres
const recherche    = ref('')
const filtreStatut = ref('')
const filtreType   = ref('')

// Modal
const modalVisible = ref(false)

// -------------------------------------------------------
// CHARGEMENT
// -------------------------------------------------------
onMounted(async () => {
  await Promise.all([charger(), chargerStats()])
})

async function charger() {
  isLoading.value = true
  erreur.value    = ''
  try {
    const data = await inventaireService.findAll(page.value, pageSize)
    inventaires.value = data.content
    totalPages.value  = data.totalPages
    totalItems.value  = data.totalElements
  } catch {
    erreur.value = 'Impossible de charger les inventaires. Vérifiez que le backend est démarré.'
  } finally {
    isLoading.value = false
  }
}

async function chargerStats() {
  try {
    stats.value = await inventaireService.getStats()
  } catch {
    // Stats non bloquantes
  }
}

async function changerPage(p) {
  page.value = p
  await charger()
}

// -------------------------------------------------------
// FILTRES (côté client sur la page courante)
// -------------------------------------------------------
const inventairesFiltres = computed(() => {
  return inventaires.value.filter(inv => {
    const texte = recherche.value.toLowerCase()
    const matchTexte =
      !texte ||
      inv.reference.toLowerCase().includes(texte) ||
      inv.entrepotNom.toLowerCase().includes(texte) ||
      (inv.createurNom && inv.createurNom.toLowerCase().includes(texte))

    const matchStatut = !filtreStatut.value || inv.statut === filtreStatut.value
    const matchType   = !filtreType.value   || inv.type   === filtreType.value

    return matchTexte && matchStatut && matchType
  })
})

// -------------------------------------------------------
// ACTIONS LISTE
// -------------------------------------------------------
function voirDetail(id) {
  router.push({ name: 'inventaire-detail', params: { id } })
}

async function demarrer(inv) {
  if (!confirm(`Démarrer le comptage de l'inventaire ${inv.reference} ?`)) return
  try {
    await inventaireService.demarrer(inv.id)
    await Promise.all([charger(), chargerStats()])
  } catch (e) {
    alert(e.response?.data || 'Erreur lors du démarrage.')
  }
}

async function annuler(inv) {
  if (!confirm(`Annuler l'inventaire ${inv.reference} ? Aucun ajustement ne sera appliqué.`)) return
  try {
    await inventaireService.annuler(inv.id)
    await Promise.all([charger(), chargerStats()])
  } catch (e) {
    alert(e.response?.data || 'Erreur lors de l\'annulation.')
  }
}

async function apresCreation() {
  await Promise.all([charger(), chargerStats()])
}
</script>

<template>
  <AppLayout>
    <div class="space-y-6">

      <!-- EN-TÊTE -->
      <div class="flex flex-col sm:flex-row sm:items-center gap-4 justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Inventaires</h1>
          <p class="text-sm text-gray-500 mt-1">
            {{ totalItems }} inventaire{{ totalItems > 1 ? 's' : '' }} enregistré{{ totalItems > 1 ? 's' : '' }}
          </p>
        </div>
        <button @click="modalVisible = true" class="btn-primary">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
          </svg>
          Nouvel inventaire
        </button>
      </div>

      <!-- STATS -->
      <InventaireStatsBar :stats="stats" />

      <!-- FILTRES -->
      <InventaireFilters
        v-model:recherche="recherche"
        v-model:statut="filtreStatut"
        v-model:type="filtreType"
      />

      <!-- Erreur API -->
      <div v-if="erreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">{{ erreur }}</div>

      <!-- LOADING -->
      <div v-if="isLoading" class="card p-12 text-center text-gray-400">
        <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-blue-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
        Chargement des inventaires…
      </div>

      <!-- TABLEAU -->
      <div v-if="!isLoading">
        <div v-if="inventairesFiltres.length === 0" class="card p-12 text-center text-gray-400 text-sm">
          Aucun inventaire ne correspond aux critères sélectionnés.
        </div>
        <div v-else class="card p-0 overflow-hidden">
          <div class="overflow-x-auto">
            <table class="w-full">
              <thead class="bg-gray-50 border-b border-gray-100">
                <tr>
                  <th class="table-header">Référence</th>
                  <th class="table-header">Type</th>
                  <th class="table-header">Entrepôt</th>
                  <th class="table-header">Progression</th>
                  <th class="table-header">Écarts</th>
                  <th class="table-header">Date prévue</th>
                  <th class="table-header">Statut</th>
                  <th class="table-header text-right">Actions</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-50">
                <InventaireTableRow
                  v-for="inv in inventairesFiltres"
                  :key="inv.id"
                  :inventaire="inv"
                  @voir="voirDetail"
                  @demarrer="demarrer"
                  @annuler="annuler"
                />
              </tbody>
            </table>
          </div>

          <!-- PAGINATION -->
          <div v-if="totalPages > 1" class="flex items-center justify-between px-4 py-3 border-t border-gray-100">
            <p class="text-sm text-gray-500">
              Page {{ page + 1 }} sur {{ totalPages }}
            </p>
            <div class="flex gap-2">
              <button
                :disabled="page === 0"
                @click="changerPage(page - 1)"
                class="btn-secondary text-sm disabled:opacity-40"
              >
                ← Précédente
              </button>
              <button
                :disabled="page >= totalPages - 1"
                @click="changerPage(page + 1)"
                class="btn-secondary text-sm disabled:opacity-40"
              >
                Suivante →
              </button>
            </div>
          </div>
        </div>
      </div>

    </div>

    <!-- MODAL CRÉATION -->
    <InventaireModal
      :visible="modalVisible"
      @fermer="modalVisible = false"
      @sauvegarde="apresCreation"
    />

  </AppLayout>
</template>
