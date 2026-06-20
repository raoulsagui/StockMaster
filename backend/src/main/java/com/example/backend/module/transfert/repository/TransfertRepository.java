package com.example.backend.module.transfert.repository;

import com.example.backend.module.transfert.entity.Transfert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransfertRepository extends JpaRepository<Transfert, Long> {

    List<Transfert> findAllByOrderByDateCreationDesc();

    List<Transfert> findByStatutOrderByDateCreationDesc(Transfert.StatutTransfert statut);

    List<Transfert> findByEntrepotSourceIdOrderByDateCreationDesc(Long entrepotId);

    List<Transfert> findByEntrepotDestinationIdOrderByDateCreationDesc(Long entrepotId);

    List<Transfert> findByProduitIdOrderByDateCreationDesc(Long produitId);
}
