<script setup>
// ============================================================
// MODULE 3 : FORMULAIRE ZONE — Création et Modification
//
// Gère les deux modes via la prop "id" :
//   - id absent → mode création  → POST /api/zones
//   - id présent → mode édition  → GET puis PUT /api/zones/{id}
//
// À la création, on charge la liste des entrepôts actifs pour
// le select. On préremplit l'entrepôt si un query param est fourni
// (ex: arrivée depuis la page "Voir les zones" d'un entrepôt).
// ============================================================

import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AppLayout        from '@/components/layout/AppLayout.vue'
import ZoneTypeSelector from '@/components/zones/ZoneTypeSelector.vue'
import zoneService      from '@/services/zoneService'
import entrepotService  from '@/services/entrepotService'

const router = useRouter()
const route  = useRoute()

const props = defineProps({
  id: { type: [String, Number], default: null },
})

const isEditing = computed(() => props.id !== null)

// -------------------------------------------------------
// ÉTAT DU FORMULAIRE
// -------------------------------------------------------
const form = ref({
  nom:              '',
  type:             '',
  description:      '',
  capaciteTotale:   '',
  capaciteUtilisee: 0,
  // Pré-remplit depuis le query param si on arrive depuis la vue entrepôts
  entrepotId:       route.query.entrepotId ? Number(route.query.entrepotId) : null,
})

const entrepots  = ref([])
const erreurs    = ref({})
const erreurApi  = ref('')
const isLoading  = ref(false)

// -------------------------------------------------------
// CHARGEMENT INITIAL
// -------------------------------------------------------
onMounted(async () => {
  try {
    // Charge uniquement les entrepôts actifs dans le select
    entrepots.value = await entrepotService.findActifs()
  } catch {
    // Non bloquant
  }

  if (isEditing.value) {
    try {
      const data = await zoneService.findById(props.id)
      form.value = {
        nom:              data.nom,
        type:             data.type,
        description:      data.description || '',
        capaciteTotale:   data.capaciteTotale,
        capaciteUtilisee: data.capaciteUtilisee,
        entrepotId:       data.entrepot?.id ?? null,
      }
    } catch {
      erreurApi.value = 'Impossible de charger cette zone.'
    }
  }
})

// -------------------------------------------------------
// VALIDATION CÔTÉ CLIENT
// -------------------------------------------------------
const valider = () => {
  erreurs.value = {}

  if (!form.value.nom.trim())
    erreurs.value.nom = 'Le nom est obligatoire.'

  if (!form.value.type)
    erreurs.value.type = 'Le type de zone est obligatoire.'

  if (!form.value.entrepotId)
    erreurs.value.entrepotId = "L'entrepôt est obligatoire."

  const capacite = parseFloat(form.value.capaciteTotale)
  if (!form.value.capaciteTotale || isNaN(capacite) || capacite < 1)
    erreurs.value.capaciteTotale = 'La capacité totale doit être d\'au moins 1 m².'

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
      type:             form.value.type,
      description:      form.value.description?.trim() || null,
      capaciteTotale:   parseFloat(form.value.capaciteTotale),
      capaciteUtilisee: parseFloat(form.value.capaciteUtilisee) || 0,
      entrepotId:       form.value.entrepotId,
    }

    if (isEditing.value) {
      await zoneService.modifier(props.id, payload)
    } else {
      await zoneService.creer(payload)
    }

    router.push({ name: 'zones' })

  } catch (e) {
    const msg = e.response?.data
    erreurApi.value = typeof msg === 'string'
      ? msg
      : (isEditing.value ? 'Erreur lors de la modification.' : 'Erreur lors de la création.')
  } finally {
    isLoading.value = false
  }
}

const annuler = () => router.push({ name: 'zones' })
</script>

<template>
  <AppLayout>
    <div class="max-w-2xl mx-auto space-y-6">

      <!-- En-tête -->
      <div class="flex items-center gap-4">
        <button
          @click="annuler"
          class="p-2 rounded-lg text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
          </svg>
        </button>
        <div>
          <h1 class="text-2xl font-bold text-gray-900">
            {{ isEditing ? 'Modifier la zone' : 'Nouvelle zone' }}
          </h1>
          <p class="text-sm text-gray-500 mt-0.5">
            {{ isEditing ? 'Modifiez les informations ci-dessous.' : 'Créez une zone dans un entrepôt.' }}
          </p>
        </div>
      </div>

      <!-- Erreur API -->
      <div v-if="erreurApi" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">
        {{ erreurApi }}
      </div>

      <form @submit.prevent="soumettre" class="card space-y-5">

        <!-- Entrepôt parent -->
        <div>
          <label for="entrepot" class="form-label">Entrepôt <span class="text-red-500">*</span></label>
          <select
            id="entrepot" v-model="form.entrepotId"
            :class="['form-input', erreurs.entrepotId ? 'border-red-400 focus:ring-red-400' : '']"
          >
            <option :value="null" disabled>Sélectionner un entrepôt…</option>
            <option v-for="e in entrepots" :key="e.id" :value="e.id">
              {{ e.nom }}
            </option>
          </select>
          <p v-if="erreurs.entrepotId" class="form-error">{{ erreurs.entrepotId }}</p>
          <p v-if="entrepots.length === 0" class="text-xs text-orange-500 mt-1">
            Aucun entrepôt actif disponible. Créez d'abord un entrepôt actif.
          </p>
        </div>

        <!-- Nom -->
        <div>
          <label for="nom" class="form-label">Nom de la zone <span class="text-red-500">*</span></label>
          <input
            id="nom" v-model="form.nom" type="text"
            placeholder="Ex : Zone A1 - Palettes, Réception Nord"
            :class="['form-input', erreurs.nom ? 'border-red-400 focus:ring-red-400' : '']"
          />
          <p v-if="erreurs.nom" class="form-error">{{ erreurs.nom }}</p>
        </div>

        <!-- Type de zone — Sélecteur visuel en cartes (composant dédié) -->
        <div>
          <label class="form-label">Type de zone <span class="text-red-500">*</span></label>
          <ZoneTypeSelector v-model="form.type" class="mt-1" />
          <p v-if="erreurs.type" class="form-error mt-1">{{ erreurs.type }}</p>
        </div>

        <!-- Description -->
        <div>
          <label for="description" class="form-label">Description <span class="text-gray-400 text-xs">(optionnel)</span></label>
          <textarea
            id="description" v-model="form.description" rows="2"
            placeholder="Ex : Zone réfrigérée pour produits frais, température 2–8°C"
            class="form-input resize-none"
          ></textarea>
        </div>

        <!-- Capacités -->
        <div class="border-t border-gray-100 pt-5">
          <p class="text-sm font-medium text-gray-700 mb-4">Capacité de la zone</p>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">

            <div>
              <label for="capaciteTotale" class="form-label">
                Capacité totale (m²) <span class="text-red-500">*</span>
              </label>
              <input
                id="capaciteTotale" v-model="form.capaciteTotale" type="number" min="1" step="0.1"
                placeholder="Ex : 500"
                :class="['form-input', erreurs.capaciteTotale ? 'border-red-400 focus:ring-red-400' : '']"
              />
              <p v-if="erreurs.capaciteTotale" class="form-error">{{ erreurs.capaciteTotale }}</p>
            </div>

            <div>
              <label for="capaciteUtilisee" class="form-label">Capacité utilisée (m²)</label>
              <input
                id="capaciteUtilisee" v-model="form.capaciteUtilisee" type="number" min="0" step="0.1"
                placeholder="Ex : 120"
                :class="['form-input', erreurs.capaciteUtilisee ? 'border-red-400 focus:ring-red-400' : '']"
              />
              <p v-if="erreurs.capaciteUtilisee" class="form-error">{{ erreurs.capaciteUtilisee }}</p>
            </div>

          </div>
        </div>

        <!-- Boutons -->
        <div class="flex items-center justify-end gap-3 pt-4 border-t border-gray-100">
          <button type="button" @click="annuler" class="btn-secondary">Annuler</button>
          <button type="submit" :disabled="isLoading" class="btn-primary">
            <svg v-if="isLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
            {{ isEditing ? 'Enregistrer les modifications' : 'Créer la zone' }}
          </button>
        </div>

      </form>
    </div>
  </AppLayout>
</template>
