<script setup>
// ============================================================
// COMPOSANT : FournisseurTableRow
// Une ligne du tableau des fournisseurs.
//
// Événements émis :
//   @modifier → l'utilisateur veut modifier ce fournisseur
//   @toggle   → l'utilisateur veut activer/désactiver ce fournisseur
// ============================================================

defineProps({
  fournisseur: { type: Object, required: true },
})

const emit = defineEmits(['modifier', 'toggle'])
</script>

<template>
  <tr class="hover:bg-gray-50 transition-colors">

    <!-- Nom -->
    <td class="table-cell">
      <p class="font-medium text-gray-900">{{ fournisseur.nom }}</p>
    </td>

    <!-- Adresse -->
    <td class="table-cell">
      <div class="flex items-center gap-1 text-sm text-gray-500">
        <svg class="w-3.5 h-3.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
        </svg>
        <span class="truncate max-w-xs">{{ fournisseur.adresse }}</span>
      </div>
    </td>

    <!-- Téléphone -->
    <td class="table-cell">
      <div class="flex items-center gap-1 text-sm text-gray-500">
        <svg class="w-3.5 h-3.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/>
        </svg>
        {{ fournisseur.telephone }}
      </div>
    </td>

    <!-- Email -->
    <td class="table-cell">
      <div class="flex items-center gap-1 text-sm text-gray-500">
        <svg class="w-3.5 h-3.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
        </svg>
        {{ fournisseur.email }}
      </div>
    </td>

    <!-- Contact principal -->
    <td class="table-cell">
      <div class="flex items-center gap-1.5">
        <div class="w-6 h-6 rounded-full bg-purple-100 flex items-center justify-center flex-shrink-0">
          <svg class="w-3.5 h-3.5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
          </svg>
        </div>
        <span class="text-sm text-gray-600">{{ fournisseur.contactPrincipal }}</span>
      </div>
    </td>

    <!-- Statut -->
    <td class="table-cell">
      <span :class="fournisseur.actif ? 'badge-actif' : 'badge-inactif'">
        {{ fournisseur.actif ? 'Actif' : 'Inactif' }}
      </span>
    </td>

    <!-- Actions -->
    <td class="table-cell">
      <div class="flex items-center justify-end gap-2">

        <!-- Modifier -->
        <button
          @click="emit('modifier', fournisseur.id)"
          class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
          title="Modifier ce fournisseur"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
          </svg>
        </button>

        <!-- Activer / Désactiver -->
        <button
          @click="emit('toggle', fournisseur)"
          :class="[
            'p-1.5 rounded-lg transition-colors',
            fournisseur.actif
              ? 'text-gray-400 hover:text-red-600 hover:bg-red-50'
              : 'text-gray-400 hover:text-green-600 hover:bg-green-50'
          ]"
          :title="fournisseur.actif ? 'Désactiver ce fournisseur' : 'Réactiver ce fournisseur'"
        >
          <svg v-if="fournisseur.actif" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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
</template>
