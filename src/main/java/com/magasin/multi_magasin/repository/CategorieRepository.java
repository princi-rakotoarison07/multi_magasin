package com.magasin.multi_magasin.repository;

import com.magasin.multi_magasin.domain.entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {

    Optional<Categorie> findByLibelleIgnoreCase(String libelle);
}
