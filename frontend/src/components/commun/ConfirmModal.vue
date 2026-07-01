<script setup>
/**
 * Modal de confirmation réutilisable.
 * Remplace les confirm() et alert() natifs du navigateur.
 *
 * Props :
 *   - visible   : affiche ou cache le modal
 *   - titre     : titre du modal
 *   - message   : texte de la question
 *   - type      : 'danger' | 'warning' | 'info' (couleur du bouton confirmer)
 *   - erreur    : message d'erreur à afficher (après une action échouée)
 *   - loading   : désactive les boutons pendant l'action
 *
 * Événements :
 *   - confirmer : l'utilisateur a cliqué "Confirmer"
 *   - annuler   : l'utilisateur a cliqué "Annuler" ou fermé le modal
 */
defineProps({
  visible:  { type: Boolean, default: false },
  titre:    { type: String,  default: 'Confirmation' },
  message:  { type: String,  default: '' },
  type:     { type: String,  default: 'danger' }, // danger | warning | info
  erreur:   { type: String,  default: '' },
  loading:  { type: Boolean, default: false },
})

defineEmits(['confirmer', 'annuler'])

const btnClasses = {
  danger:  'bg-red-600 hover:bg-red-700 text-white',
  warning: 'bg-yellow-500 hover:bg-yellow-600 text-white',
  info:    'bg-blue-600 hover:bg-blue-700 text-white',
}

const iconConfig = {
  danger:  { bg: 'bg-red-100',    text: 'text-red-600'    },
  warning: { bg: 'bg-yellow-100', text: 'text-yellow-600' },
  info:    { bg: 'bg-blue-100',   text: 'text-blue-600'   },
}
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition-opacity duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="visible"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40"
        @click.self="$emit('annuler')"
      >
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden">

          <!-- CORPS -->
          <div class="p-6">
            <div class="flex items-start gap-4">

              <!-- Icône -->
              <div :class="[iconConfig[type]?.bg, 'p-3 rounded-full flex-shrink-0']">
                <!-- Danger / Warning : point d'exclamation -->
                <svg v-if="type !== 'info'"
                  :class="[iconConfig[type]?.text, 'w-6 h-6']"
                  fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M12 9v2m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
                </svg>
                <!-- Info : point d'interrogation -->
                <svg v-else
                  :class="[iconConfig[type]?.text, 'w-6 h-6']"
                  fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
              </div>

              <!-- Texte -->
              <div class="flex-1 min-w-0">
                <h3 class="text-base font-semibold text-gray-900">{{ titre }}</h3>
                <p class="text-sm text-gray-500 mt-1 leading-relaxed">{{ message }}</p>

                <!-- Erreur -->
                <div v-if="erreur"
                  class="mt-3 bg-red-50 border border-red-200 rounded-lg px-3 py-2 text-sm text-red-700">
                  {{ erreur }}
                </div>
              </div>
            </div>
          </div>

          <!-- PIED -->
          <div class="flex items-center justify-end gap-3 px-6 py-4 bg-gray-50 border-t border-gray-100">
            <button
              @click="$emit('annuler')"
              :disabled="loading"
              class="px-4 py-2 rounded-lg text-sm font-medium text-gray-600 bg-white border border-gray-300 hover:bg-gray-50 disabled:opacity-50 transition-colors"
            >
              Annuler
            </button>
            <button
              @click="$emit('confirmer')"
              :disabled="loading"
              :class="[btnClasses[type], 'px-4 py-2 rounded-lg text-sm font-medium disabled:opacity-50 transition-colors flex items-center gap-2']"
            >
              <svg v-if="loading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
              Confirmer
            </button>
          </div>

        </div>
      </div>
    </Transition>
  </Teleport>
</template>
