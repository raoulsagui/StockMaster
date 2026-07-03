<script setup>
// ============================================================
// MODULE 2 : FORMULAIRE ENTREPÔT — Création et Modification
//
// Gère les deux modes via la prop "id" :
//   - id absent → mode création  → POST /api/entrepots
//   - id présent → mode édition  → GET puis PUT /api/entrepots/{id}
//
// Même structure que UtilisateurFormView pour la cohérence du projet.
// ============================================================

import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppLayout from '@/layout/AppLayout.vue'
import entrepotService from '@/services/entrepotService'
import utilisateurService from '@/services/utilisateurService'

const router = useRouter()

// Prop "id" injectée par vue-router (props: true dans le router)
const props = defineProps({
  id: { type: [String, Number], default: null },
})

// true si on modifie, false si on crée
const isEditing = computed(() => props.id !== null)

// -------------------------------------------------------
// ÉTAT DU FORMULAIRE
// -------------------------------------------------------
const form = ref({
  nom:              '',
  adresse:          '',
  capaciteTotale:   '',
  responsableId:    null,
})

// Liste des utilisateurs pour le select "Responsable"
const utilisateurs  = ref([])
const erreurs       = ref({})
const erreurApi     = ref('')
const isLoading     = ref(false)

// -------------------------------------------------------
// CHARGEMENT INITIAL
// On charge la liste des utilisateurs ET, en mode édition,
// les données de l'entrepôt en parallèle pour aller plus vite.
// -------------------------------------------------------
onMounted(async () => {
  try {
    // Charge tous les utilisateurs pour peupler le select responsable
    utilisateurs.value = await utilisateurService.findAll()
  } catch {
    // Non bloquant : le responsable est optionnel
  }

  if (isEditing.value) {
    try {
      const data = await entrepotService.findById(props.id)
      form.value = {
        nom:            data.nom,
        adresse:        data.adresse,
        capaciteTotale: data.capaciteTotale,
        responsableId:  data.responsable?.id ?? null,
      }
    } catch {
      erreurApi.value = 'Impossible de charger cet entrepôt.'
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
  else if (form.value.nom.trim().length < 2)
    erreurs.value.nom = 'Le nom doit contenir au moins 2 caractères.'

  if (!form.value.adresse.trim())
    erreurs.value.adresse = "L'adresse est obligatoire."

  const capacite = parseFloat(form.value.capaciteTotale)
  if (!form.value.capaciteTotale || isNaN(capacite) || capacite < 1)
    erreurs.value.capaciteTotale = 'La capacité totale doit être d\'au moins 1 m³.'

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
      nom:            form.value.nom.trim(),
      adresse:        form.value.adresse.trim(),
      capaciteTotale: parseFloat(form.value.capaciteTotale),
      responsableId:  form.value.responsableId || null,
    }

    if (isEditing.value) {
      await entrepotService.modifier(props.id, payload)
    } else {
      await entrepotService.creer(payload)
    }

    router.push({ name: 'entrepots' })

  } catch (e) {
    const msg = e.response?.data
    erreurApi.value = typeof msg === 'string'
      ? msg
      : (isEditing.value ? "Erreur lors de la modification." : "Erreur lors de la création.")
  } finally {
    isLoading.value = false
  }
}

const annuler = () => router.push({ name: 'entrepots' })
</script>

<template>
  <AppLayout>
    <div class="max-w-2xl mx-auto space-y-6">

      <!-- En-tête avec bouton retour -->
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
            {{ isEditing ? "Modifier l'entrepôt" : 'Nouvel entrepôt' }}
          </h1>
          <p class="text-sm text-gray-500 mt-0.5">
            {{ isEditing ? 'Modifiez les informations ci-dessous.' : 'Remplissez le formulaire pour créer un entrepôt.' }}
          </p>
        </div>
      </div>

      <!-- Erreur API globale -->
      <div v-if="erreurApi" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">
        {{ erreurApi }}
      </div>

      <!-- Formulaire -->
      <form @submit.prevent="soumettre" class="card space-y-5">

        <!-- Nom -->
        <div>
          <label for="nom" class="form-label">Nom de l'entrepôt <span class="text-red-500">*</span></label>
          <input
            id="nom" v-model="form.nom" type="text"
            placeholder="Ex : Entrepôt Principal Paris"
            :class="['form-input', erreurs.nom ? 'border-red-400 focus:ring-red-400' : '']"
          />
          <p v-if="erreurs.nom" class="form-error">{{ erreurs.nom }}</p>
        </div>

        <!-- Adresse -->
        <div>
          <label for="adresse" class="form-label">Adresse <span class="text-red-500">*</span></label>
          <input
            id="adresse" v-model="form.adresse" type="text"
            placeholder="Ex : 15 Rue de la Logistique, 75001 Paris"
            :class="['form-input', erreurs.adresse ? 'border-red-400 focus:ring-red-400' : '']"
          />
          <p v-if="erreurs.adresse" class="form-error">{{ erreurs.adresse }}</p>
        </div>

        <!-- Capacités -->
        <div class="border-t border-gray-100 pt-5">
          <p class="text-sm font-medium text-gray-700 mb-4">Capacité de stockage</p>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">

            <div>
              <label for="capaciteTotale" class="form-label">
                Capacité totale (m³) <span class="text-red-500">*</span>
              </label>
              <input
                id="capaciteTotale" v-model="form.capaciteTotale" type="number" min="1" step="0.1"
                placeholder="Ex : 5000"
                :class="['form-input', erreurs.capaciteTotale ? 'border-red-400 focus:ring-red-400' : '']"
              />
              <p v-if="erreurs.capaciteTotale" class="form-error">{{ erreurs.capaciteTotale }}</p>
            </div>

          </div>
        </div>

        <!-- Responsable -->
        <div class="border-t border-gray-100 pt-5">
          <label for="responsable" class="form-label">Responsable</label>
          <select id="responsable" v-model="form.responsableId" class="form-input">
            <option :value="null">Aucun responsable assigné</option>
            <option
              v-for="u in utilisateurs"
              :key="u.id"
              :value="u.id"
            >
              {{ u.prenom }} {{ u.nom }} — {{ u.role }}
            </option>
          </select>
          <p class="text-xs text-gray-400 mt-1">Optionnel — peut être défini ultérieurement.</p>
        </div>

        <!-- Boutons -->
        <div class="flex items-center justify-end gap-3 pt-4 border-t border-gray-100">
          <button type="button" @click="annuler" class="btn-secondary">Annuler</button>
          <button type="submit" :disabled="isLoading" class="btn-primary">
            <svg v-if="isLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
            {{ isEditing ? 'Enregistrer les modifications' : "Créer l'entrepôt" }}
          </button>
        </div>

      </form>
    </div>
  </AppLayout>
</template>
