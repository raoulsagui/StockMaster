<script setup>
// ============================================================
// COMPOSANT : AppToast
//
// Affiche les notifications toast en bas à droite de l'écran.
// Placé une seule fois dans App.vue.
//
// Types : success | error | warning | info
// ============================================================

import { useToast } from '@/composables/useToast'

const { toasts, supprimer } = useToast()

const config = {
  success: {
    icon:  'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z',
    bar:   'bg-green-500',
    bg:    'bg-white border-l-4 border-green-500',
    text:  'text-green-700',
    sub:   'text-green-600',
  },
  error: {
    icon:  'M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z',
    bar:   'bg-red-500',
    bg:    'bg-white border-l-4 border-red-500',
    text:  'text-red-700',
    sub:   'text-red-600',
  },
  warning: {
    icon:  'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z',
    bar:   'bg-yellow-500',
    bg:    'bg-white border-l-4 border-yellow-500',
    text:  'text-yellow-700',
    sub:   'text-yellow-600',
  },
  info: {
    icon:  'M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z',
    bar:   'bg-blue-500',
    bg:    'bg-white border-l-4 border-blue-500',
    text:  'text-blue-700',
    sub:   'text-blue-600',
  },
}
</script>

<template>
  <Teleport to="body">
    <div class="fixed bottom-5 right-5 z-[9999] flex flex-col gap-2 max-w-sm w-full pointer-events-none">
      <TransitionGroup
        tag="div"
        class="flex flex-col gap-2"
        enter-active-class="transition duration-300 ease-out"
        enter-from-class="translate-x-full opacity-0"
        enter-to-class="translate-x-0 opacity-100"
        leave-active-class="transition duration-200 ease-in"
        leave-from-class="translate-x-0 opacity-100"
        leave-to-class="translate-x-full opacity-0"
      >
        <div
          v-for="toast in toasts"
          :key="toast.id"
          :class="['pointer-events-auto rounded-xl shadow-lg overflow-hidden', config[toast.type]?.bg ?? 'bg-white border-l-4 border-gray-400']"
        >
          <div class="flex items-start gap-3 px-4 py-3">
            <!-- Icône -->
            <svg
              :class="['w-5 h-5 flex-shrink-0 mt-0.5', config[toast.type]?.text ?? 'text-gray-600']"
              fill="none" stroke="currentColor" viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                :d="config[toast.type]?.icon" />
            </svg>

            <!-- Message -->
            <p :class="['flex-1 text-sm font-medium leading-snug', config[toast.type]?.text ?? 'text-gray-700']">
              {{ toast.message }}
            </p>

            <!-- Bouton fermer -->
            <button
              @click="supprimer(toast.id)"
              class="p-0.5 text-gray-400 hover:text-gray-600 rounded transition-colors flex-shrink-0"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>
