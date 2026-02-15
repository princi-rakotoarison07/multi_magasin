package com.magasin.multi_magasin.service.impl;

import com.magasin.multi_magasin.domain.entity.MouvementStock;
import com.magasin.multi_magasin.repository.MouvementStockRepository;
import com.magasin.multi_magasin.service.StockService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockServiceImpl implements StockService {

    private final MouvementStockRepository mouvementStockRepository;

    public StockServiceImpl(MouvementStockRepository mouvementStockRepository) {
        this.mouvementStockRepository = mouvementStockRepository;
    }

    @Override
    public List<MouvementStock> getAllMouvements() {
        return mouvementStockRepository.findAllByOrderByCreatedAtDesc();
    }
}
