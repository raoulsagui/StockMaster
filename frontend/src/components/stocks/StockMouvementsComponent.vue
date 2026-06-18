<script setup>
// ============================================================
// COMPOSANT : Panneau historique des mouvements d'un stock
//
// S'affiche en side panel (ou modal sur mobile) et charge
// l'historique paginé depuis l'API.
// ============================================================

import { ref, watch } from 'vue'
import stockService from '@/services/stockService'

const props = defineProps({
  visible: { type: Boolean, required: true },
  stock:   { type: Object,  default: null },
})

const emit = defineEmits(['fermer'])

const mouvements   = ref([])
const isLoading    = ref(false)
const page         = ref(0)
const totalPages   = ref(0)
const totalElements = ref(0)

// Charge l'historique quand le panneau s'ouvre ou change de stock
watch(() => [props.visible, props.stock], async ([visible, stock]) => {
  if (!visible || !stock) return
  page.value = 0
  await chargerMouvements()
})

async function chargerMouvements() {
  if (!props.stock) return
  isLoading.value = true
  try {
    const data = await stockService.getMouvements(props.stock.id, page.value, 15)
    mouvements.value  = data.content
    totalPages.value  = data.totalPages
    totalElements.value = data.totalElements
  } catch {
    mouvements.value = []
  } finally {
    isLoading.value = false
  }
}

async function changerPage(p) {
  page.value = p
  await chargerMouvements()
}

// Labels et couleurs par type de mouvement
const typeConfig = {
  ENTREE:               { label: 'Entrée',       class: 'bg-green-100 text-green-700', sign: '+' },
  SORTIE:               { label: 'Sortie',        class: 'bg-red-100 text-red-700',    sign: '-' },
  TRANSFERT_SORTIE:     { label: 'Transfert →',   class: 'bg-blue-100 text-blue-700',  sign: '-' },
  TRANSFERT_ENTREE:     { label: '← Transfert',   class: 'bg-blue-100 text-blue-700',  sign: '+' },
  AJUSTEMENT:           { label: 'Ajustement',    class: 'bg-yellow-100 text-yellow-700', sign: '±' },
  RESERVATION:          { label: 'Réservation',   class: 'bg-purple-100 text-purple-700', sign: '-' },
  LIBERATION_RESERVATION: { label: 'Libération',  class: 'bg-purple-100 text-purple-700', sign: '+' },
}

const getTypeConfig = (type) => typeConfig[type] ?? { label: type, class: 'bg-gray-100 text-gray-700', sign: '' }

const formatDate = (dateStr) => {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleString('fr-FR', {
    day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit',
  })
}
</script>

<template>
  <Teleport to="body">
    <!-- Overlay -->
    <Transition enter-active-class="transition ease-out duration-200" enter-from-class="opacity-0" enter-to-class="opacity-100"
      leave-active-class="transition ease-in duration-150" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="visible" class="fixed inset-0 z-40 bg-black/30" @click="emit('fermer')" />
    </Transition>

    <!-- Panneau latéral -->
    <Transition enter-active-class="transition ease-out duration-300" enter-from-class="translate-x-full" enter-to-class="translate-x-0"
      leave-active-class="transition ease-in duration-200" leave-from-class="translate-x-0" leave-to-class="translate-x-full">
      <div v-if="visible"
        class="fixed right-0 top-0 h-full w-full sm:w-96 bg-white shadow-2xl z-50 flex flex-col">

        <!-- Header -->
        <div class="flex items-center justify-between px-5 py-4 border-b border-gray-100 flex-shrink-0">
          <div>
            <h2 class="text-base font-semibold text-gray-900">Historique des mouvements</h2>
            <p class="text-xs text-gray-500 mt-0.5" v-if="stock">
              {{ stock.produitNom }} — {{ stock.entrepotNom }}
            </p>
          </div>
          <button @click="emit('fermer')" class="p-2 rounded-lg text-gray-400 hover:bg-gray-100 transition-colors">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>

        <!-- Corps scrollable -->
        <div class="flex-1 overflow-y-auto">

          <!-- Loader -->
          <div v-if="isLoading" class="p-8 text-center text-gray-400">
            <svg class="w-5 h-5 animate-spin mx-auto mb-2 text-blue-500" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
            Chargement…
          </div>

          <!-- Vide -->
          <div v-else-if="mouvements.length === 0" class="p-8 text-center text-gray-400 text-sm">
            Aucun mouvement enregistré.
          </div>

          <!-- Liste des mouvements -->
          <div v-else class="divide-y divide-gray-50">
            <div
              v-for="m in mouvements" :key="m.id"
              class="px-5 py-3 hover:bg-gray-50 transition-colors"
            >
              <div class="flex items-start justify-between gap-3">
                <div class="flex-1 min-w-0">
                  <!-- Type + quantité -->
                  <div class="flex items-center gap-2">
                    <span :class="['inline-flex items-center px-2 py-0.5 rounded text-xs font-medium', getTypeConfig(m.type).class]">
                      {{ getTypeConfig(m.type).label }}
                    </span>
                    <span :class="[
                      'text-sm font-bold',
                      ['ENTREE','TRANSFERT_ENTREE','LIBERATION_RESERVATION'].includes(m.type) ? 'text-green-600' : 'text-red-600'
                    ]">
                      {{ getTypeConfig(m.type).sign }}{{ m.quantite }}
                    </span>
                  </div>
                  <!-- Référence + note -->
                  <p v-if="m.reference" class="text-xs text-gray-500 mt-1">Réf : {{ m.reference }}</p>
                  <p v-if="m.note" class="text-xs text-gray-400 mt-0.5 truncate">{{ m.note }}</p>
                  <!-- Utilisateur -->
                  <p class="text-xs text-gray-400 mt-1">{{ m.utilisateurNom ?? 'Système' }}</p>
                </div>
                <!-- Côté droit : stock après + date -->
                <div class="text-right flex-shrink-0">
                  <p class="text-sm font-semibold text-gray-700">→ {{ m.quantiteApres }}</p>
                  <p class="text-xs text-gray-400 mt-0.5">{{ formatDate(m.dateCreation) }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination en bas -->
        <div v-if="totalPages > 1"
          class="border-t border-gray-100 px-5 py-3 flex items-center justify-between flex-shrink-0">
          <p class="text-xs text-gray-500">{{ totalElements }} mouvements</p>
          <div class="flex items-center gap-1">
            <button @click="changerPage(page - 1)" :disabled="page === 0"
              class="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 disabled:opacity-30">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
              </svg>
            </button>
            <span class="text-xs text-gray-500 px-2">{{ page + 1 }} / {{ totalPages }}</span>
            <button @click="changerPage(page + 1)" :disabled="page >= totalPages - 1"
              class="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 disabled:opacity-30">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
              </svg>
            </button>
          </div>
        </div>

      </div>
    </Transition>
  </Teleport>
</template>
