package com.magasin.multi_magasin.repository;

import com.magasin.multi_magasin.domain.entity.Caisse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaisseRepository extends JpaRepository<Caisse, Long> {
}
