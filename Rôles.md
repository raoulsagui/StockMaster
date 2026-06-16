Oui exactement, c'est à toi de le définir. Le CDC liste juste les 4 rôles :

Administrateur

Gestionnaire d'entrepôt

Magasinier

Auditeur

C'est un choix de conception. Voilà une proposition logique basée sur les responsabilités métier :

Action	ADMIN	GESTIONNAIRE	MAGASINIER	AUDITEUR
Gérer les utilisateurs	✅	❌	❌	❌
Gérer les entrepôts/zones	✅	✅	❌	❌
Gérer les produits/catégories	✅	✅	❌	❌
Gérer les fournisseurs	✅	✅	❌	❌
Créer entrées/sorties/transferts	✅	✅	✅	❌
Valider entrées/sorties	✅	✅	❌	❌
Créer/gérer commandes	✅	✅	❌	❌
Faire un inventaire	✅	✅	✅	❌
Consulter stocks/mouvements	✅	✅	✅	✅
Consulter rapports/alertes	✅	✅	✅	✅
L'Auditeur est en lecture seule — il consulte sans jamais modifier. Le Magasinier fait les opérations terrain (entrées, sorties, inventaires) mais ne configure rien. Le Gestionnaire gère tout sauf les utilisateurs.