<script setup>
// ============================================================
// VUE : Gestion des transferts inter-entrepôts (Module 10)
//
// Flux en deux étapes :
//   1. Créer (BROUILLON) — ADMIN/GESTIONNAIRE/MAGASINIER
//   2. Expédier (EXPEDIE) — retire stock source — ADMIN/GESTIONNAIRE/MAGASINIER
//   3. Réceptionner (RECU) — crédite stock destination — ADMIN/GESTIONNAIRE/MAGASINIER
//   4. Annuler (ANNULE) — depuis BROUILLON uniquement — ADMIN/GESTIONNAIRE/MAGASINIER
// ============================================================

import { ref, computed, onMounted } from 'vue'
import AppLayout        from '@/layout/AppLayout.vue'
import transfertService from '@/services/transfertService'
import produitService   from '@/services/produitService'
import entrepotService  from '@/services/entrepotService'
import { usePermissions } from '@/composables/usePermissions'
import { useToast }       from '@/composables/useToast'

const { peutCreerMouvement } = usePermissions()
const toast = useToast()

// -------------------------------------------------------
// DONNÉES
// -------------------------------------------------------
const transferts = ref([])
const produits   = ref([])
const entrepots  = ref([])
const isLoading  = ref(false)
const erreur     = ref('')

onMounted(async () => { await chargerDonnees() })

async function chargerDonnees() {
  isLoading.value = true
  erreur.value    = ''
  try {
    const [t, p, e] = await Promise.all([
      transfertService.findAll(),
      produitService.findAll(),
      entrepotService.findActifs(),
    ])
    transferts.value = t
    produits.value   = p
    entrepots.value  = e
  } catch {
    erreur.value = 'Impossible de charger les données.'
  } finally {
    isLoading.value = false
  }
}

// -------------------------------------------------------
// FILTRES
// -------------------------------------------------------
const filtreStatut = ref('')

const transfertsFiltres = computed(() =>
  transferts.value.filter(t => !filtreStatut.value || t.statut === filtreStatut.value)
)

// -------------------------------------------------------
// MODAL CRÉATION
// -------------------------------------------------------
const showModal    = ref(false)
const isSubmitting = ref(false)
const erreurApi    = ref('')
const form = ref({
  produitId:             null,
  entrepotSourceId:      null,
  entrepotDestinationId: null,
  quantite:              '',
  note:                  '',
})

function ouvrirCreer() {
  form.value = { produitId: null, entrepotSourceId: null, entrepotDestinationId: null, quantite: '', note: '' }
  erreurApi.value = ''
  showModal.value = true
}

async function soumettreCreation() {
  if (!form.value.produitId || !form.value.entrepotSourceId || !form.value.entrepotDestinationId || !form.value.quantite) {
    erreurApi.value = 'Tous les champs obligatoires doivent être remplis.'
    return
  }
  if (form.value.entrepotSourceId === form.value.entrepotDestinationId) {
    erreurApi.value = 'L\'entrepôt source et destination doivent être différents.'
    return
  }
  isSubmitting.value = true
  erreurApi.value    = ''
  try {
    const created = await transfertService.creer({
      produitId:             form.value.produitId,
      entrepotSourceId:      form.value.entrepotSourceId,
      entrepotDestinationId: form.value.entrepotDestinationId,
      quantite:              Number(form.value.quantite),
      note:                  form.value.note || null,
    })
    transferts.value.unshift(created)
    showModal.value = false
  } catch (e) {
    erreurApi.value = e.response?.data ?? 'Erreur lors de la création.'
  } finally {
    isSubmitting.value = false
  }
}

// -------------------------------------------------------
// ACTIONS
// -------------------------------------------------------
async function expedier(t) {
  try {
    const updated = await transfertService.expedier(t.id)
    const idx = transferts.value.findIndex(x => x.id === updated.id)
    if (idx !== -1) transferts.value[idx] = updated
  } catch (e) {
    toast.error(e.response?.data ?? "Erreur lors de l'expédition.")
  }
}

async function receptionner(t) {
  try {
    const updated = await transfertService.receptionner(t.id)
    const idx = transferts.value.findIndex(x => x.id === updated.id)
    if (idx !== -1) transferts.value[idx] = updated
  } catch (e) {
    toast.error(e.response?.data ?? 'Erreur lors de la réception.')
  }
}

async function annuler(t) {
  try {
    const updated = await transfertService.annuler(t.id)
    const idx = transferts.value.findIndex(x => x.id === updated.id)
    if (idx !== -1) transferts.value[idx] = updated
    toast.success(`Transfert ${t.reference} annulé.`)
  } catch (e) {
    toast.error(e.response?.data ?? "Erreur lors de l'annulation.")
  }
}

// -------------------------------------------------------
// HELPERS
// -------------------------------------------------------
const statutConfig = {
  BROUILLON: { label: 'Brouillon',  class: 'bg-yellow-100 text-yellow-700' },
  EXPEDIE:   { label: 'Expédié',    class: 'bg-blue-100 text-blue-700'     },
  RECU:      { label: 'Reçu',       class: 'bg-green-100 text-green-700'   },
  ANNULE:    { label: 'Annulé',     class: 'bg-red-100 text-red-700'       },
}

const formatDate = (d) => d ? new Date(d).toLocaleString('fr-FR', {
  day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
}) : '—'
</script>

<template>
  <AppLayout>
    <div class="space-y-4 md:space-y-6">

      <!-- EN-TÊTE -->
      <div class="flex items-center justify-between">
        <div class="hidden md:block">
          <h1 class="text-2xl font-bold text-gray-900">Transferts</h1>
          <p class="text-sm text-gray-500 mt-0.5">{{ transferts.length }} transferts inter-entrepôts</p>
        </div>
        <button v-if="peutCreerMouvement" @click="ouvrirCreer" class="btn-primary">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
          </svg>
          Nouveau transfert
        </button>
      </div>

      <!-- FILTRE STATUT -->
      <div class="card p-3 md:p-4">
        <div class="flex flex-wrap gap-2">
          <button
            v-for="s in ['', 'BROUILLON', 'EXPEDIE', 'RECU', 'ANNULE']"
            :key="s"
            @click="filtreStatut = s"
            :class="['px-3 py-1.5 rounded-lg text-sm font-medium transition-colors',
              filtreStatut === s ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200']"
          >
            {{ s === '' ? 'Tous' : (statutConfig[s]?.label ?? s) }}
          </button>
        </div>
      </div>

      <!-- ERREUR -->
      <div v-if="erreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">{{ erreur }}</div>

      <!-- LOADING -->
      <div v-if="isLoading" class="card p-12 text-center text-gray-400">
        <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-blue-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
        Chargement…
      </div>

      <!-- TABLEAU -->
      <div v-if="!isLoading" class="card p-0 overflow-hidden">
        <div v-if="transfertsFiltres.length === 0" class="p-12 text-center text-gray-400 text-sm">
          Aucun transfert trouvé.
        </div>
        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50 border-b border-gray-100">
              <tr>
                <th class="table-header">Référence</th>
                <th class="table-header">Produit</th>
                <th class="table-header">Source → Destination</th>
                <th class="table-header">Quantité</th>
                <th class="table-header">Statut</th>
                <th class="table-header">Date</th>
                <th class="table-header text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <tr v-for="t in transfertsFiltres" :key="t.id" class="hover:bg-gray-50 transition-colors">
                <td class="table-cell font-mono text-xs text-gray-600">{{ t.reference }}</td>
                <td class="table-cell">
                  <p class="font-medium text-gray-900">{{ t.produitNom }}</p>
                  <p class="text-xs text-gray-400">{{ t.produitReference }}</p>
                </td>
                <td class="table-cell">
                  <div class="flex items-center gap-1.5 text-sm">
                    <span class="text-gray-700 font-medium">{{ t.entrepotSourceNom }}</span>
                    <svg class="w-3.5 h-3.5 text-gray-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 8l4 4m0 0l-4 4m4-4H3"/>
                    </svg>
                    <span class="text-gray-700 font-medium">{{ t.entrepotDestinationNom }}</span>
                  </div>
                </td>
                <td class="table-cell font-semibold text-blue-700">{{ t.quantite }}</td>
                <td class="table-cell">
                  <span :class="['inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium',
                    statutConfig[t.statut]?.class ?? 'bg-gray-100 text-gray-700']">
                    {{ statutConfig[t.statut]?.label ?? t.statut }}
                  </span>
                </td>
                <td class="table-cell text-xs text-gray-400">{{ formatDate(t.dateCreation) }}</td>
                <td class="table-cell">
                  <div class="flex items-center justify-end gap-1">
                    <!-- Expédier -->
                    <button
                      v-if="peutCreerMouvement && t.statut === 'BROUILLON'"
                      @click="expedier(t)"
                      class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                      title="Expédier — retire du stock source"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M17 8l4 4m0 0l-4 4m4-4H3"/>
                      </svg>
                    </button>
                    <!-- Réceptionner -->
                    <button
                      v-if="peutCreerMouvement && t.statut === 'EXPEDIE'"
                      @click="receptionner(t)"
                      class="p-1.5 text-gray-400 hover:text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                      title="Réceptionner — crédite le stock destination"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                      </svg>
                    </button>
                    <!-- Annuler -->
                    <button
                      v-if="peutCreerMouvement && t.statut === 'BROUILLON'"
                      @click="annuler(t)"
                      class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                      title="Annuler le transfert"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
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

    <!-- MODAL CRÉATION -->
    <Teleport to="body">
      <Transition
        enter-active-class="transition duration-200" enter-from-class="opacity-0" enter-to-class="opacity-100"
        leave-active-class="transition duration-150" leave-from-class="opacity-100" leave-to-class="opacity-0"
      >
        <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="showModal = false"/>
          <div class="relative bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">

            <div class="flex items-center justify-between p-6 border-b border-gray-100">
              <h2 class="text-lg font-semibold text-gray-900">Nouveau transfert</h2>
              <button @click="showModal = false" class="p-2 text-gray-400 hover:bg-gray-100 rounded-lg">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            </div>

            <form @submit.prevent="soumettreCreation" class="p-6 space-y-4">
              <div v-if="erreurApi" class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg p-3">
                {{ erreurApi }}
              </div>

              <div>
                <label class="form-label">Produit <span class="text-red-500">*</span></label>
                <select v-model="form.produitId" class="form-input">
                  <option :value="null" disabled>Sélectionner un produit…</option>
                  <option v-for="p in produits" :key="p.id" :value="p.id">
                    {{ p.nom }} ({{ p.reference }})
                  </option>
                </select>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="form-label">Entrepôt source <span class="text-red-500">*</span></label>
                  <select v-model="form.entrepotSourceId" class="form-input">
                    <option :value="null" disabled>Source…</option>
                    <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
                  </select>
                </div>
                <div>
                  <label class="form-label">Entrepôt destination <span class="text-red-500">*</span></label>
                  <select v-model="form.entrepotDestinationId" class="form-input">
                    <option :value="null" disabled>Destination…</option>
                    <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
                  </select>
                </div>
              </div>

              <div>
                <label class="form-label">Quantité <span class="text-red-500">*</span></label>
                <input v-model="form.quantite" type="number" min="1" placeholder="Ex : 20" class="form-input"/>
              </div>

              <div>
                <label class="form-label">Note <span class="text-gray-400 text-xs">(optionnel)</span></label>
                <textarea v-model="form.note" rows="2" placeholder="Raison du transfert…" class="form-input resize-none"/>
              </div>

              <div class="flex items-center justify-end gap-3 pt-2 border-t border-gray-100">
                <button type="button" @click="showModal = false" class="btn-secondary">Annuler</button>
                <button type="submit" :disabled="isSubmitting" class="btn-primary">
                  <svg v-if="isSubmitting" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                  </svg>
                  Créer le transfert
                </button>
              </div>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>

  </AppLayout>
</template>
