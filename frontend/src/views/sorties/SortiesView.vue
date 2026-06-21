<script setup>
import { ref, computed, onMounted } from 'vue'
import AppLayout       from '@/layout/AppLayout.vue'
import sortieService   from '@/services/sortieService'
import produitService  from '@/services/produitService'
import entrepotService from '@/services/entrepotService'
import { usePermissions } from '@/composables/usePermissions'
import { useToast }       from '@/composables/useToast'

const { peutCreerMouvement, peutValiderMouvement } = usePermissions()
const toast = useToast()

const sorties   = ref([])
const produits  = ref([])
const entrepots = ref([])
const isLoading = ref(false)
const erreur    = ref('')

onMounted(() => chargerDonnees())

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

const filtreStatut = ref('')
const sortiesFiltrees = computed(() =>
  sorties.value.filter(s => !filtreStatut.value || s.statut === filtreStatut.value)
)

const nbBrouillons = computed(() => sorties.value.filter(s => s.statut === 'BROUILLON').length)
const nbValides    = computed(() => sorties.value.filter(s => s.statut === 'VALIDE').length)
const nbAnnules    = computed(() => sorties.value.filter(s => s.statut === 'ANNULE').length)

const showModal    = ref(false)
const isSubmitting = ref(false)
const erreurApi    = ref('')
const form = ref({ produitId: null, entrepotId: null, quantite: '', motif: 'LIVRAISON', destinataire: '', note: '' })

const MOTIFS = [
  { value: 'LIVRAISON',          label: 'Livraison client'      },
  { value: 'RETOUR_FOURNISSEUR', label: 'Retour fournisseur'    },
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
    toast.success('Bon de sortie créé.')
  } catch (e) {
    erreurApi.value = e.response?.data ?? 'Erreur lors de la création.'
  } finally {
    isSubmitting.value = false
  }
}

async function valider(sortie) {
  try {
    const updated = await sortieService.valider(sortie.id)
    const idx = sorties.value.findIndex(s => s.id === updated.id)
    if (idx !== -1) sorties.value[idx] = updated
    toast.success('Bon validé — stock déduit.')
  } catch (e) {
    toast.error(e.response?.data ?? 'Erreur lors de la validation.')
  }
}

async function annuler(sortie) {
  try {
    const updated = await sortieService.annuler(sortie.id)
    const idx = sorties.value.findIndex(s => s.id === updated.id)
    if (idx !== -1) sorties.value[idx] = updated
    toast.success(`Bon ${sortie.reference} annulé.`)
  } catch (e) {
    toast.error(e.response?.data ?? "Erreur lors de l'annulation.")
  }
}

const statutConfig = {
  BROUILLON: { label: 'Brouillon', dot: 'bg-amber-400',  class: 'bg-amber-50 text-amber-700 ring-1 ring-amber-200'     },
  VALIDE:    { label: 'Validé',    dot: 'bg-emerald-500', class: 'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200' },
  ANNULE:    { label: 'Annulé',    dot: 'bg-red-400',     class: 'bg-red-50 text-red-700 ring-1 ring-red-200'           },
}

const motifLabels = {
  LIVRAISON: 'Livraison', RETOUR_FOURNISSEUR: 'Retour fournisseur',
  CASSE: 'Casse', PERTE: 'Perte', AUTRE: 'Autre',
}

const formatDate = (d) => d ? new Date(d).toLocaleString('fr-FR', {
  day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
}) : '—'
</script>

<template>
  <AppLayout>
    <div class="space-y-5">

      <!-- EN-TÊTE GRADIENT -->
      <div class="page-header bg-gradient-to-r from-orange-500 to-red-600 shadow-lg shadow-orange-500/20">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-xl font-bold text-white">Sorties de stock</h1>
            <p class="text-orange-100 text-sm mt-0.5">{{ sorties.length }} bons de sortie</p>
          </div>
          <button v-if="peutCreerMouvement" @click="ouvrirCreer"
            class="inline-flex items-center gap-2 bg-white/20 hover:bg-white/30 text-white px-4 py-2 rounded-xl text-sm font-medium transition-colors backdrop-blur-sm border border-white/20">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
            </svg>
            Nouveau bon
          </button>
        </div>
        <div class="flex gap-4 mt-4">
          <div class="bg-white/15 rounded-xl px-3 py-2 text-center">
            <p class="text-lg font-bold text-white">{{ nbBrouillons }}</p>
            <p class="text-xs text-orange-100">En attente</p>
          </div>
          <div class="bg-white/15 rounded-xl px-3 py-2 text-center">
            <p class="text-lg font-bold text-white">{{ nbValides }}</p>
            <p class="text-xs text-orange-100">Validés</p>
          </div>
          <div class="bg-white/15 rounded-xl px-3 py-2 text-center">
            <p class="text-lg font-bold text-white">{{ nbAnnules }}</p>
            <p class="text-xs text-orange-100">Annulés</p>
          </div>
        </div>
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
          <span v-if="s === ''" class="text-xs opacity-60">({{ sorties.length }})</span>
        </button>
      </div>

      <div v-if="erreur" class="rounded-xl bg-red-50 border border-red-200 text-red-700 text-sm p-4">{{ erreur }}</div>

      <div v-if="isLoading" class="card p-12 text-center text-gray-400">
        <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-orange-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
        Chargement…
      </div>

      <div v-if="!isLoading" class="card p-0 overflow-hidden">
        <div v-if="sortiesFiltrees.length === 0" class="py-16 text-center text-gray-300">
          <svg class="w-10 h-10 mx-auto mb-3 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 16l4-4m0 0l-4-4m4 4H7"/>
          </svg>
          <p class="text-sm">Aucun bon de sortie trouvé.</p>
        </div>
        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50/80 border-b border-gray-100">
              <tr>
                <th class="table-header pl-5">Référence</th>
                <th class="table-header">Produit</th>
                <th class="table-header">Entrepôt</th>
                <th class="table-header">Motif</th>
                <th class="table-header">Quantité</th>
                <th class="table-header">Statut</th>
                <th class="table-header">Date</th>
                <th class="table-header text-right pr-5">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <tr v-for="s in sortiesFiltrees" :key="s.id" class="table-row-hover">
                <td class="table-cell pl-5 font-mono text-xs text-gray-500 font-medium">{{ s.reference }}</td>
                <td class="table-cell">
                  <p class="font-semibold text-gray-900">{{ s.produitNom }}</p>
                  <p class="text-xs text-gray-400 mt-0.5">{{ s.produitReference }}</p>
                </td>
                <td class="table-cell">
                  <span class="inline-flex items-center gap-1.5 text-gray-600 text-sm">
                    <svg class="w-3.5 h-3.5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16"/>
                    </svg>
                    {{ s.entrepotNom }}
                  </span>
                </td>
                <td class="table-cell">
                  <span class="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded-full font-medium">
                    {{ motifLabels[s.motif] ?? s.motif }}
                  </span>
                </td>
                <td class="table-cell">
                  <span class="inline-flex items-center gap-1 font-bold text-red-500">
                    <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M19 14l-7 7m0 0l-7-7m7 7V3"/>
                    </svg>
                    {{ s.quantite }}
                  </span>
                </td>
                <td class="table-cell">
                  <span :class="['status-dot', statutConfig[s.statut]?.class ?? 'bg-gray-50 text-gray-700']">
                    <span :class="['w-1.5 h-1.5 rounded-full', statutConfig[s.statut]?.dot ?? 'bg-gray-400']"></span>
                    {{ statutConfig[s.statut]?.label ?? s.statut }}
                  </span>
                </td>
                <td class="table-cell text-xs text-gray-400">{{ formatDate(s.dateCreation) }}</td>
                <td class="table-cell pr-5">
                  <div class="flex items-center justify-end gap-1">
                    <button v-if="peutValiderMouvement && s.statut === 'BROUILLON'" @click="valider(s)"
                      class="p-1.5 text-gray-400 hover:text-emerald-600 hover:bg-emerald-50 rounded-lg transition-colors" title="Valider">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                      </svg>
                    </button>
                    <button v-if="peutValiderMouvement && s.statut === 'BROUILLON'" @click="annuler(s)"
                      class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors" title="Annuler">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                      </svg>
                    </button>
                    <span v-if="s.statut === 'VALIDE'" class="text-xs text-gray-400 italic">{{ s.validateurNom ?? '—' }}</span>
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
                <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-orange-500 to-red-600 flex items-center justify-center">
                  <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
                  </svg>
                </div>
                <h2 class="text-base font-semibold text-gray-900">Nouveau bon de sortie</h2>
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
                <input v-model="form.destinataire" type="text" placeholder="Nom du client…" class="form-input"/>
              </div>
              <div>
                <label class="form-label">Note <span class="text-gray-400 text-xs">(optionnel)</span></label>
                <textarea v-model="form.note" rows="2" placeholder="Remarques…" class="form-input resize-none"/>
              </div>
              <div class="flex items-center justify-end gap-3 pt-2 border-t border-gray-100">
                <button type="button" @click="showModal = false" class="btn-secondary">Annuler</button>
                <button type="submit" :disabled="isSubmitting" class="btn-primary bg-orange-500 hover:bg-orange-600 shadow-orange-500/20">
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
