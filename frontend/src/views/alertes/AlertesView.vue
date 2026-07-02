<script setup>
import { ref, computed, onMounted } from 'vue'
import AppLayout     from '@/layout/AppLayout.vue'
import alerteService from '@/services/alerteService'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const isAdmin   = computed(() => authStore.role === 'ADMIN')

// -------------------------------------------------------
// DONNÉES
// -------------------------------------------------------
const alertes   = ref([])
const stats     = ref(null)
const isLoading = ref(false)
const erreur    = ref('')
const scanning  = ref(false)

// FILTRES
const filtreStatut   = ref('')   // '' | 'NON_LUE' | 'LUE' | 'RESOLUE'
const filtreType     = ref('')   // '' | 'STOCK_FAIBLE' | 'ZONE_SATUREE' | 'PRODUIT_EXPIRE'
const filtreSeverite = ref('')   // '' | 'CRITIQUE' | 'WARNING' | 'INFO'

onMounted(async () => {
  await Promise.all([charger(), chargerStats()])
})

async function charger() {
  isLoading.value = true
  erreur.value    = ''
  try {
    alertes.value = await alerteService.findAll(filtreStatut.value || null)
  } catch {
    erreur.value = 'Impossible de charger les alertes.'
  } finally {
    isLoading.value = false
  }
}

async function chargerStats() {
  try { stats.value = await alerteService.getStats() } catch { /* non bloquant */ }
}

// -------------------------------------------------------
// LISTE FILTRÉE (côté frontend pour rapidité)
// -------------------------------------------------------
const alertesFiltrees = computed(() => {
  return alertes.value.filter(a => {
    const matchType     = !filtreType.value     || a.type     === filtreType.value
    const matchSeverite = !filtreSeverite.value || a.severite === filtreSeverite.value
    const matchStatut   = !filtreStatut.value   || a.statut   === filtreStatut.value
    return matchType && matchSeverite && matchStatut
  })
})

const nonLues = computed(() => alertes.value.filter(a => a.statut === 'NON_LUE').length)

// -------------------------------------------------------
// ACTIONS
// -------------------------------------------------------
async function marquerLue(alerte) {
  try {
    const updated = await alerteService.marquerLue(alerte.id)
    Object.assign(alerte, updated)
    await chargerStats()
  } catch { alert('Erreur.') }
}

async function marquerResolue(alerte) {
  try {
    const updated = await alerteService.marquerResolue(alerte.id)
    Object.assign(alerte, updated)
    await chargerStats()
  } catch { alert('Erreur.') }
}

async function toutMarquerLu() {
  try {
    await alerteService.marquerToutesLues()
    await Promise.all([charger(), chargerStats()])
  } catch { alert('Erreur.') }
}

async function lancerScan() {
  scanning.value = true
  try {
    await alerteService.scanner('TOUS')
    await Promise.all([charger(), chargerStats()])
  } catch { alert('Erreur lors du scan.') } finally {
    scanning.value = false
  }
}

// -------------------------------------------------------
// HELPERS AFFICHAGE
// -------------------------------------------------------
function fmtDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleString('fr-FR', { day:'2-digit', month:'2-digit', year:'numeric', hour:'2-digit', minute:'2-digit' })
}

const SEVERITE_CONFIG = {
  CRITIQUE: { bg: 'bg-red-100',    text: 'text-red-700',    dot: 'bg-red-500',    label: 'Critique',       border: 'border-l-red-500' },
  WARNING:  { bg: 'bg-orange-100', text: 'text-orange-700', dot: 'bg-orange-400', label: 'Avertissement',  border: 'border-l-orange-400' },
  INFO:     { bg: 'bg-blue-100',   text: 'text-blue-700',   dot: 'bg-blue-400',   label: 'Information',    border: 'border-l-blue-400' },
}

const TYPE_CONFIG = {
  STOCK_FAIBLE:    { icon: 'M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4', label: 'Stock faible',   color: 'text-red-600' },
  PRODUIT_EXPIRE:  { icon: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z',                    label: 'Produit expiré',  color: 'text-orange-600' },
  ZONE_SATUREE:    { icon: 'M4 5a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1V5zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1V5zM4 15a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1H5a1 1 0 01-1-1v-4zm10 0a1 1 0 011-1h4a1 1 0 011 1v4a1 1 0 01-1 1h-4a1 1 0 01-1-1v-4z', label: 'Zone saturée',   color: 'text-blue-600' },
}

const STATUT_CONFIG = {
  NON_LUE: { bg: 'bg-red-50 text-red-600',     label: 'Non lue' },
  LUE:     { bg: 'bg-gray-100 text-gray-500',   label: 'Lue' },
  RESOLUE: { bg: 'bg-green-100 text-green-700', label: 'Résolue' },
}
</script>

<template>
  <AppLayout>
    <div class="space-y-6">

      <!-- EN-TÊTE -->
      <div class="flex flex-col sm:flex-row sm:items-center gap-4 justify-between">
        <div>
          <div class="flex items-center gap-3">
            <h1 class="text-2xl font-bold text-gray-900">Alertes</h1>
            <span v-if="nonLues > 0"
              class="inline-flex items-center justify-center w-6 h-6 rounded-full bg-red-500 text-white text-xs font-bold">
              {{ nonLues > 99 ? '99+' : nonLues }}
            </span>
          </div>
          <p class="text-sm text-gray-500 mt-1">Surveillance automatique du stock, des produits et des zones</p>
        </div>
        <div class="flex items-center gap-2 flex-wrap">
          <button v-if="nonLues > 0" @click="toutMarquerLu"
            class="btn-secondary text-sm">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
            </svg>
            Tout marquer lu
          </button>
          <!-- <button v-if="isAdmin" @click="lancerScan" :disabled="scanning" class="btn-primary text-sm">
            <svg v-if="scanning" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
            <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
            </svg>
            {{ scanning ? 'Scan en cours…' : 'Scanner maintenant' }}
          </button> -->
        </div>
      </div>

      <!-- STATS CARDS -->
      <div v-if="stats" class="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div class="card p-4 flex items-center gap-3">
          <div class="w-10 h-10 bg-red-100 rounded-xl flex items-center justify-center">
            <svg class="w-5 h-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/>
            </svg>
          </div>
          <div>
            <p class="text-xl font-bold text-red-600">{{ stats.nonLues }}</p>
            <p class="text-xs text-gray-500">Non lues</p>
          </div>
        </div>
        <div class="card p-4 flex items-center gap-3">
          <div class="w-10 h-10 bg-red-50 rounded-xl flex items-center justify-center">
            <span class="text-red-500 text-xs font-bold">!</span>
          </div>
          <div>
            <p class="text-xl font-bold text-red-600">{{ stats.parSeverite?.CRITIQUE || 0 }}</p>
            <p class="text-xs text-gray-500">Critiques actives</p>
          </div>
        </div>
        <div class="card p-4 flex items-center gap-3">
          <div class="w-10 h-10 bg-orange-100 rounded-xl flex items-center justify-center">
            <span class="text-orange-500 text-xs font-bold">⚠</span>
          </div>
          <div>
            <p class="text-xl font-bold text-orange-600">{{ stats.parSeverite?.WARNING || 0 }}</p>
            <p class="text-xs text-gray-500">Avertissements</p>
          </div>
        </div>
        <div class="card p-4 flex items-center gap-3">
          <div class="w-10 h-10 bg-green-100 rounded-xl flex items-center justify-center">
            <svg class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
          </div>
          <div>
            <p class="text-xl font-bold text-green-600">{{ stats.resolues }}</p>
            <p class="text-xs text-gray-500">Résolues</p>
          </div>
        </div>
      </div>

      <!-- FILTRES -->
      <div class="card p-4 flex flex-wrap gap-3 items-end">
        <div>
          <label class="form-label text-xs">Statut</label>
          <select v-model="filtreStatut" class="form-input py-1.5 text-sm">
            <option value="">Tous les statuts</option>
            <option value="NON_LUE">Non lues</option>
            <option value="LUE">Lues</option>
            <option value="RESOLUE">Résolues</option>
          </select>
        </div>
        <div>
          <label class="form-label text-xs">Type</label>
          <select v-model="filtreType" class="form-input py-1.5 text-sm">
            <option value="">Tous les types</option>
            <option value="STOCK_FAIBLE">Stock faible</option>
            <option value="ZONE_SATUREE">Zone saturée</option>
            <option value="PRODUIT_EXPIRE">Produit expiré</option>
          </select>
        </div>
        <div>
          <label class="form-label text-xs">Sévérité</label>
          <select v-model="filtreSeverite" class="form-input py-1.5 text-sm">
            <option value="">Toutes</option>
            <option value="CRITIQUE">Critique</option>
            <option value="WARNING">Avertissement</option>
            <option value="INFO">Information</option>
          </select>
        </div>
        <button @click="filtreStatut='';filtreType='';filtreSeverite=''" class="btn-secondary py-1.5 text-sm">
          Réinitialiser
        </button>
      </div>

      <!-- ERREUR -->
      <div v-if="erreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">{{ erreur }}</div>

      <!-- LOADING -->
      <div v-if="isLoading" class="card p-12 text-center text-gray-400">
        <svg class="w-6 h-6 animate-spin mx-auto mb-2 text-blue-500" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
        Chargement des alertes…
      </div>

      <!-- LISTE DES ALERTES -->
      <div v-if="!isLoading" class="space-y-3">
        <div v-if="alertesFiltrees.length === 0" class="card p-12 text-center">
          <svg class="w-12 h-12 text-green-300 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          <p class="text-gray-500 font-medium">Aucune alerte</p>
          <p class="text-gray-400 text-sm mt-1">Tout est sous contrôle !</p>
        </div>

        <div v-for="alerte in alertesFiltrees" :key="alerte.id"
          :class="['card p-0 overflow-hidden border-l-4 transition-opacity',
            SEVERITE_CONFIG[alerte.severite]?.border,
            alerte.statut === 'RESOLUE' ? 'opacity-60' : '']">
          <div class="p-4 flex items-start gap-4">

            <!-- ICÔNE TYPE -->
            <div :class="['w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0 mt-0.5',
              SEVERITE_CONFIG[alerte.severite]?.bg]">
              <svg :class="['w-5 h-5', TYPE_CONFIG[alerte.type]?.color]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="TYPE_CONFIG[alerte.type]?.icon"/>
              </svg>
            </div>

            <!-- CONTENU -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 flex-wrap mb-1">
                <!-- Badge sévérité -->
                <span :class="['inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-semibold',
                  SEVERITE_CONFIG[alerte.severite]?.bg, SEVERITE_CONFIG[alerte.severite]?.text]">
                  <span :class="['w-1.5 h-1.5 rounded-full', SEVERITE_CONFIG[alerte.severite]?.dot]"></span>
                  {{ SEVERITE_CONFIG[alerte.severite]?.label }}
                </span>
                <!-- Badge type -->
                <span class="text-xs text-gray-500 font-medium">{{ TYPE_CONFIG[alerte.type]?.label }}</span>
                <!-- Badge statut -->
                <span :class="['ml-auto text-xs px-2 py-0.5 rounded-full font-medium', STATUT_CONFIG[alerte.statut]?.bg]">
                  {{ STATUT_CONFIG[alerte.statut]?.label }}
                </span>
              </div>

              <!-- Message -->
              <p class="text-sm text-gray-700 leading-relaxed">{{ alerte.message }}</p>

              <!-- Contexte (produit / zone) -->
              <div class="flex items-center gap-4 mt-2 flex-wrap">
                <span v-if="alerte.produitNom" class="text-xs text-gray-400">
                  📦 {{ alerte.produitNom }} <span class="text-gray-300">|</span> {{ alerte.entrepotNom }}
                </span>
                <span v-if="alerte.zoneNom" class="text-xs text-gray-400">
                  🏭 {{ alerte.zoneNom }} <span class="text-gray-300">|</span> {{ alerte.zoneEntrepotNom }}
                </span>
                <span v-if="alerte.valeurActuelle != null" class="text-xs text-gray-400">
                  Valeur : <strong>{{ alerte.type === 'ZONE_SATUREE' ? alerte.valeurActuelle.toFixed(1) + ' %' : alerte.valeurActuelle }}</strong>
                  <template v-if="alerte.seuil != null"> / Seuil : <strong>{{ alerte.type === 'ZONE_SATUREE' ? alerte.seuil.toFixed(0) + ' %' : alerte.seuil }}</strong></template>
                </span>
                <span class="text-xs text-gray-300 ml-auto">{{ fmtDate(alerte.dateCreation) }}</span>
              </div>
            </div>

            <!-- ACTIONS -->
            <div class="flex flex-col gap-1.5 flex-shrink-0">
              <button v-if="alerte.statut === 'NON_LUE'" @click="marquerLue(alerte)"
                class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                title="Marquer comme lue">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                </svg>
              </button>
              <button v-if="alerte.statut !== 'RESOLUE'" @click="marquerResolue(alerte)"
                class="p-1.5 text-gray-400 hover:text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                title="Marquer comme résolue">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
              </button>
            </div>

          </div>
        </div>
      </div>

    </div>
  </AppLayout>
</template>
