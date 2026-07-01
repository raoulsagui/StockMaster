<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import AppLayout                 from '@/layout/AppLayout.vue'
import EmplacementStatsBar       from '@/components/emplacements/EmplacementStatsBar.vue'
import EmplacementNavigateur     from '@/components/emplacements/EmplacementNavigateur.vue'
import RayonListe                from '@/components/emplacements/RayonListe.vue'
import EtagereListe              from '@/components/emplacements/EtagereListe.vue'
import EmplacementTable          from '@/components/emplacements/EmplacementTable.vue'
import EmplacementModal          from '@/components/emplacements/EmplacementModal.vue'
import emplacementService        from '@/services/emplacementService'
import entrepotService           from '@/services/entrepotService'
import zoneService               from '@/services/zoneService'
import { useAuthStore }          from '@/stores/authStore'

const auth      = useAuthStore()
const canEdit   = computed(() => ['ADMIN','GESTIONNAIRE','MAGASINIER'].includes(auth.role))
const canToggle = computed(() => ['ADMIN','GESTIONNAIRE'].includes(auth.role))

// ── DONNÉES ────────────────────────────────────────────
const entrepots    = ref([])
const zones        = ref([])
const rayons       = ref([])
const etageres     = ref([])
const emplacements = ref([])
const stats        = ref(null)

const entrepotId = ref(null)
const zoneId     = ref(null)
const rayonId    = ref(null)
const etagereId  = ref(null)
const isLoading  = ref(false)
const erreur     = ref('')

// ── FILTRES ─────────────────────────────────────────────
const filtreStatut = ref('')
const filtreType   = ref('')
const recherche    = ref('')

const emplacementsFiltres = computed(() =>
  emplacements.value.filter(e => {
    const matchStatut = !filtreStatut.value || e.statut === filtreStatut.value
    const matchType   = !filtreType.value   || e.type   === filtreType.value
    const matchSearch = !recherche.value
      || e.code.toLowerCase().includes(recherche.value.toLowerCase())
      || e.adresseComplete?.toLowerCase().includes(recherche.value.toLowerCase())
    return matchStatut && matchType && matchSearch
  })
)

// ── CHARGEMENT EN CASCADE ───────────────────────────────
onMounted(async () => {
  try { entrepots.value = await entrepotService.findActifs() }
  catch { erreur.value = 'Impossible de charger les entrepôts.' }
})

watch(entrepotId, async (id) => {
  zones.value = []; rayons.value = []; etageres.value = []; emplacements.value = []
  zoneId.value = null; rayonId.value = null; etagereId.value = null; stats.value = null
  if (!id) return
  try {
    zones.value = (await zoneService.findAll()).filter(z => z.entrepot?.id === id)
    stats.value = await emplacementService.getStats(id)
  } catch { erreur.value = 'Erreur chargement zones.' }
})

watch(zoneId, async (id) => {
  rayons.value = []; etageres.value = []; emplacements.value = []
  rayonId.value = null; etagereId.value = null
  if (!id) return
  try { rayons.value = await emplacementService.getRayonsByZone(id) }
  catch { erreur.value = 'Erreur chargement rayons.' }
})

watch(rayonId, async (id) => {
  etageres.value = []; emplacements.value = []; etagereId.value = null
  if (!id) return
  try { etageres.value = await emplacementService.getEtageresByRayon(id) }
  catch { erreur.value = 'Erreur chargement étagères.' }
})

watch(etagereId, async (id) => {
  emplacements.value = []
  if (!id) return
  isLoading.value = true
  try { emplacements.value = await emplacementService.getByEtagere(id) }
  catch { erreur.value = 'Erreur chargement emplacements.' }
  finally { isLoading.value = false }
})

// ── BREADCRUMB ─────────────────────────────────────────
const breadcrumb = computed(() => {
  const parts = []
  if (entrepotId.value) parts.push(entrepots.value.find(e => e.id === entrepotId.value)?.nom || '…')
  if (zoneId.value)     parts.push(zones.value.find(z => z.id === zoneId.value)?.nom || '…')
  if (rayonId.value)    parts.push('R-' + (rayons.value.find(r => r.id === rayonId.value)?.code || '…'))
  if (etagereId.value)  parts.push('ET-' + (etageres.value.find(e => e.id === etagereId.value)?.code || '…'))
  return parts
})

// ── MODAL ───────────────────────────────────────────────
const modal = ref({ visible: false, mode: 'creer', type: null, item: null, loading: false, erreur: '' })

function ouvrirCreation(type) {
  modal.value = { visible: true, mode: 'creer', type, item: null, loading: false, erreur: '' }
}
function ouvrirModification(type, item) {
  modal.value = { visible: true, mode: 'modifier', type, item, loading: false, erreur: '' }
}
const fermerModal = () => { modal.value.visible = false }

async function soumettre(form) {
  modal.value.loading = true; modal.value.erreur = ''
  const { mode, type, item } = modal.value

  // Injecte l'ID parent dans le payload si création
  const payload = { ...form }
  if (mode === 'creer') {
    if (type === 'rayon')       payload.zoneId     = zoneId.value
    if (type === 'etagere')     payload.rayonId    = rayonId.value
    if (type === 'emplacement') payload.etagereId  = etagereId.value
  }

  try {
    if (type === 'rayon') {
      const res = mode === 'creer'
        ? await emplacementService.creerRayon(payload)
        : await emplacementService.modifierRayon(item.id, payload)
      const idx = rayons.value.findIndex(r => r.id === res.id)
      idx >= 0 ? rayons.value.splice(idx, 1, res) : rayons.value.push(res)

    } else if (type === 'etagere') {
      const res = mode === 'creer'
        ? await emplacementService.creerEtagere(payload)
        : await emplacementService.modifierEtagere(item.id, payload)
      const idx = etageres.value.findIndex(e => e.id === res.id)
      idx >= 0 ? etageres.value.splice(idx, 1, res) : etageres.value.push(res)

    } else {
      const res = mode === 'creer'
        ? await emplacementService.creer(payload)
        : await emplacementService.modifier(item.id, payload)
      const idx = emplacements.value.findIndex(e => e.id === res.id)
      idx >= 0 ? emplacements.value.splice(idx, 1, res) : emplacements.value.push(res)
      if (entrepotId.value) stats.value = await emplacementService.getStats(entrepotId.value)
    }
    fermerModal()
  } catch (e) {
    modal.value.erreur = e.response?.data || 'Erreur lors de la sauvegarde.'
  } finally { modal.value.loading = false }
}

async function toggleStatut(type, item) {
  try {
    const res = type === 'rayon'
      ? await emplacementService.toggleRayonStatut(item.id)
      : await emplacementService.toggleEtagereStatut(item.id)
    Object.assign(item, res)
  } catch { alert('Erreur lors du changement de statut.') }
}

async function changerStatutEmplacement({ emp, statut }) {
  try {
    const res = await emplacementService.changerStatut(emp.id, statut)
    Object.assign(emp, res)
    if (entrepotId.value) stats.value = await emplacementService.getStats(entrepotId.value)
  } catch { alert('Erreur.') }
}

// Code de l'étagère active (pour le titre du tableau)
const etagereActiveCode = computed(() =>
  etageres.value.find(e => e.id === etagereId.value)?.code || ''
)
</script>

<template>
  <AppLayout>
    <div class="space-y-5">

      <!-- EN-TÊTE -->
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Emplacements</h1>
        <p class="text-sm text-gray-500 mt-1">
          Navigation hiérarchique : Entrepôt → Zone → Rayon → Étagère → Emplacement
        </p>
      </div>

      <!-- NAVIGATEUR -->
      <EmplacementNavigateur
        :entrepots="entrepots"
        :zones="zones"
        :rayons="rayons"
        :etageres="etageres"
        :entrepot-id="entrepotId"
        :zone-id="zoneId"
        :rayon-id="rayonId"
        :etagere-id="etagereId"
        :breadcrumb="breadcrumb"
        :can-edit="canEdit"
        @update:entrepot-id="entrepotId = $event"
        @update:zone-id="zoneId = $event"
        @update:rayon-id="rayonId = $event"
        @update:etagere-id="etagereId = $event"
        @creer-rayon="ouvrirCreation('rayon')"
        @creer-etagere="ouvrirCreation('etagere')"
      />

      <!-- STATS -->
      <EmplacementStatsBar v-if="stats" :stats="stats" />

      <!-- ERREUR -->
      <div v-if="erreur" class="card border-red-200 bg-red-50 text-red-700 text-sm p-4">{{ erreur }}</div>

      <!-- RAYONS + ÉTAGÈRES -->
      <div v-if="zoneId" class="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <RayonListe
          :rayons="rayons"
          :rayon-id="rayonId"
          :can-edit="canEdit"
          :can-toggle="canToggle"
          @select="rayonId = $event"
          @modifier="ouvrirModification('rayon', $event)"
          @toggle="toggleStatut('rayon', $event)"
        />
        <EtagereListe
          :etageres="etageres"
          :etagere-id="etagereId"
          :rayon-id="rayonId"
          :can-edit="canEdit"
          :can-toggle="canToggle"
          @select="etagereId = $event"
          @modifier="ouvrirModification('etagere', $event)"
          @toggle="toggleStatut('etagere', $event)"
        />
      </div>

      <!-- TABLEAU EMPLACEMENTS -->
      <div v-if="etagereId" class="card p-0 overflow-hidden">
        <!-- En-tête avec filtres -->
        <div class="px-4 py-3 border-b border-gray-100 flex items-center justify-between bg-gray-50 flex-wrap gap-3">
          <h3 class="text-sm font-semibold text-gray-700">
            Emplacements — ETAGERE-{{ etagereActiveCode }}
            <span class="text-gray-400 font-normal ml-1">({{ emplacementsFiltres.length }})</span>
          </h3>
          <div class="flex items-center gap-2 flex-wrap">
            <input v-model="recherche" type="text" placeholder="Rechercher…" class="form-input py-1.5 text-sm w-36"/>
            <select v-model="filtreStatut" class="form-input py-1.5 text-sm">
              <option value="">Tous statuts</option>
              <option value="LIBRE">Libre</option>
              <option value="OCCUPE">Occupé</option>
              <option value="RESERVE">Réservé</option>
              <option value="BLOQUE">Bloqué</option>
            </select>
            <select v-model="filtreType" class="form-input py-1.5 text-sm">
              <option value="">Tous types</option>
              <option value="PALETTE">Palette</option>
              <option value="BAC">Bac</option>
              <option value="ETAGERE_OUVERTE">Étagère ouverte</option>
              <option value="ARMOIRE">Armoire</option>
              <option value="SOL">Sol</option>
            </select>
            <button v-if="canEdit" @click="ouvrirCreation('emplacement')" class="btn-primary py-1.5 text-sm">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
              </svg>
              Nouvel emplacement
            </button>
          </div>
        </div>

        <EmplacementTable
          :emplacements="emplacementsFiltres"
          :etagere-code="etagereActiveCode"
          :is-loading="isLoading"
          :can-edit="canEdit"
          @modifier="ouvrirModification('emplacement', $event)"
          @changer-statut="changerStatutEmplacement"
        />
      </div>

      <!-- ÉTAT VIDE INITIAL -->
      <div v-if="!entrepotId" class="card p-12 text-center">
        <div class="w-16 h-16 bg-gray-100 rounded-2xl flex items-center justify-center mx-auto mb-4">
          <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
          </svg>
        </div>
        <p class="text-gray-500 font-medium">Sélectionnez un entrepôt</p>
        <p class="text-gray-400 text-sm mt-1">Puis naviguez dans la hiérarchie pour gérer les emplacements.</p>
      </div>

    </div>

    <!-- MODAL -->
    <EmplacementModal
      :visible="modal.visible"
      :mode="modal.mode"
      :type="modal.type"
      :item="modal.item"
      :loading="modal.loading"
      :erreur="modal.erreur"
      @fermer="fermerModal"
      @soumettre="soumettre"
    />

  </AppLayout>
</template>
