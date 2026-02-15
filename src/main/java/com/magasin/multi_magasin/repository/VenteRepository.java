package com.magasin.multi_magasin.repository;

import com.magasin.multi_magasin.domain.entity.Vente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenteRepository extends JpaRepository<Vente, Long> {

    @Override
    @EntityGraph(attributePaths = {"client"})
    List<Vente> findAll();
}
