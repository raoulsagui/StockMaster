package com.example.backend.module.fournisseur.service;

import com.example.backend.module.fournisseur.dto.FournisseurRequestDTO;
import com.example.backend.module.fournisseur.dto.FournisseurResponseDTO;
import com.example.backend.module.fournisseur.entity.Fournisseur;
import com.example.backend.module.fournisseur.repository.FournisseurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FournisseurService {

    private final FournisseurRepository fournisseurRepository;

    public List<FournisseurResponseDTO> findAll() {
        return fournisseurRepository.findAll()
                .stream()
                .map(FournisseurResponseDTO::fromEntity)
                .toList();
    }

    public FournisseurResponseDTO findById(Long id) {
        return FournisseurResponseDTO.fromEntity(findOrThrow(id));
    }

    public List<FournisseurResponseDTO> findActifs() {
        return fournisseurRepository.findByActifTrue()
                .stream()
                .map(FournisseurResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public FournisseurResponseDTO creer(FournisseurRequestDTO dto) {
        if (fournisseurRepository.existsByNom(dto.getNom()))
            throw new RuntimeException("Un fournisseur avec ce nom existe déjà");

        if (fournisseurRepository.existsByEmail(dto.getEmail()))
            throw new RuntimeException("Un fournisseur avec cet email existe déjà");

        Fournisseur fournisseur = Fournisseur.builder()
                .nom(dto.getNom())
                .adresse(dto.getAdresse())
                .telephone(dto.getTelephone())
                .email(dto.getEmail())
                .contactPrincipal(dto.getContactPrincipal())
                .actif(true)
                .build();

        return FournisseurResponseDTO.fromEntity(fournisseurRepository.save(fournisseur));
    }

    @Transactional
    public FournisseurResponseDTO modifier(Long id, FournisseurRequestDTO dto) {
        Fournisseur fournisseur = findOrThrow(id);

        if (fournisseurRepository.existsByNomAndIdNot(dto.getNom(), id))
            throw new RuntimeException("Un autre fournisseur porte déjà ce nom");

        if (fournisseurRepository.existsByEmailAndIdNot(dto.getEmail(), id))
            throw new RuntimeException("Un autre fournisseur utilise déjà cet email");

        fournisseur.setNom(dto.getNom());
        fournisseur.setAdresse(dto.getAdresse());
        fournisseur.setTelephone(dto.getTelephone());
        fournisseur.setEmail(dto.getEmail());
        fournisseur.setContactPrincipal(dto.getContactPrincipal());

        return FournisseurResponseDTO.fromEntity(fournisseurRepository.save(fournisseur));
    }

    @Transactional
    public FournisseurResponseDTO toggleStatut(Long id) {
        Fournisseur fournisseur = findOrThrow(id);
        fournisseur.setActif(!fournisseur.isActif());
        return FournisseurResponseDTO.fromEntity(fournisseurRepository.save(fournisseur));
    }

    // -------------------------------------------------------
    private Fournisseur findOrThrow(Long id) {
        return fournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fournisseur introuvable : id=" + id));
    }
}
