package com.magasin.multi_magasin.repository;

import com.magasin.multi_magasin.domain.entity.MouvementStock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {

    @EntityGraph(attributePaths = {"produit", "typeMouvement", "referenceVente", "unite"})
    List<MouvementStock> findAllByOrderByCreatedAtDesc();
}
