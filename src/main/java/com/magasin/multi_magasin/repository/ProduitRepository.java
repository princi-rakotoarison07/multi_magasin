package com.magasin.multi_magasin.repository;

import com.magasin.multi_magasin.domain.entity.Produit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProduitRepository extends JpaRepository<Produit, Long> {

    @Override
    @EntityGraph(attributePaths = {"categorie", "prixUnitaires", "prixUnitaires.unite"})
    List<Produit> findAll();

    @EntityGraph(attributePaths = {"categorie", "prixUnitaires", "prixUnitaires.unite"})
    @Query("SELECT p FROM Produit p WHERE " +
           "(:keyword IS NULL OR LOWER(p.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.codeBarre) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categorieId IS NULL OR p.categorie.id = :categorieId)")
    List<Produit> search(@Param("keyword") String keyword, @Param("categorieId") Long categorieId);

    boolean existsByCodeBarreIgnoreCase(String codeBarre);

    long countByActiveTrue();
}
