package com.example.backend.module.entrepot.service;

import com.example.backend.module.entrepot.dto.EntrepotRequestDTO;
import com.example.backend.module.entrepot.dto.EntrepotResponseDTO;
import com.example.backend.module.entrepot.entity.Entrepot;
import com.example.backend.module.entrepot.repository.EntrepotRepository;
import com.example.backend.module.utilisateur.entity.Utilisateur;
import com.example.backend.module.utilisateur.repository.UtilisateurRepository;
import com.example.backend.module.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service du module Entrepôt.
 *
 * Contient toute la logique métier relative aux entrepôts :
 *   - Validation des règles (nom unique, capacité utilisée ≤ capacité totale…)
 *   - Conversion entité ↔ DTO
 *   - Gestion des transactions BDD
 *
 * Architecture respectée : Controller → Service → Repository.
 * Le service est le seul endroit où se prennent les décisions métier.
 */
@Service
@RequiredArgsConstructor
public class EntrepotService {

    private final EntrepotRepository entrepotRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ZoneRepository zoneRepository;

    /**
     * Récupère la liste de tous les entrepôts avec leur nombre de zones.
     *
     * @return liste des DTOs de réponse enrichis
     */
    public List<EntrepotResponseDTO> findAll() {
        return entrepotRepository.findAll()
                .stream()
                .map(e -> EntrepotResponseDTO.fromEntity(e,
                        zoneRepository.countByEntrepotId(e.getId())))
                .toList();
    }

    /**
     * Récupère un entrepôt par son identifiant.
     *
     * @param id Identifiant de l'entrepôt
     * @return DTO de réponse avec nombre de zones
     * @throws RuntimeException si l'entrepôt n'existe pas
     */
    public EntrepotResponseDTO findById(Long id) {
        Entrepot entrepot = findEntrepotOrThrow(id);
        long nombreZones  = zoneRepository.countByEntrepotId(id);
        return EntrepotResponseDTO.fromEntity(entrepot, nombreZones);
    }

    /**
     * Crée un nouvel entrepôt.
     *
     * Règles métier vérifiées :
     *   1. Le nom doit être unique dans le système
     *   2. La capacité utilisée ne peut pas dépasser la capacité totale
     *   3. Si un responsableId est fourni, l'utilisateur doit exister
     *
     * @param dto Les données de l'entrepôt à créer
     * @return DTO de l'entrepôt créé
     */
    @Transactional
    public EntrepotResponseDTO creer(EntrepotRequestDTO dto) {
        // Règle 1 : unicité du nom
        if (entrepotRepository.existsByNom(dto.getNom())) {
            throw new RuntimeException("Un entrepôt avec ce nom existe déjà");
        }

        // Règle 2 : cohérence des capacités
        double utilise = dto.getCapaciteUtilisee() != null ? dto.getCapaciteUtilisee() : 0.0;
        if (utilise > dto.getCapaciteTotale()) {
            throw new RuntimeException(
                "La capacité utilisée (" + utilise + " m²) ne peut pas dépasser "
                + "la capacité totale (" + dto.getCapaciteTotale() + " m²)"
            );
        }

        // Règle 3 : résolution du responsable (optionnel)
        Utilisateur responsable = resolveResponsable(dto.getResponsableId());

        Entrepot entrepot = Entrepot.builder()
                .nom(dto.getNom())
                .adresse(dto.getAdresse())
                .capaciteTotale(dto.getCapaciteTotale())
                .capaciteUtilisee(utilise)
                .responsable(responsable)
                .actif(true)
                .build();

        Entrepot saved = entrepotRepository.save(entrepot);
        return EntrepotResponseDTO.fromEntity(saved, 0L);
    }

    /**
     * Modifie un entrepôt existant.
     *
     * Règles métier vérifiées :
     *   1. L'entrepôt doit exister
     *   2. Si le nom change, le nouveau nom ne doit pas être pris par un autre entrepôt
     *   3. La capacité utilisée ne peut pas dépasser la nouvelle capacité totale
     *   4. La nouvelle capacité totale ne peut pas être inférieure à la capacité utilisée actuelle
     *
     * @param id  Identifiant de l'entrepôt à modifier
     * @param dto Nouvelles données
     * @return DTO de l'entrepôt mis à jour
     */
    @Transactional
    public EntrepotResponseDTO modifier(Long id, EntrepotRequestDTO dto) {
        Entrepot entrepot = findEntrepotOrThrow(id);

        // Règle 2 : si le nom change, vérifier qu'il n'est pas pris
        if (!entrepot.getNom().equals(dto.getNom())
                && entrepotRepository.existsByNomAndIdNot(dto.getNom(), id)) {
            throw new RuntimeException("Un autre entrepôt porte déjà ce nom");
        }

        // Règle 3 & 4 : cohérence des capacités
        double utilise = dto.getCapaciteUtilisee() != null
                ? dto.getCapaciteUtilisee()
                : entrepot.getCapaciteUtilisee();

        if (utilise > dto.getCapaciteTotale()) {
            throw new RuntimeException(
                "La capacité utilisée (" + utilise + " m²) ne peut pas dépasser "
                + "la nouvelle capacité totale (" + dto.getCapaciteTotale() + " m²)"
            );
        }

        // Résolution du responsable
        Utilisateur responsable = resolveResponsable(dto.getResponsableId());

        // Mise à jour des champs
        entrepot.setNom(dto.getNom());
        entrepot.setAdresse(dto.getAdresse());
        entrepot.setCapaciteTotale(dto.getCapaciteTotale());
        entrepot.setCapaciteUtilisee(utilise);
        entrepot.setResponsable(responsable);

        Entrepot saved = entrepotRepository.save(entrepot);
        long nombreZones = zoneRepository.countByEntrepotId(id);
        return EntrepotResponseDTO.fromEntity(saved, nombreZones);
    }

    /**
     * Active ou désactive un entrepôt.
     *
     * Même pattern que UtilisateurService.toggleStatut().
     * Un entrepôt désactivé ne peut plus recevoir de nouvelles zones ou de stock.
     *
     * @param id Identifiant de l'entrepôt
     * @return DTO de l'entrepôt avec son nouveau statut
     */
    @Transactional
    public EntrepotResponseDTO toggleStatut(Long id) {
        Entrepot entrepot = findEntrepotOrThrow(id);

        // Inverse l'état actuel (actif ↔ inactif)
        entrepot.setActif(!entrepot.isActif());

        Entrepot saved = entrepotRepository.save(entrepot);
        long nombreZones = zoneRepository.countByEntrepotId(id);
        return EntrepotResponseDTO.fromEntity(saved, nombreZones);
    }

    /**
     * Récupère uniquement les entrepôts actifs.
     * Utilisé lors de la création d'une zone pour proposer les entrepôts disponibles.
     *
     * @return liste des entrepôts actifs
     */
    public List<EntrepotResponseDTO> findActifs() {
        return entrepotRepository.findByActifTrue()
                .stream()
                .map(e -> EntrepotResponseDTO.fromEntity(e,
                        zoneRepository.countByEntrepotId(e.getId())))
                .toList();
    }

    // -------------------------------------------------------
    // MÉTHODES PRIVÉES — Helpers internes du service
    // -------------------------------------------------------

    /**
     * Cherche un entrepôt par son ID ou lance une exception explicite.
     * Factorisé car utilisé dans plusieurs méthodes publiques.
     */
    private Entrepot findEntrepotOrThrow(Long id) {
        return entrepotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrepôt introuvable : id=" + id));
    }

    /**
     * Résout l'entité Utilisateur à partir d'un ID optionnel.
     * Retourne null si aucun ID n'est fourni.
     *
     * @param responsableId ID du responsable, peut être null
     * @return L'entité Utilisateur ou null
     */
    private Utilisateur resolveResponsable(Long responsableId) {
        if (responsableId == null) return null;
        return utilisateurRepository.findById(responsableId)
                .orElseThrow(() -> new RuntimeException(
                    "Responsable introuvable : id=" + responsableId
                ));
    }
}
