// ============================================================
// CONFIGURATION DU ROUTER — Vue Router 4
//
// Le router gère la navigation entre les pages.
// Chaque "route" associe une URL à un composant Vue.
//
// On utilise le "lazy loading" (import dynamique) pour
// toutes les vues sauf LoginView :
//   () => import('./views/...')
// Cela signifie que le fichier JS d'une vue n'est téléchargé
// par le navigateur QUE quand l'utilisateur navigue vers elle.
// → Performance améliorée au chargement initial.
//
// Structure des routes :
//   /login                     → LoginView (sans layout)
//   /                          → redirige vers /tableau-de-bord
//   /tableau-de-bord           → TableauDeBordView (avec AppLayout)
//   /utilisateurs              → UtilisateursView
//   /utilisateurs/creer        → UtilisateurFormView (création)
//   /utilisateurs/:id/modifier → UtilisateurFormView (modification)
//   … (autres modules à venir)
// ============================================================

import { createRouter, createWebHistory } from 'vue-router'

// LoginView importé directement (pas de lazy loading car c'est
// la première page chargée — on veut qu'elle soit instantanée)
import LoginView from '@/views/auth/LoginView.vue'

const router = createRouter({
  // createWebHistory utilise l'API History du navigateur.
  // Les URLs seront /tableau-de-bord et non /#/tableau-de-bord.
  // Important : le serveur doit renvoyer index.html pour toutes les routes.
  history: createWebHistory(import.meta.env.BASE_URL),

  routes: [
    // Route de connexion
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true },
    },

    // Changement de mot de passe obligatoire (première connexion / reset)
    // public: true car l'utilisateur est connecté mais doit changer son mdp
    // avant de pouvoir accéder au reste de l'application
    {
      path: '/changer-mot-de-passe',
      name: 'changer-mot-de-passe',
      component: () => import('@/views/auth/ChangerMotDePasseView.vue'),
      meta: { public: true },
    },

    // -------------------------------------------------------
    // REDIRECTION RACINE
    // Accéder à "/" redirige automatiquement vers le tableau de bord
    // -------------------------------------------------------
    {
      path: '/',
      redirect: { name: 'tableau-de-bord' },
    },

    // -------------------------------------------------------
    // ROUTES PROTÉGÉES (nécessitent d'être connecté)
    // Toutes ces routes utilisent AppLayout via un composant parent.
    // -------------------------------------------------------

    // Tableau de bord
    {
      path: '/tableau-de-bord',
      name: 'tableau-de-bord',
      component: () => import('@/views/tableau-de-bord/TableauDeBordView.vue'),
      meta: { title: 'Tableau de bord' },
    },

    // --- MODULE 1 : Utilisateurs ---
    {
      path: '/utilisateurs',
      name: 'utilisateurs',
      component: () => import('@/views/utilisateurs/UtilisateursView.vue'),
      meta: { title: 'Utilisateurs' },
    },

    // --- MODULE 2 : Entrepôts (placeholder) ---
    {
      path: '/entrepots',
      name: 'entrepots',
      component: () => import('@/views/entrepots/EntrepotsView.vue'),
      meta: { title: 'Entrepôts' },
    },

    // --- MODULE 3 : Zones ---
    {
      path: '/zones',
      name: 'zones',
      component: () => import('@/views/zones/ZonesView.vue'),
      meta: { title: 'Zones' },
    },

    // --- MODULE 4 : Produits ---
    {
      path: '/produits',
      name: 'produits',
      component: () => import('@/views/produits/ProduitsView.vue'),
      meta: { title: 'Produits' },
    },

    // --- MODULE 5 : Catégories ---
    {
      path: '/categories',
      name: 'categories',
      component: () => import('@/views/categories/CategoriesView.vue'),
      meta: { title: 'Catégories' },
    },

    // --- MODULE 6 : Fournisseurs ---
    {
      path: '/fournisseurs',
      name: 'fournisseurs',
      component: () => import('@/views/fournisseurs/FournisseursView.vue'),
      meta: { title: 'Fournisseurs' },
    },

    // --- MODULE 7 : Stocks ---
    {
      path: '/stocks',
      name: 'stocks',
      component: () => import('@/views/stocks/StocksView.vue'),
      meta: { title: 'Stocks' },
    },

    // --- MODULE 8 : Entrées ---
    {
      path: '/entrees',
      name: 'entrees',
      component: () => import('@/views/entrees/EntreesView.vue'),
      meta: { title: 'Entrées' },
    },

    // --- MODULE 9 : Sorties ---
    {
      path: '/sorties',
      name: 'sorties',
      component: () => import('@/views/sorties/SortiesView.vue'),
      meta: { title: 'Sorties' },
    },

    // --- MODULE 10 : Transferts ---
    {
      path: '/transferts',
      name: 'transferts',
      component: () => import('@/views/transferts/TransfertsView.vue'),
      meta: { title: 'Transferts' },
    },

    // --- MODULE 11 : Inventaires ---
    {
      path: '/inventaires',
      name: 'inventaires',
      component: () => import('@/views/inventaires/InventairesView.vue'),
      meta: { title: 'Inventaires' },
    },

    // --- MODULE 12 : Alertes ---
    {
      path: '/alertes',
      name: 'alertes',
      component: () => import('@/views/alertes/AlertesView.vue'),
      meta: { title: 'Alertes' },
    },

    // --- MODULE 14 : Rapports ---
    {
      path: '/rapports',
      name: 'rapports',
      component: () => import('@/views/rapports/RapportsView.vue'),
      meta: { title: 'Rapports' },
    },

    // --- Emplacements ---
    {
      path: '/emplacements',
      name: 'emplacements',
      component: () => import('@/views/emplacements/EmplacementsView.vue'),
      meta: { title: 'Emplacements' },
    },

    // --- Commandes ---
    {
      path: '/commandes',
      name: 'commandes',
      component: () => import('@/views/commandes/CommandesView.vue'),
      meta: { title: 'Commandes' },
    },
  ],
})

// -------------------------------------------------------
// GUARD DE NAVIGATION GLOBAL
//
// S'exécute AVANT chaque changement de route.
// Utilise authStore (Pinia) pour vérifier si l'utilisateur
// est connecté (token présent + valide).
// -------------------------------------------------------
router.beforeEach((to) => {
  const token          = localStorage.getItem('token')
  const isAuthenticated = !!token

  // Récupère les infos utilisateur stockées après login
  const utilisateurJson = localStorage.getItem('utilisateur')
  const utilisateur     = utilisateurJson ? JSON.parse(utilisateurJson) : null

  // Cas 1 : route protégée + non connecté → login
  if (!to.meta.public && !isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  // Cas 2 : connecté mais doit changer son mot de passe
  // On le bloque sur la page de changement sauf s'il y est déjà
  if (
    isAuthenticated &&
    utilisateur?.doitChangerMotDePasse &&
    to.name !== 'changer-mot-de-passe' &&
    to.name !== 'login'
  ) {
    return { name: 'changer-mot-de-passe' }
  }

  // Cas 3 : déjà connecté + tente d'aller au login → dashboard
  if (to.name === 'login' && isAuthenticated) {
    return { name: 'tableau-de-bord' }
  }

  return true
})

export default router
