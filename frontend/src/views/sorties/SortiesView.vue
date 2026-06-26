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

      <!-- EN-TÊTE sobre -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-xl font-semibold text-gray-900">Sorties de stock</h1>
          <p class="text-sm text-gray-400 mt-0.5">{{ sorties.length }} bons de sortie</p>
        </div>
        <button v-if="peutCreerMouvement" @click="ouvrirCreer"
          class="inline-flex items-center gap-1.5 bg-indigo-600 text-white hover:bg-indigo-700 px-3.5 py-2 rounded-lg text-sm font-medium transition-colors shadow-sm">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
          </svg>
          Nouveau bon
        </button>
      </div>

      <!-- Mini KPI sobres -->
      <div class="flex gap-3">
        <div class="bg-white rounded-lg border border-gray-200 px-3.5 py-2 text-center min-w-[80px]">
          <p class="text-lg font-semibold text-gray-900">{{ nbBrouillons }}</p>
          <p class="text-xs text-gray-500">En attente</p>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 px-3.5 py-2 text-center min-w-[80px]">
          <p class="text-lg font-semibold text-gray-900">{{ nbValides }}</p>
          <p class="text-xs text-gray-500">Validés</p>
        </div>
        <div class="bg-white rounded-lg border border-gray-200 px-3.5 py-2 text-center min-w-[80px]">
          <p class="text-lg font-semibold text-gray-900">{{ nbAnnules }}</p>
          <p class="text-xs text-gray-500">Annulés</p>
        </div>
      </div>

      <!-- FILTRES PILLS -->
      <div class="flex flex-wrap gap-1.5">
        <button v-for="s in ['', 'BROUILLON', 'VALIDE', 'ANNULE']" :key="s"
          @click="filtreStatut = s"
          :class="['px-3 py-1.5 rounded-lg text-sm font-medium transition-colors border',
            filtreStatut === s
              ? 'bg-gray-900 text-white border-gray-900'
              : 'bg-white text-gray-600 border-gray-200 hover:border-gray-300 hover:bg-gray-50']"
        >
          <span v-if="s && statutConfig[s]" :class="['w-1.5 h-1.5 rounded-full inline-block mr-1.5 align-middle', statutConfig[s].dot]"></span>
          {{ s === '' ? 'Tous' : (statutConfig[s]?.label ?? s) }}
          <span v-if="s === ''" class="text-xs opacity-60 ml-1">({{ sorties.length }})</span>
        </button>
      </div>

      <div v-if="erreur" class="rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm p-3">{{ erreur }}</div>

      <div v-if="isLoading" class="bg-white rounded-lg border border-gray-200 p-10 text-center text-gray-400 text-sm">
        <svg class="w-5 h-5 animate-spin mx-auto mb-2 text-indigo-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
        Chargement…
      </div>

      <div v-if="!isLoading" class="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div v-if="sortiesFiltrees.length === 0" class="py-12 text-center text-gray-300 text-sm">
          <svg class="w-8 h-8 mx-auto mb-2 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 16l4-4m0 0l-4-4m4 4H7"/>
          </svg>
          Aucun bon de sortie trouvé.
        </div>
        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50 border-b border-gray-100">
              <tr>
                <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider py-3 px-4">Réf.</th>
                <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider py-3 px-4">Produit</th>
                <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider py-3 px-4">Entrepôt</th>
                <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider py-3 px-4 hidden sm:table-cell">Motif</th>
                <th class="text-right text-xs font-medium text-gray-500 uppercase tracking-wider py-3 px-4">Qté</th>
                <th class="text-center text-xs font-medium text-gray-500 uppercase tracking-wider py-3 px-4">Statut</th>
                <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider py-3 px-4 hidden md:table-cell">Date</th>
                <th class="text-right text-xs font-medium text-gray-500 uppercase tracking-wider py-3 px-4">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <tr v-for="s in sortiesFiltrees" :key="s.id" class="hover:bg-gray-50/50 transition-colors">
                <td class="py-3 px-4 font-mono text-xs text-gray-500 font-medium">{{ s.reference }}</td>
                <td class="py-3 px-4">
                  <p class="text-sm font-medium text-gray-900">{{ s.produitNom }}</p>
                  <p class="text-xs text-gray-400 mt-0.5">{{ s.produitReference }}</p>
                </td>
                <td class="py-3 px-4 text-sm text-gray-600">{{ s.entrepotNom }}</td>
                <td class="py-3 px-4 hidden sm:table-cell">
                  <span class="text-xs bg-gray-100 text-gray-600 px-1.5 py-0.5 rounded font-medium">{{ motifLabels[s.motif] ?? s.motif }}</span>
                </td>
                <td class="py-3 px-4 text-right text-sm font-semibold text-red-600">{{ s.quantite }}</td>
                <td class="py-3 px-4 text-center">
                  <span :class="['inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium', statutConfig[s.statut]?.class]">
                    <span :class="['w-1.5 h-1.5 rounded-full', statutConfig[s.statut]?.dot]"></span>
                    {{ statutConfig[s.statut]?.label ?? s.statut }}
                  </span>
                </td>
                <td class="py-3 px-4 text-xs text-gray-400 hidden md:table-cell">{{ formatDate(s.dateCreation) }}</td>
                <td class="py-3 px-4 text-right">
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
                    <span v-if="s.statut === 'VALIDE'" class="text-xs text-gray-400 italic">{{ s.validateurNom ?? '✓' }}</span>
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
          <div class="absolute inset-0 bg-black/40" @click="showModal = false"/>
          <div class="relative bg-white rounded-xl shadow-xl w-full max-w-md max-h-[90vh] overflow-y-auto">
            <div class="flex items-center justify-between p-4 border-b border-gray-100">
              <h2 class="text-base font-semibold text-gray-900">Nouveau bon de sortie</h2>
              <button @click="showModal = false" class="p-1.5 text-gray-400 hover:bg-gray-100 rounded-lg transition-colors">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            </div>
            <form @submit.prevent="soumettreCreation" class="p-4 space-y-3.5">
              <div v-if="erreurApi" class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg p-2.5">{{ erreurApi }}</div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Produit <span class="text-red-500">*</span></label>
                <select v-model="form.produitId" class="w-full text-sm border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                  <option :value="null" disabled>Sélectionner un produit…</option>
                  <option v-for="p in produits" :key="p.id" :value="p.id">{{ p.nom }} ({{ p.reference }})</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Entrepôt source <span class="text-red-500">*</span></label>
                <select v-model="form.entrepotId" class="w-full text-sm border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                  <option :value="null" disabled>Sélectionner un entrepôt…</option>
                  <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
                </select>
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">Quantité <span class="text-red-500">*</span></label>
                  <input v-model="form.quantite" type="number" min="1" placeholder="Ex : 10" class="w-full text-sm border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"/>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">Motif</label>
                  <select v-model="form.motif" class="w-full text-sm border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                    <option v-for="m in MOTIFS" :key="m.value" :value="m.value">{{ m.label }}</option>
                  </select>
                </div>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Destinataire <span class="text-gray-400 text-xs">(opt.)</span></label>
                <input v-model="form.destinataire" type="text" placeholder="Nom du client…" class="w-full text-sm border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"/>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Note <span class="text-gray-400 text-xs">(opt.)</span></label>
                <textarea v-model="form.note" rows="2" placeholder="Remarques…" class="w-full text-sm border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 resize-none"/>
              </div>
              <div class="flex items-center justify-end gap-2.5 pt-2 border-t border-gray-100">
                <button type="button" @click="showModal = false" class="px-4 py-2 text-sm font-medium text-gray-600 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">Annuler</button>
                <button type="submit" :disabled="isSubmitting" class="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 disabled:opacity-50 transition-colors">
                  <svg v-if="isSubmitting" class="w-4 h-4 animate-spin inline mr-1" fill="none" viewBox="0 0 24 24">
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
