package com.example.backend.module.categorie.service;

import com.example.backend.module.categorie.dto.CategorieRequestDTO;
import com.example.backend.module.categorie.dto.CategorieResponseDTO;
import com.example.backend.module.categorie.entity.Categorie;
import com.example.backend.module.categorie.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategorieService {

    private final CategorieRepository categorieRepository;

    public List<CategorieResponseDTO> findAll() {
        return categorieRepository.findAll()
                .stream()
                .map(CategorieResponseDTO::fromEntity)
                .toList();
    }

    public CategorieResponseDTO findById(Long id) {
        return CategorieResponseDTO.fromEntity(
                categorieRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Catégorie introuvable : id=" + id))
        );
    }

    @Transactional
    public CategorieResponseDTO creer(CategorieRequestDTO dto) {
        if (categorieRepository.existsByNom(dto.getNom()))
            throw new RuntimeException("Une catégorie avec ce nom existe déjà");

        Categorie categorie = Categorie.builder()
                .nom(dto.getNom())
                .description(dto.getDescription())
                .build();

        return CategorieResponseDTO.fromEntity(categorieRepository.save(categorie));
    }

    @Transactional
    public CategorieResponseDTO modifier(Long id, CategorieRequestDTO dto) {
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable : id=" + id));

        if (categorieRepository.existsByNomAndIdNot(dto.getNom(), id))
            throw new RuntimeException("Une catégorie avec ce nom existe déjà");

        categorie.setNom(dto.getNom());
        categorie.setDescription(dto.getDescription());

        return CategorieResponseDTO.fromEntity(categorieRepository.save(categorie));
    }

    @Transactional
    public void supprimer(Long id) {
        if (!categorieRepository.existsById(id))
            throw new RuntimeException("Catégorie introuvable : id=" + id);
        categorieRepository.deleteById(id);
    }
}
