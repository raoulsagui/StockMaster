<script setup>
/**
 * Modal de création / modification d'une commande fournisseur.
 *
 * Mode création  : commandeId = null  → POST /api/commandes
 * Mode édition   : commandeId = {id} → PUT  /api/commandes/{id}
 *
 * Fonctionnalités :
 *   - Sélection fournisseur (actifs uniquement)
 *   - Sélection entrepôt de destination (actifs uniquement)
 *   - Date de livraison prévue
 *   - Gestion dynamique des lignes (ajout / suppression / saisie prix)
 *   - Calcul en temps réel du montant total
 *   - Validation : au moins une ligne, pas de doublon produit
 */
import { ref, computed, watch, onMounted } from 'vue'
import fournisseurService from '@/services/fournisseurService'
import entrepotService    from '@/services/entrepotService'
import produitService     from '@/services/produitService'
import commandeService    from '@/services/commandeService'

const props = defineProps({
  visible:    { type: Boolean, default: false },
  commandeId: { type: Number,  default: null  },
})
const emit = defineEmits(['fermer', 'sauvegarde'])

// -------------------------------------------------------
// ÉTAT
// -------------------------------------------------------
const form = ref({
  fournisseurId:       null,
  entrepotId:          null,
  dateLivraisonPrevue: '',
  note:                '',
  lignes:              [],
})

const erreur     = ref('')
const isLoading  = ref(false)
const isEdition  = computed(() => !!props.commandeId)

// Données de référence
const fournisseurs = ref([])
const entrepots    = ref([])
const produits     = ref([])

// -------------------------------------------------------
// CHARGEMENT DES DONNÉES DE RÉFÉRENCE
// -------------------------------------------------------
onMounted(async () => {
  const [f, e, p] = await Promise.all([
    fournisseurService.findActifs(),
    entrepotService.findActifs(),
    produitService.findAll(),
  ])
  fournisseurs.value = f
  entrepots.value    = e
  produits.value     = p.filter(prod => prod.actif)
})

// En mode édition, pré-remplir le formulaire quand la modal s'ouvre
watch(() => props.visible, async (open) => {
  if (!open) return
  if (!isEdition.value) {
    reinitialiser()
    return
  }
  isLoading.value = true
  try {
    const c = await commandeService.findById(props.commandeId)
    form.value = {
      fournisseurId:       c.fournisseurId,
      entrepotId:          c.entrepotId,
      dateLivraisonPrevue: c.dateLivraisonPrevue ?? '',
      note:                c.note ?? '',
      lignes: c.lignes.map(l => ({
        produitId:        l.produitId,
        quantiteCommandee: l.quantiteCommandee,
        prixUnitaire:     l.prixUnitaire ?? '',
        note:             l.note ?? '',
      })),
    }
  } catch {
    erreur.value = 'Impossible de charger la commande.'
  } finally {
    isLoading.value = false
  }
})

// -------------------------------------------------------
// PRODUITS DISPONIBLES (hors ceux déjà dans les lignes)
// -------------------------------------------------------
const produitsDisponibles = computed(() => {
  const idsUtilises = new Set(form.value.lignes.map(l => l.produitId).filter(Boolean))
  return produits.value.filter(p => !idsUtilises.has(p.id))
})

function produitParId(id) {
  return produits.value.find(p => p.id === id) ?? null
}

// -------------------------------------------------------
// GESTION DES LIGNES
// -------------------------------------------------------
function ajouterLigne() {
  form.value.lignes.push({
    produitId:         null,
    quantiteCommandee: 1,
    prixUnitaire:      '',
    note:              '',
  })
}

function supprimerLigne(index) {
  form.value.lignes.splice(index, 1)
}

// -------------------------------------------------------
// MONTANT TOTAL (calculé en temps réel)
// -------------------------------------------------------
const montantTotal = computed(() => {
  let total = 0
  let incomplete = false
  for (const l of form.value.lignes) {
    if (!l.prixUnitaire || !l.quantiteCommandee) { incomplete = true; continue }
    total += Number(l.prixUnitaire) * Number(l.quantiteCommandee)
  }
  if (total === 0 && form.value.lignes.length > 0) return null
  return { valeur: total, incomplete }
})

function formatMontant(v) {
  return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(v)
}

// -------------------------------------------------------
// VALIDATION FORMULAIRE
// -------------------------------------------------------
const peutSoumettre = computed(() => {
  if (!form.value.fournisseurId || !form.value.entrepotId) return false
  if (form.value.lignes.length === 0) return false
  return form.value.lignes.every(l => l.produitId && l.quantiteCommandee >= 1)
})

// -------------------------------------------------------
// SOUMISSION
// -------------------------------------------------------
async function soumettre() {
  erreur.value    = ''
  isLoading.value = true
  try {
    const payload = {
      fournisseurId:       Number(form.value.fournisseurId),
      entrepotId:          Number(form.value.entrepotId),
      dateLivraisonPrevue: form.value.dateLivraisonPrevue || null,
      note:                form.value.note || null,
      lignes: form.value.lignes.map(l => ({
        produitId:         Number(l.produitId),
        quantiteCommandee: Number(l.quantiteCommandee),
        prixUnitaire:      l.prixUnitaire !== '' ? Number(l.prixUnitaire) : null,
        note:              l.note || null,
      })),
    }
    const result = isEdition.value
      ? await commandeService.modifier(props.commandeId, payload)
      : await commandeService.creer(payload)

    emit('sauvegarde', result)
    fermer()
  } catch (e) {
    erreur.value = e.response?.data || 'Une erreur est survenue.'
  } finally {
    isLoading.value = false
  }
}

function fermer() {
  reinitialiser()
  emit('fermer')
}

function reinitialiser() {
  form.value = { fournisseurId: null, entrepotId: null, dateLivraisonPrevue: '', note: '', lignes: [] }
  erreur.value = ''
}
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
      <div v-if="visible"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40"
        @click.self="fermer"
      >
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-3xl max-h-[92vh] flex flex-col overflow-hidden">

          <!-- EN-TÊTE -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 flex-shrink-0">
            <div>
              <h2 class="text-lg font-bold text-gray-900">
                {{ isEdition ? 'Modifier la commande' : 'Nouvelle commande fournisseur' }}
              </h2>
              <p class="text-xs text-gray-500 mt-0.5">
                {{ isEdition ? 'Modifiable uniquement en statut BROUILLON' : 'La commande sera créée en brouillon' }}
              </p>
            </div>
            <button @click="fermer" class="btn-icon text-gray-400 hover:text-gray-600">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <!-- CORPS -->
          <div class="flex-1 overflow-y-auto px-6 py-5 space-y-6">

            <!-- Erreur globale -->
            <div v-if="erreur" class="bg-red-50 border border-red-200 rounded-lg px-4 py-3 text-sm text-red-700">
              {{ erreur }}
            </div>

            <!-- === EN-TÊTE DE LA COMMANDE === -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">

              <!-- Fournisseur -->
              <div>
                <label class="form-label">Fournisseur <span class="text-red-500">*</span></label>
                <select v-model="form.fournisseurId" class="form-input">
                  <option :value="null" disabled>Choisir un fournisseur…</option>
                  <option v-for="f in fournisseurs" :key="f.id" :value="f.id">
                    {{ f.nom }}
                  </option>
                </select>
              </div>

              <!-- Entrepôt de destination -->
              <div>
                <label class="form-label">Entrepôt de destination <span class="text-red-500">*</span></label>
                <select v-model="form.entrepotId" class="form-input">
                  <option :value="null" disabled>Choisir un entrepôt…</option>
                  <option v-for="e in entrepots" :key="e.id" :value="e.id">
                    {{ e.nom }}
                  </option>
                </select>
              </div>

              <!-- Date livraison prévue -->
              <div>
                <label class="form-label">Date de livraison prévue</label>
                <input type="date" v-model="form.dateLivraisonPrevue" class="form-input" />
              </div>

              <!-- Note -->
              <div>
                <label class="form-label">Note <span class="text-gray-400 font-normal">(optionnel)</span></label>
                <input type="text" v-model="form.note"
                  placeholder="Commentaire sur la commande…"
                  class="form-input"
                />
              </div>

            </div>

            <!-- === LIGNES DE COMMANDE === -->
            <div>
              <div class="flex items-center justify-between mb-3">
                <h3 class="text-sm font-semibold text-gray-800">
                  Lignes de commande
                  <span class="ml-1.5 text-xs font-normal text-gray-400">
                    ({{ form.lignes.length }} ligne{{ form.lignes.length > 1 ? 's' : '' }})
                  </span>
                </h3>
                <button
                  type="button"
                  @click="ajouterLigne"
                  :disabled="produitsDisponibles.length === 0"
                  class="flex items-center gap-1.5 px-3 py-1.5 bg-blue-600 text-white rounded-lg text-xs font-medium hover:bg-blue-700 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                >
                  <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
                  </svg>
                  Ajouter un produit
                </button>
              </div>

              <!-- État vide -->
              <div v-if="form.lignes.length === 0"
                class="border-2 border-dashed border-gray-200 rounded-xl p-8 text-center text-gray-400 text-sm">
                <svg class="w-8 h-8 mx-auto mb-2 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                    d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
                </svg>
                Aucune ligne. Cliquez sur "Ajouter un produit" pour commencer.
              </div>

              <!-- Tableau des lignes -->
              <div v-else class="border border-gray-200 rounded-xl overflow-hidden">
                <table class="w-full text-sm">
                  <thead class="bg-gray-50 border-b border-gray-100">
                    <tr>
                      <th class="text-left px-4 py-2.5 text-xs font-semibold text-gray-500 uppercase">Produit</th>
                      <th class="text-right px-3 py-2.5 text-xs font-semibold text-gray-500 uppercase w-28">Qté</th>
                      <th class="text-right px-3 py-2.5 text-xs font-semibold text-gray-500 uppercase w-36">Prix unit. HT</th>
                      <th class="text-right px-3 py-2.5 text-xs font-semibold text-gray-500 uppercase w-32">Sous-total</th>
                      <th class="w-10"></th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-gray-50">
                    <tr v-for="(ligne, idx) in form.lignes" :key="idx"
                      class="hover:bg-gray-50/50 transition-colors">

                      <!-- Produit -->
                      <td class="px-4 py-2">
                        <select v-model="ligne.produitId" class="form-input text-sm w-full">
                          <option :value="null" disabled>Sélectionner…</option>
                          <!-- Produit déjà sélectionné sur cette ligne -->
                          <option
                            v-if="ligne.produitId && produitParId(ligne.produitId)"
                            :value="ligne.produitId"
                          >
                            {{ produitParId(ligne.produitId).nom }}
                          </option>
                          <!-- Produits disponibles (non encore utilisés) -->
                          <option v-for="p in produitsDisponibles" :key="p.id" :value="p.id">
                            {{ p.nom }}
                          </option>
                        </select>
                        <p v-if="ligne.produitId && produitParId(ligne.produitId)"
                          class="text-xs text-gray-400 mt-0.5 pl-0.5">
                          Réf. {{ produitParId(ligne.produitId).reference }}
                        </p>
                      </td>

                      <!-- Quantité -->
                      <td class="px-3 py-2">
                        <input
                          v-model.number="ligne.quantiteCommandee"
                          type="number" min="1"
                          class="form-input text-right w-full font-mono"
                        />
                      </td>

                      <!-- Prix unitaire -->
                      <td class="px-3 py-2">
                        <div class="relative">
                          <input
                            v-model="ligne.prixUnitaire"
                            type="number" min="0" step="0.01"
                            placeholder="0.00"
                            class="form-input text-right w-full pr-7 font-mono"
                          />
                          <span class="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 text-xs pointer-events-none">€</span>
                        </div>
                      </td>

                      <!-- Sous-total -->
                      <td class="px-3 py-2 text-right">
                        <span v-if="ligne.prixUnitaire && ligne.quantiteCommandee"
                          class="text-sm font-mono font-medium text-gray-800">
                          {{ formatMontant(Number(ligne.prixUnitaire) * Number(ligne.quantiteCommandee)) }}
                        </span>
                        <span v-else class="text-gray-300 text-xs">—</span>
                      </td>

                      <!-- Supprimer -->
                      <td class="px-2 py-2 text-center">
                        <button
                          type="button"
                          @click="supprimerLigne(idx)"
                          class="btn-icon text-red-400 hover:bg-red-50 hover:text-red-600"
                          title="Supprimer cette ligne"
                        >
                          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                              d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
                          </svg>
                        </button>
                      </td>
                    </tr>
                  </tbody>
                  <!-- Pied : total -->
                  <tfoot v-if="montantTotal" class="border-t-2 border-gray-200 bg-gray-50">
                    <tr>
                      <td colspan="3" class="px-4 py-3 text-sm font-semibold text-gray-700 text-right">
                        Total HT
                        <span v-if="montantTotal.incomplete" class="text-xs font-normal text-gray-400 ml-1">
                          (partiel — certains prix manquants)
                        </span>
                      </td>
                      <td class="px-3 py-3 text-right">
                        <span class="text-base font-bold font-mono text-gray-900">
                          {{ formatMontant(montantTotal.valeur) }}
                        </span>
                      </td>
                      <td></td>
                    </tr>
                  </tfoot>
                </table>
              </div>
            </div>

          </div>

          <!-- PIED -->
          <div class="flex items-center justify-between px-6 py-4 border-t border-gray-100 bg-gray-50 flex-shrink-0">
            <p class="text-xs text-gray-400">
              {{ form.lignes.length }} produit{{ form.lignes.length > 1 ? 's' : '' }} ·
              {{ form.lignes.reduce((s, l) => s + (Number(l.quantiteCommandee) || 0), 0) }} unité{{ form.lignes.reduce((s, l) => s + (Number(l.quantiteCommandee) || 0), 0) > 1 ? 's' : '' }}
            </p>
            <div class="flex gap-3">
              <button type="button" @click="fermer" class="btn-secondary">Annuler</button>
              <button
                type="button"
                @click="soumettre"
                :disabled="!peutSoumettre || isLoading"
                class="btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <svg v-if="isLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                {{ isEdition ? 'Enregistrer' : 'Créer la commande' }}
              </button>
            </div>
          </div>

        </div>
      </div>
    </Transition>
  </Teleport>
</template>
