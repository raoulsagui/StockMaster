<script setup>
defineProps({
  donnees: { type: Object, required: true },
})

function fmtDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleString('fr-FR', { day:'2-digit', month:'2-digit', year:'numeric' })
}
function fmtNb(n) { return n != null ? Number(n).toLocaleString('fr-FR') : '—' }

const badgeMvt = (type) => {
  const map = {
    ENTREE:'bg-green-100 text-green-700', SORTIE:'bg-red-100 text-red-700',
    TRANSFERT:'bg-blue-100 text-blue-700', AJUSTEMENT:'bg-orange-100 text-orange-700',
  }
  return map[type] || 'bg-gray-100 text-gray-600'
}
</script>

<template>
  <div class="space-y-4">
    <!-- KPIs par type -->
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
      <div class="card p-4 text-center">
        <p class="text-2xl font-bold text-gray-900">{{ fmtNb(donnees.totalMouvements) }}</p>
        <p class="text-xs text-gray-500 mt-1">Total mouvements</p>
      </div>
      <div v-for="(count, type) in donnees.countParType" :key="type" class="card p-4 text-center">
        <p :class="['text-2xl font-bold',
          badgeMvt(type).includes('green') ? 'text-green-600' :
          badgeMvt(type).includes('red')   ? 'text-red-600'   : 'text-blue-600']">
          {{ fmtNb(count) }}
        </p>
        <p class="text-xs text-gray-500 mt-1">{{ type }}</p>
      </div>
    </div>

    <!-- Tableau -->
    <div class="card p-0 overflow-hidden">
      <div class="px-5 py-3 border-b border-gray-100">
        <h3 class="text-sm font-semibold text-gray-700">Liste des mouvements</h3>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-50 border-b border-gray-100">
            <tr>
              <th class="table-header">Type</th>
              <th class="table-header">Produit</th>
              <th class="table-header">Entrepôt</th>
              <th class="table-header text-right">Qté</th>
              <th class="table-header text-right">Stock après</th>
              <th class="table-header">Opérateur</th>
              <th class="table-header">Date</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-50">
            <tr v-for="m in donnees.mouvements" :key="m.id" class="hover:bg-gray-50">
              <td class="table-cell">
                <span :class="['text-xs px-2 py-0.5 rounded-full font-medium', badgeMvt(m.type)]">{{ m.type }}</span>
              </td>
              <td class="table-cell">
                <p class="font-medium text-gray-900 text-sm">{{ m.produitNom }}</p>
                <p class="text-xs text-gray-400">{{ m.produitReference }}</p>
              </td>
              <td class="table-cell text-sm text-gray-600">{{ m.entrepotNom }}</td>
              <td class="table-cell text-right font-semibold text-sm">{{ fmtNb(m.quantite) }}</td>
              <td class="table-cell text-right text-sm text-gray-500">{{ fmtNb(m.quantiteApres) }}</td>
              <td class="table-cell text-sm text-gray-500">{{ m.utilisateurNom }}</td>
              <td class="table-cell text-xs text-gray-400">{{ fmtDate(m.dateCreation) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
