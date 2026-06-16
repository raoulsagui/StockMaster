<script setup>
// ============================================================
// COMPOSANT : ZoneTypeSelector
//
// Sélecteur visuel du type de zone sous forme de 3 cartes radio.
// Utilisé dans ZoneFormView pour remplacer un simple <select>.
//
// L'interaction visuelle est plus claire : l'utilisateur voit
// la description de chaque type et la carte sélectionnée
// se met en évidence avec la couleur du type.
//
// Utilise defineModel pour se brancher sur v-model du parent :
//   <ZoneTypeSelector v-model="form.type" />
// ============================================================

// defineModel → bindé sur v-model dans le parent
const modelValue = defineModel({ type: String, default: '' })

// -------------------------------------------------------
// CONFIGURATION DES TYPES
// Tableau de définition : chaque type a son label,
// sa description et sa classe de couleur active.
// -------------------------------------------------------
const TYPES = [
  {
    value: 'RECEPTION',
    label: 'Réception',
    description: 'Déchargement et contrôle des marchandises entrantes',
    // Classes appliquées quand ce type est sélectionné
    activeClass: 'bg-blue-50 text-blue-700 border-blue-400',
    // Classes quand non sélectionné
    inactiveClass: 'bg-white border-gray-200 hover:border-gray-300',
  },
  {
    value: 'STOCKAGE',
    label: 'Stockage',
    description: 'Conservation des produits en attente de distribution',
    activeClass: 'bg-green-50 text-green-700 border-green-400',
    inactiveClass: 'bg-white border-gray-200 hover:border-gray-300',
  },
  {
    value: 'EXPEDITION',
    label: 'Expédition',
    description: 'Préparation et chargement des commandes sortantes',
    activeClass: 'bg-orange-50 text-orange-700 border-orange-400',
    inactiveClass: 'bg-white border-gray-200 hover:border-gray-300',
  },
]
</script>

<template>
  <!--
    Grille de 3 cartes — chacune est un label HTML cliquable
    qui active le radio input caché à l'intérieur.
    L'input sr-only (screen reader only) reste accessible
    sans être visible visuellement.
  -->
  <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
    <label
      v-for="type in TYPES"
      :key="type.value"
      :class="[
        'relative flex flex-col gap-1 p-3 rounded-lg border-2 cursor-pointer transition-all',
        modelValue === type.value ? type.activeClass : type.inactiveClass
      ]"
    >
      <!-- Input radio caché — la sélection est gérée visuellement par le label -->
      <input
        type="radio"
        :value="type.value"
        :checked="modelValue === type.value"
        @change="modelValue = type.value"
        class="sr-only"
      />

      <!-- Icône de validation quand sélectionné -->
      <div class="flex items-center justify-between">
        <span class="font-semibold text-sm">{{ type.label }}</span>
        <svg
          v-if="modelValue === type.value"
          class="w-4 h-4 flex-shrink-0"
          fill="currentColor"
          viewBox="0 0 20 20"
        >
          <path fill-rule="evenodd"
            d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
            clip-rule="evenodd"/>
        </svg>
      </div>

      <!-- Description du type -->
      <span class="text-xs opacity-70 leading-snug">{{ type.description }}</span>
    </label>
  </div>
</template>
