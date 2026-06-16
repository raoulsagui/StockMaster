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
 */
@Service
@RequiredArgsConstructor
public class EntrepotService {

    private final EntrepotRepository entrepotRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ZoneRepository zoneRepository;

    public List<EntrepotResponseDTO> findAll() {
        return entrepotRepository.findAll()
                .stream()
                .map(e -> EntrepotResponseDTO.fromEntity(e,
                        zoneRepository.countByEntrepotId(e.getId())))
                .toList();
    }

    public EntrepotResponseDTO findById(Long id) {
        Entrepot entrepot = findEntrepotOrThrow(id);
        long nombreZones = zoneRepository.countByEntrepotId(id);
        return EntrepotResponseDTO.fromEntity(entrepot, nombreZones);
    }

    @Transactional
    public EntrepotResponseDTO creer(EntrepotRequestDTO dto) {
        if (entrepotRepository.existsByNom(dto.getNom())) {
            throw new RuntimeException("Un entrepôt avec ce nom existe déjà");
        }

        double utilise = dto.getCapaciteUtilisee() != null ? dto.getCapaciteUtilisee() : 0.0;
        if (utilise > dto.getCapaciteTotale()) {
            throw new RuntimeException(
                "La capacité utilisée (" + utilise + " m²) ne peut pas dépasser "
                + "la capacité totale (" + dto.getCapaciteTotale() + " m²)"
            );
        }

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

    @Transactional
    public EntrepotResponseDTO modifier(Long id, EntrepotRequestDTO dto) {
        Entrepot entrepot = findEntrepotOrThrow(id);

        if (!entrepot.getNom().equals(dto.getNom())
                && entrepotRepository.existsByNomAndIdNot(dto.getNom(), id)) {
            throw new RuntimeException("Un autre entrepôt porte déjà ce nom");
        }

        double utilise = dto.getCapaciteUtilisee() != null
                ? dto.getCapaciteUtilisee()
                : entrepot.getCapaciteUtilisee();

        if (utilise > dto.getCapaciteTotale()) {
            throw new RuntimeException(
                "La capacité utilisée (" + utilise + " m²) ne peut pas dépasser "
                + "la nouvelle capacité totale (" + dto.getCapaciteTotale() + " m²)"
            );
        }

        Utilisateur responsable = resolveResponsable(dto.getResponsableId());

        entrepot.setNom(dto.getNom());
        entrepot.setAdresse(dto.getAdresse());
        entrepot.setCapaciteTotale(dto.getCapaciteTotale());
        entrepot.setCapaciteUtilisee(utilise);
        entrepot.setResponsable(responsable);

        Entrepot saved = entrepotRepository.save(entrepot);
        long nombreZones = zoneRepository.countByEntrepotId(id);
        return EntrepotResponseDTO.fromEntity(saved, nombreZones);
    }

    @Transactional
    public EntrepotResponseDTO toggleStatut(Long id) {
        Entrepot entrepot = findEntrepotOrThrow(id);
        entrepot.setActif(!entrepot.isActif());
        Entrepot saved = entrepotRepository.save(entrepot);
        long nombreZones = zoneRepository.countByEntrepotId(id);
        return EntrepotResponseDTO.fromEntity(saved, nombreZones);
    }

    public List<EntrepotResponseDTO> findActifs() {
        return entrepotRepository.findByActifTrue()
                .stream()
                .map(e -> EntrepotResponseDTO.fromEntity(e,
                        zoneRepository.countByEntrepotId(e.getId())))
                .toList();
    }

    // -------------------------------------------------------
    // MÉTHODES PRIVÉES
    // -------------------------------------------------------

    private Entrepot findEntrepotOrThrow(Long id) {
        return entrepotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrepôt introuvable : id=" + id));
    }

    private Utilisateur resolveResponsable(Long responsableId) {
        if (responsableId == null) return null;
        return utilisateurRepository.findById(responsableId)
                .orElseThrow(() -> new RuntimeException(
                    "Responsable introuvable : id=" + responsableId
                ));
    }
}
