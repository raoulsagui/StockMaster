<script setup>
import { ref, computed, onMounted } from 'vue'
import AppLayout          from '@/layout/AppLayout.vue'
import entreeService      from '@/services/entreeService'
import produitService     from '@/services/produitService'
import entrepotService    from '@/services/entrepotService'
import fournisseurService from '@/services/fournisseurService'
import { usePermissions } from '@/composables/usePermissions'
import { useToast }       from '@/composables/useToast'

const { peutCreerMouvement, peutValiderMouvement } = usePermissions()
const toast = useToast()

const entrees     = ref([])
const produits    = ref([])
const entrepots   = ref([])
const fournisseurs = ref([])
const isLoading   = ref(false)
const erreur      = ref('')

onMounted(() => chargerDonnees())

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

const filtreStatut = ref('')

const entreesFiltrees = computed(() =>
  entrees.value.filter(e => !filtreStatut.value || e.statut === filtreStatut.value)
)

// KPI rapides
const nbBrouillons = computed(() => entrees.value.filter(e => e.statut === 'BROUILLON').length)
const nbValides    = computed(() => entrees.value.filter(e => e.statut === 'VALIDE').length)
const nbAnnules    = computed(() => entrees.value.filter(e => e.statut === 'ANNULE').length)

const showModal    = ref(false)
const isSubmitting = ref(false)
const erreurApi    = ref('')
const form = ref({ produitId: null, entrepotId: null, fournisseurId: null, quantite: '', prixUnitaire: '', note: '' })

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
    toast.success('Bon de réception créé.')
  } catch (e) {
    erreurApi.value = e.response?.data ?? 'Erreur lors de la création.'
  } finally {
    isSubmitting.value = false
  }
}

async function valider(entree) {
  try {
    const updated = await entreeService.valider(entree.id)
    const idx = entrees.value.findIndex(e => e.id === updated.id)
    if (idx !== -1) entrees.value[idx] = updated
    toast.success('Bon validé — stock mis à jour.')
  } catch (e) {
    toast.error(e.response?.data ?? 'Erreur lors de la validation.')
  }
}

async function annuler(entree) {
  try {
    const updated = await entreeService.annuler(entree.id)
    const idx = entrees.value.findIndex(e => e.id === updated.id)
    if (idx !== -1) entrees.value[idx] = updated
    toast.success(`Bon ${entree.reference} annulé.`)
  } catch (e) {
    toast.error(e.response?.data ?? "Erreur lors de l'annulation.")
  }
}

const statutConfig = {
  BROUILLON: { label: 'Brouillon', dot: 'bg-amber-400',   class: 'bg-amber-50 text-amber-700 ring-1 ring-amber-200'  },
  VALIDE:    { label: 'Validé',    dot: 'bg-emerald-500',  class: 'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200' },
  ANNULE:    { label: 'Annulé',    dot: 'bg-red-400',      class: 'bg-red-50 text-red-700 ring-1 ring-red-200'        },
}

const formatDate = (d) => d ? new Date(d).toLocaleString('fr-FR', {
  day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
}) : '—'
</script>

<template>
  <AppLayout>
    <div class="space-y-5">

      <!-- EN-TÊTE GRADIENT -->
      <div class="page-header bg-gradient-to-r from-emerald-600 to-teal-700 shadow-lg shadow-emerald-500/20">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-xl font-bold text-white">Entrées de stock</h1>
            <p class="text-emerald-100 text-sm mt-0.5">{{ entrees.length }} bons de réception</p>
          </div>
          <button v-if="peutCreerMouvement" @click="ouvrirCreer"
            class="inline-flex items-center gap-2 bg-white/20 hover:bg-white/30 text-white px-4 py-2 rounded-xl text-sm font-medium transition-colors backdrop-blur-sm border border-white/20">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
            </svg>
            Nouveau bon
          </button>
        </div>

        <!-- Mini KPI dans le header -->
        <div class="flex gap-4 mt-4">
          <div class="bg-white/15 rounded-xl px-3 py-2 text-center">
            <p class="text-lg font-bold text-white">{{ nbBrouillons }}</p>
            <p class="text-xs text-emerald-100">En attente</p>
          </div>
          <div class="bg-white/15 rounded-xl px-3 py-2 text-center">
            <p class="text-lg font-bold text-white">{{ nbValides }}</p>
            <p class="text-xs text-emerald-100">Validés</p>
          </div>
          <div class="bg-white/15 rounded-xl px-3 py-2 text-center">
            <p class="text-lg font-bold text-white">{{ nbAnnules }}</p>
            <p class="text-xs text-emerald-100">Annulés</p>
          </div>
        </div>
        <!-- Décor -->
        <div class="absolute -right-8 -top-8 w-32 h-32 rounded-full bg-white/5 pointer-events-none"></div>
        <div class="absolute -right-4 top-8 w-20 h-20 rounded-full bg-white/5 pointer-events-none"></div>
      </div>

      <!-- FILTRES PILLS -->
      <div class="flex flex-wrap gap-2">
        <button v-for="s in ['', 'BROUILLON', 'VALIDE', 'ANNULE']" :key="s"
          @click="filtreStatut = s"
          :class="['inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full text-sm font-medium transition-all duration-200 border',
            filtreStatut === s
              ? 'bg-gray-900 text-white border-gray-900 shadow-sm'
              : 'bg-white text-gray-600 border-gray-200 hover:border-gray-300 hover:bg-gray-50']"
        >
          <span v-if="s && statutConfig[s]" :class="['w-1.5 h-1.5 rounded-full', statutConfig[s].dot]"></span>
          {{ s === '' ? 'Tous' : (statutConfig[s]?.label ?? s) }}
          <span v-if="s === ''" class="text-xs opacity-60">({{ entrees.length }})</span>
        </button>
      </div>

      <!-- ERREUR -->
      <div v-if="erreur" class="rounded-xl bg-red-50 border border-red-200 text-red-700 text-sm p-4">{{ erreur }}</div>

      <!-- LOADING -->
      <div v-if="isLoading" class="card p-12 text-center text-gray-400">
        <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-emerald-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
        Chargement…
      </div>

      <!-- TABLEAU -->
      <div v-if="!isLoading" class="card p-0 overflow-hidden">
        <div v-if="entreesFiltrees.length === 0" class="py-16 text-center text-gray-300">
          <svg class="w-10 h-10 mx-auto mb-3 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
          </svg>
          <p class="text-sm">Aucun bon de réception trouvé.</p>
        </div>
        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50/80 border-b border-gray-100">
              <tr>
                <th class="table-header pl-5">Référence</th>
                <th class="table-header">Produit</th>
                <th class="table-header">Entrepôt</th>
                <th class="table-header">Fournisseur</th>
                <th class="table-header">Quantité</th>
                <th class="table-header">Statut</th>
                <th class="table-header">Date</th>
                <th class="table-header text-right pr-5">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <tr v-for="e in entreesFiltrees" :key="e.id" class="table-row-hover">
                <td class="table-cell pl-5 font-mono text-xs text-gray-500 font-medium">{{ e.reference }}</td>
                <td class="table-cell">
                  <p class="font-semibold text-gray-900">{{ e.produitNom }}</p>
                  <p class="text-xs text-gray-400 mt-0.5">{{ e.produitReference }}</p>
                </td>
                <td class="table-cell">
                  <span class="inline-flex items-center gap-1.5 text-gray-600 text-sm">
                    <svg class="w-3.5 h-3.5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16"/>
                    </svg>
                    {{ e.entrepotNom }}
                  </span>
                </td>
                <td class="table-cell text-gray-500 text-sm">{{ e.fournisseurNom || '—' }}</td>
                <td class="table-cell">
                  <span class="inline-flex items-center gap-1 font-bold text-emerald-600">
                    <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 10l7-7m0 0l7 7m-7-7v18"/>
                    </svg>
                    {{ e.quantite }}
                  </span>
                </td>
                <td class="table-cell">
                  <span :class="['status-dot', statutConfig[e.statut]?.class ?? 'bg-gray-50 text-gray-700']">
                    <span :class="['w-1.5 h-1.5 rounded-full', statutConfig[e.statut]?.dot ?? 'bg-gray-400']"></span>
                    {{ statutConfig[e.statut]?.label ?? e.statut }}
                  </span>
                </td>
                <td class="table-cell text-xs text-gray-400">{{ formatDate(e.dateCreation) }}</td>
                <td class="table-cell pr-5">
                  <div class="flex items-center justify-end gap-1">
                    <button v-if="peutValiderMouvement && e.statut === 'BROUILLON'" @click="valider(e)"
                      class="p-1.5 text-gray-400 hover:text-emerald-600 hover:bg-emerald-50 rounded-lg transition-colors" title="Valider">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                      </svg>
                    </button>
                    <button v-if="peutValiderMouvement && e.statut === 'BROUILLON'" @click="annuler(e)"
                      class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors" title="Annuler">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                      </svg>
                    </button>
                    <span v-if="e.statut === 'VALIDE'" class="text-xs text-gray-400 italic">{{ e.validateurNom ?? '—' }}</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- MODAL -->
    <Teleport to="body">
      <Transition enter-active-class="transition duration-200" enter-from-class="opacity-0" enter-to-class="opacity-100"
        leave-active-class="transition duration-150" leave-from-class="opacity-100" leave-to-class="opacity-0">
        <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="showModal = false"/>
          <div class="relative bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
            <div class="flex items-center justify-between p-5 border-b border-gray-100">
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center">
                  <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
                  </svg>
                </div>
                <h2 class="text-base font-semibold text-gray-900">Nouveau bon de réception</h2>
              </div>
              <button @click="showModal = false" class="p-2 text-gray-400 hover:bg-gray-100 rounded-lg transition-colors">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            </div>
            <form @submit.prevent="soumettreCreation" class="p-5 space-y-4">
              <div v-if="erreurApi" class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-xl p-3">{{ erreurApi }}</div>
              <div>
                <label class="form-label">Produit <span class="text-red-500">*</span></label>
                <select v-model="form.produitId" class="form-input">
                  <option :value="null" disabled>Sélectionner un produit…</option>
                  <option v-for="p in produits" :key="p.id" :value="p.id">{{ p.nom }} ({{ p.reference }})</option>
                </select>
              </div>
              <div>
                <label class="form-label">Entrepôt de réception <span class="text-red-500">*</span></label>
                <select v-model="form.entrepotId" class="form-input">
                  <option :value="null" disabled>Sélectionner un entrepôt…</option>
                  <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
                </select>
              </div>
              <div>
                <label class="form-label">Fournisseur <span class="text-gray-400 text-xs">(optionnel)</span></label>
                <select v-model="form.fournisseurId" class="form-input">
                  <option :value="null">Aucun fournisseur</option>
                  <option v-for="f in fournisseurs" :key="f.id" :value="f.id">{{ f.nom }}</option>
                </select>
              </div>
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
              <div>
                <label class="form-label">Note <span class="text-gray-400 text-xs">(optionnel)</span></label>
                <textarea v-model="form.note" rows="2" placeholder="Remarques…" class="form-input resize-none"/>
              </div>
              <div class="flex items-center justify-end gap-3 pt-2 border-t border-gray-100">
                <button type="button" @click="showModal = false" class="btn-secondary">Annuler</button>
                <button type="submit" :disabled="isSubmitting" class="btn-primary bg-emerald-600 hover:bg-emerald-700 shadow-emerald-600/20">
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
