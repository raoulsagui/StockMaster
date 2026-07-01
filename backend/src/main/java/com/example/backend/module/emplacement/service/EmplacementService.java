package com.example.backend.module.emplacement.service;

import com.example.backend.module.emplacement.dto.*;
import com.example.backend.module.emplacement.entity.*;
import com.example.backend.module.emplacement.repository.*;
import com.example.backend.module.zone.entity.Zone;
import com.example.backend.module.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service de la hiérarchie Rayon → Étagère → Emplacement.
 *
 * Règles métier :
 *   - Code unique par niveau (rayon dans une zone, étagère dans un rayon, etc.)
 *   - On ne peut pas créer un rayon dans une zone inactive
 *   - L'adresseComplete est calculée au moment du save
 *   - Suppression en cascade gérée par JPA (orphanRemoval)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmplacementService {

    private final RayonRepository       rayonRepository;
    private final EtagereRepository     etagereRepository;
    private final EmplacementRepository emplacementRepository;
    private final ZoneRepository        zoneRepository;

    // ================================================================
    // RAYONS
    // ================================================================

    public List<RayonResponseDTO> findRayonsByZone(Long zoneId) {
        return rayonRepository.findByZoneIdOrderByCode(zoneId)
                .stream()
                .map(r -> RayonResponseDTO.fromEntity(r, etagereRepository.countByRayonId(r.getId())))
                .toList();
    }

    public RayonResponseDTO findRayonById(Long id) {
        Rayon r = findRayonOrThrow(id);
        return RayonResponseDTO.fromEntity(r, etagereRepository.countByRayonId(id));
    }

    @Transactional
    public RayonResponseDTO creerRayon(RayonRequestDTO dto) {
        Zone zone = zoneRepository.findById(dto.getZoneId())
                .orElseThrow(() -> new RuntimeException("Zone introuvable : id=" + dto.getZoneId()));

        if (!zone.isActif())
            throw new RuntimeException("Impossible de créer un rayon dans une zone inactive.");

        if (rayonRepository.existsByCodeAndZoneId(dto.getCode(), dto.getZoneId()))
            throw new RuntimeException(
                "Un rayon avec le code \"" + dto.getCode() + "\" existe déjà dans cette zone.");

        Rayon rayon = Rayon.builder()
                .code(dto.getCode())
                .libelle(dto.getLibelle())
                .zone(zone)
                .build();

        rayon = rayonRepository.save(rayon);
        return RayonResponseDTO.fromEntity(rayon, 0L);
    }

    @Transactional
    public RayonResponseDTO modifierRayon(Long id, RayonRequestDTO dto) {
        Rayon rayon = findRayonOrThrow(id);

        if (!rayon.getCode().equals(dto.getCode())
                && rayonRepository.existsByCodeAndZoneIdAndIdNot(dto.getCode(), rayon.getZone().getId(), id))
            throw new RuntimeException("Un autre rayon porte déjà ce code dans cette zone.");

        rayon.setCode(dto.getCode());
        rayon.setLibelle(dto.getLibelle());

        rayon = rayonRepository.save(rayon);
        return RayonResponseDTO.fromEntity(rayon, etagereRepository.countByRayonId(id));
    }

    @Transactional
    public RayonResponseDTO toggleRayonStatut(Long id) {
        Rayon rayon = findRayonOrThrow(id);
        rayon.setActif(!rayon.isActif());
        rayon = rayonRepository.save(rayon);
        return RayonResponseDTO.fromEntity(rayon, etagereRepository.countByRayonId(id));
    }

    // ================================================================
    // ÉTAGÈRES
    // ================================================================

    public List<EtagereResponseDTO> findEtageresByRayon(Long rayonId) {
        return etagereRepository.findByRayonIdOrderByCode(rayonId)
                .stream()
                .map(e -> EtagereResponseDTO.fromEntity(e, emplacementRepository.countByEtagereId(e.getId())))
                .toList();
    }

    public EtagereResponseDTO findEtagereById(Long id) {
        Etagere e = findEtagereOrThrow(id);
        return EtagereResponseDTO.fromEntity(e, emplacementRepository.countByEtagereId(id));
    }

    @Transactional
    public EtagereResponseDTO creerEtagere(EtagereRequestDTO dto) {
        Rayon rayon = findRayonOrThrow(dto.getRayonId());

        if (!rayon.isActif())
            throw new RuntimeException("Impossible de créer une étagère dans un rayon inactif.");

        if (etagereRepository.existsByCodeAndRayonId(dto.getCode(), dto.getRayonId()))
            throw new RuntimeException(
                "Une étagère avec le code \"" + dto.getCode() + "\" existe déjà dans ce rayon.");

        Etagere etagere = Etagere.builder()
                .code(dto.getCode())
                .libelle(dto.getLibelle())
                .niveaux(dto.getNiveaux())
                .rayon(rayon)
                .build();

        etagere = etagereRepository.save(etagere);
        return EtagereResponseDTO.fromEntity(etagere, 0L);
    }

    @Transactional
    public EtagereResponseDTO modifierEtagere(Long id, EtagereRequestDTO dto) {
        Etagere etagere = findEtagereOrThrow(id);

        if (!etagere.getCode().equals(dto.getCode())
                && etagereRepository.existsByCodeAndRayonIdAndIdNot(dto.getCode(), etagere.getRayon().getId(), id))
            throw new RuntimeException("Une autre étagère porte déjà ce code dans ce rayon.");

        etagere.setCode(dto.getCode());
        etagere.setLibelle(dto.getLibelle());
        etagere.setNiveaux(dto.getNiveaux());

        etagere = etagereRepository.save(etagere);
        return EtagereResponseDTO.fromEntity(etagere, emplacementRepository.countByEtagereId(id));
    }

    @Transactional
    public EtagereResponseDTO toggleEtagereStatut(Long id) {
        Etagere etagere = findEtagereOrThrow(id);
        etagere.setActif(!etagere.isActif());
        etagere = etagereRepository.save(etagere);
        return EtagereResponseDTO.fromEntity(etagere, emplacementRepository.countByEtagereId(id));
    }

    // ================================================================
    // EMPLACEMENTS
    // ================================================================

    public List<EmplacementResponseDTO> findEmplacementsByEtagere(Long etagereId) {
        return emplacementRepository.findByEtagereIdOrderByCode(etagereId)
                .stream().map(EmplacementResponseDTO::fromEntity).toList();
    }

    public List<EmplacementResponseDTO> findEmplacementsByEntrepot(Long entrepotId) {
        return emplacementRepository.findByEntrepotId(entrepotId)
                .stream().map(EmplacementResponseDTO::fromEntity).toList();
    }

    public List<EmplacementResponseDTO> findEmplacementsByZone(Long zoneId) {
        return emplacementRepository.findByZoneId(zoneId)
                .stream().map(EmplacementResponseDTO::fromEntity).toList();
    }

    public List<EmplacementResponseDTO> findEmplacementsLibres(Long entrepotId) {
        return emplacementRepository.findLibresByEntrepotId(entrepotId)
                .stream().map(EmplacementResponseDTO::fromEntity).toList();
    }

    public EmplacementResponseDTO findEmplacementById(Long id) {
        return EmplacementResponseDTO.fromEntity(findEmplacementOrThrow(id));
    }

    public Map<String, Object> getStatsEntrepot(Long entrepotId) {
        List<Object[]> rows = emplacementRepository.countByStatutForEntrepot(entrepotId);
        Map<String, Long> parStatut = new HashMap<>();
        long total = 0;
        for (Object[] row : rows) {
            String statut = row[0].toString();
            long count    = (Long) row[1];
            parStatut.put(statut, count);
            total += count;
        }
        long libres  = parStatut.getOrDefault("LIBRE", 0L);
        long occupes = parStatut.getOrDefault("OCCUPE", 0L);
        double tauxOccupation = total > 0 ? (double) occupes / total * 100 : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("total",          total);
        stats.put("parStatut",      parStatut);
        stats.put("tauxOccupation", Math.round(tauxOccupation * 10.0) / 10.0);
        stats.put("libres",         libres);
        return stats;
    }

    @Transactional
    public EmplacementResponseDTO creerEmplacement(EmplacementRequestDTO dto) {
        Etagere etagere = findEtagereOrThrow(dto.getEtagereId());

        if (!etagere.isActif())
            throw new RuntimeException("Impossible de créer un emplacement sur une étagère inactive.");

        if (emplacementRepository.existsByCodeAndEtagereId(dto.getCode(), dto.getEtagereId()))
            throw new RuntimeException(
                "Un emplacement avec le code \"" + dto.getCode() + "\" existe déjà sur cette étagère.");

        Emplacement emp = Emplacement.builder()
                .code(dto.getCode())
                .type(dto.getType())
                .capaciteMax(dto.getCapaciteMax())
                .description(dto.getDescription())
                .etagere(etagere)
                .build();

        // L'adresseComplete est construite dans le @PrePersist
        emp = emplacementRepository.save(emp);
        return EmplacementResponseDTO.fromEntity(emp);
    }

    @Transactional
    public EmplacementResponseDTO modifierEmplacement(Long id, EmplacementRequestDTO dto) {
        Emplacement emp = findEmplacementOrThrow(id);

        if (!emp.getCode().equals(dto.getCode())
                && emplacementRepository.existsByCodeAndEtagereIdAndIdNot(dto.getCode(), emp.getEtagere().getId(), id))
            throw new RuntimeException("Un autre emplacement porte déjà ce code sur cette étagère.");

        emp.setCode(dto.getCode());
        emp.setType(dto.getType());
        emp.setCapaciteMax(dto.getCapaciteMax());
        emp.setDescription(dto.getDescription());
        // Recalcule l'adresse complète si le code change
        emp.setAdresseComplete(emp.buildAdresseComplete());

        return EmplacementResponseDTO.fromEntity(emplacementRepository.save(emp));
    }

    @Transactional
    public EmplacementResponseDTO changerStatut(Long id, String statut) {
        Emplacement emp = findEmplacementOrThrow(id);
        emp.setStatut(Emplacement.StatutEmplacement.valueOf(statut));
        return EmplacementResponseDTO.fromEntity(emplacementRepository.save(emp));
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private Rayon findRayonOrThrow(Long id) {
        return rayonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rayon introuvable : id=" + id));
    }

    private Etagere findEtagereOrThrow(Long id) {
        return etagereRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Étagère introuvable : id=" + id));
    }

    private Emplacement findEmplacementOrThrow(Long id) {
        return emplacementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emplacement introuvable : id=" + id));
    }
}
