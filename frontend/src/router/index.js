import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/auth/LoginView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),

  routes: [
    // -------------------------------------------------------
    // ROUTES PUBLIQUES
    // -------------------------------------------------------
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true },
    },
    {
      path: '/changer-mot-de-passe',
      name: 'changer-mot-de-passe',
      component: () => import('@/views/auth/ChangerMotDePasseView.vue'),
      meta: { public: true },
    },
    {
      path: '/non-autorise',
      name: 'non-autorise',
      component: () => import('@/views/auth/NonAutoriseView.vue'),
      meta: { public: true },
    },

    // Redirection racine
    {
      path: '/',
      redirect: { name: 'tableau-de-bord' },
    },

    // -------------------------------------------------------
    // TABLEAU DE BORD — tous les rôles
    // -------------------------------------------------------
    {
      path: '/tableau-de-bord',
      name: 'tableau-de-bord',
      component: () => import('@/views/tableau-de-bord/TableauDeBordView.vue'),
      meta: { title: 'Tableau de bord' },
    },

    // -------------------------------------------------------
    // MODULE 1 : Utilisateurs — ADMIN uniquement
    // -------------------------------------------------------
    {
      path: '/utilisateurs',
      name: 'utilisateurs',
      component: () => import('@/views/utilisateurs/UtilisateursView.vue'),
      meta: { title: 'Utilisateurs', roles: ['ADMIN'] },
    },

    // Profil — tous les rôles
    {
      path: '/profil',
      name: 'profil',
      component: () => import('@/views/profil/ProfilView.vue'),
      meta: { title: 'Mon profil' },
    },

    // -------------------------------------------------------
    // MODULE 2 : Entrepôts — ADMIN + GESTIONNAIRE
    // -------------------------------------------------------
    {
      path: '/entrepots',
      name: 'entrepots',
      component: () => import('@/views/entrepots/EntrepotsView.vue'),
      meta: { title: 'Entrepôts', roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER', 'AUDITEUR'] },
    },
    {
      path: '/entrepots/creer',
      name: 'entrepots-creer',
      component: () => import('@/views/entrepots/EntrepotFormView.vue'),
      meta: { title: 'Nouvel entrepôt', roles: ['ADMIN', 'GESTIONNAIRE'] },
    },
    {
      path: '/entrepots/:id/modifier',
      name: 'entrepots-modifier',
      component: () => import('@/views/entrepots/EntrepotFormView.vue'),
      props: true,
      meta: { title: 'Modifier un entrepôt', roles: ['ADMIN', 'GESTIONNAIRE'] },
    },

    // -------------------------------------------------------
    // MODULE 3 : Zones — ADMIN + GESTIONNAIRE
    // -------------------------------------------------------
    {
      path: '/zones',
      name: 'zones',
      component: () => import('@/views/zones/ZonesView.vue'),
      meta: { title: 'Zones de stockage', roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER', 'AUDITEUR'] },
    },
    {
      path: '/zones/creer',
      name: 'zones-creer',
      component: () => import('@/views/zones/ZoneFormView.vue'),
      meta: { title: 'Nouvelle zone', roles: ['ADMIN', 'GESTIONNAIRE'] },
    },
    {
      path: '/zones/:id/modifier',
      name: 'zones-modifier',
      component: () => import('@/views/zones/ZoneFormView.vue'),
      props: true,
      meta: { title: 'Modifier une zone', roles: ['ADMIN', 'GESTIONNAIRE'] },
    },

    // -------------------------------------------------------
    // MODULE 4 : Produits — ADMIN + GESTIONNAIRE
    // -------------------------------------------------------
    {
      path: '/produits',
      name: 'produits',
      component: () => import('@/views/produits/ProduitsView.vue'),
      meta: { title: 'Produits', roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER', 'AUDITEUR'] },
    },

    // -------------------------------------------------------
    // MODULE 5 : Catégories — ADMIN + GESTIONNAIRE
    // -------------------------------------------------------
    {
      path: '/categories',
      name: 'categories',
      component: () => import('@/views/categories/CategoriesView.vue'),
      meta: { title: 'Catégories', roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER', 'AUDITEUR'] },
    },

    // -------------------------------------------------------
    // MODULE 6 : Fournisseurs — ADMIN + GESTIONNAIRE
    // -------------------------------------------------------
    {
      path: '/fournisseurs',
      name: 'fournisseurs',
      component: () => import('@/views/fournisseurs/FournisseursView.vue'),
      meta: { title: 'Fournisseurs', roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER', 'AUDITEUR'] },
    },

    // -------------------------------------------------------
    // MODULE 7 : Stocks — tous les rôles
    // -------------------------------------------------------
    {
      path: '/stocks',
      name: 'stocks',
      component: () => import('@/views/stocks/StocksView.vue'),
      meta: { title: 'Stocks' },
    },

    // -------------------------------------------------------
    // MODULE 8 : Entrées — ADMIN + GESTIONNAIRE + MAGASINIER
    // -------------------------------------------------------
    {
      path: '/entrees',
      name: 'entrees',
      component: () => import('@/views/entrees/EntreesView.vue'),
      meta: { title: 'Entrées', roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER'] },
    },

    // -------------------------------------------------------
    // MODULE 9 : Sorties — ADMIN + GESTIONNAIRE + MAGASINIER
    // -------------------------------------------------------
    {
      path: '/sorties',
      name: 'sorties',
      component: () => import('@/views/sorties/SortiesView.vue'),
      meta: { title: 'Sorties', roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER'] },
    },

    // -------------------------------------------------------
    // MODULE 10 : Transferts — ADMIN + GESTIONNAIRE + MAGASINIER
    // -------------------------------------------------------
    {
      path: '/transferts',
      name: 'transferts',
      component: () => import('@/views/transferts/TransfertsView.vue'),
      meta: { title: 'Transferts', roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER'] },
    },

    // -------------------------------------------------------
    // MODULE 11 : Inventaires — ADMIN + GESTIONNAIRE + MAGASINIER
    // -------------------------------------------------------
    {
      path: '/inventaires',
      name: 'inventaires',
      component: () => import('@/views/inventaires/InventairesView.vue'),
      meta: { title: 'Inventaires', roles: ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER'] },
    },

    // -------------------------------------------------------
    // MODULE 12 : Alertes — tous les rôles
    // -------------------------------------------------------
    {
      path: '/alertes',
      name: 'alertes',
      component: () => import('@/views/alertes/AlertesView.vue'),
      meta: { title: 'Alertes' },
    },

    // -------------------------------------------------------
    // MODULE 14 : Rapports — tous les rôles
    // -------------------------------------------------------
    {
      path: '/rapports',
      name: 'rapports',
      component: () => import('@/views/rapports/RapportsView.vue'),
      meta: { title: 'Rapports' },
    },

    // -------------------------------------------------------
    // Emplacements — ADMIN + GESTIONNAIRE
    // -------------------------------------------------------
    {
      path: '/emplacements',
      name: 'emplacements',
      component: () => import('@/views/emplacements/EmplacementsView.vue'),
      meta: { title: 'Emplacements', roles: ['ADMIN', 'GESTIONNAIRE'] },
    },

    // -------------------------------------------------------
    // Commandes — ADMIN + GESTIONNAIRE
    // -------------------------------------------------------
    {
      path: '/commandes',
      name: 'commandes',
      component: () => import('@/views/commandes/CommandesView.vue'),
      meta: { title: 'Commandes', roles: ['ADMIN', 'GESTIONNAIRE'] },
    },
  ],
})

// -------------------------------------------------------
// GUARD DE NAVIGATION GLOBAL
// -------------------------------------------------------
router.beforeEach((to) => {
  const token           = localStorage.getItem('token')
  const isAuthenticated = !!token
  const utilisateurJson = localStorage.getItem('utilisateur')
  const utilisateur     = utilisateurJson ? JSON.parse(utilisateurJson) : null
  const role            = utilisateur?.role || null

  // Cas 1 : route protégée + non connecté → login
  if (!to.meta.public && !isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  // Cas 2 : connecté mais doit changer son mot de passe
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

  // Cas 4 : vérification des rôles
  // Si la route a des rôles définis et que l'utilisateur n'en a pas un → 403
  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!role || !to.meta.roles.includes(role)) {
      return { name: 'non-autorise' }
    }
  }

  return true
})

export default router
