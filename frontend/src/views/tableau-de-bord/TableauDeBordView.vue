<script setup>
// ============================================================
// TABLEAU DE BORD — Page d'accueil de l'application
//
// Affiche les KPI principaux et un résumé des activités.
// Pour l'instant les données sont statiques (mock).
// Plus tard on les chargera depuis l'API Spring Boot.
//
// Ce composant utilise AppLayout pour avoir la sidebar et le header.
// ============================================================

import { ref } from 'vue'
import AppLayout from '@/components/layout/AppLayout.vue'

// -------------------------------------------------------
// DONNÉES MOCK — Simulent la réponse de l'API
// On les remplacera par des appels au service API (Module 13)
// -------------------------------------------------------
const kpis = ref([
  {
    id: 1,
    label: 'Entrepôts actifs',
    value: '4',
    change: '+1 ce mois',
    changeType: 'positive', // positive | negative | neutral
    icon: 'warehouse',
    color: 'blue',
  },
  {
    id: 2,
    label: 'Produits en stock',
    value: '1 284',
    change: '+56 cette semaine',
    changeType: 'positive',
    icon: 'box',
    color: 'green',
  },
  {
    id: 3,
    label: 'Produits critiques',
    value: '12',
    change: '3 en rupture',
    changeType: 'negative',
    icon: 'alert',
    color: 'red',
  },
  {
    id: 4,
    label: 'Entrées du mois',
    value: '342',
    change: '+8% vs mois dernier',
    changeType: 'positive',
    icon: 'arrow-in',
    color: 'indigo',
  },
  {
    id: 5,
    label: 'Sorties du mois',
    value: '289',
    change: '-3% vs mois dernier',
    changeType: 'negative',
    icon: 'arrow-out',
    color: 'orange',
  },
  {
    id: 6,
    label: 'Utilisateurs actifs',
    value: '8',
    change: '2 admins',
    changeType: 'neutral',
    icon: 'users',
    color: 'purple',
  },
])

// Derniers mouvements de stock (mock)
const derniersMouvements = ref([
  { id: 1, type: 'ENTRÉE',    produit: 'Laptop Dell XPS 15',    quantite: 20, entrepot: 'Entrepôt Paris',  date: '12/06/2026' },
  { id: 2, type: 'SORTIE',    produit: 'Souris Logitech MX3',   quantite: 5,  entrepot: 'Entrepôt Lyon',   date: '12/06/2026' },
  { id: 3, type: 'TRANSFERT', produit: 'Câble HDMI 2m',          quantite: 50, entrepot: 'Paris → Lyon',    date: '11/06/2026' },
  { id: 4, type: 'ENTRÉE',    produit: 'Écran Samsung 27"',      quantite: 10, entrepot: 'Entrepôt Nantes', date: '11/06/2026' },
  { id: 5, type: 'SORTIE',    produit: 'Clavier mécanique RGB',  quantite: 3,  entrepot: 'Entrepôt Paris',  date: '10/06/2026' },
])

// Alertes récentes (mock)
const alertes = ref([
  { id: 1, severity: 'danger',  message: 'Câble USB-C — Stock en rupture (Entrepôt Lyon)' },
  { id: 2, severity: 'warning', message: 'Écran Samsung — Stock faible (3 restants)' },
  { id: 3, severity: 'warning', message: 'Zone B Nantes — Capacité à 92%' },
])

// Mapping couleur → classes Tailwind pour les cartes KPI
const colorMap = {
  blue:   { icon: 'bg-blue-600'   },
  green:  { icon: 'bg-green-600'  },
  red:    { icon: 'bg-red-600'    },
  indigo: { icon: 'bg-indigo-600' },
  orange: { icon: 'bg-orange-500' },
  purple: { icon: 'bg-purple-600' },
}
</script>

<template>
  <!-- AppLayout enveloppe tout le contenu de la page -->
  <AppLayout>
    <div class="space-y-6">

      <!-- ===== EN-TÊTE DE PAGE ===== -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Tableau de bord</h1>
          <p class="text-sm text-gray-500 mt-1">
            Bienvenue, voici un aperçu de votre activité en temps réel.
          </p>
        </div>
        <span class="hidden sm:block text-sm text-gray-400">Vendredi 12 juin 2026</span>
      </div>

      <!-- ===== GRILLE DES KPI ===== -->
      <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">
        <div
          v-for="kpi in kpis"
          :key="kpi.id"
          class="card flex items-center gap-4 hover:shadow-md transition-shadow duration-200"
        >
          <!-- Icône colorée -->
          <div :class="['w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0', colorMap[kpi.color].icon]">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path v-if="kpi.icon === 'warehouse'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
              <path v-else-if="kpi.icon === 'box'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
              <path v-else-if="kpi.icon === 'alert'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
              <path v-else-if="kpi.icon === 'arrow-in'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"/>
              <path v-else-if="kpi.icon === 'arrow-out'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
              <path v-else-if="kpi.icon === 'users'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/>
            </svg>
          </div>

          <!-- Valeur + label + variation -->
          <div class="flex-1 min-w-0">
            <p class="text-2xl font-bold text-gray-900">{{ kpi.value }}</p>
            <p class="text-sm text-gray-500 truncate">{{ kpi.label }}</p>
            <p :class="[
              'text-xs mt-0.5 font-medium',
              kpi.changeType === 'positive' ? 'text-green-600' :
              kpi.changeType === 'negative' ? 'text-red-500' :
              'text-gray-400'
            ]">
              {{ kpi.change }}
            </p>
          </div>
        </div>
      </div>

      <!-- ===== MOUVEMENTS + ALERTES ===== -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">

        <!-- Derniers mouvements (2/3) -->
        <div class="card lg:col-span-2">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-base font-semibold text-gray-900">Derniers mouvements</h3>
            <RouterLink :to="{ name: 'stocks' }" class="text-sm text-blue-600 hover:text-blue-700 font-medium">
              Voir tout →
            </RouterLink>
          </div>
          <div class="overflow-x-auto">
            <table class="w-full">
              <thead>
                <tr class="border-b border-gray-100">
                  <th class="table-header pl-0">Type</th>
                  <th class="table-header">Produit</th>
                  <th class="table-header">Qté</th>
                  <th class="table-header">Entrepôt</th>
                  <th class="table-header">Date</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-50">
                <tr v-for="mvt in derniersMouvements" :key="mvt.id" class="hover:bg-gray-50 transition-colors">
                  <td class="table-cell pl-0">
                    <span :class="[
                      'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium',
                      mvt.type === 'ENTRÉE'    ? 'bg-green-100 text-green-700'  :
                      mvt.type === 'SORTIE'    ? 'bg-red-100 text-red-700'      :
                      'bg-blue-100 text-blue-700'
                    ]">{{ mvt.type }}</span>
                  </td>
                  <td class="table-cell font-medium text-gray-900">{{ mvt.produit }}</td>
                  <td class="table-cell">{{ mvt.quantite }}</td>
                  <td class="table-cell text-gray-500">{{ mvt.entrepot }}</td>
                  <td class="table-cell text-gray-400">{{ mvt.date }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Alertes (1/3) -->
        <div class="card">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-base font-semibold text-gray-900">Alertes</h3>
            <RouterLink :to="{ name: 'alertes' }" class="text-sm text-blue-600 hover:text-blue-700 font-medium">
              Voir tout →
            </RouterLink>
          </div>
          <div class="space-y-3">
            <div
              v-for="alerte in alertes"
              :key="alerte.id"
              :class="['flex items-start gap-3 p-3 rounded-lg', alerte.severity === 'danger' ? 'bg-red-50' : 'bg-yellow-50']"
            >
              <svg
                :class="['w-5 h-5 flex-shrink-0 mt-0.5', alerte.severity === 'danger' ? 'text-red-500' : 'text-yellow-500']"
                fill="none" stroke="currentColor" viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
              </svg>
              <p :class="['text-xs leading-snug', alerte.severity === 'danger' ? 'text-red-700' : 'text-yellow-700']">
                {{ alerte.message }}
              </p>
            </div>
          </div>
        </div>

      </div>
    </div>
  </AppLayout>
</template>
