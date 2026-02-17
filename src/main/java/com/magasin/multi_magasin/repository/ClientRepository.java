package com.magasin.multi_magasin.repository;

import com.magasin.multi_magasin.domain.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(String nom, String prenom);
    List<Client> findByNomContainingIgnoreCase(String nom);
    List<Client> findByTelephoneContainingIgnoreCase(String telephone);
    @org.springframework.data.jpa.repository.Query("SELECT c FROM Client c WHERE " +
           "LOWER(c.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.telephone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Client> search(@org.springframework.data.repository.query.Param("keyword") String keyword);
}
