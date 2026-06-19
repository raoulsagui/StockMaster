<script setup>
/**
 * Modal de réception d'une commande fournisseur.
 *
 * Affiche les lignes de la commande et permet de saisir
 * la quantité réellement reçue pour chaque produit.
 *
 * Fonctionnalités :
 *   - Pré-remplit quantiteRecue = quantiteCommandee (cas normal)
 *   - Affiche l'écart en temps réel (livraison partielle / surplus)
 *   - Bouton "Tout conforme" pour pré-valider en un clic
 *   - Appelle PATCH /api/commandes/{id}/receptionner
 */
import { ref, computed, watch } from 'vue'
import commandeService from '@/services/commandeService'

const props = defineProps({
  visible:  { type: Boolean, default: false },
  commande: { type: Object,  default: null  },
})
const emit = defineEmits(['fermer', 'receptionnee'])

const lignesRecues = ref([])
const note         = ref('')
const erreur       = ref('')
const isLoading    = ref(false)

// -------------------------------------------------------
// INITIALISATION des lignes à l'ouverture
// -------------------------------------------------------
watch(() => props.visible, (open) => {
  if (!open || !props.commande) return
  erreur.value = ''
  note.value   = ''
  lignesRecues.value = props.commande.lignes.map(l => ({
    ligneId:          l.id,
    produitNom:       l.produitNom,
    produitReference: l.produitReference,
    quantiteCommandee: l.quantiteCommandee,
    quantiteRecue:    l.quantiteCommandee, // pré-remplit avec la quantité commandée
  }))
})

// -------------------------------------------------------
// HELPERS
// -------------------------------------------------------
function toutConforme() {
  lignesRecues.value.forEach(l => { l.quantiteRecue = l.quantiteCommandee })
}

function ecartLigne(l) {
  return (l.quantiteRecue ?? 0) - l.quantiteCommandee
}

const peutSoumettre = computed(() =>
  lignesRecues.value.length > 0 &&
  lignesRecues.value.every(l => l.quantiteRecue != null && l.quantiteRecue >= 0)
)

// Résumé global des écarts
const resume = computed(() => {
  const surplus  = lignesRecues.value.filter(l => ecartLigne(l) > 0).length
  const manque   = lignesRecues.value.filter(l => ecartLigne(l) < 0).length
  const conforme = lignesRecues.value.filter(l => ecartLigne(l) === 0).length
  return { surplus, manque, conforme }
})

// -------------------------------------------------------
// SOUMISSION
// -------------------------------------------------------
async function soumettre() {
  erreur.value    = ''
  isLoading.value = true
  try {
    const payload = {
      note: note.value || null,
      lignes: lignesRecues.value.map(l => ({
        ligneId:      l.ligneId,
        quantiteRecue: Number(l.quantiteRecue),
      })),
    }
    const updated = await commandeService.receptionner(props.commande.id, payload)
    emit('receptionnee', updated)
    fermer()
  } catch (e) {
    erreur.value = e.response?.data || 'Erreur lors de la réception.'
  } finally {
    isLoading.value = false
  }
}

function fermer() {
  lignesRecues.value = []
  note.value         = ''
  erreur.value       = ''
  emit('fermer')
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
      <div v-if="visible && commande"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40"
        @click.self="fermer"
      >
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] flex flex-col overflow-hidden">

          <!-- EN-TÊTE -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 flex-shrink-0">
            <div>
              <h2 class="text-lg font-bold text-gray-900">Réceptionner la livraison</h2>
              <p class="text-xs text-gray-500 mt-0.5">
                Commande <span class="font-mono font-semibold">{{ commande.reference }}</span>
                · {{ commande.fournisseurNom }}
              </p>
            </div>
            <button @click="fermer" class="btn-icon text-gray-400 hover:text-gray-600">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <!-- CORPS -->
          <div class="flex-1 overflow-y-auto px-6 py-5 space-y-5">

            <!-- Info entrepôt -->
            <div class="flex items-center gap-2 px-4 py-3 bg-blue-50 border border-blue-100 rounded-xl text-sm text-blue-700">
              <svg class="w-4 h-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
              Les quantités reçues seront ajoutées en stock dans
              <span class="font-semibold ml-1">{{ commande.entrepotNom }}</span>.
            </div>

            <!-- Erreur -->
            <div v-if="erreur"
              class="bg-red-50 border border-red-200 rounded-lg px-4 py-3 text-sm text-red-700">
              {{ erreur }}
            </div>

            <!-- Bouton tout conforme -->
            <div class="flex items-center justify-between">
              <p class="text-sm font-medium text-gray-700">
                Saisir les quantités reçues
              </p>
              <button
                type="button"
                @click="toutConforme"
                class="flex items-center gap-1.5 px-3 py-1.5 bg-green-50 text-green-700 border border-green-200 rounded-lg text-xs font-medium hover:bg-green-100 transition-colors"
              >
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                </svg>
                Tout conforme
              </button>
            </div>

            <!-- Lignes de réception -->
            <div class="border border-gray-200 rounded-xl overflow-hidden">
              <table class="w-full text-sm">
                <thead class="bg-gray-50 border-b border-gray-100">
                  <tr>
                    <th class="text-left px-4 py-2.5 text-xs font-semibold text-gray-500 uppercase">Produit</th>
                    <th class="text-right px-3 py-2.5 text-xs font-semibold text-gray-500 uppercase w-28">Commandé</th>
                    <th class="text-right px-3 py-2.5 text-xs font-semibold text-gray-500 uppercase w-28">Reçu</th>
                    <th class="text-center px-3 py-2.5 text-xs font-semibold text-gray-500 uppercase w-24">Écart</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-50">
                  <tr
                    v-for="ligne in lignesRecues"
                    :key="ligne.ligneId"
                    :class="[
                      'transition-colors',
                      ecartLigne(ligne) !== 0 ? 'bg-orange-50/50' : 'hover:bg-gray-50',
                    ]"
                  >
                    <!-- Produit -->
                    <td class="px-4 py-3">
                      <p class="font-medium text-gray-900">{{ ligne.produitNom }}</p>
                      <p class="text-xs text-gray-400">{{ ligne.produitReference }}</p>
                    </td>

                    <!-- Quantité commandée -->
                    <td class="px-3 py-3 text-right">
                      <span class="font-mono text-gray-600">{{ ligne.quantiteCommandee }}</span>
                    </td>

                    <!-- Quantité reçue (saisie) -->
                    <td class="px-3 py-3">
                      <input
                        v-model.number="ligne.quantiteRecue"
                        type="number" min="0"
                        class="form-input text-right w-full font-mono"
                        :class="ecartLigne(ligne) !== 0 ? 'border-orange-300 focus:ring-orange-400' : ''"
                      />
                    </td>

                    <!-- Écart -->
                    <td class="px-3 py-3 text-center">
                      <template v-if="ligne.quantiteRecue != null">
                        <span v-if="ecartLigne(ligne) === 0"
                          class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-700">
                          ✓ Conforme
                        </span>
                        <span v-else-if="ecartLigne(ligne) > 0"
                          class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-bold bg-blue-100 text-blue-700">
                          ▲ +{{ ecartLigne(ligne) }}
                        </span>
                        <span v-else
                          class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-bold bg-red-100 text-red-700">
                          ▼ {{ ecartLigne(ligne) }}
                        </span>
                      </template>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Résumé des écarts -->
            <div v-if="lignesRecues.length > 0"
              class="flex items-center gap-4 px-4 py-3 bg-gray-50 rounded-xl text-xs font-medium">
              <span class="text-green-700">
                ✓ {{ resume.conforme }} conforme{{ resume.conforme > 1 ? 's' : '' }}
              </span>
              <span v-if="resume.surplus > 0" class="text-blue-700">
                ▲ {{ resume.surplus }} surplus
              </span>
              <span v-if="resume.manque > 0" class="text-red-700">
                ▼ {{ resume.manque }} manque{{ resume.manque > 1 ? 's' : '' }}
              </span>
            </div>

            <!-- Note de réception -->
            <div>
              <label class="form-label">Note de réception <span class="text-gray-400 font-normal">(optionnel)</span></label>
              <textarea
                v-model="note"
                rows="2"
                placeholder="Commentaire sur la livraison : colis endommagé, conditionnement différent…"
                class="form-input resize-none"
              ></textarea>
            </div>

          </div>

          <!-- PIED -->
          <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-gray-100 bg-gray-50 flex-shrink-0">
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
              <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
              </svg>
              Valider la réception
            </button>
          </div>

        </div>
      </div>
    </Transition>
  </Teleport>
</template>
