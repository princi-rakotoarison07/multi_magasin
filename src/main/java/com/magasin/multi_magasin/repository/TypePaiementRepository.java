package com.magasin.multi_magasin.repository;

import com.magasin.multi_magasin.domain.entity.TypePaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypePaiementRepository extends JpaRepository<TypePaiement, Long> {
}
