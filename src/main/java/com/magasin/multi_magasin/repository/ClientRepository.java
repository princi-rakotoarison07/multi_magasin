package com.magasin.multi_magasin.repository;

import com.magasin.multi_magasin.domain.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByNomContainingIgnoreCase(String nom);
    List<Client> findByTelephoneContainingIgnoreCase(String telephone);
    List<Client> findByEmailContainingIgnoreCase(String email);
}
