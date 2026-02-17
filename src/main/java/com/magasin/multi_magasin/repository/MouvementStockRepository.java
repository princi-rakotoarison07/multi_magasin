package com.magasin.multi_magasin.repository;

import com.magasin.multi_magasin.domain.entity.MouvementStock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {

    @EntityGraph(attributePaths = {"produit", "typeMouvement", "referenceVente"})
    List<MouvementStock> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(CASE WHEN tm.libelle = 'ENTREE' THEN m.quantite " +
           "WHEN tm.libelle = 'SORTIE' THEN -m.quantite ELSE 0 END), 0) " +
           "FROM MouvementStock m JOIN m.typeMouvement tm WHERE m.produit.id = :produitId")
    BigDecimal getStockDisponible(@Param("produitId") Long produitId);

    @Query("SELECT new com.magasin.multi_magasin.backoffice.dto.ProduitStockDto(p, " +
           "COALESCE(SUM(CASE WHEN tm.libelle = 'ENTREE' THEN m.quantite " +
           "WHEN tm.libelle = 'SORTIE' THEN -m.quantite ELSE 0 END), 0)) " +
           "FROM Produit p " +
           "LEFT JOIN MouvementStock m ON m.produit = p " +
           "LEFT JOIN m.typeMouvement tm " +
           "WHERE p.active = true " +
           "GROUP BY p")
    List<com.magasin.multi_magasin.backoffice.dto.ProduitStockDto> getEtatStock();
}
