package com.example.backend.module.zone.service;

import com.example.backend.module.entrepot.entity.Entrepot;
import com.example.backend.module.entrepot.repository.EntrepotRepository;
import com.example.backend.module.zone.dto.ZoneRequestDTO;
import com.example.backend.module.zone.dto.ZoneResponseDTO;
import com.example.backend.module.zone.entity.Zone;
import com.example.backend.module.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service du module Zone de stockage.
 *
 * Contient la logique métier des zones :
 *   - Vérification que la zone appartient à un entrepôt actif
 *   - Unicité du nom au sein d'un même entrepôt
 *
 * Les capacités (totale/utilisée) sont portées par l'entrepôt.
 * La mise à jour lors des mouvements de stock sera gérée dans le module mouvements.
 */
@Service
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final EntrepotRepository entrepotRepository;

    /**
     * Récupère toutes les zones du système.
     *
     * @return liste de tous les DTOs de zones
     */
    public List<ZoneResponseDTO> findAll() {
        return zoneRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(ZoneResponseDTO::fromEntity)
                .toList();
    }

    /**
     * Récupère toutes les zones d'un entrepôt donné.
     *
     * @param entrepotId L'id de l'entrepôt
     * @return liste des zones de l'entrepôt
     * @throws RuntimeException si l'entrepôt n'existe pas
     */
    public List<ZoneResponseDTO> findByEntrepot(Long entrepotId) {
        findEntrepotOrThrow(entrepotId);
        return zoneRepository.findByEntrepotId(entrepotId)
                .stream()
                .map(ZoneResponseDTO::fromEntity)
                .toList();
    }

    /**
     * Récupère une zone par son identifiant.
     *
     * @param id Identifiant de la zone
     * @return DTO de la zone
     * @throws RuntimeException si la zone n'existe pas
     */
    public ZoneResponseDTO findById(Long id) {
        Zone zone = findZoneOrThrow(id);
        return ZoneResponseDTO.fromEntity(zone);
    }

    /**
     * Crée une nouvelle zone dans un entrepôt.
     *
     * Règles métier vérifiées :
     *   1. L'entrepôt doit exister et être actif
     *   2. Le nom de la zone doit être unique dans cet entrepôt
     *
     * @param dto Les données de la zone à créer
     * @return DTO de la zone créée
     */
    @Transactional
    public ZoneResponseDTO creer(ZoneRequestDTO dto) {
        Entrepot entrepot = findEntrepotOrThrow(dto.getEntrepotId());

        // Règle 1 : l'entrepôt doit être actif
        if (!entrepot.isActif()) {
            throw new RuntimeException(
                "Impossible de créer une zone dans l'entrepôt \"" + entrepot.getNom()
                + "\" car il est actuellement désactivé"
            );
        }

        // Règle 2 : unicité du nom au sein de l'entrepôt
        if (zoneRepository.existsByNomAndEntrepotId(dto.getNom(), dto.getEntrepotId())) {
            throw new RuntimeException(
                "Une zone nommée \"" + dto.getNom() + "\" existe déjà dans cet entrepôt"
            );
        }

        // Règle 3 : cohérence capaciteUtilisee <= capaciteTotale (si les deux sont fournis)
        if (dto.getCapaciteTotale() != null && dto.getCapaciteUtilisee() != null
                && dto.getCapaciteUtilisee() > dto.getCapaciteTotale()) {
            throw new RuntimeException(
                "La capacité utilisée ne peut pas dépasser la capacité totale"
            );
        }

        // Règle 4 : la capacité de la zone ne dépasse pas la capacité disponible de l'entrepôt
        if (dto.getCapaciteTotale() != null) {
            double dejaAlloue = zoneRepository.sumCapaciteTotaleByEntrepotId(dto.getEntrepotId());
            double disponible = entrepot.getCapaciteTotale() - dejaAlloue;
            if (dto.getCapaciteTotale() > disponible) {
                throw new RuntimeException(
                    "La capacité de la zone (" + dto.getCapaciteTotale() + " m³) dépasse "
                    + "la capacité disponible de l'entrepôt (" + disponible + " m³ restants sur "
                    + entrepot.getCapaciteTotale() + " m³ au total)"
                );
            }
        }

        Zone zone = Zone.builder()
                .nom(dto.getNom())
                .type(dto.getType())
                .description(dto.getDescription())
                .entrepot(entrepot)
                .capaciteTotale(dto.getCapaciteTotale())
                .capaciteUtilisee(dto.getCapaciteUtilisee() != null ? dto.getCapaciteUtilisee() : 0.0)
                .actif(true)
                .build();

        Zone saved = zoneRepository.save(zone);
        return ZoneResponseDTO.fromEntity(saved);
    }

    /**
     * Modifie une zone existante.
     *
     * Règles métier vérifiées :
     *   1. La zone doit exister
     *   2. Si le nom change, le nouveau nom ne doit pas être pris dans le même entrepôt
     *   3. Si l'entrepôt change, le nouvel entrepôt doit exister et être actif
     *
     * @param id  Identifiant de la zone à modifier
     * @param dto Nouvelles données
     * @return DTO de la zone mise à jour
     */
    @Transactional
    public ZoneResponseDTO modifier(Long id, ZoneRequestDTO dto) {
        Zone zone = findZoneOrThrow(id);
        Entrepot entrepot = findEntrepotOrThrow(dto.getEntrepotId());

        // Règle 3 : si on change d'entrepôt, le nouvel entrepôt doit être actif
        if (!entrepot.isActif()) {
            throw new RuntimeException(
                "L'entrepôt cible \"" + entrepot.getNom() + "\" est désactivé"
            );
        }

        // Règle 2 : unicité du nom dans l'entrepôt (en excluant la zone actuelle)
        if (!zone.getNom().equals(dto.getNom())
                && zoneRepository.existsByNomAndEntrepotIdAndIdNot(
                        dto.getNom(), dto.getEntrepotId(), id)) {
            throw new RuntimeException(
                "Une zone nommée \"" + dto.getNom() + "\" existe déjà dans cet entrepôt"
            );
        }

        // Règle 4 : cohérence capaciteUtilisee <= capaciteTotale
        if (dto.getCapaciteTotale() != null && dto.getCapaciteUtilisee() != null
                && dto.getCapaciteUtilisee() > dto.getCapaciteTotale()) {
            throw new RuntimeException(
                "La capacité utilisée ne peut pas dépasser la capacité totale"
            );
        }

        // Règle 5 : la capacité de la zone ne dépasse pas la capacité disponible de l'entrepôt
        if (dto.getCapaciteTotale() != null) {
            double dejaAlloue = zoneRepository.sumCapaciteTotaleByEntrepotIdExcluding(dto.getEntrepotId(), id);
            double disponible = entrepot.getCapaciteTotale() - dejaAlloue;
            if (dto.getCapaciteTotale() > disponible) {
                throw new RuntimeException(
                    "La capacité de la zone (" + dto.getCapaciteTotale() + " m³) dépasse "
                    + "la capacité disponible de l'entrepôt (" + disponible + " m³ restants sur "
                    + entrepot.getCapaciteTotale() + " m³ au total)"
                );
            }
        }

        zone.setNom(dto.getNom());
        zone.setType(dto.getType());
        zone.setDescription(dto.getDescription());
        zone.setEntrepot(entrepot);
        zone.setCapaciteTotale(dto.getCapaciteTotale());
        if (dto.getCapaciteUtilisee() != null) {
            zone.setCapaciteUtilisee(dto.getCapaciteUtilisee());
        }

        Zone saved = zoneRepository.save(zone);
        return ZoneResponseDTO.fromEntity(saved);
    }

    /**
     * Active ou désactive une zone.
     *
     * Une zone désactivée n'accepte plus de nouveau stock
     * mais conserve son historique de mouvements.
     *
     * @param id Identifiant de la zone
     * @return DTO de la zone avec son nouveau statut
     */
    @Transactional
    public ZoneResponseDTO toggleStatut(Long id) {
        Zone zone = findZoneOrThrow(id);
        zone.setActif(!zone.isActif());
        Zone saved = zoneRepository.save(zone);
        return ZoneResponseDTO.fromEntity(saved);
    }

    // -------------------------------------------------------
    // MÉTHODES PRIVÉES
    // -------------------------------------------------------

    private Zone findZoneOrThrow(Long id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone introuvable : id=" + id));
    }

    private Entrepot findEntrepotOrThrow(Long id) {
        return entrepotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrepôt introuvable : id=" + id));
    }
}
