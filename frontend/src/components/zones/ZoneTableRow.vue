<script setup>
// ============================================================
// COMPOSANT : ZoneTableRow
//
// Représente une ligne dans le tableau des zones.
// Contient les données de la zone + ses boutons d'action.
//
// Le composant est "pur affichage" : il ne fait aucun appel API.
// Les actions sont remontées au parent via des événements.
//
// Événements émis :
//   @modifier → l'utilisateur veut modifier cette zone
//   @toggle   → l'utilisateur veut activer/désactiver cette zone
// ============================================================

defineProps({
  /** L'objet zone complet retourné par l'API */
  zone: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['modifier', 'toggle'])

// -------------------------------------------------------
// CONSTANTES D'AFFICHAGE
// Définies directement dans le composant pour éviter de les
// passer en props (ce sont des données statiques de l'UI).
// -------------------------------------------------------

/** Labels lisibles par type de zone */
const TYPES_LABELS = {
  RECEPTION:  'Réception',
  STOCKAGE:   'Stockage',
  EXPEDITION: 'Expédition',
}

/** Chemin SVG de l'icône selon le type */
const TYPE_ICONS = {
  RECEPTION:  'M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z',
  STOCKAGE:   'M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4',
  EXPEDITION: 'M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8l1.526 10.668A2 2 0 008.518 20h6.964a2 2 0 001.992-1.332L19 8',
}

/** Classes de couleur du badge par type */
const TYPE_COLORS = {
  RECEPTION:  'bg-blue-100 text-blue-700',
  STOCKAGE:   'bg-green-100 text-green-700',
  EXPEDITION: 'bg-orange-100 text-orange-700',
}

// -------------------------------------------------------
// HELPERS D'AFFICHAGE
// -------------------------------------------------------

function getCouleurBarre(taux) {
  if (taux >= 85) return 'bg-red-500'
  if (taux >= 60) return 'bg-orange-400'
  return 'bg-green-500'
}

function getCouleurTaux(taux) {
  if (taux >= 85) return 'text-red-600'
  if (taux >= 60) return 'text-orange-500'
  return 'text-green-600'
}

function formatCapacite(val) {
  return val != null ? val.toLocaleString('fr-FR') + ' m²' : '—'
}
</script>

<template>
  <tr class="hover:bg-gray-50 transition-colors">

    <!-- Nom + description optionnelle -->
    <td class="table-cell">
      <div>
        <p class="font-medium text-gray-900">{{ zone.nom }}</p>
        <p v-if="zone.description" class="text-xs text-gray-400 truncate max-w-xs">
          {{ zone.description }}
        </p>
      </div>
    </td>

    <!-- Entrepôt parent -->
    <td class="table-cell">
      <span class="text-sm text-gray-600">{{ zone.entrepot?.nom || '—' }}</span>
    </td>

    <!-- Badge type (icône + label + couleur selon le type) -->
    <td class="table-cell">
      <span :class="['inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium', TYPE_COLORS[zone.type]]">
        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="TYPE_ICONS[zone.type]"/>
        </svg>
        {{ TYPES_LABELS[zone.type] }}
      </span>
    </td>

    <!-- Barre d'occupation + taux numérique -->
    <td class="table-cell w-44">
      <div class="flex items-center gap-2">
        <div class="flex-1 h-1.5 bg-gray-100 rounded-full overflow-hidden">
          <div
            :class="['h-1.5 rounded-full transition-all duration-500', getCouleurBarre(zone.tauxOccupation)]"
            :style="{ width: Math.min(zone.tauxOccupation, 100) + '%' }"
          ></div>
        </div>
        <span :class="['text-xs font-semibold w-10 text-right', getCouleurTaux(zone.tauxOccupation)]">
          {{ zone.tauxOccupation }} %
        </span>
      </div>
    </td>

    <!-- Capacité utilisée / totale -->
    <td class="table-cell text-sm text-gray-500">
      {{ formatCapacite(zone.capaciteUtilisee) }}
      <span class="text-gray-300 mx-0.5">/</span>
      {{ formatCapacite(zone.capaciteTotale) }}
    </td>

    <!-- Badge statut -->
    <td class="table-cell">
      <span :class="zone.actif ? 'badge-actif' : 'badge-inactif'">
        {{ zone.actif ? 'Actif' : 'Inactif' }}
      </span>
    </td>

    <!-- Boutons d'action -->
    <td class="table-cell">
      <div class="flex items-center justify-end gap-2">

        <!-- Modifier -->
        <button
          @click="emit('modifier', zone.id)"
          class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
          title="Modifier cette zone"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
          </svg>
        </button>

        <!-- Activer / Désactiver -->
        <button
          @click="emit('toggle', zone)"
          :class="[
            'p-1.5 rounded-lg transition-colors',
            zone.actif
              ? 'text-gray-400 hover:text-red-600 hover:bg-red-50'
              : 'text-gray-400 hover:text-green-600 hover:bg-green-50'
          ]"
          :title="zone.actif ? 'Désactiver cette zone' : 'Réactiver cette zone'"
        >
          <svg v-if="zone.actif" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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
