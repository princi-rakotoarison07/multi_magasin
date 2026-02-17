package com.magasin.multi_magasin.service;

import com.magasin.multi_magasin.backoffice.dto.MouvementStockCreateForm;
import com.magasin.multi_magasin.domain.entity.MouvementStock;
import com.magasin.multi_magasin.domain.entity.TypeMouvement;

import java.util.List;

public interface StockService {
    List<MouvementStock> getAllMouvements();
    List<com.magasin.multi_magasin.backoffice.dto.ProduitStockDto> getEtatStock();
    List<TypeMouvement> getAllTypeMouvements();
    void createMouvements(MouvementStockCreateForm form);
}
