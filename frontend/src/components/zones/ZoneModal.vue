<script setup>
// ============================================================
// COMPOSANT : ZoneModal
// Modal de création / modification d'une zone.
//
// Props :
//   visible    (Boolean) → affiche ou cache le modal
//   zoneId     (Number)  → si fourni, mode édition ; sinon création
//   entrepotId (Number)  → pré-remplit l'entrepôt si fourni
//
// Événements :
//   @fermer     → le parent ferme le modal
//   @sauvegarde → création/modification réussie, le parent recharge
// ============================================================

import { ref, computed, watch } from 'vue'
import ZoneTypeSelector from '@/components/zones/ZoneTypeSelector.vue'
import zoneService      from '@/services/zoneService'
import entrepotService  from '@/services/entrepotService'

const props = defineProps({
  visible:    { type: Boolean, required: true },
  zoneId:     { type: [Number, String], default: null },
  entrepotId: { type: [Number, String], default: null },
})

const emit = defineEmits(['fermer', 'sauvegarde'])

const isEditing = computed(() => props.zoneId !== null)

// -------------------------------------------------------
// ÉTAT
// -------------------------------------------------------
const form = ref({
  nom:              '',
  type:             '',
  description:      '',
  entrepotId:       null,
  capacite:         '',
})

const entrepots = ref([])
const erreurs   = ref({})
const erreurApi = ref('')
const isLoading = ref(false)

const entrepotSelectionne = computed(() =>
  entrepots.value.find(e => e.id === form.value.entrepotId) ?? null
)

// Recharge les données chaque fois que le modal s'ouvre
watch(() => props.visible, async (val) => {
  if (!val) return
  erreurs.value   = {}
  erreurApi.value = ''

  try {
    entrepots.value = await entrepotService.findActifs()
  } catch { /* non bloquant */ }

  if (isEditing.value) {
    try {
      const data = await zoneService.findById(props.zoneId)
      form.value = {
        nom:         data.nom,
        type:        data.type,
        description: data.description || '',
        entrepotId:  data.entrepot?.id ?? null,
        capacite:    data.capacite ?? '',
      }
    } catch {
      erreurApi.value = 'Impossible de charger cette zone.'
    }
  } else {
    form.value = {
      nom:         '',
      type:        '',
      description: '',
      entrepotId:  props.entrepotId ? Number(props.entrepotId) : null,
      capacite:    '',
    }
  }
})

// -------------------------------------------------------
// VALIDATION
// -------------------------------------------------------
const valider = () => {
  erreurs.value = {}

  if (!form.value.nom.trim())
    erreurs.value.nom = 'Le nom est obligatoire.'

  if (!form.value.type)
    erreurs.value.type = 'Le type de zone est obligatoire.'

  if (!form.value.entrepotId)
    erreurs.value.entrepotId = "L'entrepôt est obligatoire."

  const capacite = parseFloat(form.value.capacite)

  if (!form.value.capacite || isNaN(capacite) || capacite <= 0)
    erreurs.value.capacite = 'La capacité est obligatoire et doit être supérieure à 0.'

  // Vérification capacité disponible de l'entrepôt
  if (!isNaN(capacite) && entrepotSelectionne.value) {
    const disponible = (entrepotSelectionne.value.capaciteTotale ?? 0) - (entrepotSelectionne.value.capaciteUtilisee ?? 0)
    if (capacite > disponible)
      erreurs.value.capacite = `Dépasse la capacité disponible de l'entrepôt (${disponible} m³ restants).`
  }

  return Object.keys(erreurs.value).length === 0
}

// -------------------------------------------------------
// SOUMISSION
// -------------------------------------------------------
const soumettre = async () => {
  if (!valider()) return
  isLoading.value = true
  erreurApi.value  = ''

  try {
    const payload = {
      nom:         form.value.nom.trim(),
      type:        form.value.type,
      description: form.value.description?.trim() || null,
      entrepotId:  form.value.entrepotId,
      capacite:    parseFloat(form.value.capacite),
    }

    if (isEditing.value) {
      await zoneService.modifier(props.zoneId, payload)
    } else {
      await zoneService.creer(payload)
    }

    emit('sauvegarde')
    emit('fermer')
  } catch (e) {
    const msg = e.response?.data
    erreurApi.value = typeof msg === 'string'
      ? msg
      : (isEditing.value ? 'Erreur lors de la modification.' : 'Erreur lors de la création.')
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div
        v-if="visible"
        class="fixed inset-0 z-50 flex items-center justify-center p-4"
        @click.self="emit('fermer')"
      >
        <!-- Fond sombre -->
        <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="emit('fermer')"></div>

        <!-- Boîte du modal -->
        <div class="relative bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">

          <!-- En-tête -->
          <div class="flex items-center justify-between p-6 border-b border-gray-100">
            <div>
              <h2 class="text-lg font-semibold text-gray-900">
                {{ isEditing ? 'Modifier la zone' : 'Nouvelle zone' }}
              </h2>
            </div>
            <button
              @click="emit('fermer')"
              class="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <!-- Formulaire -->
          <form @submit.prevent="soumettre" class="p-6 space-y-5">

            <!-- Erreur API -->
            <div v-if="erreurApi" class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg p-3">
              {{ erreurApi }}
            </div>

            <!-- Entrepôt parent -->
            <div>
              <label class="form-label">Entrepôt <span class="text-red-500">*</span></label>
              <select
                v-model="form.entrepotId"
                :class="['form-input', erreurs.entrepotId ? 'border-red-400 focus:ring-red-400' : '']"
              >
                <option :value="null" disabled>Sélectionner un entrepôt…</option>
                <option v-for="e in entrepots" :key="e.id" :value="e.id">{{ e.nom }}</option>
              </select>
              <p v-if="erreurs.entrepotId" class="form-error">{{ erreurs.entrepotId }}</p>
              <p v-if="entrepots.length === 0" class="text-xs text-orange-500 mt-1">
                Aucun entrepôt actif disponible.
              </p>
            </div>

            <!-- Nom -->
            <div>
              <label class="form-label">Nom de la zone <span class="text-red-500">*</span></label>
              <input
                v-model="form.nom" type="text"
                placeholder="Ex : Zone A1, Réception Nord"
                :class="['form-input', erreurs.nom ? 'border-red-400 focus:ring-red-400' : '']"
              />
              <p v-if="erreurs.nom" class="form-error">{{ erreurs.nom }}</p>
            </div>

            <!-- Type de zone -->
            <div>
              <label class="form-label">Type de zone <span class="text-red-500">*</span></label>
              <ZoneTypeSelector v-model="form.type" class="mt-1" />
              <p v-if="erreurs.type" class="form-error mt-1">{{ erreurs.type }}</p>
            </div>

            <!-- Description -->
            <div>
              <label class="form-label">Description <span class="text-gray-400 text-xs">(optionnel)</span></label>
              <textarea
                v-model="form.description" rows="2"
                placeholder="Ex : Zone réfrigérée pour produits frais, température 2–8°C"
                class="form-input resize-none"
              ></textarea>
            </div>

            <!-- Capacité -->
            <div class="border-t border-gray-100 pt-4">
              <p class="text-sm font-medium text-gray-700 mb-3">
                Capacité allouée <span class="text-red-500">*</span>
              </p>
              <div>
                <label class="form-label">Capacité (m³)</label>
                <input
                  v-model="form.capacite"
                  type="number" min="0.1" step="0.1"
                  placeholder="Ex : 500"
                  :class="['form-input', erreurs.capacite ? 'border-red-400 focus:ring-red-400' : '']"
                />
                <p v-if="erreurs.capacite" class="form-error">{{ erreurs.capacite }}</p>
                <p v-if="entrepotSelectionne" class="text-xs text-gray-500 mt-1">
                  Disponible :
                  <span class="font-medium text-gray-700">
                    {{ (entrepotSelectionne.capaciteTotale ?? 0) - (entrepotSelectionne.capaciteUtilisee ?? 0) }} m³
                  </span>
                  sur {{ entrepotSelectionne.capaciteTotale ?? '?' }} m³ au total
                </p>
              </div>
            </div>

            <!-- Boutons -->
            <div class="flex items-center justify-end gap-3 pt-4 border-t border-gray-100">
              <button type="button" @click="emit('fermer')" class="btn-secondary">Annuler</button>
              <button type="submit" :disabled="isLoading" class="btn-primary">
                <svg v-if="isLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                {{ isEditing ? 'Enregistrer' : 'Créer la zone' }}
              </button>
            </div>

          </form>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-enter-active, .modal-leave-active { transition: opacity 0.2s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
</style>
