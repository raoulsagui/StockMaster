<script setup>
/**
 * Vue principale du module Commandes Fournisseur.
 *
 * Affiche :
 *   - 5 KPI (total, brouillons, validées, livrées, annulées)
 *   - Filtres (statut, texte)
 *   - Tableau paginé des commandes
 *   - Modal de création
 *   - Modal de réception (sur clic "Réceptionner")
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import AppLayout          from '@/layout/AppLayout.vue'
import CommandeStatsBar   from '@/components/commandes/CommandeStatsBar.vue'
import CommandeFilters    from '@/components/commandes/CommandeFilters.vue'
import CommandeTableRow   from '@/components/commandes/CommandeTableRow.vue'
import CommandeModal      from '@/components/commandes/CommandeModal.vue'
import ReceptionModal     from '@/components/commandes/ReceptionModal.vue'
import ConfirmModal       from '@/components/commun/ConfirmModal.vue'
import commandeService    from '@/services/commandeService'

const router    = useRouter()
const authStore = useAuthStore()

// -------------------------------------------------------
// ÉTAT
// -------------------------------------------------------
const commandes   = ref([])
const stats       = ref({})
const isLoading   = ref(false)
const erreur      = ref('')

// Pagination
const page        = ref(0)
const totalPages  = ref(0)
const totalItems  = ref(0)
const PAGE_SIZE   = 20

// Filtres
const recherche   = ref('')
const filtreStatut = ref('')

// Modal création / édition
const modalVisible    = ref(false)
const commandeEditeeId = ref(null)

// Modal réception
const receptionVisible       = ref(false)
const commandeAReceptionner  = ref(null)
const receptionLoading       = ref(false)

// Modal confirmation
const confirmModal = ref({
  visible:  false,
  titre:    '',
  message:  '',
  type:     'danger',
  erreur:   '',
  loading:  false,
  action:   null, // fonction à exécuter si confirmé
})

function ouvrirConfirm({ titre, message, type = 'danger', action }) {
  confirmModal.value = { visible: true, titre, message, type, erreur: '', loading: false, action }
}

function fermerConfirm() {
  confirmModal.value.visible = false
  confirmModal.value.erreur  = ''
}

async function executerConfirm() {
  confirmModal.value.loading = true
  confirmModal.value.erreur  = ''
  try {
    await confirmModal.value.action()
    fermerConfirm()
  } catch (e) {
    confirmModal.value.erreur = e.response?.data || 'Une erreur est survenue.'
  } finally {
    confirmModal.value.loading = false
  }
}

// -------------------------------------------------------
// CHARGEMENT
// -------------------------------------------------------
onMounted(() => Promise.all([charger(), chargerStats()]))

async function charger() {
  isLoading.value = true
  erreur.value    = ''
  try {
    const data      = await commandeService.findAll(page.value, PAGE_SIZE)
    commandes.value = data.content
    totalPages.value = data.totalPages
    totalItems.value = data.totalElements
  } catch {
    erreur.value = 'Impossible de charger les commandes. Vérifiez que le backend est démarré.'
  } finally {
    isLoading.value = false
  }
}

async function chargerStats() {
  try { stats.value = await commandeService.getStats() } catch { /* non bloquant */ }
}

async function changerPage(p) {
  page.value = p
  await charger()
}

// -------------------------------------------------------
// FILTRES côté client
// -------------------------------------------------------
const commandesFiltrees = computed(() =>
  commandes.value.filter(c => {
    const t = recherche.value.toLowerCase()
    const matchTexte =
      !t ||
      c.reference.toLowerCase().includes(t) ||
      c.fournisseurNom.toLowerCase().includes(t) ||
      c.entrepotNom.toLowerCase().includes(t)
    const matchStatut = !filtreStatut.value || c.statut === filtreStatut.value
    return matchTexte && matchStatut
  })
)

// -------------------------------------------------------
// ACTIONS TABLEAU
// -------------------------------------------------------
function voirDetail(id) {
  router.push({ name: 'commande-detail', params: { id } })
}

function ouvrirCreation() {
  commandeEditeeId.value = null
  modalVisible.value     = true
}

function valider(commande) {
  ouvrirConfirm({
    titre:   'Valider la commande',
    message: `Valider la commande ${commande.reference} ? Elle sera transmise au fournisseur et ne pourra plus être modifiée.`,
    type:    'info',
    action:  async () => {
      await commandeService.valider(commande.id)
      await Promise.all([charger(), chargerStats()])
    },
  })
}

async function ouvrirReception(commande) {
  receptionLoading.value = true
  try {
    // Charge la commande complète avec ses lignes avant d'ouvrir le modal
    commandeAReceptionner.value = await commandeService.findById(commande.id)
    receptionVisible.value      = true
  } catch {
    erreur.value = 'Impossible de charger les détails de la commande.'
  } finally {
    receptionLoading.value = false
  }
}

function annuler(commande) {
  ouvrirConfirm({
    titre:   'Annuler la commande',
    message: `Annuler la commande ${commande.reference} ? Aucun mouvement de stock ne sera généré.`,
    type:    'danger',
    action:  async () => {
      await commandeService.annuler(commande.id)
      await Promise.all([charger(), chargerStats()])
    },
  })
}

async function apresEnregistrement() {
  await Promise.all([charger(), chargerStats()])
}

async function apresReception() {
  commandeAReceptionner.value = null
  await Promise.all([charger(), chargerStats()])
}
</script>

<template>
  <AppLayout>
    <div class="space-y-6">

      <!-- EN-TÊTE -->
      <div class="flex flex-col sm:flex-row sm:items-center gap-4 justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Commandes fournisseurs</h1>
          <p class="text-sm text-gray-500 mt-1">
            {{ totalItems }} commande{{ totalItems > 1 ? 's' : '' }} enregistrée{{ totalItems > 1 ? 's' : '' }}
          </p>
        </div>
        <button @click="ouvrirCreation" class="btn-primary">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
          </svg>
          Nouvelle commande
        </button>
      </div>

      <!-- KPI -->
      <CommandeStatsBar :stats="stats" />

      <!-- FILTRES -->
      <CommandeFilters
        v-model:recherche="recherche"
        v-model:statut="filtreStatut"
      />

      <!-- Erreur API -->
      <div v-if="erreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">
        {{ erreur }}
      </div>

      <!-- LOADING -->
      <div v-if="isLoading" class="card p-12 text-center text-gray-400">
        <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-blue-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
        Chargement des commandes…
      </div>

      <!-- TABLEAU -->
      <div v-if="!isLoading">
        <div v-if="commandesFiltrees.length === 0"
          class="card p-12 text-center text-gray-400 text-sm">
          Aucune commande ne correspond aux critères sélectionnés.
        </div>

        <div v-else class="card p-0 overflow-hidden">
          <div class="overflow-x-auto">
            <table class="w-full">
              <thead class="bg-gray-50 border-b border-gray-100">
                <tr>
                  <th class="table-header">Référence</th>
                  <th class="table-header">Fournisseur</th>
                  <th class="table-header">Entrepôt</th>
                  <th class="table-header text-right">Montant HT</th>
                  <th class="table-header">Livraison prévue</th>
                  <th class="table-header">Statut</th>
                  <th class="table-header text-right">Actions</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-50">
                <CommandeTableRow
                  v-for="c in commandesFiltrees"
                  :key="c.id"
                  :commande="c"
                  @voir="voirDetail"
                  @valider="valider"
                  @receptionner="ouvrirReception"
                  @annuler="annuler"
                />
              </tbody>
            </table>
          </div>

          <!-- PAGINATION -->
          <div v-if="totalPages > 1"
            class="flex items-center justify-between px-4 py-3 border-t border-gray-100">
            <p class="text-sm text-gray-500">Page {{ page + 1 }} sur {{ totalPages }}</p>
            <div class="flex gap-2">
              <button :disabled="page === 0" @click="changerPage(page - 1)"
                class="btn-secondary text-sm disabled:opacity-40">
                ← Précédente
              </button>
              <button :disabled="page >= totalPages - 1" @click="changerPage(page + 1)"
                class="btn-secondary text-sm disabled:opacity-40">
                Suivante →
              </button>
            </div>
          </div>
        </div>
      </div>

    </div>

    <!-- MODAL CRÉATION / ÉDITION -->
    <CommandeModal
      :visible="modalVisible"
      :commande-id="commandeEditeeId"
      @fermer="modalVisible = false"
      @sauvegarde="apresEnregistrement"
    />

    <!-- MODAL RÉCEPTION -->
    <ReceptionModal
      :visible="receptionVisible"
      :commande="commandeAReceptionner"
      @fermer="receptionVisible = false"
      @receptionnee="apresReception"
    />

    <!-- MODAL CONFIRMATION -->
    <ConfirmModal
      :visible="confirmModal.visible"
      :titre="confirmModal.titre"
      :message="confirmModal.message"
      :type="confirmModal.type"
      :erreur="confirmModal.erreur"
      :loading="confirmModal.loading"
      @confirmer="executerConfirm"
      @annuler="fermerConfirm"
    />

  </AppLayout>
</template>
