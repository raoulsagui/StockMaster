<script setup>
// ============================================================
// VUE : Gestion des sorties de stock (Module 9)
//
// Flux :
//   1. ADMIN/GESTIONNAIRE/MAGASINIER crée un bon (BROUILLON)
//   2. ADMIN/GESTIONNAIRE valide → stock décrémenté automatiquement
//   3. ADMIN/GESTIONNAIRE peut annuler un BROUILLON
// ============================================================

import { ref, computed, onMounted } from 'vue'
import AppLayout       from '@/layout/AppLayout.vue'
import sortieService   from '@/services/sortieService'
import produitService  from '@/services/produitService'
import entrepotService from '@/services/entrepotService'
import { usePermissions } from '@/composables/usePermissions'

const { peutCreerMouvement, peutValiderMouvement } = usePermissions()

// -------------------------------------------------------
// DONNÉES
// -------------------------------------------------------
const sorties   = ref([])
const produits  = ref([])
const entrepots = ref([])
const isLoading = ref(false)
const erreur    = ref('')

onMounted(async () => { await chargerDonnees() })

async function chargerDonnees() {
  isLoading.value = true
  erreur.value    = ''
  try {
    const [s, p, e] = await Promise.all([
      sortieService.findAll(),
      produitService.findAll(),
      entrepotService.findActifs(),
    ])
    sorties.value   = s
    produits.value  = p
    entrepots.value = e
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

const sortiesFiltrees = computed(() =>
  sorties.value.filter(s => !filtreStatut.value || s.statut === filtreStatut.value)
)

// -------------------------------------------------------
// MODAL CRÉATION
// -------------------------------------------------------
const showModal    = ref(false)
const isSubmitting = ref(false)
const erreurApi    = ref('')
const form = ref({
  produitId:   null,
  entrepotId:  null,
  quantite:    '',
  motif:       'LIVRAISON',
  destinataire:'',
  note:        '',
})

const MOTIFS = [
  { value: 'LIVRAISON',          label: 'Livraison client'     },
  { value: 'RETOUR_FOURNISSEUR', label: 'Retour fournisseur'   },
  { value: 'CASSE',              label: 'Casse / détérioration' },
  { value: 'PERTE',              label: 'Perte / vol'           },
  { value: 'AUTRE',              label: 'Autre'                 },
]

function ouvrirCreer() {
  form.value = { produitId: null, entrepotId: null, quantite: '', motif: 'LIVRAISON', destinataire: '', note: '' }
  erreurApi.value = ''
  showModal.value = true
}

async function soumettreCreation() {
  if (!form.value.produitId || !form.value.entrepotId || !form.value.quantite) {
    erreurApi.value = 'Produit, entrepôt et quantité sont obligatoires.'
    return
  }
  isSubmitting.value = true
  erreurApi.value    = ''
  try {
    const created = await sortieService.creer({
      produitId:    form.value.produitId,
      entrepotId:   form.value.entrepotId,
      quantite:     Number(form.value.quantite),
      motif:        form.value.motif,
      destinataire: form.value.destinataire || null,
      note:         form.value.note || null,
    })
    sorties.value.unshift(created)
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
async function valider(sortie) {
  try {
    const updated = await sortieService.valider(sortie.id)
    const idx = sorties.value.findIndex(s => s.id === updated.id)
    if (idx !== -1) sorties.value[idx] = updated
  } catch (e) {
    alert(e.response?.data ?? 'Erreur lors de la validation.')
  }
}

async function annuler(sortie) {
  if (!confirm(`Annuler le bon ${sortie.reference} ?`)) return
  try {
    const updated = await sortieService.annuler(sortie.id)
    const idx = sorties.value.findIndex(s => s.id === updated.id)
    if (idx !== -1) sorties.value[idx] = updated
  } catch (e) {
    alert(e.response?.data ?? 'Erreur lors de l\'annulation.')
  }
}

// -------------------------------------------------------
// HELPERS
// -------------------------------------------------------
const statutConfig = {
  BROUILLON: { label: 'Brouillon', class: 'bg-yellow-100 text-yellow-700' },
  VALIDE:    { label: 'Validé',    class: 'bg-green-100 text-green-700'   },
  ANNULE:    { label: 'Annulé',    class: 'bg-red-100 text-red-700'       },
}

const motifLabels = {
  LIVRAISON:          'Livraison',
  RETOUR_FOURNISSEUR: 'Retour fournisseur',
  CASSE:              'Casse',
  PERTE:              'Perte',
  AUTRE:              'Autre',
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
          <h1 class="text-2xl font-bold text-gray-900">Sorties de stock</h1>
          <p class="text-sm text-gray-500 mt-0.5">{{ sorties.length }} bons de sortie</p>
        </div>
        <button v-if="peutCreerMouvement" @click="ouvrirCreer" class="btn-primary">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
          </svg>
          Nouveau bon
        </button>
      </div>

      <!-- FILTRE STATUT -->
      <div class="card p-3 md:p-4">
        <div class="flex flex-wrap gap-2">
          <button
            v-for="s in ['', 'BROUILLON', 'VALIDE', 'ANNULE']"
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
        <div v-if="sortiesFiltrees.length === 0" class="p-12 text-center text-gray-400 text-sm">
          Aucun bon de sortie trouvé.
        </div>
        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50 border-b border-gray-100">
              <tr>
                <th class="table-header">Référence</th>
                <th class="table-header">Produit</th>
                <th class="table-header">Entrepôt</th>
                <th class="table-header">Motif</th>
                <th class="table-header">Quantité</th>
                <th class="table-header">Statut</th>
                <th class="table-header">Date</th>
                <th class="table-header text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <tr v-for="s in sortiesFiltrees" :key="s.id" class="hover:bg-gray-50 transition-colors">
                <td class="table-cell font-mono text-xs text-gray-600">{{ s.reference }}</td>
                <td class="table-cell">
                  <p class="font-medium text-gray-900">{{ s.produitNom }}</p>
                  <p class="text-xs text-gray-400">{{ s.produitReference }}</p>
                </td>
                <td class="table-cell text-gray-600">{{ s.entrepotNom }}</td>
                <td class="table-cell text-xs text-gray-500">{{ motifLabels[s.motif] ?? s.motif }}</td>
                <td class="table-cell font-semibold text-red-600">-{{ s.quantite }}</td>
                <td class="table-cell">
                  <span :class="['inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium',
                    statutConfig[s.statut]?.class ?? 'bg-gray-100 text-gray-700']">
                    {{ statutConfig[s.statut]?.label ?? s.statut }}
                  </span>
                </td>
                <td class="table-cell text-xs text-gray-400">{{ formatDate(s.dateCreation) }}</td>
                <td class="table-cell">
                  <div class="flex items-center justify-end gap-1">
                    <button
                      v-if="peutValiderMouvement && s.statut === 'BROUILLON'"
                      @click="valider(s)"
                      class="p-1.5 text-gray-400 hover:text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                      title="Valider — déduit le stock"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                      </svg>
                    </button>
                    <button
                      v-if="peutValiderMouvement && s.statut === 'BROUILLON'"
                      @click="annuler(s)"
                      class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                      title="Annuler"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                      </svg>
                    </button>
                    <span v-if="s.statut === 'VALIDE'" class="text-xs text-gray-400">
                      {{ s.validateurNom ?? '—' }}
                    </span>
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
              <h2 class="text-lg font-semibold text-gray-900">Nouveau bon de sortie</h2>
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

              <div>
                <label class="form-label">Entrepôt source <span class="text-red-500">*</span></label>
                <select v-model="form.entrepotId" class="form-input">
                  <option :value="null" disabled>Sélectionner un entrepôt…</option>
                  <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
                </select>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="form-label">Quantité <span class="text-red-500">*</span></label>
                  <input v-model="form.quantite" type="number" min="1" placeholder="Ex : 10" class="form-input"/>
                </div>
                <div>
                  <label class="form-label">Motif</label>
                  <select v-model="form.motif" class="form-input">
                    <option v-for="m in MOTIFS" :key="m.value" :value="m.value">{{ m.label }}</option>
                  </select>
                </div>
              </div>

              <div>
                <label class="form-label">Destinataire <span class="text-gray-400 text-xs">(optionnel)</span></label>
                <input v-model="form.destinataire" type="text" placeholder="Nom du client ou référence" class="form-input"/>
              </div>

              <div>
                <label class="form-label">Note <span class="text-gray-400 text-xs">(optionnel)</span></label>
                <textarea v-model="form.note" rows="2" placeholder="Remarques…" class="form-input resize-none"/>
              </div>

              <div class="flex items-center justify-end gap-3 pt-2 border-t border-gray-100">
                <button type="button" @click="showModal = false" class="btn-secondary">Annuler</button>
                <button type="submit" :disabled="isSubmitting" class="btn-primary">
                  <svg v-if="isSubmitting" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                  </svg>
                  Créer le bon
                </button>
              </div>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>

  </AppLayout>
</template>
