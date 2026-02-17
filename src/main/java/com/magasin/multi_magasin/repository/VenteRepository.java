package com.magasin.multi_magasin.repository;

import com.magasin.multi_magasin.domain.entity.Vente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

@Repository
public interface VenteRepository extends JpaRepository<Vente, Long> {

    @Override
    @EntityGraph(attributePaths = {"client"})
    List<Vente> findAll();

    @EntityGraph(attributePaths = {"client"})
    @Query("SELECT v FROM Vente v WHERE v.createdAt BETWEEN :start AND :end ORDER BY v.createdAt DESC")
    List<Vente> findByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
