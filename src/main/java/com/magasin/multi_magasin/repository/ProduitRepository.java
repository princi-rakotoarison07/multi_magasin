package com.magasin.multi_magasin.repository;

import com.magasin.multi_magasin.domain.entity.Produit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProduitRepository extends JpaRepository<Produit, Long> {

    @Override
    @EntityGraph(attributePaths = {"categorie", "prixUnitaires", "prixUnitaires.unite"})
    List<Produit> findAll();

    boolean existsByCodeBarreIgnoreCase(String codeBarre);
}
