<script setup>
// ============================================================
// COMPOSANT : Tableau des utilisateurs + pagination
//
// Reçoit la liste déjà filtrée/paginée, émet les actions.
// Ne contient aucune logique métier ni appel API.
// ============================================================

defineProps({
  utilisateurs:  { type: Array,   required: true },
  isLoading:     { type: Boolean, default: false },
  pageCourante:  { type: Number,  required: true },
  totalPages:    { type: Number,  required: true },
  parPage:       { type: Number,  required: true },
  totalFiltres:  { type: Number,  required: true }, // nb total après filtres
})

const emit = defineEmits([
  'modifier',
  'toggle-statut',
  'reinitialiser-mdp',
  'page-precedente',
  'page-suivante',
  'changer-page',
])

// Helpers d'affichage
const rolesLabels = {
  ADMIN:        'Administrateur',
  GESTIONNAIRE: 'Gestionnaire',
  MAGASINIER:   'Magasinier',
  AUDITEUR:     'Auditeur',
}

const avatarColors = {
  ADMIN:        'bg-purple-600',
  GESTIONNAIRE: 'bg-blue-600',
  MAGASINIER:   'bg-green-600',
  AUDITEUR:     'bg-yellow-500',
}

const getInitiales = (u) => `${u.prenom[0]}${u.nom[0]}`.toUpperCase()

const formatDate = (dateStr) => {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('fr-FR', {
    day: '2-digit', month: 'short', year: 'numeric',
  })
}
</script>

<template>
  <div class="card p-0 overflow-hidden">

    <!-- Loader -->
    <div v-if="isLoading" class="p-12 text-center text-gray-400">
      <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-blue-500" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
      </svg>
      Chargement…
    </div>

    <!-- Tableau -->
    <div v-else class="overflow-x-auto">
      <table class="w-full">
        <thead class="bg-gray-50 border-b border-gray-100">
          <tr>
            <th class="table-header">Utilisateur</th>
            <th class="table-header hidden md:table-cell">Email</th>
            <th class="table-header">Rôle</th>
            <th class="table-header hidden sm:table-cell">Statut</th>
            <th class="table-header hidden lg:table-cell">Créé le</th>
            <th class="table-header text-right">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">

          <tr v-if="utilisateurs.length === 0">
            <td colspan="6" class="px-6 py-12 text-center text-gray-400 text-sm">
              Aucun utilisateur ne correspond aux critères.
            </td>
          </tr>

          <tr
            v-for="u in utilisateurs"
            :key="u.id"
            class="hover:bg-gray-50 transition-colors"
          >
            <!-- Avatar + Nom -->
            <td class="table-cell">
              <div class="flex items-center gap-3">
                <div :class="['w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0', avatarColors[u.role]]">
                  <span class="text-white text-xs font-bold">{{ getInitiales(u) }}</span>
                </div>
                <div>
                  <span class="font-medium text-gray-900 block">{{ u.prenom }} {{ u.nom }}</span>
                  <span class="text-xs text-gray-400 md:hidden">{{ u.email }}</span>
                </div>
              </div>
            </td>

            <td class="table-cell text-gray-500 hidden md:table-cell">{{ u.email }}</td>

            <td class="table-cell">
              <span :class="`badge-${u.role}`">{{ rolesLabels[u.role] }}</span>
            </td>

            <td class="table-cell hidden sm:table-cell">
              <span :class="u.actif ? 'badge-actif' : 'badge-inactif'">
                {{ u.actif ? 'Actif' : 'Inactif' }}
              </span>
            </td>

            <td class="table-cell text-gray-400 hidden lg:table-cell">
              {{ formatDate(u.dateCreation) }}
            </td>

            <!-- Actions -->
            <td class="table-cell">
              <div class="flex items-center justify-end gap-1">

                <button
                  @click="emit('modifier', u)"
                  class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                  title="Modifier"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                  </svg>
                </button>

                <button
                  @click="emit('reinitialiser-mdp', u)"
                  class="p-1.5 text-gray-400 hover:text-yellow-600 hover:bg-yellow-50 rounded-lg transition-colors"
                  title="Réinitialiser le mot de passe"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"/>
                  </svg>
                </button>

                <button
                  @click="emit('toggle-statut', u)"
                  :class="['p-1.5 rounded-lg transition-colors',
                    u.actif
                      ? 'text-gray-400 hover:text-red-600 hover:bg-red-50'
                      : 'text-gray-400 hover:text-green-600 hover:bg-green-50']"
                  :title="u.actif ? 'Désactiver' : 'Réactiver'"
                >
                  <svg v-if="u.actif" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"/>
                  </svg>
                  <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                  </svg>
                </button>

              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div
      v-if="!isLoading && totalPages > 1"
      class="flex flex-col sm:flex-row items-center justify-between gap-3 px-4 py-3 border-t border-gray-100"
    >
      <p class="text-sm text-gray-500">
        {{ (pageCourante - 1) * parPage + 1 }}–{{ Math.min(pageCourante * parPage, totalFiltres) }}
        sur {{ totalFiltres }} utilisateurs
      </p>
      <div class="flex items-center gap-1">
        <button
          @click="emit('page-precedente')"
          :disabled="pageCourante === 1"
          class="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
          </svg>
        </button>
        <button
          v-for="p in totalPages" :key="p"
          @click="emit('changer-page', p)"
          :class="['w-8 h-8 rounded-lg text-sm font-medium transition-colors',
            p === pageCourante ? 'bg-blue-600 text-white' : 'text-gray-600 hover:bg-gray-100']"
        >
          {{ p }}
        </button>
        <button
          @click="emit('page-suivante')"
          :disabled="pageCourante === totalPages"
          class="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
          </svg>
        </button>
      </div>
    </div>

  </div>
</template>
