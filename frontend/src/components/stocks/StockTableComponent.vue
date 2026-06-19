<script setup>
// ============================================================
// COMPOSANT : Tableau des stocks + pagination
//
// Affiche la liste des stocks avec indicateurs visuels
// pour les alertes (stock faible, sur-stock).
// ============================================================

defineProps({
  stocks:               { type: Array,   required: true },
  isLoading:            { type: Boolean, default: false },
  pageCourante:         { type: Number,  required: true },
  totalPages:           { type: Number,  required: true },
  parPage:              { type: Number,  required: true },
  totalFiltres:         { type: Number,  required: true },
  peutConfigurerSeuils: { type: Boolean, default: false },
})

const emit = defineEmits([
  'voir-mouvements',   // ouvre le panneau historique
  'configurer-seuils', // ouvre la modal de configuration
  'page-precedente',
  'page-suivante',
  'changer-page',
])
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
            <th class="table-header">Produit</th>
            <th class="table-header hidden md:table-cell">Entrepôt</th>
            <th class="table-header">Disponible</th>
            <th class="table-header hidden sm:table-cell">Réservé</th>
            <th class="table-header hidden lg:table-cell">Transit</th>
            <th class="table-header hidden md:table-cell">Seuils</th>
            <th class="table-header hidden sm:table-cell">Statut</th>
            <th class="table-header text-right">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-50">

          <tr v-if="stocks.length === 0">
            <td colspan="8" class="px-6 py-12 text-center text-gray-400 text-sm">
              Aucun stock ne correspond aux critères.
            </td>
          </tr>

          <tr
            v-for="s in stocks"
            :key="s.id"
            :class="['transition-colors', s.enStockFaible ? 'bg-red-50 hover:bg-red-100' : 'hover:bg-gray-50']"
          >
            <!-- Produit -->
            <td class="table-cell">
              <div>
                <span class="font-medium text-gray-900 block">{{ s.produitNom }}</span>
                <span class="text-xs text-gray-400">{{ s.produitReference }}</span>
                <!-- Entrepôt visible sur mobile sous le nom -->
                <span class="text-xs text-blue-600 md:hidden block mt-0.5">{{ s.entrepotNom }}</span>
              </div>
            </td>

            <!-- Entrepôt -->
            <td class="table-cell hidden md:table-cell text-gray-600">
              {{ s.entrepotNom }}
            </td>

            <!-- Quantité disponible avec alerte visuelle -->
            <td class="table-cell">
              <div class="flex items-center gap-1.5">
                <span :class="[
                  'text-lg font-bold',
                  s.enStockFaible ? 'text-red-600' : 'text-gray-900'
                ]">
                  {{ s.quantiteDisponible }}
                </span>
                <!-- Icône alerte stock faible -->
                <svg v-if="s.enStockFaible" class="w-4 h-4 text-red-500 flex-shrink-0"
                  fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                </svg>
              </div>
            </td>

            <td class="table-cell hidden sm:table-cell text-gray-500">{{ s.quantiteReservee }}</td>
            <td class="table-cell hidden lg:table-cell text-gray-500">{{ s.quantiteEnTransit }}</td>

            <!-- Seuils configurés -->
            <td class="table-cell hidden md:table-cell">
              <div class="text-xs text-gray-500 space-y-0.5">
                <div v-if="s.stockMinimum != null">
                  Min : <span class="font-medium text-gray-700">{{ s.stockMinimum }}</span>
                </div>
                <div v-if="s.stockMaximum != null">
                  Max : <span class="font-medium text-gray-700">{{ s.stockMaximum }}</span>
                </div>
                <div v-if="s.stockMinimum == null && s.stockMaximum == null" class="text-gray-300">
                  —
                </div>
              </div>
            </td>

            <!-- Badge statut -->
            <td class="table-cell hidden sm:table-cell">
              <span v-if="s.enStockFaible"
                class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-700">
                Stock faible
              </span>
              <span v-else-if="s.enSurStock"
                class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-orange-100 text-orange-700">
                Sur-stock
              </span>
              <span v-else
                class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-700">
                Normal
              </span>
            </td>

            <!-- Actions -->
            <td class="table-cell">
              <div class="flex items-center justify-end gap-1">

                <!-- Voir l'historique des mouvements -->
                <button
                  @click="emit('voir-mouvements', s)"
                  class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                  title="Historique des mouvements"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
                  </svg>
                </button>

                <!-- Configurer les seuils min/max -->
                <button
                  v-if="peutConfigurerSeuils"
                  @click="emit('configurer-seuils', s)"
                  class="p-1.5 text-gray-400 hover:text-purple-600 hover:bg-purple-50 rounded-lg transition-colors"
                  title="Configurer les seuils"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4"/>
                  </svg>
                </button>

              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="!isLoading && totalPages > 1"
      class="flex flex-col sm:flex-row items-center justify-between gap-3 px-4 py-3 border-t border-gray-100">
      <p class="text-sm text-gray-500">
        {{ (pageCourante - 1) * parPage + 1 }}–{{ Math.min(pageCourante * parPage, totalFiltres) }}
        sur {{ totalFiltres }} stocks
      </p>
      <div class="flex items-center gap-1">
        <button @click="emit('page-precedente')" :disabled="pageCourante === 1"
          class="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
          </svg>
        </button>
        <button v-for="p in totalPages" :key="p" @click="emit('changer-page', p)"
          :class="['w-8 h-8 rounded-lg text-sm font-medium transition-colors',
            p === pageCourante ? 'bg-blue-600 text-white' : 'text-gray-600 hover:bg-gray-100']">
          {{ p }}
        </button>
        <button @click="emit('page-suivante')" :disabled="pageCourante === totalPages"
          class="p-1.5 rounded-lg text-gray-400 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
          </svg>
        </button>
      </div>
    </div>

  </div>
</template>
