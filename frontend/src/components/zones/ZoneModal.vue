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
  capaciteTotale:   '',
  capaciteUtilisee: '',
})

const entrepots = ref([])
const erreurs   = ref({})
const erreurApi = ref('')
const isLoading = ref(false)

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
        nom:              data.nom,
        type:             data.type,
        description:      data.description || '',
        entrepotId:       data.entrepot?.id ?? null,
        capaciteTotale:   data.capaciteTotale ?? '',
        capaciteUtilisee: data.capaciteUtilisee ?? '',
      }
    } catch {
      erreurApi.value = 'Impossible de charger cette zone.'
    }
  } else {
    form.value = {
      nom:              '',
      type:             '',
      description:      '',
      entrepotId:       props.entrepotId ? Number(props.entrepotId) : null,
      capaciteTotale:   '',
      capaciteUtilisee: '',
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

  const total   = parseFloat(form.value.capaciteTotale)
  const utilise = parseFloat(form.value.capaciteUtilisee)

  if (form.value.capaciteTotale !== '' && (isNaN(total) || total <= 0))
    erreurs.value.capaciteTotale = 'La capacité totale doit être supérieure à 0.'

  if (form.value.capaciteUtilisee !== '' && (isNaN(utilise) || utilise < 0))
    erreurs.value.capaciteUtilisee = 'La capacité utilisée ne peut pas être négative.'

  if (!isNaN(total) && !isNaN(utilise) && utilise > total)
    erreurs.value.capaciteUtilisee = 'La capacité utilisée ne peut pas dépasser la capacité totale.'

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
      nom:              form.value.nom.trim(),
      type:             form.value.type,
      description:      form.value.description?.trim() || null,
      entrepotId:       form.value.entrepotId,
      capaciteTotale:   form.value.capaciteTotale !== '' ? parseFloat(form.value.capaciteTotale) : null,
      capaciteUtilisee: form.value.capaciteUtilisee !== '' ? parseFloat(form.value.capaciteUtilisee) : null,
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

            <!-- Capacités -->
            <div class="border-t border-gray-100 pt-4">
              <p class="text-sm font-medium text-gray-700 mb-3">
                Capacité de stockage
                <span class="text-xs font-normal text-gray-400 ml-1">(optionnel)</span>
              </p>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="form-label">Capacité totale (m³)</label>
                  <input
                    v-model="form.capaciteTotale"
                    type="number" min="0.1" step="0.1"
                    placeholder="Ex : 500"
                    :class="['form-input', erreurs.capaciteTotale ? 'border-red-400 focus:ring-red-400' : '']"
                  />
                  <p v-if="erreurs.capaciteTotale" class="form-error">{{ erreurs.capaciteTotale }}</p>
                </div>
                <div>
                  <label class="form-label">Capacité utilisée (m³)</label>
                  <input
                    v-model="form.capaciteUtilisee"
                    type="number" min="0" step="0.1"
                    placeholder="Ex : 120"
                    :class="['form-input', erreurs.capaciteUtilisee ? 'border-red-400 focus:ring-red-400' : '']"
                  />
                  <p v-if="erreurs.capaciteUtilisee" class="form-error">{{ erreurs.capaciteUtilisee }}</p>
                </div>
              </div>
              <!-- Barre de prévisualisation du taux si les deux champs sont renseignés -->
              <div
                v-if="form.capaciteTotale && form.capaciteUtilisee && parseFloat(form.capaciteTotale) > 0"
                class="mt-3"
              >
                <div class="flex items-center justify-between mb-1">
                  <span class="text-xs text-gray-500">Taux d'occupation</span>
                  <span class="text-xs font-semibold text-gray-700">
                    {{ Math.min(Math.round((parseFloat(form.capaciteUtilisee) / parseFloat(form.capaciteTotale)) * 100), 100) }} %
                  </span>
                </div>
                <div class="h-2 bg-gray-100 rounded-full overflow-hidden">
                  <div
                    :class="[
                      'h-full rounded-full transition-all duration-300',
                      (parseFloat(form.capaciteUtilisee) / parseFloat(form.capaciteTotale)) >= 0.85 ? 'bg-red-500'
                      : (parseFloat(form.capaciteUtilisee) / parseFloat(form.capaciteTotale)) >= 0.60 ? 'bg-orange-400'
                      : 'bg-green-500',
                    ]"
                    :style="{ width: Math.min((parseFloat(form.capaciteUtilisee) / parseFloat(form.capaciteTotale)) * 100, 100) + '%' }"
                  ></div>
                </div>
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
