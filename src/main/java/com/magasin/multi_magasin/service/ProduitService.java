package com.magasin.multi_magasin.service;

import com.magasin.multi_magasin.backoffice.dto.ProduitCreateForm;
import com.magasin.multi_magasin.domain.entity.Produit;

import java.util.List;

public interface ProduitService {

    List<Produit> getAllProduits();

    Produit createProduit(ProduitCreateForm form);

    Produit createProduit(ProduitCreateForm form, String photoUrl);
}
