package com.magasin.multi_magasin.service;

import com.magasin.multi_magasin.domain.entity.Vente;
import java.util.List;

public interface VenteService {
    List<Vente> getAllVentes();

    Vente createVente(com.magasin.multi_magasin.frontoffice.dto.VenteCreateForm form);

    List<Vente> getVentesByDateRange(java.time.LocalDate dateDebut, java.time.LocalDate dateFin);
}
