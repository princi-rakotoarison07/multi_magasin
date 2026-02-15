package com.magasin.multi_magasin.repository;

import com.magasin.multi_magasin.domain.entity.Unite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UniteRepository extends JpaRepository<Unite, Long> {

    Optional<Unite> findBySymboleIgnoreCase(String symbole);
}
