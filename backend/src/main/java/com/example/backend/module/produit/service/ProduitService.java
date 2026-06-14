package com.example.backend.module.produit.service;

import com.example.backend.module.categorie.entity.Categorie;
import com.example.backend.module.categorie.repository.CategorieRepository;
import com.example.backend.module.produit.dto.ProduitRequestDTO;
import com.example.backend.module.produit.dto.ProduitResponseDTO;
import com.example.backend.module.produit.entity.Produit;
import com.example.backend.module.produit.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;

    public List<ProduitResponseDTO> findAll() {
        return produitRepository.findAll()
                .stream()
                .map(ProduitResponseDTO::fromEntity)
                .toList();
    }

    public ProduitResponseDTO findById(Long id) {
        return ProduitResponseDTO.fromEntity(
                produitRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Produit introuvable : id=" + id))
        );
    }

    @Transactional
    public ProduitResponseDTO creer(ProduitRequestDTO dto) {
        if (produitRepository.existsByReference(dto.getReference()))
            throw new RuntimeException("Un produit avec cette référence existe déjà");

        if (dto.getCodeBarres() != null && !dto.getCodeBarres().isBlank()
                && produitRepository.existsByCodeBarres(dto.getCodeBarres()))
            throw new RuntimeException("Un produit avec ce code-barres existe déjà");

        Produit produit = Produit.builder()
                .reference(dto.getReference())
                .codeBarres(dto.getCodeBarres())
                .nom(dto.getNom())
                .description(dto.getDescription())
                .categorie(resolveCategorie(dto.getCategorieId()))
                .prixAchat(dto.getPrixAchat())
                .prixVente(dto.getPrixVente())
                .poids(dto.getPoids())
                .volume(dto.getVolume())
                .actif(true)
                .build();

        return ProduitResponseDTO.fromEntity(produitRepository.save(produit));
    }

    @Transactional
    public ProduitResponseDTO modifier(Long id, ProduitRequestDTO dto) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable : id=" + id));

        if (produitRepository.existsByReferenceAndIdNot(dto.getReference(), id))
            throw new RuntimeException("Un produit avec cette référence existe déjà");

        if (dto.getCodeBarres() != null && !dto.getCodeBarres().isBlank()
                && produitRepository.existsByCodeBarresAndIdNot(dto.getCodeBarres(), id))
            throw new RuntimeException("Un produit avec ce code-barres existe déjà");

        produit.setReference(dto.getReference());
        produit.setCodeBarres(dto.getCodeBarres());
        produit.setNom(dto.getNom());
        produit.setDescription(dto.getDescription());
        produit.setCategorie(resolveCategorie(dto.getCategorieId()));
        produit.setPrixAchat(dto.getPrixAchat());
        produit.setPrixVente(dto.getPrixVente());
        produit.setPoids(dto.getPoids());
        produit.setVolume(dto.getVolume());

        return ProduitResponseDTO.fromEntity(produitRepository.save(produit));
    }

    @Transactional
    public ProduitResponseDTO toggleActif(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable : id=" + id));
        produit.setActif(!produit.isActif());
        return ProduitResponseDTO.fromEntity(produitRepository.save(produit));
    }

    private Categorie resolveCategorie(Long categorieId) {
        if (categorieId == null) return null;
        return categorieRepository.findById(categorieId)
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable : id=" + categorieId));
    }
}
