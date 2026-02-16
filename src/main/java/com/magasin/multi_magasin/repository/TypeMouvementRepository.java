package com.magasin.multi_magasin.repository;

import com.magasin.multi_magasin.domain.entity.TypeMouvement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TypeMouvementRepository extends JpaRepository<TypeMouvement, Long> {
    Optional<TypeMouvement> findByLibelle(String libelle);
    Optional<TypeMouvement> findByLibelleIgnoreCase(String libelle);
}
