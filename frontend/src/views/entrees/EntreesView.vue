<script setup>
// ============================================================
// VUE : Gestion des entrées de stock (Module 8)
//
// Flux :
//   1. ADMIN/GESTIONNAIRE/MAGASINIER crée un bon (BROUILLON)
//   2. ADMIN/GESTIONNAIRE valide → stock incrémenté automatiquement
//   3. ADMIN/GESTIONNAIRE peut annuler un BROUILLON
// ============================================================

import { ref, computed, onMounted } from 'vue'
import AppLayout          from '@/layout/AppLayout.vue'
import entreeService      from '@/services/entreeService'
import produitService     from '@/services/produitService'
import entrepotService    from '@/services/entrepotService'
import fournisseurService from '@/services/fournisseurService'
import { usePermissions } from '@/composables/usePermissions'

const { peutCreerMouvement, peutValiderMouvement } = usePermissions()

// -------------------------------------------------------
// DONNÉES
// -------------------------------------------------------
const entrees     = ref([])
const produits    = ref([])
const entrepots   = ref([])
const fournisseurs = ref([])
const isLoading   = ref(false)
const erreur      = ref('')

onMounted(async () => {
  await chargerDonnees()
})

async function chargerDonnees() {
  isLoading.value = true
  erreur.value    = ''
  try {
    const [e, p, ent, f] = await Promise.all([
      entreeService.findAll(),
      produitService.findAll(),
      entrepotService.findActifs(),
      fournisseurService.findActifs(),
    ])
    entrees.value      = e
    produits.value     = p
    entrepots.value    = ent
    fournisseurs.value = f
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

const entreesFiltrees = computed(() =>
  entrees.value.filter(e =>
    !filtreStatut.value || e.statut === filtreStatut.value
  )
)

// -------------------------------------------------------
// MODAL CRÉATION
// -------------------------------------------------------
const showModal    = ref(false)
const isSubmitting = ref(false)
const erreurApi    = ref('')
const form = ref({
  produitId:     null,
  entrepotId:    null,
  fournisseurId: null,
  quantite:      '',
  prixUnitaire:  '',
  note:          '',
})

function ouvrirCreer() {
  form.value = { produitId: null, entrepotId: null, fournisseurId: null, quantite: '', prixUnitaire: '', note: '' }
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
    const created = await entreeService.creer({
      produitId:     form.value.produitId,
      entrepotId:    form.value.entrepotId,
      fournisseurId: form.value.fournisseurId || null,
      quantite:      Number(form.value.quantite),
      prixUnitaire:  form.value.prixUnitaire ? Number(form.value.prixUnitaire) : null,
      note:          form.value.note || null,
    })
    entrees.value.unshift(created)
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
async function valider(entree) {
  try {
    const updated = await entreeService.valider(entree.id)
    const idx = entrees.value.findIndex(e => e.id === updated.id)
    if (idx !== -1) entrees.value[idx] = updated
  } catch (e) {
    alert(e.response?.data ?? 'Erreur lors de la validation.')
  }
}

async function annuler(entree) {
  if (!confirm(`Annuler le bon ${entree.reference} ?`)) return
  try {
    const updated = await entreeService.annuler(entree.id)
    const idx = entrees.value.findIndex(e => e.id === updated.id)
    if (idx !== -1) entrees.value[idx] = updated
  } catch (e) {
    alert(e.response?.data ?? 'Erreur lors de l\'annulation.')
  }
}

// -------------------------------------------------------
// HELPERS
// -------------------------------------------------------
const statutConfig = {
  BROUILLON: { label: 'Brouillon',  class: 'bg-yellow-100 text-yellow-700' },
  VALIDE:    { label: 'Validé',     class: 'bg-green-100 text-green-700'   },
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
          <h1 class="text-2xl font-bold text-gray-900">Entrées de stock</h1>
          <p class="text-sm text-gray-500 mt-0.5">{{ entrees.length }} bons de réception</p>
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
        <div v-if="entreesFiltrees.length === 0" class="p-12 text-center text-gray-400 text-sm">
          Aucun bon de réception trouvé.
        </div>
        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50 border-b border-gray-100">
              <tr>
                <th class="table-header">Référence</th>
                <th class="table-header">Produit</th>
                <th class="table-header">Entrepôt</th>
                <th class="table-header">Fournisseur</th>
                <th class="table-header">Quantité</th>
                <th class="table-header">Statut</th>
                <th class="table-header">Date</th>
                <th class="table-header text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <tr v-for="e in entreesFiltrees" :key="e.id" class="hover:bg-gray-50 transition-colors">
                <td class="table-cell font-mono text-xs text-gray-600">{{ e.reference }}</td>
                <td class="table-cell">
                  <p class="font-medium text-gray-900">{{ e.produitNom }}</p>
                  <p class="text-xs text-gray-400">{{ e.produitReference }}</p>
                </td>
                <td class="table-cell text-gray-600">{{ e.entrepotNom }}</td>
                <td class="table-cell text-gray-500">{{ e.fournisseurNom || '—' }}</td>
                <td class="table-cell font-semibold text-green-700">+{{ e.quantite }}</td>
                <td class="table-cell">
                  <span :class="['inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium',
                    statutConfig[e.statut]?.class ?? 'bg-gray-100 text-gray-700']">
                    {{ statutConfig[e.statut]?.label ?? e.statut }}
                  </span>
                </td>
                <td class="table-cell text-xs text-gray-400">{{ formatDate(e.dateCreation) }}</td>
                <td class="table-cell">
                  <div class="flex items-center justify-end gap-1">
                    <!-- Valider -->
                    <button
                      v-if="peutValiderMouvement && e.statut === 'BROUILLON'"
                      @click="valider(e)"
                      class="p-1.5 text-gray-400 hover:text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                      title="Valider ce bon — met à jour le stock"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                      </svg>
                    </button>
                    <!-- Annuler -->
                    <button
                      v-if="peutValiderMouvement && e.statut === 'BROUILLON'"
                      @click="annuler(e)"
                      class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                      title="Annuler ce bon"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M6 18L18 6M6 6l12 12"/>
                      </svg>
                    </button>
                    <!-- Validé par -->
                    <span v-if="e.statut === 'VALIDE'" class="text-xs text-gray-400">
                      {{ e.validateurNom ?? '—' }}
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

            <!-- Header -->
            <div class="flex items-center justify-between p-6 border-b border-gray-100">
              <h2 class="text-lg font-semibold text-gray-900">Nouveau bon de réception</h2>
              <button @click="showModal = false" class="p-2 text-gray-400 hover:bg-gray-100 rounded-lg">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            </div>

            <!-- Formulaire -->
            <form @submit.prevent="soumettreCreation" class="p-6 space-y-4">
              <div v-if="erreurApi" class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg p-3">
                {{ erreurApi }}
              </div>

              <!-- Produit -->
              <div>
                <label class="form-label">Produit <span class="text-red-500">*</span></label>
                <select v-model="form.produitId" class="form-input">
                  <option :value="null" disabled>Sélectionner un produit…</option>
                  <option v-for="p in produits" :key="p.id" :value="p.id">
                    {{ p.nom }} ({{ p.reference }})
                  </option>
                </select>
              </div>

              <!-- Entrepôt -->
              <div>
                <label class="form-label">Entrepôt de réception <span class="text-red-500">*</span></label>
                <select v-model="form.entrepotId" class="form-input">
                  <option :value="null" disabled>Sélectionner un entrepôt…</option>
                  <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
                </select>
              </div>

              <!-- Fournisseur -->
              <div>
                <label class="form-label">Fournisseur <span class="text-gray-400 text-xs">(optionnel)</span></label>
                <select v-model="form.fournisseurId" class="form-input">
                  <option :value="null">Aucun fournisseur</option>
                  <option v-for="f in fournisseurs" :key="f.id" :value="f.id">{{ f.nom }}</option>
                </select>
              </div>

              <!-- Quantité + Prix -->
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="form-label">Quantité <span class="text-red-500">*</span></label>
                  <input v-model="form.quantite" type="number" min="1" placeholder="Ex : 50" class="form-input"/>
                </div>
                <div>
                  <label class="form-label">Prix unitaire (€) <span class="text-gray-400 text-xs">(optionnel)</span></label>
                  <input v-model="form.prixUnitaire" type="number" min="0" step="0.01" placeholder="Ex : 12.50" class="form-input"/>
                </div>
              </div>

              <!-- Note -->
              <div>
                <label class="form-label">Note <span class="text-gray-400 text-xs">(optionnel)</span></label>
                <textarea v-model="form.note" rows="2" placeholder="Remarques sur la réception…" class="form-input resize-none"/>
              </div>

              <!-- Boutons -->
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
