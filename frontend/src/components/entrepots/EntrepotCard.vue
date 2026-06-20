<script setup>
// ============================================================
// COMPOSANT : EntrepotCard
//
// Carte d'affichage d'un entrepôt dans la grille.
// Affiche toutes les informations d'un entrepôt :
//   - Nom, adresse, statut
//   - Barre de progression du taux d'occupation
//   - Métriques (capacité totale / utilisée / nombre de zones)
//   - Responsable assigné
//   - Boutons d'action (zones, modifier, activer/désactiver)
//
// Émet des événements vers le parent pour les actions
// afin de garder la logique de navigation dans la vue parente.
//
// Événements émis :
//   @voir-zones  → l'utilisateur veut voir les zones de cet entrepôt
//   @modifier    → l'utilisateur veut modifier cet entrepôt
//   @toggle      → l'utilisateur veut activer/désactiver l'entrepôt
// ============================================================

defineProps({
  /** L'objet entrepôt complet retourné par l'API */
  entrepot: {
    type: Object,
    required: true,
  },
})

// On déclare les événements que ce composant peut émettre.
// Le parent écoute ces événements avec @voir-zones="...", etc.
const emit = defineEmits(['voir-zones', 'modifier', 'toggle'])

defineProps({
  entrepot: {
    type: Object,
    required: true,
  },
  peutModifier: {
    type: Boolean,
    default: false,
  },
})

// -------------------------------------------------------
// HELPERS D'AFFICHAGE — Logique de couleurs selon le taux
// -------------------------------------------------------

/**
 * Classe CSS Tailwind pour la barre de progression.
 * Vert < 60%, Orange 60–85%, Rouge > 85%
 */
function getCouleurBarre(taux) {
  if (taux >= 85) return 'bg-red-500'
  if (taux >= 60) return 'bg-orange-400'
  return 'bg-green-500'
}

/**
 * Classe CSS Tailwind pour le texte du taux.
 */
function getCouleurTaux(taux) {
  if (taux >= 85) return 'text-red-600'
  if (taux >= 60) return 'text-orange-500'
  return 'text-green-600'
}

/**
 * Formate une capacité en m³ avec séparateur de milliers.
 * Ex : 5000 → "5 000 m³"
 */
function formatCapacite(val) {
  return val != null ? val.toLocaleString('fr-FR') + ' m³' : '—'
}
</script>

<template>
  <div class="card p-5 hover:shadow-md transition-shadow">

    <!-- ===== EN-TÊTE : Nom + adresse + actions ===== -->
    <div class="flex items-start justify-between mb-4">
      <div class="flex-1 min-w-0">

        <!-- Nom + badge statut sur la même ligne -->
        <div class="flex items-center gap-2 flex-wrap">
          <h2 class="font-semibold text-gray-900 text-base truncate">
            {{ entrepot.nom }}
          </h2>
          <span :class="entrepot.actif ? 'badge-actif' : 'badge-inactif'">
            {{ entrepot.actif ? 'Actif' : 'Inactif' }}
          </span>
        </div>

        <!-- Adresse avec icône pin -->
        <div class="flex items-center gap-1 mt-1 text-sm text-gray-500">
          <svg class="w-3.5 h-3.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
          </svg>
          <span class="truncate">{{ entrepot.adresse }}</span>
        </div>

      </div>

      <!-- Groupe de boutons d'action -->
      <div class="flex items-center gap-1 ml-3 flex-shrink-0">

        <!-- Bouton : Voir les zones -->
        <button
          @click="emit('voir-zones', entrepot.id)"
          class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
          title="Voir les zones de cet entrepôt"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M4 5a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1V5zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1V5zM4 15a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1v-4zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z"/>
          </svg>
        </button>

        <!-- Bouton : Modifier -->
        <button
          v-if="peutModifier"
          @click="emit('modifier', entrepot.id)"
          class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
          title="Modifier cet entrepôt"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
          </svg>
        </button>

        <!-- Bouton : Activer / Désactiver -->
        <button
          v-if="peutModifier"
          @click="emit('toggle', entrepot)""
          :class="[
            'p-1.5 rounded-lg transition-colors',
            entrepot.actif
              ? 'text-gray-400 hover:text-red-600 hover:bg-red-50'
              : 'text-gray-400 hover:text-green-600 hover:bg-green-50'
          ]"
          :title="entrepot.actif ? 'Désactiver cet entrepôt' : 'Réactiver cet entrepôt'"
        >
          <!-- Icône interdiction si actif, icône check si inactif -->
          <svg v-if="entrepot.actif" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"/>
          </svg>
          <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
        </button>

      </div>
    </div>

    <!-- ===== BARRE D'OCCUPATION ===== -->
    <div class="mb-4">
      <div class="flex justify-between items-center mb-1.5">
        <span class="text-xs text-gray-500">Taux d'occupation</span>
        <span :class="['text-xs font-semibold', getCouleurTaux(entrepot.tauxOccupation)]">
          {{ entrepot.tauxOccupation }} %
        </span>
      </div>
      <!-- Barre pleine = bg-gray-100, portion colorée selon le taux -->
      <div class="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
        <div
          :class="['h-2 rounded-full transition-all duration-500', getCouleurBarre(entrepot.tauxOccupation)]"
          :style="{ width: Math.min(entrepot.tauxOccupation, 100) + '%' }"
        ></div>
      </div>
    </div>

    <!-- ===== MÉTRIQUES : 3 tuiles ===== -->
    <div class="grid grid-cols-3 gap-3">

      <div class="bg-gray-50 rounded-lg p-2.5 text-center">
        <p class="text-xs text-gray-400 mb-0.5">Total</p>
        <p class="text-sm font-semibold text-gray-700">{{ formatCapacite(entrepot.capaciteTotale) }}</p>
      </div>

      <div class="bg-gray-50 rounded-lg p-2.5 text-center">
        <p class="text-xs text-gray-400 mb-0.5">Utilisé</p>
        <p class="text-sm font-semibold text-gray-700">{{ formatCapacite(entrepot.capaciteUtilisee) }}</p>
      </div>

      <div class="bg-gray-50 rounded-lg p-2.5 text-center">
        <p class="text-xs text-gray-400 mb-0.5">Zones</p>
        <p class="text-sm font-semibold text-blue-600">{{ entrepot.nombreZones }}</p>
      </div>

    </div>

    <!-- ===== RESPONSABLE (affiché uniquement s'il y en a un) ===== -->
    <div
      v-if="entrepot.responsable"
      class="mt-3 pt-3 border-t border-gray-100 flex items-center gap-2"
    >
      <div class="w-6 h-6 rounded-full bg-blue-100 flex items-center justify-center flex-shrink-0">
        <svg class="w-3.5 h-3.5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
        </svg>
      </div>
      <span class="text-xs text-gray-500">{{ entrepot.responsable.nomComplet }}</span>
    </div>

  </div>
</template>
