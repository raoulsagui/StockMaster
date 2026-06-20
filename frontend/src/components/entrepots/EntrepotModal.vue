<script setup>
// ============================================================
// COMPOSANT : EntrepotModal
// Modal de création / modification d'un entrepôt.
//
// Props :
//   visible   (Boolean) → affiche ou cache le modal
//   entrepotId (Number) → si fourni, mode édition ; sinon création
//
// Événements :
//   @fermer  → le parent ferme le modal
//   @sauvegarde → création/modification réussie, le parent recharge
// ============================================================

import { ref, computed, watch } from 'vue'
import entrepotService    from '@/services/entrepotService'
import utilisateurService from '@/services/utilisateurService'
import dashboardService   from '@/services/dashboardService'
import { usePermissions } from '@/composables/usePermissions'

const { estAdmin } = usePermissions()

const props = defineProps({
  visible:    { type: Boolean, required: true },
  entrepotId: { type: [Number, String], default: null },
})

const emit = defineEmits(['fermer', 'sauvegarde'])

const isEditing = computed(() => props.entrepotId !== null)

// -------------------------------------------------------
// ÉTAT
// -------------------------------------------------------
const form = ref({
  nom:              '',
  adresse:          '',
  capaciteTotale:   '',
  capaciteUtilisee: 0,
  responsableId:    null,
})

const utilisateurs  = ref([])
const membresSelectionnes = ref([]) // IDs des membres cochés
const erreurs      = ref({})
const erreurApi    = ref('')
const isLoading    = ref(false)

// Filtre les utilisateurs éligibles comme membres (GESTIONNAIRE + MAGASINIER)
const utilisateursMembres = computed(() =>
  utilisateurs.value.filter(u => ['GESTIONNAIRE', 'MAGASINIER'].includes(u.role) && u.actif)
)

// Recharge les données chaque fois que le modal s'ouvre
watch(() => props.visible, async (val) => {
  if (!val) return
  erreurs.value  = {}
  erreurApi.value = ''

  try {
    utilisateurs.value = await utilisateurService.findAll()
  } catch { /* non bloquant */ }

  if (isEditing.value) {
    try {
      const data = await entrepotService.findById(props.entrepotId)
      form.value = {
        nom:              data.nom,
        adresse:          data.adresse,
        capaciteTotale:   data.capaciteTotale,
        capaciteUtilisee: data.capaciteUtilisee,
        responsableId:    data.responsable?.id ?? null,
      }
      membresSelectionnes.value = data.membresIds ?? []
    } catch {
      erreurApi.value = "Impossible de charger cet entrepôt."
    }
  } else {
    form.value = { nom: '', adresse: '', capaciteTotale: '', capaciteUtilisee: 0, responsableId: null }
    membresSelectionnes.value = []
  }
})

// -------------------------------------------------------
// VALIDATION
// -------------------------------------------------------
const valider = () => {
  erreurs.value = {}

  if (!form.value.nom.trim())
    erreurs.value.nom = 'Le nom est obligatoire.'
  else if (form.value.nom.trim().length < 2)
    erreurs.value.nom = 'Le nom doit contenir au moins 2 caractères.'

  if (!form.value.adresse.trim())
    erreurs.value.adresse = "L'adresse est obligatoire."

  const capacite = parseFloat(form.value.capaciteTotale)
  if (!form.value.capaciteTotale || isNaN(capacite) || capacite < 1)
    erreurs.value.capaciteTotale = "La capacité totale doit être d'au moins 1 m³."

  const utilise = parseFloat(form.value.capaciteUtilisee) || 0
  if (!isNaN(capacite) && utilise > capacite)
    erreurs.value.capaciteUtilisee = 'Ne peut pas dépasser la capacité totale.'

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
      adresse:          form.value.adresse.trim(),
      capaciteTotale:   parseFloat(form.value.capaciteTotale),
      capaciteUtilisee: parseFloat(form.value.capaciteUtilisee) || 0,
      responsableId:    form.value.responsableId || null,
    }

    if (isEditing.value) {
      await entrepotService.modifier(props.entrepotId, payload)
      // Mise à jour des membres séparément (ADMIN uniquement)
      if (estAdmin.value) {
        await dashboardService.mettreAJourMembres(props.entrepotId, membresSelectionnes.value)
      }
    } else {
      const created = await entrepotService.creer(payload)
      // Assigne les membres après création (ADMIN uniquement)
      if (estAdmin.value && membresSelectionnes.value.length > 0) {
        await dashboardService.mettreAJourMembres(created.id, membresSelectionnes.value)
      }
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
  <!-- Overlay — clique en dehors pour fermer -->
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
                {{ isEditing ? "Modifier l'entrepôt" : 'Nouvel entrepôt' }}
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

          <!-- Corps du formulaire -->
          <form @submit.prevent="soumettre" class="p-6 space-y-5">

            <!-- Erreur API -->
            <div v-if="erreurApi" class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg p-3">
              {{ erreurApi }}
            </div>

            <!-- Nom -->
            <div>
              <label class="form-label">Nom de l'entrepôt <span class="text-red-500">*</span></label>
              <input
                v-model="form.nom" type="text"
                placeholder="Ex : Entrepôt Principal Paris"
                :class="['form-input', erreurs.nom ? 'border-red-400 focus:ring-red-400' : '']"
              />
              <p v-if="erreurs.nom" class="form-error">{{ erreurs.nom }}</p>
            </div>

            <!-- Adresse -->
            <div>
              <label class="form-label">Adresse <span class="text-red-500">*</span></label>
              <input
                v-model="form.adresse" type="text"
                placeholder="Ex : 15 Rue de la Logistique, 75001 Paris"
                :class="['form-input', erreurs.adresse ? 'border-red-400 focus:ring-red-400' : '']"
              />
              <p v-if="erreurs.adresse" class="form-error">{{ erreurs.adresse }}</p>
            </div>

            <!-- Capacités -->
            <div class="border-t border-gray-100 pt-4">
              <p class="text-sm font-medium text-gray-700 mb-3">Capacité de stockage</p>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="form-label">Capacité totale (m³) <span class="text-red-500">*</span></label>
                  <input
                    v-model="form.capaciteTotale" type="number" min="1" step="0.1"
                    placeholder="Ex : 5000"
                    :class="['form-input', erreurs.capaciteTotale ? 'border-red-400 focus:ring-red-400' : '']"
                  />
                  <p v-if="erreurs.capaciteTotale" class="form-error">{{ erreurs.capaciteTotale }}</p>
                </div>
                <div>
                  <label class="form-label">Capacité utilisée (m³)</label>
                  <input
                    v-model="form.capaciteUtilisee" type="number" min="0" step="0.1"
                    placeholder="Ex : 1200"
                    :class="['form-input', erreurs.capaciteUtilisee ? 'border-red-400 focus:ring-red-400' : '']"
                  />
                  <p v-if="erreurs.capaciteUtilisee" class="form-error">{{ erreurs.capaciteUtilisee }}</p>
                </div>
              </div>
            </div>

            <!-- Responsable -->
            <div class="border-t border-gray-100 pt-4">
              <label class="form-label">Responsable</label>
              <select v-model="form.responsableId" class="form-input">
                <option :value="null">Aucun responsable assigné</option>
                <option v-for="u in utilisateurs" :key="u.id" :value="u.id">
                  {{ u.prenom }} {{ u.nom }} — {{ u.role }}
                </option>
              </select>
              <p class="text-xs text-gray-400 mt-1">Optionnel — peut être défini ultérieurement.</p>
            </div>

            <!-- Membres assignés (ADMIN uniquement) -->
            <div v-if="estAdmin" class="border-t border-gray-100 pt-4">
              <p class="form-label mb-2">Membres assignés</p>
              <p class="text-xs text-gray-400 mb-3">
                Ces utilisateurs verront cet entrepôt dans leur tableau de bord.
              </p>
              <div v-if="utilisateursMembres.length === 0" class="text-xs text-gray-400">
                Aucun gestionnaire ou magasinier actif disponible.
              </div>
              <div v-else class="space-y-2 max-h-40 overflow-y-auto pr-1">
                <label
                  v-for="u in utilisateursMembres"
                  :key="u.id"
                  class="flex items-center gap-3 p-2 rounded-lg hover:bg-gray-50 cursor-pointer"
                >
                  <input
                    type="checkbox"
                    :value="u.id"
                    v-model="membresSelectionnes"
                    class="w-4 h-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                  />
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-medium text-gray-700">{{ u.prenom }} {{ u.nom }}</p>
                    <p class="text-xs text-gray-400">{{ u.role === 'GESTIONNAIRE' ? 'Gestionnaire' : 'Magasinier' }}</p>
                  </div>
                </label>
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
                {{ isEditing ? 'Enregistrer' : "Créer l'entrepôt" }}
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
