<script setup>
// ============================================================
// MODULE 1 : FORMULAIRE UTILISATEUR — Branché sur l'API
//
// Gère la création ET la modification d'un utilisateur.
// On distingue les deux modes via la prop "id" :
//   - id absent → mode création  → POST /api/utilisateurs
//   - id présent → mode édition  → GET puis PUT /api/utilisateurs/{id}
// ============================================================

import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppLayout from '@/components/layout/AppLayout.vue'
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
  prenom:              '',
  nom:                 '',
  email:               '',
  role:                '',
  motDePasse:          '',
  confirmerMotDePasse: '',
  actif:               true,
})

// Erreurs de validation champ par champ
const erreurs   = ref({})
// Erreur générale retournée par l'API
const erreurApi = ref('')
const isLoading = ref(false)

// -------------------------------------------------------
// CHARGEMENT EN MODE MODIFICATION
// onMounted = exécuté une fois que le composant est monté
// -------------------------------------------------------
onMounted(async () => {
  if (isEditing.value) {
    try {
      // GET /api/utilisateurs/{id} → on pré-remplit le formulaire
      const data = await utilisateurService.findById(props.id)
      // On spread les données dans form (motDePasse reste vide)
      form.value = { ...form.value, ...data }
    } catch (e) {
      erreurApi.value = 'Impossible de charger cet utilisateur.'
    }
  }
})

// -------------------------------------------------------
// VALIDATION CÔTÉ CLIENT
// Vérifie les champs AVANT d'appeler l'API.
// Retourne true si tout est valide.
// -------------------------------------------------------
const valider = () => {
  erreurs.value = {}

  if (!form.value.prenom.trim())
    erreurs.value.prenom = 'Le prénom est obligatoire.'

  if (!form.value.nom.trim())
    erreurs.value.nom = 'Le nom est obligatoire.'

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!form.value.email.trim())
    erreurs.value.email = "L'email est obligatoire."
  else if (!emailRegex.test(form.value.email))
    erreurs.value.email = "L'email n'est pas valide."

  if (!form.value.role)
    erreurs.value.role = 'Veuillez sélectionner un rôle.'

  // Mot de passe obligatoire uniquement à la création
  if (!isEditing.value) {
    if (!form.value.motDePasse)
      erreurs.value.motDePasse = 'Le mot de passe est obligatoire.'
    else if (form.value.motDePasse.length < 8)
      erreurs.value.motDePasse = 'Minimum 8 caractères.'

    if (form.value.motDePasse !== form.value.confirmerMotDePasse)
      erreurs.value.confirmerMotDePasse = 'Les mots de passe ne correspondent pas.'
  }

  return Object.keys(erreurs.value).length === 0
}

// -------------------------------------------------------
// SOUMISSION — Appels réels à l'API Spring Boot
// -------------------------------------------------------
const soumettre = async () => {
  if (!valider()) return

  isLoading.value = true
  erreurApi.value = ''

  try {
    // Payload envoyé au backend (on exclut confirmerMotDePasse)
    const payload = {
      prenom:     form.value.prenom,
      nom:        form.value.nom,
      email:      form.value.email,
      role:       form.value.role,
      // En modification, si motDePasse vide → null → le service ne change pas le mdp
      motDePasse: form.value.motDePasse || null,
    }

    if (isEditing.value) {
      // PUT /api/utilisateurs/{id}
      await utilisateurService.modifier(props.id, payload)
    } else {
      // POST /api/utilisateurs
      await utilisateurService.creer(payload)
    }

    // Succès → retour à la liste
    router.push({ name: 'utilisateurs' })

  } catch (e) {
    // Le backend peut retourner un message d'erreur en texte
    const msg = e.response?.data
    erreurApi.value = typeof msg === 'string'
      ? msg
      : (isEditing.value ? 'Erreur lors de la modification.' : 'Erreur lors de la création.')
  } finally {
    isLoading.value = false
  }
}

const annuler = () => router.push({ name: 'utilisateurs' })
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
            {{ isEditing ? "Modifier l'utilisateur" : 'Nouvel utilisateur' }}
          </h1>
          <p class="text-sm text-gray-500 mt-0.5">
            {{ isEditing ? 'Modifiez les informations ci-dessous.' : 'Remplissez le formulaire pour créer un compte.' }}
          </p>
        </div>
      </div>

      <!-- Erreur API globale -->
      <div v-if="erreurApi" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">
        {{ erreurApi }}
      </div>

      <!-- Formulaire -->
      <form @submit.prevent="soumettre" class="card space-y-5">

        <!-- Prénom + Nom -->
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label for="prenom" class="form-label">Prénom <span class="text-red-500">*</span></label>
            <input
              id="prenom" v-model="form.prenom" type="text" placeholder="Alice"
              :class="['form-input', erreurs.prenom ? 'border-red-400 focus:ring-red-400' : '']"
            />
            <p v-if="erreurs.prenom" class="form-error">{{ erreurs.prenom }}</p>
          </div>
          <div>
            <label for="nom" class="form-label">Nom <span class="text-red-500">*</span></label>
            <input
              id="nom" v-model="form.nom" type="text" placeholder="Martin"
              :class="['form-input', erreurs.nom ? 'border-red-400 focus:ring-red-400' : '']"
            />
            <p v-if="erreurs.nom" class="form-error">{{ erreurs.nom }}</p>
          </div>
        </div>

        <!-- Email -->
        <div>
          <label for="email" class="form-label">Email <span class="text-red-500">*</span></label>
          <input
            id="email" v-model="form.email" type="email" placeholder="alice.martin@stockmaster.fr"
            :class="['form-input', erreurs.email ? 'border-red-400 focus:ring-red-400' : '']"
          />
          <p v-if="erreurs.email" class="form-error">{{ erreurs.email }}</p>
        </div>

        <!-- Rôle -->
        <div>
          <label for="role" class="form-label">Rôle <span class="text-red-500">*</span></label>
          <select
            id="role" v-model="form.role"
            :class="['form-input', erreurs.role ? 'border-red-400 focus:ring-red-400' : '']"
          >
            <option value="" disabled>Sélectionner un rôle…</option>
            <option value="ADMIN">Administrateur</option>
            <option value="GESTIONNAIRE">Gestionnaire d'entrepôt</option>
            <option value="MAGASINIER">Magasinier</option>
            <option value="AUDITEUR">Auditeur</option>
          </select>
          <p v-if="erreurs.role" class="form-error">{{ erreurs.role }}</p>
          <p class="text-xs text-gray-400 mt-1">
            Admin : accès complet | Gestionnaire : gestion entrepôts |
            Magasinier : mouvements | Auditeur : lecture seule
          </p>
        </div>

        <!-- Mot de passe — uniquement à la création -->
        <template v-if="!isEditing">
          <div class="border-t border-gray-100 pt-5">
            <p class="text-sm font-medium text-gray-700 mb-4">Mot de passe</p>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label for="motDePasse" class="form-label">Mot de passe <span class="text-red-500">*</span></label>
                <input
                  id="motDePasse" v-model="form.motDePasse" type="password" placeholder="Min. 8 caractères"
                  :class="['form-input', erreurs.motDePasse ? 'border-red-400 focus:ring-red-400' : '']"
                />
                <p v-if="erreurs.motDePasse" class="form-error">{{ erreurs.motDePasse }}</p>
              </div>
              <div>
                <label for="confirmerMotDePasse" class="form-label">Confirmer <span class="text-red-500">*</span></label>
                <input
                  id="confirmerMotDePasse" v-model="form.confirmerMotDePasse" type="password" placeholder="Répéter"
                  :class="['form-input', erreurs.confirmerMotDePasse ? 'border-red-400 focus:ring-red-400' : '']"
                />
                <p v-if="erreurs.confirmerMotDePasse" class="form-error">{{ erreurs.confirmerMotDePasse }}</p>
              </div>
            </div>
          </div>
        </template>

        <!-- Toggle statut — uniquement en modification -->
        <div v-if="isEditing" class="flex items-center gap-3 py-2">
          <label class="relative inline-flex items-center cursor-pointer">
            <input type="checkbox" v-model="form.actif" class="sr-only peer" />
            <div class="w-10 h-5 bg-gray-200 peer-focus:ring-2 peer-focus:ring-blue-300 rounded-full peer
                        peer-checked:bg-blue-600 after:content-[''] after:absolute after:top-0.5 after:left-0.5
                        after:bg-white after:rounded-full after:h-4 after:w-4 after:transition-transform
                        peer-checked:after:translate-x-5">
            </div>
          </label>
          <span class="text-sm text-gray-700">
            Compte <strong>{{ form.actif ? 'actif' : 'inactif' }}</strong>
          </span>
        </div>

        <!-- Boutons d'action -->
        <div class="flex items-center justify-end gap-3 pt-4 border-t border-gray-100">
          <button type="button" @click="annuler" class="btn-secondary">Annuler</button>
          <button type="submit" :disabled="isLoading" class="btn-primary">
            <svg v-if="isLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
            {{ isEditing ? 'Enregistrer les modifications' : "Créer l'utilisateur" }}
          </button>
        </div>

      </form>
    </div>
  </AppLayout>
</template>
