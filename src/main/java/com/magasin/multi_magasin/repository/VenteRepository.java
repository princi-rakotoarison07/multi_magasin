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

    @EntityGraph(attributePaths = {"client", "paiements", "paiements.typePaiement"})
    @Query("SELECT v FROM Vente v WHERE v.createdAt BETWEEN :start AND :end ORDER BY v.createdAt DESC")
    List<Vente> findByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(v) FROM Vente v WHERE CAST(v.createdAt AS date) = CAST(:date AS date)")
    long countByDate(@Param("date") LocalDateTime date);

    @Query("SELECT SUM(v.total) FROM Vente v WHERE CAST(v.createdAt AS date) = CAST(:date AS date)")
    java.math.BigDecimal sumTotalByDate(@Param("date") LocalDateTime date);
    
    @EntityGraph(attributePaths = {"client", "paiements", "paiements.typePaiement"})
    List<Vente> findTop5ByOrderByCreatedAtDesc();
}
