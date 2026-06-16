<script setup>
// ============================================================
// COMPOSANT MODAL GÉNÉRIQUE
//
// Utilisé par tous les modules pour les formulaires de création,
// modification, suppression. Évite de dupliquer le code d'overlay,
// de transition et de structure du panneau dans chaque vue.
//
// USAGE :
//   <AppModal :show="showModal" title="Nouveau produit" @close="showModal = false">
//     <form>…</form>
//   </AppModal>
//
// PROPS :
//   show    : boolean — affiche ou cache la modal
//   title   : string  — titre dans le header
//   maxWidth: string  — largeur max du panneau (défaut: 'sm:max-w-lg')
//
// EVENTS :
//   close   : émis quand l'utilisateur clique sur X ou l'overlay
//
// SLOTS :
//   default : contenu du corps de la modal (formulaire, texte…)
//   footer  : (optionnel) remplace le pied par défaut
// ============================================================

defineProps({
  show:     { type: Boolean, required: true },
  title:    { type: String,  required: true },
  maxWidth: { type: String,  default: 'sm:max-w-lg' },
})

const emit = defineEmits(['close'])
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <!--
        Conteneur plein écran.
        Sur mobile  : modal collée en bas (items-end)
        Sur desktop : modal centrée (sm:items-center)
      -->
      <div
        v-if="show"
        class="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4"
      >
        <!-- Overlay sombre — clic ferme la modal -->
        <div class="absolute inset-0 bg-black/50" @click="emit('close')"></div>

        <!-- Panneau de la modal -->
        <div :class="[
          'relative bg-white w-full sm:rounded-2xl rounded-t-2xl',
          'shadow-2xl max-h-[90vh] overflow-y-auto',
          maxWidth
        ]">

          <!-- Header : titre + bouton fermer (sticky pour rester visible au scroll) -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 sticky top-0 bg-white rounded-t-2xl z-10">
            <h2 class="text-lg font-semibold text-gray-900">{{ title }}</h2>
            <button
              type="button"
              @click="emit('close')"
              class="p-2 rounded-lg text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <!-- Corps : injecté via le slot par défaut -->
          <div class="px-6 py-5">
            <slot />
          </div>

        </div>
      </div>
    </Transition>
  </Teleport>
</template>
