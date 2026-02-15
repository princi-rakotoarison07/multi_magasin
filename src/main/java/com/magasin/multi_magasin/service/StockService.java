package com.magasin.multi_magasin.service;

import com.magasin.multi_magasin.domain.entity.MouvementStock;

import java.util.List;

public interface StockService {

    List<MouvementStock> getAllMouvements();
}
