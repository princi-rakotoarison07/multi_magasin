package com.magasin.multi_magasin.service;

import com.magasin.multi_magasin.backoffice.dto.ProduitCreateForm;
import com.magasin.multi_magasin.domain.entity.Produit;

import java.util.List;

public interface ProduitService {

    List<Produit> getAllProduits();

    List<Produit> searchProduits(String keyword, Long categorieId);

    Produit createProduit(ProduitCreateForm form);

    Produit createProduit(ProduitCreateForm form, String photoUrl);

    Produit updateProduit(Long id, ProduitCreateForm form, String photoUrl);

    void deleteProduit(Long id);
}
