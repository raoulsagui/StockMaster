<script setup>
defineProps({
  emplacements: { type: Array, default: () => [] },
  etagereCode:  { type: String, default: '' },
  isLoading:    { type: Boolean, default: false },
  canEdit:      { type: Boolean, default: false },
})
const emit = defineEmits(['modifier', 'changer-statut'])

const STATUT_EMP = {
  LIBRE:   { bg: 'bg-green-100 text-green-700',  dot: 'bg-green-500',  label: 'Libre' },
  OCCUPE:  { bg: 'bg-red-100 text-red-700',      dot: 'bg-red-500',    label: 'Occupé' },
  RESERVE: { bg: 'bg-orange-100 text-orange-700',dot: 'bg-orange-400', label: 'Réservé' },
  BLOQUE:  { bg: 'bg-gray-200 text-gray-600',    dot: 'bg-gray-400',   label: 'Bloqué' },
}
const TYPE_EMP = {
  PALETTE:'🔲 Palette', BAC:'📦 Bac', ETAGERE_OUVERTE:'🗄️ Étagère', ARMOIRE:'🚪 Armoire', SOL:'⬛ Sol',
}
</script>

<template>
  <div class="overflow-x-auto">
    <!-- Loading -->
    <div v-if="isLoading" class="p-8 text-center text-gray-400">
      <svg class="w-5 h-5 animate-spin mx-auto text-blue-500" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
      </svg>
    </div>

    <!-- Vide -->
    <div v-else-if="emplacements.length === 0" class="p-8 text-center text-gray-400 text-sm">
      Aucun emplacement trouvé.
    </div>

    <!-- Tableau -->
    <table v-else class="w-full">
      <thead class="bg-gray-50 border-b border-gray-100">
        <tr>
          <th class="table-header">Code</th>
          <th class="table-header">Adresse complète</th>
          <th class="table-header">Type</th>
          <th class="table-header">Capacité</th>
          <th class="table-header">Statut</th>
          <th class="table-header">Description</th>
          <th v-if="canEdit" class="table-header text-right">Actions</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-gray-50">
        <tr v-for="emp in emplacements" :key="emp.id" class="hover:bg-gray-50 transition-colors">
          <td class="table-cell">
            <span class="font-mono text-sm font-semibold text-gray-800">{{ emp.code }}</span>
          </td>
          <td class="table-cell">
            <span class="text-xs text-gray-500 font-mono">{{ emp.adresseComplete }}</span>
          </td>
          <td class="table-cell text-sm text-gray-600">
            {{ TYPE_EMP[emp.type] || emp.type }}
          </td>
          <td class="table-cell text-sm text-gray-500">
            {{ emp.capaciteMax != null ? emp.capaciteMax + ' u.' : '—' }}
          </td>
          <td class="table-cell">
            <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-full font-medium', STATUT_EMP[emp.statut]?.bg]">
              <span :class="['w-1.5 h-1.5 rounded-full', STATUT_EMP[emp.statut]?.dot]"></span>
              {{ STATUT_EMP[emp.statut]?.label }}
            </span>
          </td>
          <td class="table-cell text-xs text-gray-400 max-w-xs truncate">
            {{ emp.description || '—' }}
          </td>
          <td v-if="canEdit" class="table-cell">
            <div class="flex items-center justify-end gap-1">
              <button @click="emit('modifier', emp)"
                class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors" title="Modifier">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
                </svg>
              </button>
              <select :value="emp.statut" @change="emit('changer-statut', { emp, statut: $event.target.value })"
                class="text-xs border border-gray-200 rounded px-1.5 py-1 text-gray-600 bg-white focus:outline-none focus:border-blue-400">
                <option value="LIBRE">Libre</option>
                <option value="OCCUPE">Occupé</option>
                <option value="RESERVE">Réservé</option>
                <option value="BLOQUE">Bloqué</option>
              </select>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
