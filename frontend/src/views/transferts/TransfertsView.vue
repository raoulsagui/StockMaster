<script setup>
import { ref, computed, onMounted } from 'vue'
import AppLayout        from '@/layout/AppLayout.vue'
import transfertService from '@/services/transfertService'
import produitService   from '@/services/produitService'
import entrepotService  from '@/services/entrepotService'
import stockService     from '@/services/stockService'
import { usePermissions } from '@/composables/usePermissions'
import { useToast }       from '@/composables/useToast'

const { peutCreerMouvement } = usePermissions()
const toast = useToast()

const transferts = ref([])
const produits   = ref([])
const entrepots  = ref([])
const stocks     = ref([])
const isLoading  = ref(false)
const erreur     = ref('')

onMounted(() => chargerDonnees())

async function chargerDonnees() {
  isLoading.value = true
  erreur.value    = ''
  try {
    const [t, p, e, s] = await Promise.all([
      transfertService.findAll(),
      produitService.findAll(),
      entrepotService.findActifs(),
      stockService.findAll(),
    ])
    transferts.value = t
    produits.value   = p
    entrepots.value  = e
    stocks.value     = s
  } catch {
    erreur.value = 'Impossible de charger les données.'
  } finally {
    isLoading.value = false
  }
}

const filtreStatut = ref('')
const transfertsFiltres = computed(() =>
  transferts.value.filter(t => !filtreStatut.value || t.statut === filtreStatut.value)
)

const nbBrouillons = computed(() => transferts.value.filter(t => t.statut === 'BROUILLON').length)
const nbExpedies   = computed(() => transferts.value.filter(t => t.statut === 'EXPEDIE').length)
const nbRecus      = computed(() => transferts.value.filter(t => t.statut === 'RECU').length)

// -------------------------------------------------------
// MODAL CRÉATION
// -------------------------------------------------------
const showModal    = ref(false)
const isSubmitting = ref(false)
const erreurApi    = ref('')
const form = ref({ produitId: null, entrepotSourceId: null, entrepotDestinationId: null, quantite: '', note: '' })

// Entrepôts source : uniquement ceux où le produit sélectionné a du stock
const entrepotsSource = computed(() => {
  if (!form.value.produitId) return entrepots.value
  const idsAvecStock = stocks.value
    .filter(s => s.produitId === form.value.produitId && s.quantiteDisponible > 0)
    .map(s => s.entrepotId)
  return entrepots.value.filter(e => idsAvecStock.includes(e.id))
})

// Entrepôts destination : tous sauf la source
const entrepotsDestination = computed(() =>
  entrepots.value.filter(e => e.id !== form.value.entrepotSourceId)
)

// Stock dispo dans la source pour le produit sélectionné
const stockDispo = computed(() => {
  if (!form.value.produitId || !form.value.entrepotSourceId) return null
  return stocks.value.find(s => s.produitId === form.value.produitId && s.entrepotId === form.value.entrepotSourceId)
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
    erreurApi.value = "L'entrepôt source et destination doivent être différents."
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
    toast.success('Transfert créé.')
  } catch (e) {
    erreurApi.value = e.response?.data ?? 'Erreur lors de la création.'
  } finally {
    isSubmitting.value = false
  }
}

async function expedier(t) {
  try {
    const updated = await transfertService.expedier(t.id)
    const idx = transferts.value.findIndex(x => x.id === updated.id)
    if (idx !== -1) transferts.value[idx] = updated
    toast.success('Transfert expédié.')
  } catch (e) {
    toast.error(e.response?.data ?? "Erreur lors de l'expédition.")
  }
}

async function receptionner(t) {
  try {
    const updated = await transfertService.receptionner(t.id)
    const idx = transferts.value.findIndex(x => x.id === updated.id)
    if (idx !== -1) transferts.value[idx] = updated
    toast.success('Transfert réceptionné.')
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

const statutConfig = {
  BROUILLON: { label: 'Brouillon', dot: 'bg-amber-400',  class: 'bg-amber-50 text-amber-700 ring-1 ring-amber-200'    },
  EXPEDIE:   { label: 'Expédié',   dot: 'bg-blue-500',   class: 'bg-blue-50 text-blue-700 ring-1 ring-blue-200'        },
  RECU:      { label: 'Reçu',      dot: 'bg-emerald-500', class: 'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200' },
  ANNULE:    { label: 'Annulé',    dot: 'bg-red-400',     class: 'bg-red-50 text-red-700 ring-1 ring-red-200'           },
}

const formatDate = (d) => d ? new Date(d).toLocaleString('fr-FR', {
  day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
}) : '—'
</script>

<template>
  <AppLayout>
    <div class="space-y-5">

<!-- EN-TÊTE sobre -->
       <div class="page-header">
         <div class="flex items-center justify-between">
           <div>
             <h1 class="text-xl font-semibold text-gray-900">Transferts</h1>
             <p class="text-sm text-gray-400 mt-0.5">{{ transferts.length }} transferts inter-entrepôts</p>
           </div>
           <button v-if="peutCreerMouvement" @click="ouvrirCreer"
             class="inline-flex items-center gap-2 bg-white border border-gray-200 text-gray-700 hover:bg-gray-50 px-4 py-2 rounded-xl text-sm font-medium transition-colors shadow-sm">
             <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
               <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
             </svg>
             Nouveau transfert
           </button>
         </div>
         <div class="flex gap-4 mt-4">
           <div class="bg-gray-100 rounded-xl px-3 py-2 text-center">
             <p class="text-lg font-bold text-gray-900">{{ nbBrouillons }}</p>
             <p class="text-xs text-gray-500">En attente</p>
           </div>
           <div class="bg-gray-100 rounded-xl px-3 py-2 text-center">
             <p class="text-lg font-bold text-gray-900">{{ nbExpedies }}</p>
             <p class="text-xs text-gray-500">En transit</p>
           </div>
           <div class="bg-gray-100 rounded-xl px-3 py-2 text-center">
             <p class="text-lg font-bold text-gray-900">{{ nbRecus }}</p>
             <p class="text-xs text-gray-500">Reçus</p>
           </div>
         </div>
       </div>

      <!-- FILTRES PILLS -->
      <div class="flex flex-wrap gap-2">
        <button v-for="s in ['', 'BROUILLON', 'EXPEDIE', 'RECU', 'ANNULE']" :key="s"
          @click="filtreStatut = s"
          :class="['inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full text-sm font-medium transition-all duration-200 border',
            filtreStatut === s
              ? 'bg-gray-900 text-white border-gray-900 shadow-sm'
              : 'bg-white text-gray-600 border-gray-200 hover:border-gray-300 hover:bg-gray-50']"
        >
          <span v-if="s && statutConfig[s]" :class="['w-1.5 h-1.5 rounded-full', statutConfig[s].dot]"></span>
          {{ s === '' ? 'Tous' : (statutConfig[s]?.label ?? s) }}
          <span v-if="s === ''" class="text-xs opacity-60">({{ transferts.length }})</span>
        </button>
      </div>

      <div v-if="erreur" class="rounded-xl bg-red-50 border border-red-200 text-red-700 text-sm p-4">{{ erreur }}</div>

      <div v-if="isLoading" class="card p-12 text-center text-gray-400">
        <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-blue-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
        Chargement…
      </div>

      <div v-if="!isLoading" class="card p-0 overflow-hidden">
        <div v-if="transfertsFiltres.length === 0" class="py-16 text-center text-gray-300">
          <svg class="w-10 h-10 mx-auto mb-3 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"/>
          </svg>
          <p class="text-sm">Aucun transfert trouvé.</p>
        </div>
        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50/80 border-b border-gray-100">
              <tr>
                <th class="table-header pl-5">Référence</th>
                <th class="table-header">Produit</th>
                <th class="table-header">Trajet</th>
                <th class="table-header">Quantité</th>
                <th class="table-header">Statut</th>
                <th class="table-header">Date</th>
                <th class="table-header text-right pr-5">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <tr v-for="t in transfertsFiltres" :key="t.id" class="table-row-hover">
                <td class="table-cell pl-5 font-mono text-xs text-gray-500 font-medium">{{ t.reference }}</td>
                <td class="table-cell">
                  <p class="font-semibold text-gray-900">{{ t.produitNom }}</p>
                  <p class="text-xs text-gray-400 mt-0.5">{{ t.produitReference }}</p>
                </td>
                <td class="table-cell">
                  <div class="flex items-center gap-2 text-sm">
                    <span class="font-medium text-gray-700 bg-gray-100 px-2 py-0.5 rounded-lg">{{ t.entrepotSourceNom }}</span>
                    <svg class="w-4 h-4 text-blue-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 8l4 4m0 0l-4 4m4-4H3"/>
                    </svg>
                    <span class="font-medium text-gray-700 bg-gray-100 px-2 py-0.5 rounded-lg">{{ t.entrepotDestinationNom }}</span>
                  </div>
                </td>
                <td class="table-cell font-bold text-blue-600">{{ t.quantite }}</td>
                <td class="table-cell">
                  <span :class="['status-dot', statutConfig[t.statut]?.class ?? 'bg-gray-50 text-gray-700']">
                    <span :class="['w-1.5 h-1.5 rounded-full', statutConfig[t.statut]?.dot ?? 'bg-gray-400']"></span>
                    {{ statutConfig[t.statut]?.label ?? t.statut }}
                  </span>
                </td>
                <td class="table-cell text-xs text-gray-400">{{ formatDate(t.dateCreation) }}</td>
                <td class="table-cell pr-5">
                  <div class="flex items-center justify-end gap-1">
                    <button v-if="peutCreerMouvement && t.statut === 'BROUILLON'" @click="expedier(t)"
                      class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors" title="Expédier">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 8l4 4m0 0l-4 4m4-4H3"/>
                      </svg>
                    </button>
                    <button v-if="peutCreerMouvement && t.statut === 'EXPEDIE'" @click="receptionner(t)"
                      class="p-1.5 text-gray-400 hover:text-emerald-600 hover:bg-emerald-50 rounded-lg transition-colors" title="Réceptionner">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                      </svg>
                    </button>
                    <button v-if="peutCreerMouvement && t.statut === 'BROUILLON'" @click="annuler(t)"
                      class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors" title="Annuler">
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
      <Transition enter-active-class="transition duration-200" enter-from-class="opacity-0" enter-to-class="opacity-100"
        leave-active-class="transition duration-150" leave-from-class="opacity-100" leave-to-class="opacity-0">
        <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="showModal = false"/>
          <div class="relative bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
            <div class="flex items-center justify-between p-5 border-b border-gray-100">
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center">
                  <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4"/>
                  </svg>
                </div>
                <h2 class="text-base font-semibold text-gray-900">Nouveau transfert</h2>
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
                <select v-model="form.produitId" class="form-input" @change="form.entrepotSourceId = null">
                  <option :value="null" disabled>Sélectionner un produit…</option>
                  <option v-for="p in produits" :key="p.id" :value="p.id">{{ p.nom }} ({{ p.reference }})</option>
                </select>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="form-label">Entrepôt source <span class="text-red-500">*</span></label>
                  <select v-model="form.entrepotSourceId" class="form-input" @change="form.entrepotDestinationId = null">
                    <option :value="null" disabled>Source…</option>
                    <option v-for="e in entrepotsSource" :key="e.id" :value="e.id">{{ e.nom }}</option>
                  </select>
                  <!-- Dispo affiché si sélectionné -->
                  <p v-if="stockDispo" class="text-xs text-gray-400 mt-1">
                    Disponible : <span class="font-semibold text-gray-700">{{ stockDispo.quantiteDisponible }}</span> unités
                  </p>
                  <p v-else-if="form.produitId && !form.entrepotSourceId" class="text-xs text-amber-600 mt-1">
                    Sélectionnez l'entrepôt source
                  </p>
                </div>
                <div>
                  <label class="form-label">Entrepôt destination <span class="text-red-500">*</span></label>
                  <select v-model="form.entrepotDestinationId" class="form-input">
                    <option :value="null" disabled>Destination…</option>
                    <option v-for="e in entrepotsDestination" :key="e.id" :value="e.id">{{ e.nom }}</option>
                  </select>
                </div>
              </div>

              <!-- Alerte si produit non en stock dans la source -->
              <div v-if="form.produitId && entrepotsSource.length === 0"
                class="bg-amber-50 border border-amber-200 text-amber-700 text-xs rounded-xl p-3 flex items-center gap-2">
                <svg class="w-4 h-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                </svg>
                Ce produit n'est en stock dans aucun entrepôt.
              </div>

              <div>
                <label class="form-label">Quantité <span class="text-red-500">*</span></label>
                <input v-model="form.quantite" type="number" min="1"
                  :max="stockDispo?.quantiteDisponible ?? undefined"
                  placeholder="Ex : 20" class="form-input"/>
              </div>
              <div>
                <label class="form-label">Note <span class="text-gray-400 text-xs">(optionnel)</span></label>
                <textarea v-model="form.note" rows="2" placeholder="Raison du transfert…" class="form-input resize-none"/>
              </div>
<div class="flex items-center justify-end gap-3 pt-2 border-t border-gray-100">
                       <button type="button" @click="showModal = false" class="btn-secondary">Annuler</button>
                       <button type="submit" :disabled="isSubmitting || entrepotsSource.length === 0" class="btn-primary">
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
