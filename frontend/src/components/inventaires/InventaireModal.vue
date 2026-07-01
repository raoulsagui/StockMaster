<script setup>
/**
 * Modal de création d'un inventaire.
 *
 * Permet de choisir :
 *   - le type (COMPLET ou PARTIEL)
 *   - l'entrepôt cible
 *   - la date prévue
 *   - pour un PARTIEL : les produits à inventorier
 */
import { ref, watch, computed, onMounted } from 'vue'
import entrepotService  from '@/services/entrepotService'
import produitService   from '@/services/produitService'
import stockService     from '@/services/stockService'
import inventaireService from '@/services/inventaireService'

const props = defineProps({
  visible: { type: Boolean, default: false },
})

const emit = defineEmits(['fermer', 'sauvegarde'])

// -------------------------------------------------------
// ÉTAT DU FORMULAIRE
// -------------------------------------------------------
const form = ref({
  type:         'COMPLET',
  entrepotId:   null,
  note:         '',
  produitsIds:  [],
})

const erreur       = ref('')
const isLoading    = ref(false)
const entrepots    = ref([])
const produits     = ref([])   // produits filtrés selon l'entrepôt sélectionné
const loadingProds = ref(false)

// -------------------------------------------------------
// CHARGEMENT
// -------------------------------------------------------
onMounted(async () => {
  entrepots.value = await entrepotService.findActifs()
})

// Quand l'entrepôt change, charger les produits présents dans ses stocks
watch(() => form.value.entrepotId, async (entrepotId) => {
  form.value.produitsIds = []
  produits.value = []
  if (!entrepotId) return

  loadingProds.value = true
  try {
    const stocks = await stockService.findByEntrepot(entrepotId)
    produits.value = stocks.map(s => ({
      id:        s.produitId,
      nom:       s.produitNom,
      reference: s.produitReference,
      categorie: s.produitCategorieNom,
      quantite:  s.quantiteDisponible,
    }))
  } catch {
    produits.value = []
  } finally {
    loadingProds.value = false
  }
})

// -------------------------------------------------------
// SOUMISSION
// -------------------------------------------------------
const peutSoumettre = computed(() => {
  if (!form.value.entrepotId) return false
  if (form.value.type === 'PARTIEL' && form.value.produitsIds.length === 0) return false
  return true
})

async function soumettre() {
  erreur.value  = ''
  isLoading.value = true
  try {
    const payload = {
      type:        form.value.type,
      entrepotId:  Number(form.value.entrepotId),
      note:        form.value.note || null,
      produitsIds: form.value.type === 'PARTIEL' ? form.value.produitsIds : undefined,
    }
    const created = await inventaireService.creer(payload)
    emit('sauvegarde', created)
    fermer()
  } catch (e) {
    erreur.value = e.response?.data || 'Une erreur est survenue.'
  } finally {
    isLoading.value = false
  }
}

function fermer() {
  Object.assign(form.value, {
    type: 'COMPLET', entrepotId: null, note: '', produitsIds: [],
  })
  erreur.value = ''
  emit('fermer')
}

function toggleProduit(id) {
  const idx = form.value.produitsIds.indexOf(id)
  if (idx === -1) form.value.produitsIds.push(id)
  else            form.value.produitsIds.splice(idx, 1)
}

function toggleTousProduits() {
  if (form.value.produitsIds.length === produits.value.length) {
    form.value.produitsIds = []
  } else {
    form.value.produitsIds = produits.value.map(p => p.id)
  }
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
      <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40">

        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] flex flex-col overflow-hidden">

          <!-- EN-TÊTE -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <div>
              <h2 class="text-lg font-bold text-gray-900">Nouvel inventaire</h2>
              <p class="text-xs text-gray-500 mt-0.5">Créez un inventaire complet ou partiel</p>
            </div>
            <button @click="fermer" class="btn-icon text-gray-400 hover:text-gray-600">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <!-- CORPS -->
          <div class="flex-1 overflow-y-auto px-6 py-5 space-y-5">

            <!-- Erreur -->
            <div v-if="erreur" class="bg-red-50 border border-red-200 rounded-lg p-3 text-sm text-red-700">
              {{ erreur }}
            </div>

            <!-- Type d'inventaire -->
            <div>
              <label class="form-label">Type d'inventaire <span class="text-red-500">*</span></label>
              <div class="grid grid-cols-2 gap-3 mt-1">
                <button
                  type="button"
                  @click="form.type = 'COMPLET'"
                  :class="[
                    'p-3 rounded-xl border-2 text-left transition-colors',
                    form.type === 'COMPLET'
                      ? 'border-blue-500 bg-blue-50'
                      : 'border-gray-300 hover:border-gray-400 bg-white',
                  ]"
                >
                  <p class="font-semibold text-sm text-gray-900">Complet</p>
                  <p class="text-xs text-gray-500 mt-0.5">Tous les produits de l'entrepôt</p>
                </button>
                <button
                  type="button"
                  @click="form.type = 'PARTIEL'"
                  :class="[
                    'p-3 rounded-xl border-2 text-left transition-colors',
                    form.type === 'PARTIEL'
                      ? 'border-blue-500 bg-blue-50'
                      : 'border-gray-300 hover:border-gray-400 bg-white',
                  ]"
                >
                  <p class="font-semibold text-sm text-gray-900">Partiel</p>
                  <p class="text-xs text-gray-500 mt-0.5">Sélection de produits</p>
                </button>
              </div>
            </div>

            <!-- Entrepôt -->
            <div>
              <label class="form-label">Entrepôt <span class="text-red-500">*</span></label>
              <select v-model="form.entrepotId" class="form-input">
                <option :value="null" disabled>Choisir un entrepôt…</option>
                <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
              </select>
            </div>

            <!-- Note -->
            <div>
              <label class="form-label">Note <span class="text-gray-400 font-normal">(optionnel)</span></label>
              <textarea
                v-model="form.note"
                rows="2"
                placeholder="Commentaire sur cet inventaire…"
                class="form-input resize-none"
              ></textarea>
            </div>

            <!-- Sélection de produits (inventaire PARTIEL) -->
            <div v-if="form.type === 'PARTIEL' && form.entrepotId">
              <div class="flex items-center justify-between mb-1">
                <label class="form-label mb-0">Produits à inventorier <span class="text-red-500">*</span></label>
                <button
                  type="button"
                  @click="toggleTousProduits"
                  class="text-xs text-blue-600 hover:text-blue-800 hover:underline"
                >
                  {{ form.produitsIds.length === produits.length ? 'Tout désélectionner' : 'Tout sélectionner' }}
                </button>
              </div>

              <div v-if="loadingProds" class="text-sm text-gray-400 py-3 text-center border border-gray-300 rounded-lg">
                Chargement des produits…
              </div>
              <div v-else-if="produits.length === 0" class="text-sm text-gray-400 py-3 text-center border border-gray-300 rounded-lg">
                Aucun produit en stock dans cet entrepôt.
              </div>
              <div v-else class="border border-gray-300 rounded-lg overflow-hidden max-h-48 overflow-y-auto">
                <label
                  v-for="p in produits"
                  :key="p.id"
                  :class="[
                    'flex items-center gap-3 px-4 py-2.5 cursor-pointer border-b border-gray-100 last:border-0 transition-colors',
                    form.produitsIds.includes(p.id) ? 'bg-blue-50' : 'hover:bg-gray-50',
                  ]"
                >
                  <input
                    type="checkbox"
                    :checked="form.produitsIds.includes(p.id)"
                    @change="toggleProduit(p.id)"
                    class="w-4 h-4 text-blue-600 rounded border-gray-300 focus:ring-blue-500"
                  />
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-medium text-gray-900 truncate">{{ p.nom }}</p>
                    <p class="text-xs text-gray-400">{{ p.reference }} · {{ p.quantite }} en stock</p>
                  </div>
                </label>
              </div>
              <p class="text-xs text-gray-500 mt-1">
                {{ form.produitsIds.length }} produit(s) sélectionné(s)
              </p>
            </div>

            <p v-else-if="form.type === 'PARTIEL' && !form.entrepotId"
              class="text-sm text-gray-400 italic border border-dashed border-gray-300 rounded-lg px-4 py-3">
              Sélectionnez d'abord un entrepôt pour voir les produits disponibles.
            </p>

          </div>

          <!-- PIED -->
          <div class="flex items-center justify-end gap-3 px-6 py-4 border-t border-gray-100 bg-gray-50">
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
              Créer l'inventaire
            </button>
          </div>

        </div>
      </div>
    </Transition>
  </Teleport>
</template>
