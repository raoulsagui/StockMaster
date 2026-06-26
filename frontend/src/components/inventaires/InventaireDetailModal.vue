<script setup>
/**
 * Modal de détail d'un inventaire.
 *
 * Affiche :
 *   - En-tête : référence, statut, entrepôt, dates, créateur
 *   - Métriques : lignes totales, avec écart, surplus, manque
 *   - Filtres rapides sur les lignes
 *   - Tableau théorie / réalité avec saisie inline
 *   - Actions : démarrer, valider, annuler (selon statut + rôle)
 */
import { ref, computed, watch } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import inventaireService from '@/services/inventaireService'

const props = defineProps({
  visible:      { type: Boolean, default: false },
  inventaireId: { type: Number,  default: null  },
})

const emit = defineEmits(['fermer', 'mis-a-jour'])

const authStore = useAuthStore()

// -------------------------------------------------------
// ÉTAT
// -------------------------------------------------------
const inventaire    = ref(null)
const isLoading     = ref(false)
const erreur        = ref('')
const actionErreur  = ref('')
const actionLoading = ref(false)

// Filtres lignes
const filtreLignes    = ref('TOUTES')
const rechercheLignes = ref('')

// Saisie inline : { [ligneId]: { quantiteComptee, note } }
const saisieEnCours = ref({})

// -------------------------------------------------------
// CHARGEMENT à l'ouverture du modal
// -------------------------------------------------------
watch(() => props.visible, async (open) => {
  if (!open) {
    // Réinitialise tout à la fermeture
    inventaire.value   = null
    erreur.value       = ''
    actionErreur.value = ''
    filtreLignes.value    = 'TOUTES'
    rechercheLignes.value = ''
    saisieEnCours.value   = {}
    return
  }
  if (!props.inventaireId) return
  await charger()
})

async function charger() {
  isLoading.value = true
  erreur.value    = ''
  try {
    inventaire.value = await inventaireService.findById(props.inventaireId)
  } catch {
    erreur.value = 'Impossible de charger cet inventaire.'
  } finally {
    isLoading.value = false
  }
}

// -------------------------------------------------------
// COMPUTED : lignes filtrées
// -------------------------------------------------------
const lignesFiltrees = computed(() => {
  if (!inventaire.value?.lignes) return []
  let lignes = inventaire.value.lignes

  const texte = rechercheLignes.value.toLowerCase().trim()
  if (texte) {
    lignes = lignes.filter(l =>
      l.produitNom.toLowerCase().includes(texte) ||
      l.produitReference.toLowerCase().includes(texte)
    )
  }

  switch (filtreLignes.value) {
    case 'NON_COMPTEES': return lignes.filter(l => !l.comptee)
    case 'AVEC_ECART':   return lignes.filter(l => l.comptee && l.ecart !== 0)
    case 'ECART_POS':    return lignes.filter(l => l.comptee && l.ecart > 0)
    case 'ECART_NEG':    return lignes.filter(l => l.comptee && l.ecart < 0)
    default:             return lignes
  }
})

// -------------------------------------------------------
// DROITS
// -------------------------------------------------------
const peutDemarrer = computed(() => inventaire.value?.statut === 'BROUILLON')
const peutSaisir   = computed(() => inventaire.value?.statut === 'EN_COURS')
const peutValider  = computed(() =>
  inventaire.value?.statut === 'EN_COURS' &&
  inventaire.value?.pourcentageProgression === 100 &&
  ['ADMIN', 'GESTIONNAIRE'].includes(authStore.role)
)
const peutAnnuler  = computed(() =>
  ['BROUILLON', 'EN_COURS'].includes(inventaire.value?.statut) &&
  ['ADMIN', 'GESTIONNAIRE'].includes(authStore.role)
)

// -------------------------------------------------------
// ACTIONS
// -------------------------------------------------------
async function demarrer() {
  actionLoading.value = true
  actionErreur.value  = ''
  try {
    inventaire.value = await inventaireService.demarrer(inventaire.value.id)
    emit('mis-a-jour', inventaire.value)
  } catch (e) {
    actionErreur.value = e.response?.data || 'Erreur lors du démarrage.'
  } finally {
    actionLoading.value = false
  }
}

async function valider() {
  actionLoading.value = true
  actionErreur.value  = ''
  try {
    inventaire.value = await inventaireService.valider(inventaire.value.id)
    emit('mis-a-jour', inventaire.value)
  } catch (e) {
    actionErreur.value = e.response?.data || 'Erreur lors de la validation.'
  } finally {
    actionLoading.value = false
  }
}

async function annuler() {
  actionLoading.value = true
  actionErreur.value  = ''
  try {
    inventaire.value = await inventaireService.annuler(inventaire.value.id)
    emit('mis-a-jour', inventaire.value)
  } catch (e) {
    actionErreur.value = e.response?.data || "Erreur lors de l'annulation."
  } finally {
    actionLoading.value = false
  }
}

// -------------------------------------------------------
// SAISIE INLINE
// -------------------------------------------------------
function ouvrirSaisie(ligne) {
  if (!peutSaisir.value) return
  saisieEnCours.value = {
    ...saisieEnCours.value,
    [ligne.id]: {
      quantiteComptee: ligne.quantiteComptee ?? ligne.quantiteTheorique,
      note:            ligne.note ?? '',
    },
  }
}

function fermerSaisie(ligneId) {
  const s = { ...saisieEnCours.value }
  delete s[ligneId]
  saisieEnCours.value = s
}

async function enregistrerSaisie(ligne) {
  const saisie = saisieEnCours.value[ligne.id]
  if (!saisie) return
  try {
    const updated = await inventaireService.saisirQuantite(
      inventaire.value.id,
      ligne.id,
      { quantiteComptee: Number(saisie.quantiteComptee), note: saisie.note || null }
    )
    const idx = inventaire.value.lignes.findIndex(l => l.id === ligne.id)
    if (idx !== -1) inventaire.value.lignes[idx] = updated

    // Recalcul local des métriques
    const total   = inventaire.value.lignes.length
    const comptes = inventaire.value.lignes.filter(l => l.comptee).length
    inventaire.value.nombreLignesComptees     = comptes
    inventaire.value.pourcentageProgression   = total === 0 ? 0 : Math.round(comptes / total * 100)
    inventaire.value.nombreLignesAvecEcart    = inventaire.value.lignes.filter(l => l.comptee && l.ecart !== 0).length
    inventaire.value.nombreLignesEcartPositif = inventaire.value.lignes.filter(l => l.comptee && l.ecart > 0).length
    inventaire.value.nombreLignesEcartNegatif = inventaire.value.lignes.filter(l => l.comptee && l.ecart < 0).length

    emit('mis-a-jour', inventaire.value)
    fermerSaisie(ligne.id)
  } catch (e) {
    actionErreur.value = e.response?.data || 'Erreur lors de la saisie.'
    fermerSaisie(ligne.id)
  }
}

// -------------------------------------------------------
// HELPERS
// -------------------------------------------------------
const statutConfig = {
  BROUILLON: { label: 'Brouillon', color: 'text-yellow-700 bg-yellow-100' },
  EN_COURS:  { label: 'En cours',  color: 'text-blue-700 bg-blue-100'     },
  VALIDE:    { label: 'Validé',    color: 'text-green-700 bg-green-100'   },
  ANNULE:    { label: 'Annulé',    color: 'text-red-700 bg-red-100'       },
}

const filtresBoutons = [
  { value: 'TOUTES',       label: 'Toutes'       },
  { value: 'NON_COMPTEES', label: 'Non comptées' },
  { value: 'AVEC_ECART',   label: 'Avec écart'   },
  { value: 'ECART_POS',    label: '▲ Surplus'    },
  { value: 'ECART_NEG',    label: '▼ Manque'     },
]
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition-opacity duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="visible"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40"
        @click.self="$emit('fermer')"
      >
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-5xl max-h-[92vh] flex flex-col overflow-hidden">

          <!-- ═══ EN-TÊTE ═══ -->
          <div class="flex items-start justify-between px-6 py-4 border-b border-gray-100 flex-shrink-0">

            <!-- Loading state dans l'en-tête -->
            <div v-if="isLoading" class="flex items-center gap-3">
              <svg class="w-5 h-5 animate-spin text-blue-500" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
              <span class="text-sm text-gray-500">Chargement…</span>
            </div>

            <!-- Titre quand chargé -->
            <div v-else-if="inventaire" class="flex-1 min-w-0">
              <div class="flex items-center gap-3 flex-wrap">
                <h2 class="text-lg font-bold text-gray-900 font-mono">{{ inventaire.reference }}</h2>
                <span
                  :class="[statutConfig[inventaire.statut]?.color, 'px-2.5 py-0.5 rounded-full text-xs font-semibold']"
                >
                  {{ statutConfig[inventaire.statut]?.label }}
                </span>
                <span class="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-gray-100 text-gray-600">
                  {{ inventaire.type === 'COMPLET' ? 'Complet' : 'Partiel' }}
                </span>
              </div>
              <p class="text-xs text-gray-500 mt-1">
                {{ inventaire.entrepotNom }}
                · Créé par {{ inventaire.createurNom }}
                · {{ new Date(inventaire.datePrevue).toLocaleDateString('fr-FR') }}
                <span v-if="inventaire.valideurNom">
                  · Validé par {{ inventaire.valideurNom }}
                </span>
              </p>
            </div>

            <div v-else class="flex-1">
              <p class="text-sm text-red-600">{{ erreur }}</p>
            </div>

            <!-- Bouton fermer -->
            <button
              @click="$emit('fermer')"
              class="ml-4 flex-shrink-0 p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <!-- ═══ CORPS ═══ -->
          <div v-if="inventaire" class="flex-1 overflow-y-auto px-6 py-5 space-y-5">

            <!-- Actions -->
            <div class="flex flex-wrap items-center gap-2">
              <button
                v-if="peutDemarrer"
                @click="demarrer"
                :disabled="actionLoading"
                class="btn-primary disabled:opacity-50 text-sm"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"/>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
                Démarrer le comptage
              </button>

              <button
                v-if="peutValider"
                @click="valider"
                :disabled="actionLoading"
                class="btn-success disabled:opacity-50 text-sm"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                </svg>
                Valider et ajuster le stock
              </button>

              <button
                v-if="peutAnnuler"
                @click="annuler"
                :disabled="actionLoading"
                class="btn-danger disabled:opacity-50 text-sm"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
                Annuler l'inventaire
              </button>
            </div>

            <!-- Erreur action -->
            <div v-if="actionErreur"
              class="bg-red-50 border border-red-200 rounded-lg px-4 py-3 text-sm text-red-700">
              {{ actionErreur }}
            </div>

            <!-- Note -->
            <p v-if="inventaire.note" class="text-sm text-gray-500 italic">
              "{{ inventaire.note }}"
            </p>

            <!-- MÉTRIQUES (sans progression) -->
            <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
              <div class="card p-3">
                <p class="text-xs text-gray-500 uppercase font-medium">Total lignes</p>
                <p class="text-xl font-bold text-gray-900 mt-1">{{ inventaire.nombreLignes }}</p>
                <p class="text-xs text-gray-400 mt-0.5">produits</p>
              </div>
              <div class="card p-3">
                <p class="text-xs text-gray-500 uppercase font-medium">Avec écart</p>
                <p class="text-xl font-bold text-orange-600 mt-1">{{ inventaire.nombreLignesAvecEcart }}</p>
                <p class="text-xs text-gray-400 mt-0.5">à ajuster</p>
              </div>
              <div class="card p-3 border-l-4 border-green-400">
                <p class="text-xs text-green-600 uppercase font-medium">▲ Surplus</p>
                <p class="text-xl font-bold text-green-700 mt-1">{{ inventaire.nombreLignesEcartPositif }}</p>
                <p class="text-xs text-gray-400 mt-0.5">écart positif</p>
              </div>
              <div class="card p-3 border-l-4 border-red-400">
                <p class="text-xs text-red-600 uppercase font-medium">▼ Manque</p>
                <p class="text-xl font-bold text-red-700 mt-1">{{ inventaire.nombreLignesEcartNegatif }}</p>
                <p class="text-xs text-gray-400 mt-0.5">écart négatif</p>
              </div>
            </div>

            <!-- FILTRES LIGNES -->
            <div class="flex flex-col sm:flex-row gap-3">
              <div class="flex-1 relative">
                <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
                  fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0"/>
                </svg>
                <input
                  v-model="rechercheLignes"
                  type="text"
                  placeholder="Rechercher un produit…"
                  class="form-input pl-9 w-full"
                />
              </div>
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="f in filtresBoutons"
                  :key="f.value"
                  @click="filtreLignes = f.value"
                  :class="[
                    'px-3 py-1.5 rounded-lg text-xs font-medium transition-colors',
                    filtreLignes === f.value
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-100 text-gray-600 hover:bg-gray-200',
                  ]"
                >
                  {{ f.label }}
                </button>
              </div>
            </div>

            <!-- TABLEAU DES LIGNES -->
            <div class="border border-gray-200 rounded-xl overflow-hidden">
              <div class="overflow-x-auto">
                <table class="w-full text-sm">
                  <thead class="bg-gray-50 border-b border-gray-100">
                    <tr>
                      <th class="table-header">Produit</th>
                      <th class="table-header text-right">Théorique</th>
                      <th class="table-header text-right">Réel compté</th>
                      <th class="table-header text-center">Écart</th>
                      <th class="table-header">Note</th>
                      <th class="table-header">Statut</th>
                      <th v-if="peutSaisir" class="table-header text-right">Action</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-gray-50">

                    <tr v-if="lignesFiltrees.length === 0">
                      <td :colspan="peutSaisir ? 7 : 6"
                        class="py-10 text-center text-gray-400 text-sm">
                        Aucune ligne ne correspond au filtre sélectionné.
                      </td>
                    </tr>

                    <template v-for="ligne in lignesFiltrees" :key="ligne.id">

                      <!-- Mode lecture -->
                      <tr v-if="!saisieEnCours[ligne.id]"
                        :class="[
                          'transition-colors',
                          ligne.comptee && ligne.ecart !== 0 ? 'bg-orange-50/40' : 'hover:bg-gray-50',
                        ]"
                      >
                        <td class="table-cell">
                          <p class="font-medium text-gray-900">{{ ligne.produitNom }}</p>
                          <p class="text-xs text-gray-400">
                            {{ ligne.produitReference }}
                            <span v-if="ligne.produitCategorieNom"> · {{ ligne.produitCategorieNom }}</span>
                          </p>
                        </td>
                        <td class="table-cell text-right">
                          <span class="font-mono font-medium text-gray-700">{{ ligne.quantiteTheorique }}</span>
                        </td>
                        <td class="table-cell text-right">
                          <span v-if="ligne.comptee" class="font-mono font-semibold text-gray-900">{{ ligne.quantiteComptee }}</span>
                          <span v-else class="text-xs text-gray-300 italic">—</span>
                        </td>
                        <td class="table-cell text-center">
                          <template v-if="ligne.comptee">
                            <span v-if="ligne.ecart === 0"
                              class="px-2 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-600">= 0</span>
                            <span v-else-if="ligne.ecart > 0"
                              class="px-2 py-0.5 rounded-full text-xs font-bold bg-green-100 text-green-700">▲ +{{ ligne.ecart }}</span>
                            <span v-else
                              class="px-2 py-0.5 rounded-full text-xs font-bold bg-red-100 text-red-700">▼ {{ ligne.ecart }}</span>
                          </template>
                          <span v-else class="text-gray-300 text-xs">—</span>
                        </td>
                        <td class="table-cell">
                          <span class="text-xs text-gray-500 italic">{{ ligne.note || '—' }}</span>
                        </td>
                        <td class="table-cell">
                          <span v-if="ligne.comptee"
                            class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-700">
                            <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                              <path fill-rule="evenodd"
                                d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                                clip-rule="evenodd"/>
                            </svg>
                            Comptée
                          </span>
                          <span v-else class="px-2 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-700">
                            En attente
                          </span>
                        </td>
                        <td v-if="peutSaisir" class="table-cell text-right">
                          <button
                            @click="ouvrirSaisie(ligne)"
                            class="btn-icon text-blue-600 hover:bg-blue-50"
                            :title="ligne.comptee ? 'Corriger' : 'Saisir'"
                          >
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                            </svg>
                          </button>
                        </td>
                      </tr>

                      <!-- Mode saisie inline -->
                      <tr v-else class="bg-blue-50/60">
                        <td class="table-cell">
                          <p class="font-medium text-gray-900">{{ ligne.produitNom }}</p>
                          <p class="text-xs text-gray-400">{{ ligne.produitReference }}</p>
                        </td>
                        <td class="table-cell text-right">
                          <span class="font-mono text-gray-600">{{ ligne.quantiteTheorique }}</span>
                        </td>
                        <td class="table-cell">
                          <input
                            v-model.number="saisieEnCours[ligne.id].quantiteComptee"
                            type="number" min="0"
                            class="form-input text-right w-24 text-sm font-mono"
                            @keyup.enter="enregistrerSaisie(ligne)"
                            @keyup.esc="fermerSaisie(ligne.id)"
                          />
                        </td>
                        <td class="table-cell text-center">
                          <span
                            v-if="saisieEnCours[ligne.id].quantiteComptee !== null"
                            :class="[
                              'px-2 py-0.5 rounded-full text-xs font-bold',
                              saisieEnCours[ligne.id].quantiteComptee - ligne.quantiteTheorique > 0 ? 'bg-green-100 text-green-700'
                              : saisieEnCours[ligne.id].quantiteComptee - ligne.quantiteTheorique < 0 ? 'bg-red-100 text-red-700'
                              : 'bg-gray-100 text-gray-600',
                            ]"
                          >
                            {{ saisieEnCours[ligne.id].quantiteComptee - ligne.quantiteTheorique > 0 ? '▲ +' : '' }}{{ saisieEnCours[ligne.id].quantiteComptee - ligne.quantiteTheorique }}
                          </span>
                        </td>
                        <td class="table-cell">
                          <input
                            v-model="saisieEnCours[ligne.id].note"
                            type="text" placeholder="Note…"
                            class="form-input text-sm w-full"
                            @keyup.enter="enregistrerSaisie(ligne)"
                            @keyup.esc="fermerSaisie(ligne.id)"
                          />
                        </td>
                        <td class="table-cell" colspan="2">
                          <div class="flex items-center justify-end gap-2">
                            <button @click="enregistrerSaisie(ligne)"
                              class="px-3 py-1.5 bg-blue-600 text-white rounded-lg text-xs font-medium hover:bg-blue-700">
                              Enregistrer
                            </button>
                            <button @click="fermerSaisie(ligne.id)"
                              class="px-3 py-1.5 bg-gray-100 text-gray-600 rounded-lg text-xs font-medium hover:bg-gray-200">
                              Annuler
                            </button>
                          </div>
                        </td>
                      </tr>

                    </template>
                  </tbody>
                </table>
              </div>
            </div>

          </div>

          <!-- ═══ PIED ═══ -->
          <div class="flex items-center justify-end px-6 py-4 border-t border-gray-100 bg-gray-50 flex-shrink-0">
            <button @click="$emit('fermer')" class="btn-secondary">Fermer</button>
          </div>

        </div>
      </div>
    </Transition>
  </Teleport>
</template>
