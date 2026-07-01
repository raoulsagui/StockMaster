<script setup>
/**
 * Vue de détail d'un inventaire.
 *
 * Affiche :
 *   - En-tête avec les métriques (progression, écarts)
 *   - Tableau de comparaison théorie/réalité par ligne
 *   - Résumé des écarts (positifs / négatifs)
 *   - Actions : démarrer, saisir, valider, annuler
 */
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import AppLayout from '@/layout/AppLayout.vue'
import inventaireService from '@/services/inventaireService'

const route     = useRoute()
const router    = useRouter()
const authStore = useAuthStore()

const inventaire  = ref(null)
const isLoading   = ref(false)
const erreur      = ref('')
const actionErreur = ref('')
const actionLoading = ref(false)

// Filtres des lignes
const filtreLignes    = ref('TOUTES') // TOUTES | NON_COMPTEES | AVEC_ECART | ECART_POS | ECART_NEG
const rechercheLignes = ref('')

// Saisie inline
const saisieEnCours   = ref({}) // { [ligneId]: { quantiteComptee, note } }

// -------------------------------------------------------
// CHARGEMENT
// -------------------------------------------------------
onMounted(async () => { await charger() })

async function charger() {
  isLoading.value = true
  erreur.value    = ''
  try {
    inventaire.value = await inventaireService.findById(route.params.id)
  } catch {
    erreur.value = 'Inventaire introuvable.'
  } finally {
    isLoading.value = false
  }
}

// -------------------------------------------------------
// COMPUTED
// -------------------------------------------------------
const lignesFiltrees = computed(() => {
  if (!inventaire.value?.lignes) return []

  let lignes = inventaire.value.lignes

  // Filtre texte
  const texte = rechercheLignes.value.toLowerCase().trim()
  if (texte) {
    lignes = lignes.filter(l =>
      l.produitNom.toLowerCase().includes(texte) ||
      l.produitReference.toLowerCase().includes(texte)
    )
  }

  // Filtre type
  switch (filtreLignes.value) {
    case 'NON_COMPTEES': return lignes.filter(l => !l.comptee)
    case 'AVEC_ECART':   return lignes.filter(l => l.comptee && l.ecart !== 0)
    case 'ECART_POS':    return lignes.filter(l => l.comptee && l.ecart > 0)
    case 'ECART_NEG':    return lignes.filter(l => l.comptee && l.ecart < 0)
    default:             return lignes
  }
})

const peutValider = computed(() =>
  inventaire.value?.statut === 'EN_COURS'
  && inventaire.value?.pourcentageProgression === 100
  && ['ADMIN', 'GESTIONNAIRE'].includes(authStore.role)
)

const peutSaisir = computed(() =>
  inventaire.value?.statut === 'EN_COURS'
)

const peutDemarrer = computed(() =>
  inventaire.value?.statut === 'BROUILLON'
)

const peutAnnuler = computed(() =>
  ['BROUILLON', 'EN_COURS'].includes(inventaire.value?.statut)
  && ['ADMIN', 'GESTIONNAIRE'].includes(authStore.role)
)

// -------------------------------------------------------
// ACTIONS
// -------------------------------------------------------
async function demarrer() {
  if (!confirm('Démarrer le comptage de cet inventaire ?')) return
  actionLoading.value = true
  actionErreur.value  = ''
  try {
    inventaire.value = await inventaireService.demarrer(inventaire.value.id)
  } catch (e) {
    actionErreur.value = e.response?.data || 'Erreur lors du démarrage.'
  } finally {
    actionLoading.value = false
  }
}

async function valider() {
  if (!confirm(
    'Valider cet inventaire ?\n\nLes ajustements de stock seront appliqués immédiatement et cette action est irréversible.'
  )) return

  actionLoading.value = true
  actionErreur.value  = ''
  try {
    inventaire.value = await inventaireService.valider(inventaire.value.id)
  } catch (e) {
    actionErreur.value = e.response?.data || 'Erreur lors de la validation.'
  } finally {
    actionLoading.value = false
  }
}

async function annuler() {
  if (!confirm('Annuler cet inventaire ? Aucun ajustement ne sera appliqué.')) return
  actionLoading.value = true
  actionErreur.value  = ''
  try {
    inventaire.value = await inventaireService.annuler(inventaire.value.id)
  } catch (e) {
    actionErreur.value = e.response?.data || 'Erreur lors de l\'annulation.'
  } finally {
    actionLoading.value = false
  }
}

// Ouvre la saisie inline pour une ligne
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
    // Met à jour la ligne localement sans tout recharger
    const idx = inventaire.value.lignes.findIndex(l => l.id === ligne.id)
    if (idx !== -1) inventaire.value.lignes[idx] = updated

    // Recalcule la progression
    const total    = inventaire.value.lignes.length
    const comptes  = inventaire.value.lignes.filter(l => l.comptee).length
    inventaire.value.nombreLignesComptees   = comptes
    inventaire.value.pourcentageProgression = total === 0 ? 0 : Math.round(comptes / total * 100)
    inventaire.value.nombreLignesAvecEcart  = inventaire.value.lignes.filter(l => l.comptee && l.ecart !== 0).length
    inventaire.value.nombreLignesEcartPositif = inventaire.value.lignes.filter(l => l.comptee && l.ecart > 0).length
    inventaire.value.nombreLignesEcartNegatif = inventaire.value.lignes.filter(l => l.comptee && l.ecart < 0).length

    fermerSaisie(ligne.id)
  } catch (e) {
    alert(e.response?.data || 'Erreur lors de la saisie.')
  }
}

// -------------------------------------------------------
// HELPERS
// -------------------------------------------------------
const statutConfig = {
  BROUILLON: { label: 'Brouillon', color: 'text-yellow-700 bg-yellow-100' },
  EN_COURS:  { label: 'En cours',  color: 'text-blue-700   bg-blue-100'   },
  VALIDE:    { label: 'Validé',    color: 'text-green-700  bg-green-100'  },
  ANNULE:    { label: 'Annulé',    color: 'text-red-700    bg-red-100'    },
}
</script>

<template>
  <AppLayout>
    <div class="space-y-6">

      <!-- NAVIGATION RETOUR -->
      <button @click="router.push({ name: 'inventaires' })"
        class="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-900 transition-colors">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
        </svg>
        Retour aux inventaires
      </button>

      <!-- LOADING -->
      <div v-if="isLoading" class="card p-16 text-center text-gray-400">
        <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-blue-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
        Chargement de l'inventaire…
      </div>

      <div v-else-if="erreur" class="card p-8 text-center text-red-600 text-sm">{{ erreur }}</div>

      <template v-else-if="inventaire">

        <!-- EN-TÊTE -->
        <div class="flex flex-col lg:flex-row lg:items-start gap-4 justify-between">
          <div>
            <div class="flex items-center gap-3 flex-wrap">
              <h1 class="text-2xl font-bold text-gray-900 font-mono">{{ inventaire.reference }}</h1>
              <span
                :class="statutConfig[inventaire.statut]?.color"
                class="px-3 py-1 rounded-full text-xs font-semibold"
              >
                {{ statutConfig[inventaire.statut]?.label }}
              </span>
              <span class="px-3 py-1 rounded-full text-xs font-semibold bg-gray-100 text-gray-600">
                {{ inventaire.type === 'COMPLET' ? 'Inventaire complet' : 'Inventaire partiel' }}
              </span>
            </div>
            <p class="text-sm text-gray-500 mt-1">
              Entrepôt : <span class="font-medium text-gray-700">{{ inventaire.entrepotNom }}</span>
              · Créé par <span class="font-medium text-gray-700">{{ inventaire.createurNom }}</span>
            </p>
            <p v-if="inventaire.valideurNom" class="text-sm text-gray-500 mt-0.5">
              Validé par <span class="font-medium text-gray-700">{{ inventaire.valideurNom }}</span>
              le {{ new Date(inventaire.dateValidation).toLocaleString('fr-FR') }}
            </p>
            <p v-if="inventaire.note" class="text-sm text-gray-500 mt-1 italic">
              "{{ inventaire.note }}"
            </p>
          </div>

          <!-- Actions -->
          <div class="flex flex-wrap items-center gap-2 flex-shrink-0">
            <button
              v-if="peutDemarrer"
              @click="demarrer"
              :disabled="actionLoading"
              class="btn-primary disabled:opacity-50"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"/>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
              Démarrer
            </button>

            <button
              v-if="peutValider"
              @click="valider"
              :disabled="actionLoading"
              class="btn-success disabled:opacity-50"
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
        <div class="grid grid-cols-2 lg:grid-cols-5 gap-4">
          <!-- Progression -->
          <div class="card p-4 col-span-2 lg:col-span-1">
            <p class="text-xs text-gray-500 uppercase font-medium">Progression</p>
            <p class="text-2xl font-bold text-blue-600 mt-1">
              {{ inventaire.nombreLignesComptees }}<span class="text-base font-normal text-gray-400"> / {{ inventaire.nombreLignes }}</span>
            </p>
            <p class="text-xs text-gray-400 mt-1">lignes comptées</p>
          </div>

          <!-- Lignes totales -->
          <div class="card p-4">
            <p class="text-xs text-gray-500 uppercase font-medium">Total lignes</p>
            <p class="text-2xl font-bold text-gray-900 mt-1">{{ inventaire.nombreLignes }}</p>
            <p class="text-xs text-gray-400 mt-1">produits à compter</p>
          </div>

          <!-- Avec écart -->
          <div class="card p-4">
            <p class="text-xs text-gray-500 uppercase font-medium">Avec écart</p>
            <p class="text-2xl font-bold text-orange-600 mt-1">{{ inventaire.nombreLignesAvecEcart }}</p>
            <p class="text-xs text-gray-400 mt-1">lignes à ajuster</p>
          </div>

          <!-- Surplus -->
          <div class="card p-4 border-l-4 border-green-400">
            <p class="text-xs text-green-600 uppercase font-medium">▲ Surplus</p>
            <p class="text-2xl font-bold text-green-700 mt-1">{{ inventaire.nombreLignesEcartPositif }}</p>
            <p class="text-xs text-gray-400 mt-1">écart positif</p>
          </div>

          <!-- Manque -->
          <div class="card p-4 border-l-4 border-red-400">
            <p class="text-xs text-red-600 uppercase font-medium">▼ Manque</p>
            <p class="text-2xl font-bold text-red-700 mt-1">{{ inventaire.nombreLignesEcartNegatif }}</p>
            <p class="text-xs text-gray-400 mt-1">écart négatif</p>
          </div>
        </div>

        <!-- FILTRES LIGNES -->
        <div class="card p-4">
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
                class="form-input pl-9"
              />
            </div>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="f in [
                  { value: 'TOUTES',      label: 'Toutes' },
                  { value: 'NON_COMPTEES',label: 'Non comptées' },
                  { value: 'AVEC_ECART',  label: 'Avec écart' },
                  { value: 'ECART_POS',   label: '▲ Surplus' },
                  { value: 'ECART_NEG',   label: '▼ Manque' },
                ]"
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
        </div>

        <!-- TABLEAU DES LIGNES -->
        <div class="card p-0 overflow-hidden">
          <div class="overflow-x-auto">
            <table class="w-full">
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
                  <td :colspan="peutSaisir ? 7 : 6" class="py-12 text-center text-gray-400 text-sm">
                    Aucune ligne ne correspond au filtre sélectionné.
                  </td>
                </tr>

                <template v-for="ligne in lignesFiltrees" :key="ligne.id">
                  <!-- Mode affichage normal -->
                  <tr v-if="!saisieEnCours[ligne.id]"
                    :class="[
                      'transition-colors',
                      ligne.comptee && ligne.ecart !== 0 ? 'bg-orange-50/40' : 'hover:bg-gray-50',
                    ]"
                  >
                    <!-- Produit -->
                    <td class="table-cell">
                      <p class="text-sm font-medium text-gray-900">{{ ligne.produitNom }}</p>
                      <p class="text-xs text-gray-400">{{ ligne.produitReference }}
                        <span v-if="ligne.produitCategorieNom"> · {{ ligne.produitCategorieNom }}</span>
                      </p>
                    </td>

                    <!-- Quantité théorique -->
                    <td class="table-cell text-right">
                      <span class="text-sm font-mono font-medium text-gray-700">
                        {{ ligne.quantiteTheorique }}
                      </span>
                    </td>

                    <!-- Quantité comptée -->
                    <td class="table-cell text-right">
                      <span v-if="ligne.comptee" class="text-sm font-mono font-semibold text-gray-900">
                        {{ ligne.quantiteComptee }}
                      </span>
                      <span v-else class="text-xs text-gray-300 italic">—</span>
                    </td>

                    <!-- Écart -->
                    <td class="table-cell text-center">
                      <template v-if="ligne.comptee">
                        <span v-if="ligne.ecart === 0"
                          class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-600">
                          = 0
                        </span>
                        <span v-else-if="ligne.ecart > 0"
                          class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-bold bg-green-100 text-green-700">
                          ▲ +{{ ligne.ecart }}
                        </span>
                        <span v-else
                          class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-bold bg-red-100 text-red-700">
                          ▼ {{ ligne.ecart }}
                        </span>
                      </template>
                      <span v-else class="text-gray-300 text-xs">—</span>
                    </td>

                    <!-- Note -->
                    <td class="table-cell">
                      <span class="text-xs text-gray-500 italic">{{ ligne.note || '—' }}</span>
                    </td>

                    <!-- Statut ligne -->
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
                      <span v-else
                        class="px-2 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-700">
                        En attente
                      </span>
                    </td>

                    <!-- Action saisie -->
                    <td v-if="peutSaisir" class="table-cell text-right">
                      <button
                        @click="ouvrirSaisie(ligne)"
                        class="btn-icon text-blue-600 hover:bg-blue-50"
                        :title="ligne.comptee ? 'Corriger la saisie' : 'Saisir la quantité'"
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
                      <p class="text-sm font-medium text-gray-900">{{ ligne.produitNom }}</p>
                      <p class="text-xs text-gray-400">{{ ligne.produitReference }}</p>
                    </td>
                    <td class="table-cell text-right">
                      <span class="text-sm font-mono text-gray-600">{{ ligne.quantiteTheorique }}</span>
                    </td>
                    <td class="table-cell text-right" colspan="1">
                      <input
                        v-model.number="saisieEnCours[ligne.id].quantiteComptee"
                        type="number"
                        min="0"
                        class="form-input text-right w-24 text-sm font-mono"
                        @keyup.enter="enregistrerSaisie(ligne)"
                        @keyup.esc="fermerSaisie(ligne.id)"
                      />
                    </td>
                    <td class="table-cell text-center">
                      <span
                        v-if="saisieEnCours[ligne.id].quantiteComptee !== null"
                        :class="[
                          'inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-bold',
                          saisieEnCours[ligne.id].quantiteComptee - ligne.quantiteTheorique > 0
                            ? 'bg-green-100 text-green-700'
                            : saisieEnCours[ligne.id].quantiteComptee - ligne.quantiteTheorique < 0
                              ? 'bg-red-100 text-red-700'
                              : 'bg-gray-100 text-gray-600',
                        ]"
                      >
                        {{
                          saisieEnCours[ligne.id].quantiteComptee - ligne.quantiteTheorique > 0
                            ? '▲ +' : ''
                        }}{{ saisieEnCours[ligne.id].quantiteComptee - ligne.quantiteTheorique }}
                      </span>
                    </td>
                    <td class="table-cell" colspan="1">
                      <input
                        v-model="saisieEnCours[ligne.id].note"
                        type="text"
                        placeholder="Note…"
                        class="form-input text-sm w-full"
                        @keyup.enter="enregistrerSaisie(ligne)"
                        @keyup.esc="fermerSaisie(ligne.id)"
                      />
                    </td>
                    <td class="table-cell" colspan="2">
                      <div class="flex items-center justify-end gap-2">
                        <button
                          @click="enregistrerSaisie(ligne)"
                          class="px-3 py-1.5 bg-blue-600 text-white rounded-lg text-xs font-medium hover:bg-blue-700"
                        >
                          Enregistrer
                        </button>
                        <button
                          @click="fermerSaisie(ligne.id)"
                          class="px-3 py-1.5 bg-gray-100 text-gray-600 rounded-lg text-xs font-medium hover:bg-gray-200"
                        >
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

      </template>
    </div>
  </AppLayout>
</template>
