<script setup>
import { ref, onMounted }            from 'vue'
import AppLayout                     from '@/layout/AppLayout.vue'
import RapportFiltres                from '@/components/rapports/RapportFiltres.vue'
import RapportResultatInventaire     from '@/components/rapports/RapportResultatInventaire.vue'
import RapportResultatMouvements     from '@/components/rapports/RapportResultatMouvements.vue'
import RapportResultatFournisseurs   from '@/components/rapports/RapportResultatFournisseurs.vue'
import rapportService                from '@/services/rapportService'

const onglet          = ref('inventaire')
const isLoading       = ref(false)
const isExporting     = ref(false)
const erreur          = ref('')
const donnees         = ref(null)
const inventaires     = ref([])
const inventaireSelId = ref(null)

const today     = new Date().toISOString().slice(0, 16)
const lastMonth = new Date(Date.now() - 30 * 86400000).toISOString().slice(0, 16)
const filtres   = ref({ dateDebut: lastMonth, dateFin: today, entrepotId: null, type: null })

onMounted(async () => {
  try { inventaires.value = await rapportService.listerInventaires() } catch { /* non bloquant */ }
})

async function generer() {
  erreur.value = ''; donnees.value = null; isLoading.value = true
  try {
    if (onglet.value === 'inventaire') {
      if (!inventaireSelId.value) { erreur.value = 'Sélectionnez un inventaire.'; return }
      donnees.value = await rapportService.getRapportInventaire(inventaireSelId.value)
    } else if (onglet.value === 'mouvements') {
      donnees.value = await rapportService.getRapportMouvements({
        dateDebut: filtres.value.dateDebut, dateFin: filtres.value.dateFin,
        entrepotId: filtres.value.entrepotId || null, type: filtres.value.type || null,
      })
    } else {
      donnees.value = await rapportService.getRapportFournisseurs({
        dateDebut: filtres.value.dateDebut, dateFin: filtres.value.dateFin,
      })
    }
  } catch (e) {
    erreur.value = e.response?.data || 'Erreur lors de la génération du rapport.'
  } finally { isLoading.value = false }
}

async function exporter(format) {
  if (!donnees.value) return
  isExporting.value = true
  try {
    if (onglet.value === 'inventaire')
      await rapportService.exportInventaire(inventaireSelId.value, format)
    else if (onglet.value === 'mouvements')
      await rapportService.exportMouvements({ dateDebut: filtres.value.dateDebut, dateFin: filtres.value.dateFin, entrepotId: filtres.value.entrepotId || null, type: filtres.value.type || null }, format)
    else
      await rapportService.exportFournisseurs({ dateDebut: filtres.value.dateDebut, dateFin: filtres.value.dateFin }, format)
  } catch { erreur.value = "Erreur lors de l'export." }
  finally { isExporting.value = false }
}
</script>

<template>
  <AppLayout>
    <div class="space-y-6">

      <!-- EN-TÊTE -->
      <div class="flex flex-col sm:flex-row sm:items-center gap-4 justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Rapports</h1>
          <p class="text-sm text-gray-500 mt-1">Générez et exportez vos rapports en PDF, Excel ou CSV</p>
        </div>
        <!-- Boutons export — visibles après génération -->
        <div v-if="donnees" class="flex items-center gap-2 flex-wrap">
          <span class="text-xs text-gray-400 mr-1">Exporter en :</span>
          <button @click="exporter('pdf')"   :disabled="isExporting"
            class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors bg-red-50 text-red-700 hover:bg-red-100 border border-red-200">
            PDF
          </button>
          <button @click="exporter('excel')" :disabled="isExporting"
            class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors bg-green-50 text-green-700 hover:bg-green-100 border border-green-200">
            Excel
          </button>
          <button @click="exporter('csv')"   :disabled="isExporting"
            class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors bg-blue-50 text-blue-700 hover:bg-blue-100 border border-blue-200">
            CSV
          </button>
        </div>
      </div>

      <!-- ONGLETS -->
      <div class="flex gap-1 bg-gray-100 p-1 rounded-xl w-fit">
        <button v-for="tab in [{id:'inventaire',label:'Inventaire'},{id:'mouvements',label:'Mouvements'},{id:'fournisseurs',label:'Fournisseurs'}]"
          :key="tab.id"
          @click="onglet = tab.id; donnees = null; erreur = ''"
          :class="['px-4 py-2 rounded-lg text-sm font-medium transition-colors',
            onglet === tab.id ? 'bg-white text-blue-700 shadow-sm' : 'text-gray-500 hover:text-gray-700']">
          {{ tab.label }}
        </button>
      </div>

      <!-- FILTRES -->
      <RapportFiltres
        :onglet="onglet"
        :inventaires="inventaires"
        :inventaire-sel-id="inventaireSelId"
        :filtres="filtres"
        :is-loading="isLoading"
        @update:inventaire-sel-id="inventaireSelId = $event"
        @update:filtres="filtres = $event"
        @generer="generer"
      />

      <!-- ERREUR -->
      <div v-if="erreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">{{ erreur }}</div>

      <!-- RÉSULTATS -->
      <RapportResultatInventaire   v-if="donnees && onglet === 'inventaire'"   :donnees="donnees" />
      <RapportResultatMouvements   v-if="donnees && onglet === 'mouvements'"   :donnees="donnees" />
      <RapportResultatFournisseurs v-if="donnees && onglet === 'fournisseurs'" :donnees="donnees" />

    </div>
  </AppLayout>
</template>
