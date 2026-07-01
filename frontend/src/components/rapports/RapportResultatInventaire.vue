<script setup>
defineProps({
  donnees: { type: Object, required: true },
})

function fmtNb(n) { return n != null ? Number(n).toLocaleString('fr-FR') : '—' }

const badgeStatut = (s) => {
  const map = {
    VALIDE: 'bg-green-100 text-green-700', EN_COURS: 'bg-blue-100 text-blue-700',
    BROUILLON: 'bg-gray-100 text-gray-500', ANNULE: 'bg-red-100 text-red-700',
  }
  return map[s] || 'bg-gray-100 text-gray-600'
}
</script>

<template>
  <div class="space-y-4">
    <!-- KPIs -->
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
      <div class="card p-4 text-center">
        <p class="text-2xl font-bold text-gray-900">{{ donnees.totalLignes }}</p>
        <p class="text-xs text-gray-500 mt-1">Lignes total</p>
      </div>
      <div class="card p-4 text-center">
        <p class="text-2xl font-bold text-orange-600">{{ donnees.lignesAvecEcart }}</p>
        <p class="text-xs text-gray-500 mt-1">Avec écart</p>
      </div>
      <div class="card p-4 text-center">
        <p class="text-2xl font-bold text-green-600">{{ donnees.lignesSurplus }}</p>
        <p class="text-xs text-gray-500 mt-1">Surplus</p>
      </div>
      <div class="card p-4 text-center">
        <p class="text-2xl font-bold text-red-600">{{ donnees.lignesManque }}</p>
        <p class="text-xs text-gray-500 mt-1">Manque</p>
      </div>
    </div>

    <!-- Tableau -->
    <div class="card p-0 overflow-hidden">
      <div class="px-5 py-3 border-b border-gray-100 flex items-center justify-between">
        <h3 class="text-sm font-semibold text-gray-700">Détail des lignes — {{ donnees.reference }}</h3>
        <span :class="['text-xs font-medium px-2 py-0.5 rounded-full', badgeStatut(donnees.statut)]">
          {{ donnees.statut }}
        </span>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-50 border-b border-gray-100">
            <tr>
              <th class="table-header">Référence</th>
              <th class="table-header">Produit</th>
              <th class="table-header">Catégorie</th>
              <th class="table-header text-right">Théorique</th>
              <th class="table-header text-right">Compté</th>
              <th class="table-header text-right">Écart</th>
              <th class="table-header">Sens</th>
              <th class="table-header">Note</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
            <tr v-for="l in donnees.lignes" :key="l.produitReference" class="hover:bg-gray-50">
              <td class="table-cell font-mono text-xs">{{ l.produitReference }}</td>
              <td class="table-cell font-medium text-gray-900">{{ l.produitNom }}</td>
              <td class="table-cell text-sm text-gray-500">{{ l.produitCategorie }}</td>
              <td class="table-cell text-right text-sm">{{ fmtNb(l.quantiteTheorique) }}</td>
              <td class="table-cell text-right text-sm">{{ fmtNb(l.quantiteComptee) }}</td>
              <td class="table-cell text-right text-sm font-semibold"
                :class="l.ecart > 0 ? 'text-green-600' : l.ecart < 0 ? 'text-red-600' : 'text-gray-400'">
                {{ l.ecart != null ? (l.ecart > 0 ? '+' + l.ecart : l.ecart) : '—' }}
              </td>
              <td class="table-cell">
                <span v-if="l.sensEcart" :class="['text-xs px-2 py-0.5 rounded-full font-medium',
                  l.sensEcart==='SURPLUS' ? 'bg-green-100 text-green-700' :
                  l.sensEcart==='MANQUE'  ? 'bg-red-100 text-red-700' : 'bg-gray-100 text-gray-500']">
                  {{ l.sensEcart }}
                </span>
              </td>
              <td class="table-cell text-xs text-gray-400">{{ l.note || '—' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
