package com.magasin.multi_magasin.service.impl;

import com.magasin.multi_magasin.domain.entity.Vente;
import com.magasin.multi_magasin.repository.VenteRepository;
import com.magasin.multi_magasin.service.VenteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VenteServiceImpl implements VenteService {

    private final VenteRepository venteRepository;

    public VenteServiceImpl(VenteRepository venteRepository) {
        this.venteRepository = venteRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vente> getAllVentes() {
        return venteRepository.findAll();
    }
}
