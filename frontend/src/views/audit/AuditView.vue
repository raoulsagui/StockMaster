<script setup>
import { ref, computed, onMounted } from 'vue'
import AppLayout from '@/layout/AppLayout.vue'
import auditService from '@/services/auditService'
import { usePermissions } from '@/composables/usePermissions'

const { peutAuditer } = usePermissions()
const logs = ref([])
const isLoading = ref(false)
const erreur = ref('')

const filtreType = ref('')
const filtreDateDebut = ref('')
const filtreDateFin = ref('')

const typesDisponibles = [
    { value: '', label: 'Toutes les entités' },
    { value: 'Utilisateur', label: 'Utilisateurs' },
    { value: 'Produit', label: 'Produits' },
    { value: 'Categorie', label: 'Catégories' },
    { value: 'Entrepot', label: 'Entrepôts' },
    { value: 'Zone', label: 'Zones' },
    { value: 'Fournisseur', label: 'Fournisseurs' },
    { value: 'Entree', label: 'Entrées' },
    { value: 'Sortie', label: 'Sorties' },
    { value: 'Transfert', label: 'Transferts' },
]

const typeLabels = {
    CREER: 'Création',
    MODIFIER: 'Modification',
    SUPPRIMER: 'Suppression',
    DESACTIVER: 'Désactivation',
    REACTIVER: 'Réactivation',
    VALIDER: 'Validation',
    ANNULER: 'Annulation',
}

const formatDate = (d) => d
    ? new Date(d).toLocaleString('fr-FR', {
        day: '2-digit', month: 'short', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
    })
    : '—'

onMounted(() => chargerLogs())

async function chargerLogs() {
    isLoading.value = true
    erreur.value = ''
    try {
        if (filtreType.value) {
            logs.value = await auditService.findByEntityType(filtreType.value)
        } else {
            logs.value = await auditService.findAll()
        }
    } catch {
        erreur.value = 'Impossible de charger les logs d\'audit.'
    } finally {
        isLoading.value = false
    }
}

function getTypeBadgeClass(type) {
    const classes = {
        CREER: 'bg-emerald-50 text-emerald-700',
        MODIFIER: 'bg-amber-50 text-amber-700',
        SUPPRIMER: 'bg-red-50 text-red-700',
        DESACTIVER: 'bg-gray-50 text-gray-700',
        REACTIVER: 'bg-blue-50 text-blue-700',
        VALIDER: 'bg-indigo-50 text-indigo-700',
        ANNULER: 'bg-orange-50 text-orange-700',
    }
    return classes[type] || 'bg-gray-50 text-gray-700'
}
</script>

<template>
    <AppLayout>
        <div class="space-y-5">

            <div class="flex items-center justify-between">
                <div>
                    <h1 class="text-xl font-semibold text-gray-900">Traçabilité</h1>
                    <p class="text-sm text-gray-400 mt-0.5">{{ logs.length }} actions enregistrées</p>
                </div>
                <button @click="chargerLogs"
                    class="inline-flex items-center gap-1.5 bg-white border border-gray-200 text-gray-600 hover:bg-gray-50 px-3.5 py-2 rounded-lg text-sm font-medium transition-colors">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 0115.357-2m15.357 2H15" />
                    </svg>
                    Actualiser
                </button>
            </div>

            <div class="bg-white rounded-lg border border-gray-200 p-4">
                <div class="flex flex-col sm:flex-row gap-3">
                    <select v-model="filtreType" @change="chargerLogs"
                        class="w-full sm:w-56 text-sm border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                        <option v-for="t in typesDisponibles" :key="t.value" :value="t.value">
                            {{ t.label }}
                        </option>
                    </select>
                    <div class="flex gap-2 flex-1">
                        <input v-model="filtreDateDebut" type="date"
                            class="flex-1 min-w-0 text-sm border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                        <input v-model="filtreDateFin" type="date"
                            class="flex-1 min-w-0 text-sm border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                    </div>
                </div>
            </div>

            <div v-if="erreur" class="rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm p-3">
                {{ erreur }}
            </div>

            <div v-if="isLoading" class="bg-white rounded-lg border border-gray-200 p-10 text-center text-gray-400 text-sm">
                <svg class="w-5 h-5 animate-spin mx-auto mb-2 text-indigo-500" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                </svg>
                Chargement…
            </div>

            <div v-else class="bg-white rounded-lg border border-gray-200 overflow-hidden">
                <div v-if="logs.length === 0" class="py-12 text-center text-gray-300 text-sm">
                    <svg class="w-8 h-8 mx-auto mb-2 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                            d="M9 12h6m-6 0l3-3m-3 0l3 3M3 6h18M3 6a2 2 0 012-2h14a2 2 0 012 2v12a2 2 0 01-2 2H5a2 2 0 01-2-2V6z" />
                    </svg>
                    Aucun log d'audit trouvé.
                </div>
                <div v-else class="overflow-x-auto">
                    <table class="w-full">
                        <thead class="bg-gray-50 border-b border-gray-100">
                            <tr>
                                <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider py-3 px-4">Date</th>
                                <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider py-3 px-4">Action</th>
                                <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider py-3 px-4">Entité</th>
                                <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider py-3 px-4">ID</th>
                                <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider py-3 px-4 hidden md:table-cell">Utilisateur</th>
                                <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider py-3 px-4 hidden lg:table-cell">Ancien</th>
                                <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider py-3 px-4 hidden lg:table-cell">Nouveau</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-gray-50">
                            <tr v-for="log in logs" :key="log.id" class="hover:bg-gray-50/50 transition-colors">
                                <td class="py-3 px-4 text-xs text-gray-400">{{ formatDate(log.dateAction) }}</td>
                                <td class="py-3 px-4">
                                    <span :class="['inline-flex items-center px-2 py-0.5 rounded text-xs font-medium', getTypeBadgeClass(log.action)]">
                                        {{ typeLabels[log.action] ?? log.action }}
                                    </span>
                                </td>
                                <td class="py-3 px-4 text-sm text-gray-600">{{ log.entityType }}</td>
                                <td class="py-3 px-4 font-mono text-xs text-gray-500">{{ log.entityId }}</td>
                                <td class="py-3 px-4 text-sm text-gray-600 hidden md:table-cell">{{ log.utilisateurEmail }}</td>
                                <td class="py-3 px-4 text-xs text-gray-400 hidden lg:table-cell truncate max-w-xs">
                                    {{ log.ancienneValeur ?? '—' }}
                                </td>
                                <td class="py-3 px-4 text-xs text-gray-400 hidden lg:table-cell truncate max-w-xs">
                                    {{ log.nouvelleValeur ?? '—' }}
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </AppLayout>
</template>