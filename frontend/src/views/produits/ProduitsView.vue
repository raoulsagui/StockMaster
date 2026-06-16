<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import AppLayout from '@/layout/AppLayout.vue'
import produitService from '@/services/produitService'
import categorieService from '@/services/categorieService'

const produits    = ref([])
const categories  = ref([])
const isLoading   = ref(false)
const erreur      = ref('')

onMounted(async () => {
  isLoading.value = true
  erreur.value    = ''
  try {
    ;[produits.value, categories.value] = await Promise.all([
      produitService.findAll(),
      categorieService.findAll(),
    ])
  } catch {
    erreur.value = 'Impossible de charger les données.'
  } finally {
    isLoading.value = false
  }
})

// -------------------------------------------------------
// FILTRES + PAGINATION
// -------------------------------------------------------
const recherche       = ref('')
const filtreCategorie = ref('')
const filtreStatut    = ref('')
const showFiltres     = ref(false)
const pageCourante    = ref(1)
const parPage         = ref(10)

const nbFiltresActifs = computed(() =>
  (filtreCategorie.value ? 1 : 0) + (filtreStatut.value ? 1 : 0)
)

watch([recherche, filtreCategorie, filtreStatut], () => { pageCourante.value = 1 })

const produitsFiltres = computed(() =>
  produits.value.filter(p => {
    const texte = recherche.value.toLowerCase()
    const matchTexte = !texte ||
      p.nom.toLowerCase().includes(texte) ||
      p.reference.toLowerCase().includes(texte) ||
      (p.codeBarres?.toLowerCase().includes(texte))
    const matchCategorie = !filtreCategorie.value || p.categorieId === Number(filtreCategorie.value)
    const matchStatut =
      !filtreStatut.value ||
      (filtreStatut.value === 'actif'   &&  p.actif) ||
      (filtreStatut.value === 'inactif' && !p.actif)
    return matchTexte && matchCategorie && matchStatut
  })
)

const totalPages       = computed(() => Math.ceil(produitsFiltres.value.length / parPage.value) || 1)
const produitsPagines  = computed(() => {
  const debut = (pageCourante.value - 1) * parPage.value
  return produitsFiltres.value.slice(debut, debut + parPage.value)
})

// -------------------------------------------------------
// FORM HELPERS
// -------------------------------------------------------
const formVide = () => ({
  reference: '', codeBarres: '', nom: '', description: '',
  categorieId: '', prixAchat: '', prixVente: '', poids: '', volume: '',
})

// -------------------------------------------------------
// MODAL CRÉATION
// -------------------------------------------------------
const showModalCreer  = ref(false)
const formCreer       = ref(formVide())
const erreursCreer    = ref({})
const erreurApiCreer  = ref('')
const isCreating      = ref(false)

function genererReference() {
  const suffix = Math.random().toString(36).substring(2, 6).toUpperCase()
  return `PROD-${suffix}`
}

function ouvrirModalCreer() {
  formCreer.value      = { ...formVide(), reference: genererReference() }
  erreursCreer.value   = {}
  erreurApiCreer.value = ''
  showModalCreer.value = true
}

function validerCreer() {
  const e = {}
  if (!formCreer.value.reference.trim()) e.reference = 'La référence est obligatoire.'
  if (!formCreer.value.nom.trim())       e.nom       = 'Le nom est obligatoire.'
  erreursCreer.value = e
  return Object.keys(e).length === 0
}

async function soumettreCreer() {
  if (!validerCreer()) return
  isCreating.value     = true
  erreurApiCreer.value = ''
  try {
    const created = await produitService.creer(toPayload(formCreer.value))
    produits.value.push(created)
    showModalCreer.value = false
  } catch (e) {
    const msg = e.response?.data
    erreurApiCreer.value = typeof msg === 'string' ? msg : 'Erreur lors de la création.'
  } finally {
    isCreating.value = false
  }
}

// -------------------------------------------------------
// MODAL MODIFICATION
// -------------------------------------------------------
const showModalModifier  = ref(false)
const formModifier       = ref({ id: null, ...formVide() })
const erreursModifier    = ref({})
const erreurApiModifier  = ref('')
const isModifying        = ref(false)

function ouvrirModalModifier(p) {
  formModifier.value = {
    id: p.id, reference: p.reference, codeBarres: p.codeBarres ?? '',
    nom: p.nom, description: p.description ?? '',
    categorieId: p.categorieId ?? '',
    prixAchat: p.prixAchat ?? '', prixVente: p.prixVente ?? '',
    poids: p.poids ?? '', volume: p.volume ?? '',
  }
  erreursModifier.value   = {}
  erreurApiModifier.value = ''
  showModalModifier.value = true
}

function validerModifier() {
  const e = {}
  if (!formModifier.value.reference.trim()) e.reference = 'La référence est obligatoire.'
  if (!formModifier.value.nom.trim())       e.nom       = 'Le nom est obligatoire.'
  erreursModifier.value = e
  return Object.keys(e).length === 0
}

async function soumettreModifier() {
  if (!validerModifier()) return
  isModifying.value       = true
  erreurApiModifier.value = ''
  try {
    const updated = await produitService.modifier(formModifier.value.id, toPayload(formModifier.value))
    const idx = produits.value.findIndex(p => p.id === updated.id)
    if (idx !== -1) produits.value[idx] = updated
    showModalModifier.value = false
  } catch (e) {
    const msg = e.response?.data
    erreurApiModifier.value = typeof msg === 'string' ? msg : 'Erreur lors de la modification.'
  } finally {
    isModifying.value = false
  }
}

// -------------------------------------------------------
// TOGGLE STATUT
// -------------------------------------------------------
async function toggleStatut(p) {
  try {
    const updated = await produitService.toggleActif(p.id)
    const idx = produits.value.findIndex(x => x.id === p.id)
    if (idx !== -1) produits.value[idx] = updated
  } catch {
    alert('Erreur lors de la mise à jour du statut.')
  }
}

// -------------------------------------------------------
// HELPERS
// -------------------------------------------------------
function toPayload(form) {
  return {
    reference:   form.reference,
    codeBarres:  form.codeBarres || null,
    nom:         form.nom,
    description: form.description || null,
    categorieId: form.categorieId || null,
    prixAchat:   form.prixAchat   !== '' ? Number(form.prixAchat)  : null,
    prixVente:   form.prixVente   !== '' ? Number(form.prixVente)  : null,
    poids:       form.poids       !== '' ? Number(form.poids)      : null,
    volume:      form.volume      !== '' ? Number(form.volume)     : null,
  }
}

const formatPrix = (val) => val != null ? `${Number(val).toFixed(2)} €` : '—'

const formatDate = (dateStr) => {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' })
}
</script>

<template>
  <AppLayout>
    <div class="space-y-4 md:space-y-6">

      <!-- EN-TÊTE -->
      <div class="flex items-center justify-between">
        <div class="hidden md:block">
          <h1 class="text-xl md:text-2xl font-bold text-gray-900">Produits</h1>
          <p class="text-sm text-gray-500 mt-0.5">{{ produits.length }} produit(s) enregistré(s)</p>
        </div>
        <button @click="ouvrirModalCreer" class="btn-primary justify-center self-end sm:self-auto">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
          </svg>
          Nouveau produit
        </button>
      </div>

      <!-- FILTRES -->
      <div class="card p-3 md:p-4">
        <!-- Mobile -->
        <div class="flex gap-3 md:hidden">
          <div class="relative flex-1">
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0"/>
            </svg>
            <input v-model="recherche" type="text" placeholder="Rechercher…" class="form-input pl-9"/>
          </div>
          <button @click="showFiltres = true"
            :class="['relative p-2.5 rounded-lg border transition-colors flex-shrink-0',
              nbFiltresActifs ? 'border-blue-500 bg-blue-50 text-blue-600' : 'border-gray-200 text-gray-500 hover:bg-gray-50']">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2a1 1 0 01-.293.707L13 13.414V19a1 1 0 01-.553.894l-4 2A1 1 0 017 21v-7.586L3.293 6.707A1 1 0 013 6V4z"/>
            </svg>
            <span v-if="nbFiltresActifs"
              class="absolute -top-1 -right-1 w-4 h-4 bg-blue-600 text-white text-xs rounded-full flex items-center justify-center">
              {{ nbFiltresActifs }}
            </span>
          </button>
        </div>
        <!-- Desktop -->
        <div class="hidden md:flex gap-3">
          <div class="relative flex-1">
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0"/>
            </svg>
            <input v-model="recherche" type="text" placeholder="Rechercher…" class="form-input pl-9"/>
          </div>
          <select v-model="filtreCategorie" class="form-input w-48">
            <option value="">Toutes les catégories</option>
            <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.nom }}</option>
          </select>
          <select v-model="filtreStatut" class="form-input w-40">
            <option value="">Tous les statuts</option>
            <option value="actif">Actif</option>
            <option value="inactif">Inactif</option>
          </select>
        </div>
      </div>

      <!-- BOTTOM SHEET FILTRES (mobile) -->
      <Teleport to="body">
        <Transition enter-active-class="transition ease-out duration-200" enter-from-class="opacity-0" enter-to-class="opacity-100"
          leave-active-class="transition ease-in duration-150" leave-from-class="opacity-100" leave-to-class="opacity-0">
          <div v-if="showFiltres" class="fixed inset-0 z-50 bg-black/40 md:hidden" @click="showFiltres = false"></div>
        </Transition>
        <Transition enter-active-class="transition ease-out duration-300" enter-from-class="translate-y-full" enter-to-class="translate-y-0"
          leave-active-class="transition ease-in duration-200" leave-from-class="translate-y-0" leave-to-class="translate-y-full">
          <div v-if="showFiltres" class="fixed bottom-0 inset-x-0 z-50 bg-white rounded-t-2xl shadow-2xl md:hidden">
            <div class="flex justify-center pt-3 pb-1">
              <div class="w-10 h-1 bg-gray-200 rounded-full"></div>
            </div>
            <div class="flex items-center justify-between px-5 py-3 border-b border-gray-100">
              <p class="text-sm font-semibold text-gray-800">Filtres</p>
              <button @click="showFiltres = false" class="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            </div>
            <div class="px-5 py-4 space-y-4">
              <div>
                <label class="form-label">Catégorie</label>
                <div class="grid grid-cols-2 gap-2 mt-1">
                  <button @click="filtreCategorie = ''"
                    :class="['px-3 py-2 rounded-lg text-sm font-medium border transition-colors',
                      filtreCategorie === '' ? 'bg-blue-600 text-white border-blue-600' : 'text-gray-600 border-gray-200']">
                    Toutes
                  </button>
                  <button v-for="c in categories" :key="c.id" @click="filtreCategorie = c.id"
                    :class="['px-3 py-2 rounded-lg text-sm font-medium border transition-colors',
                      filtreCategorie === c.id ? 'bg-blue-600 text-white border-blue-600' : 'text-gray-600 border-gray-200']">
                    {{ c.nom }}
                  </button>
                </div>
              </div>
              <div>
                <label class="form-label">Statut</label>
                <div class="grid grid-cols-3 gap-2 mt-1">
                  <button v-for="s in [['', 'Tous'], ['actif', 'Actif'], ['inactif', 'Inactif']]" :key="s[0]"
                    @click="filtreStatut = s[0]"
                    :class="['px-3 py-2 rounded-lg text-sm font-medium border transition-colors',
                      filtreStatut === s[0] ? 'bg-blue-600 text-white border-blue-600' : 'text-gray-600 border-gray-200']">
                    {{ s[1] }}
                  </button>
                </div>
              </div>
              <button @click="filtreCategorie = ''; filtreStatut = ''"
                class="w-full py-2 text-sm text-red-500 hover:bg-red-50 rounded-lg transition-colors">
                Réinitialiser les filtres
              </button>
            </div>
          </div>
        </Transition>
      </Teleport>

      <!-- ERREUR -->
      <div v-if="erreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">{{ erreur }}</div>

      <!-- TABLEAU -->
      <div class="card p-0 overflow-hidden">
        <div v-if="isLoading" class="p-12 text-center text-gray-400">
          <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-blue-500" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
          Chargement…
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50 border-b border-gray-100">
              <tr>
                <th class="table-header">Produit</th>
                <th class="table-header hidden md:table-cell">Référence</th>
                <th class="table-header hidden lg:table-cell">Catégorie</th>
                <th class="table-header hidden md:table-cell">Prix vente</th>
                <th class="table-header hidden sm:table-cell">Statut</th>
                <th class="table-header text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <tr v-if="produitsFiltres.length === 0">
                <td colspan="6" class="px-6 py-12 text-center text-gray-400 text-sm">
                  Aucun produit ne correspond aux critères.
                </td>
              </tr>
              <tr v-for="p in produitsPagines" :key="p.id" class="hover:bg-gray-50 transition-colors">
                <td class="table-cell">
                  <div>
                    <span class="font-medium text-gray-900 block">{{ p.nom }}</span>
                    <span class="text-xs text-gray-400 md:hidden">{{ p.reference }}</span>
                  </div>
                </td>
                <td class="table-cell text-gray-500 hidden md:table-cell">{{ p.reference }}</td>
                <td class="table-cell hidden lg:table-cell">
                  <span v-if="p.categorieNom" class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-purple-100 text-purple-700">
                    {{ p.categorieNom }}
                  </span>
                  <span v-else class="text-gray-400">—</span>
                </td>
                <td class="table-cell hidden md:table-cell text-gray-700 font-medium">{{ formatPrix(p.prixVente) }}</td>
                <td class="table-cell hidden sm:table-cell">
                  <span :class="p.actif ? 'badge-actif' : 'badge-inactif'">
                    {{ p.actif ? 'Actif' : 'Inactif' }}
                  </span>
                </td>
                <td class="table-cell">
                  <div class="flex items-center justify-end gap-1">
                    <button @click="ouvrirModalModifier(p)"
                      class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors" title="Modifier">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                      </svg>
                    </button>
                    <button @click="toggleStatut(p)"
                      :class="['p-1.5 rounded-lg transition-colors', p.actif
                        ? 'text-gray-400 hover:text-red-600 hover:bg-red-50'
                        : 'text-gray-400 hover:text-green-600 hover:bg-green-50']"
                      :title="p.actif ? 'Désactiver' : 'Réactiver'">
                      <svg v-if="p.actif" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                          d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"/>
                      </svg>
                      <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                      </svg>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- PAGINATION -->
        <div v-if="!isLoading && totalPages > 1"
          class="flex flex-col sm:flex-row items-center justify-between gap-3 px-4 py-3 border-t border-gray-100">
          <p class="text-sm text-gray-500">
            {{ (pageCourante - 1) * parPage + 1 }}–{{ Math.min(pageCourante * parPage, produitsFiltres.length) }}
            sur {{ produitsFiltres.length }} produits
          </p>
          <div class="flex items-center gap-1">
            <button @click="pageCourante--" :disabled="pageCourante === 1"
              class="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
              </svg>
            </button>
            <button v-for="p in totalPages" :key="p" @click="pageCourante = p"
              :class="['w-8 h-8 rounded-lg text-sm font-medium transition-colors',
                p === pageCourante ? 'bg-blue-600 text-white' : 'text-gray-600 hover:bg-gray-100']">
              {{ p }}
            </button>
            <button @click="pageCourante++" :disabled="pageCourante === totalPages"
              class="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL CRÉATION -->
    <Teleport to="body">
      <Transition enter-active-class="transition duration-200" enter-from-class="opacity-0" enter-to-class="opacity-100"
        leave-active-class="transition duration-150" leave-from-class="opacity-100" leave-to-class="opacity-0">
        <div v-if="showModalCreer" class="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
          <div class="absolute inset-0 bg-black/50" @click="showModalCreer = false"></div>
          <div class="relative bg-white w-full sm:max-w-xl sm:rounded-2xl rounded-t-2xl shadow-2xl max-h-[90vh] overflow-y-auto">
            <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 sticky top-0 bg-white rounded-t-2xl">
              <h2 class="text-lg font-semibold text-gray-900">Nouveau produit</h2>
              <button @click="showModalCreer = false" class="p-2 rounded-lg text-gray-400 hover:bg-gray-100 transition-colors">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            </div>
            <form @submit.prevent="soumettreCreer" class="px-6 py-5 space-y-4">
              <div v-if="erreurApiCreer" class="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg p-3">{{ erreurApiCreer }}</div>

              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="form-label">Référence <span class="text-red-500">*</span></label>
                  <div class="flex gap-1.5">
                    <input v-model="formCreer.reference" type="text" placeholder="REF-001"
                      :class="['form-input', erreursCreer.reference ? 'border-red-400' : '']"/>
                    <button type="button" @click="formCreer.reference = genererReference()"
                      class="flex-shrink-0 p-2 rounded-lg border border-gray-300 text-gray-500 hover:bg-gray-50 transition-colors" title="Générer une référence">
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
                      </svg>
                    </button>
                  </div>
                  <p v-if="erreursCreer.reference" class="form-error">{{ erreursCreer.reference }}</p>
                </div>
                <div>
                  <label class="form-label">Code-barres</label>
                  <input v-model="formCreer.codeBarres" type="text" placeholder="1234567890" class="form-input"/>
                </div>
              </div>

              <div>
                <label class="form-label">Nom <span class="text-red-500">*</span></label>
                <input v-model="formCreer.nom" type="text" placeholder="Nom du produit"
                  :class="['form-input', erreursCreer.nom ? 'border-red-400' : '']"/>
                <p v-if="erreursCreer.nom" class="form-error">{{ erreursCreer.nom }}</p>
              </div>

              <div>
                <label class="form-label">Catégorie</label>
                <select v-model="formCreer.categorieId" class="form-input">
                  <option value="">Sans catégorie</option>
                  <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.nom }}</option>
                </select>
              </div>

              <div>
                <label class="form-label">Description</label>
                <textarea v-model="formCreer.description" rows="2" class="form-input resize-none" placeholder="Description optionnelle…"/>
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="form-label">Prix d'achat (€)</label>
                  <input v-model="formCreer.prixAchat" type="number" step="0.01" min="0" placeholder="0.00" class="form-input"/>
                </div>
                <div>
                  <label class="form-label">Prix de vente (€)</label>
                  <input v-model="formCreer.prixVente" type="number" step="0.01" min="0" placeholder="0.00" class="form-input"/>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="form-label">Poids (kg)</label>
                  <input v-model="formCreer.poids" type="number" step="0.001" min="0" placeholder="0.000" class="form-input"/>
                </div>
                <div>
                  <label class="form-label">Volume (m³)</label>
                  <input v-model="formCreer.volume" type="number" step="0.001" min="0" placeholder="0.000" class="form-input"/>
                </div>
              </div>

              <div class="flex gap-3 pt-2">
                <button type="button" @click="showModalCreer = false" class="btn-secondary flex-1">Annuler</button>
                <button type="submit" :disabled="isCreating" class="btn-primary flex-1 justify-center">
                  <svg v-if="isCreating" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                  </svg>
                  Créer le produit
                </button>
              </div>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- MODAL MODIFICATION -->
    <Teleport to="body">
      <Transition enter-active-class="transition duration-200" enter-from-class="opacity-0" enter-to-class="opacity-100"
        leave-active-class="transition duration-150" leave-from-class="opacity-100" leave-to-class="opacity-0">
        <div v-if="showModalModifier" class="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
          <div class="absolute inset-0 bg-black/50" @click="showModalModifier = false"></div>
          <div class="relative bg-white w-full sm:max-w-xl sm:rounded-2xl rounded-t-2xl shadow-2xl max-h-[90vh] overflow-y-auto">
            <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 sticky top-0 bg-white rounded-t-2xl">
              <h2 class="text-lg font-semibold text-gray-900">Modifier le produit</h2>
              <button @click="showModalModifier = false" class="p-2 rounded-lg text-gray-400 hover:bg-gray-100 transition-colors">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            </div>
            <form @submit.prevent="soumettreModifier" class="px-6 py-5 space-y-4">
              <div v-if="erreurApiModifier" class="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg p-3">{{ erreurApiModifier }}</div>

              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="form-label">Référence <span class="text-red-500">*</span></label>
                  <input v-model="formModifier.reference" type="text"
                    :class="['form-input', erreursModifier.reference ? 'border-red-400' : '']"/>
                  <p v-if="erreursModifier.reference" class="form-error">{{ erreursModifier.reference }}</p>
                </div>
                <div>
                  <label class="form-label">Code-barres</label>
                  <input v-model="formModifier.codeBarres" type="text" class="form-input"/>
                </div>
              </div>

              <div>
                <label class="form-label">Nom <span class="text-red-500">*</span></label>
                <input v-model="formModifier.nom" type="text"
                  :class="['form-input', erreursModifier.nom ? 'border-red-400' : '']"/>
                <p v-if="erreursModifier.nom" class="form-error">{{ erreursModifier.nom }}</p>
              </div>

              <div>
                <label class="form-label">Catégorie</label>
                <select v-model="formModifier.categorieId" class="form-input">
                  <option value="">Sans catégorie</option>
                  <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.nom }}</option>
                </select>
              </div>

              <div>
                <label class="form-label">Description</label>
                <textarea v-model="formModifier.description" rows="2" class="form-input resize-none"/>
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="form-label">Prix d'achat (€)</label>
                  <input v-model="formModifier.prixAchat" type="number" step="0.01" min="0" class="form-input"/>
                </div>
                <div>
                  <label class="form-label">Prix de vente (€)</label>
                  <input v-model="formModifier.prixVente" type="number" step="0.01" min="0" class="form-input"/>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="form-label">Poids (kg)</label>
                  <input v-model="formModifier.poids" type="number" step="0.001" min="0" class="form-input"/>
                </div>
                <div>
                  <label class="form-label">Volume (m³)</label>
                  <input v-model="formModifier.volume" type="number" step="0.001" min="0" class="form-input"/>
                </div>
              </div>

              <div class="flex gap-3 pt-2">
                <button type="button" @click="showModalModifier = false" class="btn-secondary flex-1">Annuler</button>
                <button type="submit" :disabled="isModifying" class="btn-primary flex-1 justify-center">
                  <svg v-if="isModifying" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                  </svg>
                  Enregistrer
                </button>
              </div>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>

  </AppLayout>
</template>
