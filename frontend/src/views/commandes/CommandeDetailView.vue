<script setup>
/**
 * Vue de détail d'une commande fournisseur.
 *
 * Affiche :
 *   - En-tête : référence, statut, fournisseur, entrepôt, dates, créateur
 *   - Métriques : montant total, nombre de lignes, nombre d'unités
 *   - Tableau des lignes avec comparaison commandé / reçu (après livraison)
 *   - Actions contextuelles selon statut et rôle
 *   - Modal de modification (si BROUILLON)
 *   - Modal de réception (si VALIDEE)
 */
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import AppLayout      from '@/layout/AppLayout.vue'
import CommandeModal  from '@/components/commandes/CommandeModal.vue'
import ReceptionModal from '@/components/commandes/ReceptionModal.vue'
import commandeService from '@/services/commandeService'

const route     = useRoute()
const router    = useRouter()
const authStore = useAuthStore()

const commande       = ref(null)
const isLoading      = ref(false)
const erreur         = ref('')
const actionErreur   = ref('')
const actionLoading  = ref(false)

// Modals
const editModalVisible      = ref(false)
const receptionModalVisible = ref(false)

// -------------------------------------------------------
// CHARGEMENT
// -------------------------------------------------------
onMounted(() => charger())

async function charger() {
  isLoading.value = true
  erreur.value    = ''
  try {
    commande.value = await commandeService.findById(route.params.id)
  } catch {
    erreur.value = 'Commande introuvable.'
  } finally {
    isLoading.value = false
  }
}

// -------------------------------------------------------
// DROITS CONTEXTUELS
// -------------------------------------------------------
const peutModifier     = computed(() =>
  commande.value?.statut === 'BROUILLON'
)
const peutValider      = computed(() =>
  commande.value?.statut === 'BROUILLON' &&
  ['ADMIN', 'GESTIONNAIRE'].includes(authStore.role)
)
const peutReceptionner = computed(() =>
  commande.value?.statut === 'VALIDEE'
)
const peutAnnuler      = computed(() =>
  ['BROUILLON', 'VALIDEE'].includes(commande.value?.statut) &&
  ['ADMIN', 'GESTIONNAIRE'].includes(authStore.role)
)

// -------------------------------------------------------
// ACTIONS
// -------------------------------------------------------
async function valider() {
  if (!confirm(`Valider la commande ${commande.value.reference} ?\n\nElle sera verrouillée et transmise au fournisseur.`)) return
  actionLoading.value = true
  actionErreur.value  = ''
  try {
    commande.value = await commandeService.valider(commande.value.id)
  } catch (e) {
    actionErreur.value = e.response?.data || 'Erreur lors de la validation.'
  } finally {
    actionLoading.value = false
  }
}

async function annuler() {
  if (!confirm(`Annuler la commande ${commande.value.reference} ? Aucun stock ne sera modifié.`)) return
  actionLoading.value = true
  actionErreur.value  = ''
  try {
    commande.value = await commandeService.annuler(commande.value.id)
  } catch (e) {
    actionErreur.value = e.response?.data || 'Erreur lors de l\'annulation.'
  } finally {
    actionLoading.value = false
  }
}

async function apresModification(updated) {
  commande.value = updated
}

async function apresReception(updated) {
  commande.value = updated
}

// -------------------------------------------------------
// HELPERS
// -------------------------------------------------------
const statutConfig = {
  BROUILLON: { label: 'Brouillon', color: 'text-yellow-700 bg-yellow-100' },
  VALIDEE:   { label: 'Validée',   color: 'text-blue-700   bg-blue-100'   },
  LIVREE:    { label: 'Livrée',    color: 'text-green-700  bg-green-100'  },
  ANNULEE:   { label: 'Annulée',   color: 'text-red-700    bg-red-100'    },
}

function formatDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleDateString('fr-FR')
}
function formatDatetime(d) {
  if (!d) return '—'
  return new Date(d).toLocaleString('fr-FR')
}
function formatMontant(m) {
  if (m == null || m === 0) return '—'
  return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(m)
}
</script>

<template>
  <AppLayout>
    <div class="space-y-6">

      <!-- RETOUR -->
      <button @click="router.push({ name: 'commandes' })"
        class="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-900 transition-colors">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
        </svg>
        Retour aux commandes
      </button>

      <!-- LOADING -->
      <div v-if="isLoading" class="card p-16 text-center text-gray-400">
        <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-blue-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
        Chargement de la commande…
      </div>

      <div v-else-if="erreur" class="card p-8 text-center text-red-600 text-sm">{{ erreur }}</div>

      <template v-else-if="commande">

        <!-- EN-TÊTE -->
        <div class="flex flex-col lg:flex-row lg:items-start gap-4 justify-between">
          <div>
            <div class="flex items-center gap-3 flex-wrap">
              <h1 class="text-2xl font-bold text-gray-900 font-mono">{{ commande.reference }}</h1>
              <span :class="[statutConfig[commande.statut]?.color, 'px-3 py-1 rounded-full text-xs font-semibold']">
                {{ statutConfig[commande.statut]?.label }}
              </span>
            </div>

            <div class="mt-2 space-y-0.5 text-sm text-gray-500">
              <p>
                Fournisseur :
                <span class="font-medium text-gray-800">{{ commande.fournisseurNom }}</span>
                <span class="text-gray-400"> · {{ commande.fournisseurEmail }}</span>
              </p>
              <p>
                Entrepôt de destination :
                <span class="font-medium text-gray-800">{{ commande.entrepotNom }}</span>
              </p>
              <p>
                Créée par <span class="font-medium text-gray-800">{{ commande.createurNom }}</span>
                le {{ formatDate(commande.dateCreation) }}
              </p>
              <p v-if="commande.valideurNom">
                Validée par <span class="font-medium text-gray-800">{{ commande.valideurNom }}</span>
                le {{ formatDatetime(commande.dateValidation) }}
              </p>
              <p v-if="commande.dateLivraisonEffective">
                Réceptionnée le
                <span class="font-medium text-gray-800">{{ formatDatetime(commande.dateLivraisonEffective) }}</span>
              </p>
              <p v-if="commande.dateLivraisonPrevue" class="text-gray-400">
                Livraison prévue : {{ formatDate(commande.dateLivraisonPrevue) }}
              </p>
              <p v-if="commande.note" class="italic text-gray-400">"{{ commande.note }}"</p>
            </div>
          </div>

          <!-- ACTIONS -->
          <div class="flex flex-wrap items-center gap-2 flex-shrink-0">
            <button
              v-if="peutModifier"
              @click="editModalVisible = true"
              class="btn-secondary"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
              </svg>
              Modifier
            </button>

            <button
              v-if="peutValider"
              @click="valider"
              :disabled="actionLoading"
              class="btn-primary disabled:opacity-50"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
              </svg>
              Valider la commande
            </button>

            <button
              v-if="peutReceptionner"
              @click="receptionModalVisible = true"
              :disabled="actionLoading"
              class="btn-success disabled:opacity-50"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
              </svg>
              Réceptionner la livraison
            </button>

            <button
              v-if="peutAnnuler"
              @click="annuler"
              :disabled="actionLoading"
              class="btn-danger disabled:opacity-50"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
              Annuler
            </button>
          </div>
        </div>

        <!-- Erreur action -->
        <div v-if="actionErreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">
          {{ actionErreur }}
        </div>

        <!-- MÉTRIQUES -->
        <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <div class="card p-4">
            <p class="text-xs text-gray-500 uppercase font-medium">Lignes</p>
            <p class="text-2xl font-bold text-gray-900 mt-1">{{ commande.nombreLignes }}</p>
            <p class="text-xs text-gray-400 mt-1">références produit</p>
          </div>
          <div class="card p-4">
            <p class="text-xs text-gray-500 uppercase font-medium">Unités</p>
            <p class="text-2xl font-bold text-gray-900 mt-1">{{ commande.nombreUnites }}</p>
            <p class="text-xs text-gray-400 mt-1">articles commandés</p>
          </div>
          <div class="card p-4 col-span-2">
            <p class="text-xs text-gray-500 uppercase font-medium">Montant total HT</p>
            <p class="text-2xl font-bold text-gray-900 mt-1 font-mono">
              {{ formatMontant(commande.montantTotal) }}
            </p>
            <p class="text-xs text-gray-400 mt-1">
              {{ commande.montantTotal > 0 ? 'Prix HT négociés' : 'Prix non renseignés' }}
            </p>
          </div>
        </div>

        <!-- TABLEAU DES LIGNES -->
        <div class="card p-0 overflow-hidden">
          <div class="px-5 py-4 border-b border-gray-100">
            <h2 class="text-sm font-semibold text-gray-800">Détail des lignes</h2>
          </div>
          <div class="overflow-x-auto">
            <table class="w-full text-sm">
              <thead class="bg-gray-50 border-b border-gray-100">
                <tr>
                  <th class="table-header">Produit</th>
                  <th class="table-header text-right">Commandé</th>
                  <!-- Colonnes réception visibles seulement si LIVREE -->
                  <template v-if="commande.statut === 'LIVREE'">
                    <th class="table-header text-right">Reçu</th>
                    <th class="table-header text-center">Écart</th>
                  </template>
                  <th class="table-header text-right">Prix unit. HT</th>
                  <th class="table-header text-right">Sous-total HT</th>
                  <th class="table-header">Note</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-50">
                <tr v-if="!commande.lignes?.length">
                  <td colspan="7" class="py-10 text-center text-gray-400 text-sm">
                    Aucune ligne dans cette commande.
                  </td>
                </tr>
                <tr
                  v-for="ligne in commande.lignes"
                  :key="ligne.id"
                  :class="[
                    'transition-colors hover:bg-gray-50',
                    commande.statut === 'LIVREE' && ligne.ecartReception !== 0 ? 'bg-orange-50/40' : '',
                  ]"
                >
                  <!-- Produit -->
                  <td class="table-cell">
                    <p class="font-medium text-gray-900">{{ ligne.produitNom }}</p>
                    <p class="text-xs text-gray-400">
                      {{ ligne.produitReference }}
                      <span v-if="ligne.produitCategorieNom"> · {{ ligne.produitCategorieNom }}</span>
                    </p>
                  </td>

                  <!-- Quantité commandée -->
                  <td class="table-cell text-right">
                    <span class="font-mono font-medium text-gray-700">{{ ligne.quantiteCommandee }}</span>
                  </td>

                  <!-- Colonnes réception -->
                  <template v-if="commande.statut === 'LIVREE'">
                    <td class="table-cell text-right">
                      <span class="font-mono font-semibold text-gray-900">{{ ligne.quantiteRecue ?? '—' }}</span>
                    </td>
                    <td class="table-cell text-center">
                      <span v-if="ligne.ecartReception === 0"
                        class="px-2 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-700">
                        ✓ Conforme
                      </span>
                      <span v-else-if="ligne.ecartReception > 0"
                        class="px-2 py-0.5 rounded-full text-xs font-bold bg-blue-100 text-blue-700">
                        ▲ +{{ ligne.ecartReception }}
                      </span>
                      <span v-else
                        class="px-2 py-0.5 rounded-full text-xs font-bold bg-red-100 text-red-700">
                        ▼ {{ ligne.ecartReception }}
                      </span>
                    </td>
                  </template>

                  <!-- Prix unitaire -->
                  <td class="table-cell text-right">
                    <span v-if="ligne.prixUnitaire" class="font-mono text-gray-700">
                      {{ formatMontant(ligne.prixUnitaire) }}
                    </span>
                    <span v-else class="text-gray-300 text-xs">—</span>
                  </td>

                  <!-- Sous-total -->
                  <td class="table-cell text-right">
                    <span v-if="ligne.sousTotal" class="font-mono font-medium text-gray-900">
                      {{ formatMontant(ligne.sousTotal) }}
                    </span>
                    <span v-else class="text-gray-300 text-xs">—</span>
                  </td>

                  <!-- Note -->
                  <td class="table-cell">
                    <span class="text-xs text-gray-500 italic">{{ ligne.note || '—' }}</span>
                  </td>
                </tr>
              </tbody>

              <!-- Pied : total -->
              <tfoot v-if="commande.montantTotal > 0" class="border-t-2 border-gray-200 bg-gray-50">
                <tr>
                  <td :colspan="commande.statut === 'LIVREE' ? 5 : 3"
                    class="px-5 py-3 text-sm font-semibold text-gray-700 text-right">
                    Total HT
                  </td>
                  <td class="px-5 py-3 text-right">
                    <span class="text-base font-bold font-mono text-gray-900">
                      {{ formatMontant(commande.montantTotal) }}
                    </span>
                  </td>
                  <td></td>
                </tr>
              </tfoot>
            </table>
          </div>
        </div>

      </template>
    </div>

    <!-- MODAL MODIFICATION -->
    <CommandeModal
      :visible="editModalVisible"
      :commande-id="commande?.id"
      @fermer="editModalVisible = false"
      @sauvegarde="apresModification"
    />

    <!-- MODAL RÉCEPTION -->
    <ReceptionModal
      :visible="receptionModalVisible"
      :commande="commande"
      @fermer="receptionModalVisible = false"
      @receptionnee="apresReception"
    />

  </AppLayout>
</template>
