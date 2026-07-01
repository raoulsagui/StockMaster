<script setup>
defineProps({
  donnees: { type: Object, required: true },
})
function fmtNb(n) { return n != null ? Number(n).toLocaleString('fr-FR') : '—' }
</script>

<template>
  <div class="space-y-4">
    <!-- KPIs -->
    <div class="grid grid-cols-2 sm:grid-cols-3 gap-3">
      <div class="card p-4 text-center">
        <p class="text-2xl font-bold text-gray-900">{{ fmtNb(donnees.totalCommandes) }}</p>
        <p class="text-xs text-gray-500 mt-1">Commandes total</p>
      </div>
      <div class="card p-4 text-center">
        <p class="text-2xl font-bold text-blue-600">{{ fmtNb(donnees.montantTotalHT) }} €</p>
        <p class="text-xs text-gray-500 mt-1">Montant total HT</p>
      </div>
      <div class="card p-4 text-center">
        <p class="text-2xl font-bold text-green-600">{{ donnees.fournisseurs?.length || 0 }}</p>
        <p class="text-xs text-gray-500 mt-1">Fournisseurs actifs</p>
      </div>
    </div>

    <!-- Tableau -->
    <div class="card p-0 overflow-hidden">
      <div class="px-5 py-3 border-b border-gray-100">
        <h3 class="text-sm font-semibold text-gray-700">Synthèse par fournisseur</h3>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-50 border-b border-gray-100">
            <tr>
              <th class="table-header">Fournisseur</th>
              <th class="table-header">Email</th>
              <th class="table-header text-right">Commandes</th>
              <th class="table-header text-right">Livrées</th>
              <th class="table-header text-right">Annulées</th>
              <th class="table-header text-right">Montant HT</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
            <tr v-for="f in donnees.fournisseurs" :key="f.fournisseurNom" class="hover:bg-gray-50">
              <td class="table-cell font-medium text-gray-900">{{ f.fournisseurNom }}</td>
              <td class="table-cell text-sm text-gray-500">{{ f.fournisseurEmail }}</td>
              <td class="table-cell text-right font-semibold">{{ fmtNb(f.nbCommandes) }}</td>
              <td class="table-cell text-right text-green-600 font-semibold">{{ fmtNb(f.nbLivrees) }}</td>
              <td class="table-cell text-right" :class="f.nbAnnulees > 0 ? 'text-red-600 font-semibold' : 'text-gray-400'">
                {{ fmtNb(f.nbAnnulees) }}
              </td>
              <td class="table-cell text-right font-semibold text-blue-700">{{ fmtNb(f.montantTotal) }} €</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
