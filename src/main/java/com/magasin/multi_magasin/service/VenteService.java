package com.magasin.multi_magasin.service;

import com.magasin.multi_magasin.frontoffice.dto.VenteCreateForm;
import com.magasin.multi_magasin.domain.entity.Vente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface VenteService {
    List<Vente> getAllVentes();

    Vente getVenteById(Long id);

    Vente createVente(VenteCreateForm form);

    List<Vente> getVentesByDateRange(java.time.LocalDate dateDebut, java.time.LocalDate dateFin);

    long countVentesByDate(java.time.LocalDateTime date);
    
    java.math.BigDecimal sumTotalByDate(java.time.LocalDateTime date);
    
    List<Vente> getRecentVentes();
}
