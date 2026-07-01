<script setup>
import { ref, onMounted } from 'vue'
import AppLayout from '@/layout/AppLayout.vue'
import codeBarresService from '@/services/codeBarresService'
import produitService from '@/services/produitService'
import zoneService from '@/services/zoneService'
import { usePermissions } from '@/composables/usePermissions'
import { useToast } from '@/composables/useToast'

const { peutGererCatalogue } = usePermissions()
const toast = useToast()

const produits = ref([])
const zones = ref([])
const isLoading = ref(false)
const erreur = ref('')

const modeScan = ref(false)
const codeScanne = ref('')

const resultatScan = ref(null)
const showQRModal = ref(false)
const qrData = ref(null)
const isGenerating = ref(false)

onMounted(() => chargerDonnees())

async function chargerDonnees() {
    isLoading.value = true
    try {
        const [p, z] = await Promise.all([
            produitService.findAll(),
            zoneService.findAll(),
        ])
        produits.value = p
        zones.value = z
    } catch {
        erreur.value = 'Impossible de charger les données.'
    } finally {
        isLoading.value = false
    }
}

async function scannerCode() {
    if (!codeScanne.value) return
    try {
        resultatScan.value = await codeBarresService.scannerProduit(codeScanne.value)
        if (!resultatScan.value) {
            toast.error('Aucun produit trouvé avec ce code.')
            resultatScan.value = null
        }
    } catch {
        toast.error('Erreur lors du scan.')
        resultatScan.value = null
    }
}

async function genererQR(type, id) {
    isGenerating.value = true
    try {
        if (type === 'produit') {
            qrData.value = await codeBarresService.getQRCodeProduit(id)
        } else if (type === 'zone') {
            qrData.value = await codeBarresService.getQRCodeZone(id)
        }
        showQRModal.value = true
    } catch {
        toast.error('Erreur lors de la génération du QR code.')
    } finally {
        isGenerating.value = false
    }
}

function downloadQR() {
    if (!qrData.value?.qrCodeBase64) return
    const link = document.createElement('a')
    link.href = `data:image/png;base64,${qrData.value.qrCodeBase64}`
    link.download = `${qrData.value.type === 'PRODUIT' ? 'produit' : 'emplacement'}-${qrData.value.id}.png`
    link.click()
}
</script>

<template>
    <AppLayout>
        <div class="space-y-5">

            <div class="page-header">
                <div class="flex items-center justify-between">
                    <div>
                        <h1 class="text-xl font-semibold text-gray-900">Codes-barres</h1>
                        <p class="text-sm text-gray-400 mt-0.5">
                            Génération et scan QR Code
                        </p>
                    </div>
                    <div class="flex gap-2">
                        <button @click="modeScan = false"
                            :class="['px-4 py-2 rounded-xl text-sm font-medium transition-colors',
                                !modeScan ? 'bg-gray-100 text-gray-900' : 'bg-white text-gray-600 hover:bg-gray-50 border border-gray-200']">
                            Générer
                        </button>
                        <button @click="modeScan = true"
                            :class="['px-4 py-2 rounded-xl text-sm font-medium transition-colors',
                                modeScan ? 'bg-gray-100 text-gray-900' : 'bg-white text-gray-600 hover:bg-gray-50 border border-gray-200']">
                            Scanner
                        </button>
                    </div>
                </div>
            </div>

            <div v-if="modeScan" class="bg-white rounded-lg border border-gray-200 p-5">
                <h3 class="text-sm font-semibold text-gray-900 mb-3">Scanner un code-barres</h3>
                <div class="flex gap-3">
                    <input v-model="codeScanne" type="text" placeholder="Code-barres du produit…"
                        class="flex-1 text-sm border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
                    <button @click="scannerCode"
                        class="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 transition-colors">
                        Scanner
                    </button>
                </div>

                <div v-if="resultatScan" class="mt-4 p-4 bg-gray-50 rounded-lg border border-gray-200">
                    <p class="text-sm font-medium text-gray-900">{{ resultatScan.nom }}</p>
                    <p class="text-xs text-gray-500 mt-1">Réf: {{ resultatScan.reference }}</p>
                </div>
            </div>

            <div v-else class="bg-white rounded-lg border border-gray-200 p-5">
                <h3 class="text-sm font-semibold text-gray-900 mb-3">Produits disponibles</h3>
                <div v-if="isLoading" class="text-center text-gray-400 py-8">
                    Chargement des produits…
                </div>
                <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3">
                    <div v-for="p in produits" :key="p.id"
                        class="border border-gray-200 rounded-lg p-3 text-center hover:border-cyan-300 hover:shadow-sm transition-all cursor-pointer"
                        @click="genererQR('produit', p.id)">
                        <div class="w-16 h-16 mx-auto mb-2 bg-gray-100 rounded-lg flex items-center justify-center">
                            <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                                    d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                            </svg>
                        </div>
                        <p class="text-xs font-medium text-gray-900 truncate">{{ p.nom }}</p>
                        <p class="text-xs text-gray-400">{{ p.reference }}</p>
                    </div>
                </div>
            </div>

            <div v-if="!modeScan" class="bg-white rounded-lg border border-gray-200 p-5">
                <h3 class="text-sm font-semibold text-gray-900 mb-3">Emplacements (zones)</h3>
                <div v-if="isLoading" class="text-center text-gray-400 py-8">
                    Chargement des emplacements…
                </div>
                <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3">
                    <div v-for="z in zones" :key="z.id"
                        class="border border-gray-200 rounded-lg p-3 text-center hover:border-teal-300 hover:shadow-sm transition-all cursor-pointer"
                        @click="genererQR('zone', z.id)">
                        <div class="w-16 h-16 mx-auto mb-2 bg-gray-100 rounded-lg flex items-center justify-center">
                            <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                                    d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                            </svg>
                        </div>
                        <p class="text-xs font-medium text-gray-900 truncate">{{ z.nom }}</p>
                        <p class="text-xs text-gray-400">{{ z.code }}</p>
                    </div>
                </div>
            </div>

        </div>
    </AppLayout>

    <Teleport to="body">
        <Transition enter-active-class="transition duration-200" enter-from-class="opacity-0" enter-to-class="opacity-100"
            leave-active-class="transition duration-150" leave-from-class="opacity-100" leave-to-class="opacity-0">
            <div v-if="showQRModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
                <div class="absolute inset-0 bg-black/40" @click="showQRModal = false"></div>
                <div class="relative bg-white rounded-xl shadow-xl w-full max-w-sm p-6 text-center">
                    <h3 class="text-base font-semibold text-gray-900 mb-4">
                        {{ qrData?.type === 'PRODUIT' ? 'QR Produit' : 'QR Emplacement' }}
                    </h3>
                    <div v-if="qrData?.qrCodeBase64" class="mx-auto mb-4 w-48 h-48 bg-gray-50 rounded-lg flex items-center justify-center">
                        <img :src="`data:image/png;base64,${qrData.qrCodeBase64}`" class="max-w-full max-h-full" alt="QR Code" />
                    </div>
                    <div class="text-sm text-gray-600 mb-4">
                        <p class="font-medium">{{ qrData?.nom }}</p>
                        <p class="text-xs text-gray-400">{{ qrData?.reference }}</p>
                    </div>
                    <div class="flex gap-2 justify-center">
                        <button @click="showQRModal = false"
                            class="px-4 py-2 text-sm font-medium text-gray-600 bg-white border border-gray-200 rounded-lg hover:bg-gray-50">
                            Fermer
                        </button>
                        <button @click="downloadQR"
                            class="px-4 py-2 text-sm font-medium text-cyan-700 bg-cyan-50 border border-cyan-200 rounded-lg hover:bg-cyan-100">
                            Télécharger
                        </button>
                    </div>
                </div>
            </div>
        </Transition>
    </Teleport>
</template>