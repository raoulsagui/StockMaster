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

@Service
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final EntrepotRepository entrepotRepository;

    public List<ZoneResponseDTO> findAll() {
        return zoneRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(ZoneResponseDTO::fromEntity)
                .toList();
    }

    public List<ZoneResponseDTO> findByEntrepot(Long entrepotId) {
        findEntrepotOrThrow(entrepotId);
        return zoneRepository.findByEntrepotId(entrepotId)
                .stream()
                .map(ZoneResponseDTO::fromEntity)
                .toList();
    }

    public ZoneResponseDTO findById(Long id) {
        return ZoneResponseDTO.fromEntity(findZoneOrThrow(id));
    }

    @Transactional
    public ZoneResponseDTO creer(ZoneRequestDTO dto) {
        Entrepot entrepot = findEntrepotOrThrow(dto.getEntrepotId());

        if (!entrepot.isActif()) {
            throw new RuntimeException(
                "Impossible de créer une zone dans l'entrepôt \"" + entrepot.getNom() + "\" car il est désactivé"
            );
        }

        if (zoneRepository.existsByNomAndEntrepotId(dto.getNom(), dto.getEntrepotId())) {
            throw new RuntimeException(
                "Une zone nommée \"" + dto.getNom() + "\" existe déjà dans cet entrepôt"
            );
        }

        // Vérification : somme des capacités des zones + nouvelle capacité <= capacité totale entrepôt
        double dejaAlloue = zoneRepository.sumCapaciteByEntrepotId(dto.getEntrepotId());
        double disponible = entrepot.getCapaciteTotale() - dejaAlloue;
        if (dto.getCapacite() > disponible) {
            throw new RuntimeException(
                "La capacité de la zone (" + dto.getCapacite() + " m³) dépasse "
                + "la capacité disponible de l'entrepôt (" + disponible + " m³ restants)"
            );
        }

        Zone zone = Zone.builder()
                .nom(dto.getNom())
                .type(dto.getType())
                .description(dto.getDescription())
                .entrepot(entrepot)
                .capacite(dto.getCapacite())
                .actif(true)
                .build();

        zoneRepository.save(zone);
        recalculerCapaciteEntrepot(entrepot);
        return ZoneResponseDTO.fromEntity(zone);
    }

    @Transactional
    public ZoneResponseDTO modifier(Long id, ZoneRequestDTO dto) {
        Zone zone = findZoneOrThrow(id);
        Entrepot entrepot = findEntrepotOrThrow(dto.getEntrepotId());

        if (!entrepot.isActif()) {
            throw new RuntimeException("L'entrepôt cible \"" + entrepot.getNom() + "\" est désactivé");
        }

        if (!zone.getNom().equals(dto.getNom())
                && zoneRepository.existsByNomAndEntrepotIdAndIdNot(dto.getNom(), dto.getEntrepotId(), id)) {
            throw new RuntimeException(
                "Une zone nommée \"" + dto.getNom() + "\" existe déjà dans cet entrepôt"
            );
        }

        // Vérification capacité disponible (en excluant la zone actuelle)
        double dejaAlloue = zoneRepository.sumCapaciteByEntrepotIdExcluding(dto.getEntrepotId(), id);
        double disponible = entrepot.getCapaciteTotale() - dejaAlloue;
        if (dto.getCapacite() > disponible) {
            throw new RuntimeException(
                "La capacité de la zone (" + dto.getCapacite() + " m³) dépasse "
                + "la capacité disponible de l'entrepôt (" + disponible + " m³ restants)"
            );
        }

        Long ancienEntrepotId = zone.getEntrepot().getId();
        zone.setNom(dto.getNom());
        zone.setType(dto.getType());
        zone.setDescription(dto.getDescription());
        zone.setEntrepot(entrepot);
        zone.setCapacite(dto.getCapacite());

        zoneRepository.save(zone);

        // Si changement d'entrepôt, recalculer les deux
        if (!ancienEntrepotId.equals(dto.getEntrepotId())) {
            recalculerCapaciteEntrepot(findEntrepotOrThrow(ancienEntrepotId));
        }
        recalculerCapaciteEntrepot(entrepot);
        return ZoneResponseDTO.fromEntity(zone);
    }

    @Transactional
    public ZoneResponseDTO toggleStatut(Long id) {
        Zone zone = findZoneOrThrow(id);
        zone.setActif(!zone.isActif());
        zoneRepository.save(zone);
        recalculerCapaciteEntrepot(zone.getEntrepot());
        return ZoneResponseDTO.fromEntity(zone);
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

    private void recalculerCapaciteEntrepot(Entrepot entrepot) {
        Double somme = zoneRepository.sumCapaciteByEntrepotId(entrepot.getId());
        entrepot.setCapaciteUtilisee(somme != null ? somme : 0.0);
        entrepotRepository.save(entrepot);
    }
}
