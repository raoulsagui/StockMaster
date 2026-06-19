// ============================================================
// COMPOSABLE : usePermissions
//
// Source unique de vérité pour les permissions par rôle.
// Basé sur la matrice définie dans Rôles.md :
//
//   Action                        ADMIN  GEST  MAGA  AUDI
//   Gérer utilisateurs              ✅    ❌    ❌    ❌
//   Gérer entrepôts/zones           ✅    ✅    ❌    ❌
//   Gérer produits/catégories       ✅    ✅    ❌    ❌
//   Gérer fournisseurs              ✅    ✅    ❌    ❌
//   Créer entrées/sorties/transferts✅    ✅    ✅    ❌
//   Valider entrées/sorties         ✅    ✅    ❌    ❌
//   Créer/gérer commandes           ✅    ✅    ❌    ❌
//   Consulter stocks/mouvements     ✅    ✅    ✅    ✅
//   Consulter rapports/alertes      ✅    ✅    ✅    ✅
//   Configurer seuils stock         ✅    ✅    ❌    ❌
//
// Usage :
//   const { peutGererCatalogue, peutCreerMouvement } = usePermissions()
//   <button v-if="peutGererCatalogue">Nouveau produit</button>
// ============================================================

import { computed } from 'vue'
import { useAuthStore } from '@/stores/authStore'

export function usePermissions() {
  const authStore = useAuthStore()
  const role = computed(() => authStore.role)

  // -------------------------------------------------------
  // HELPERS DE BASE
  // -------------------------------------------------------

  /** ADMIN uniquement */
  const estAdmin = computed(() => role.value === 'ADMIN')

  /** ADMIN + GESTIONNAIRE */
  const estAdminOuGestionnaire = computed(() =>
    ['ADMIN', 'GESTIONNAIRE'].includes(role.value)
  )

  /** ADMIN + GESTIONNAIRE + MAGASINIER */
  const estPersonnelTerrain = computed(() =>
    ['ADMIN', 'GESTIONNAIRE', 'MAGASINIER'].includes(role.value)
  )

  // -------------------------------------------------------
  // PERMISSIONS MÉTIER
  // -------------------------------------------------------

  /** Gérer les utilisateurs (créer, modifier, désactiver, reset mdp) */
  const peutGererUtilisateurs = estAdmin

  /** Gérer entrepôts et zones (créer, modifier, activer/désactiver) */
  const peutGererEntrepots = estAdminOuGestionnaire

  /** Gérer produits et catégories */
  const peutGererCatalogue = estAdminOuGestionnaire

  /** Gérer fournisseurs */
  const peutGererFournisseurs = estAdminOuGestionnaire

  /** Configurer les seuils min/max du stock */
  const peutConfigurerSeuils = estAdminOuGestionnaire

  /** Créer des entrées, sorties, transferts */
  const peutCreerMouvement = estPersonnelTerrain

  /** Valider des entrées/sorties (ADMIN + GESTIONNAIRE) */
  const peutValiderMouvement = estAdminOuGestionnaire

  /** Gérer des commandes fournisseurs */
  const peutGererCommandes = estAdminOuGestionnaire

  /** Consulter stocks, mouvements, alertes, rapports (tous les rôles) */
  const peutConsulter = computed(() => !!role.value)

  return {
    role,
    estAdmin,
    estAdminOuGestionnaire,
    estPersonnelTerrain,
    peutGererUtilisateurs,
    peutGererEntrepots,
    peutGererCatalogue,
    peutGererFournisseurs,
    peutConfigurerSeuils,
    peutCreerMouvement,
    peutValiderMouvement,
    peutGererCommandes,
    peutConsulter,
  }
}
