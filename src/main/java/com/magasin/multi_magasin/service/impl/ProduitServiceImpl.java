package com.magasin.multi_magasin.service.impl;

import com.magasin.multi_magasin.backoffice.dto.ProduitCreateForm;
import com.magasin.multi_magasin.domain.entity.Categorie;
import com.magasin.multi_magasin.domain.entity.PrixUnitaire;
import com.magasin.multi_magasin.domain.entity.Produit;
import com.magasin.multi_magasin.domain.entity.Unite;
import com.magasin.multi_magasin.repository.CategorieRepository;
import com.magasin.multi_magasin.repository.ProduitRepository;
import com.magasin.multi_magasin.repository.UniteRepository;
import com.magasin.multi_magasin.service.ProduitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProduitServiceImpl implements ProduitService {

    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;
    private final UniteRepository uniteRepository;

    public ProduitServiceImpl(
            ProduitRepository produitRepository,
            CategorieRepository categorieRepository,
            UniteRepository uniteRepository
    ) {
        this.produitRepository = produitRepository;
        this.categorieRepository = categorieRepository;
        this.uniteRepository = uniteRepository;
    }

    @Override
    @Transactional
    public void deleteProduit(Long id) {
        if (!produitRepository.existsById(id)) {
            throw new IllegalArgumentException("Produit introuvable");
        }
        produitRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Produit updateProduit(Long id, ProduitCreateForm form, String photoUrl) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));

        produit.setNom(form.getNom());
        String codeBarre = (form.getCodeBarre() != null && !form.getCodeBarre().isBlank()) ? form.getCodeBarre().trim() : null;
        if (codeBarre != null && !codeBarre.equalsIgnoreCase(produit.getCodeBarre()) && produitRepository.existsByCodeBarreIgnoreCase(codeBarre)) {
            throw new IllegalArgumentException("Ce code barre existe déjà.");
        }
        produit.setCodeBarre(codeBarre);
        produit.setDesignation(form.getDesignation());
        if (photoUrl != null) {
            produit.setPhotoUrl(photoUrl);
        }
        produit.setActive(form.isActive());

        if (form.getCategorieId() != null) {
            Categorie categorie = categorieRepository.findById(form.getCategorieId())
                    .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable"));
            produit.setCategorie(categorie);
        } else {
            produit.setCategorie(null);
        }

        Unite unite = uniteRepository.findById(form.getUniteId())
                .orElseThrow(() -> new IllegalArgumentException("Unité introuvable"));

        // Update price logic - simplified: update the first price or add a new one if none exists
        if (produit.getPrixUnitaires().isEmpty()) {
            PrixUnitaire prixUnitaire = new PrixUnitaire();
            prixUnitaire.setProduit(produit);
            prixUnitaire.setUnite(unite);
            prixUnitaire.setPrix(form.getPrix());
            produit.getPrixUnitaires().add(prixUnitaire);
        } else {
            // Assuming we only manage one price per product for now in this simple form
            PrixUnitaire prixUnitaire = produit.getPrixUnitaires().get(0);
            prixUnitaire.setUnite(unite);
            prixUnitaire.setPrix(form.getPrix());
        }

        return produitRepository.save(produit);
    }

    @Override
    public List<Produit> getAllProduits() {
        return produitRepository.findAll();
    }

    @Override
    public List<Produit> searchProduits(String keyword, Long categorieId) {
        if ((keyword == null || keyword.isBlank()) && categorieId == null) {
            return getAllProduits();
        }
        return produitRepository.search(keyword, categorieId);
    }

    @Override
    @Transactional
    public Produit createProduit(ProduitCreateForm form) {
        return createProduit(form, null);
    }

    @Override
    @Transactional
    public Produit createProduit(ProduitCreateForm form, String photoUrl) {
        Produit produit = new Produit();
        produit.setNom(form.getNom());
        String codeBarre = (form.getCodeBarre() != null && !form.getCodeBarre().isBlank()) ? form.getCodeBarre().trim() : null;
        if (codeBarre != null && produitRepository.existsByCodeBarreIgnoreCase(codeBarre)) {
            throw new IllegalArgumentException("Ce code barre existe déjà.");
        }
        produit.setCodeBarre(codeBarre);
        produit.setDesignation(form.getDesignation());
        produit.setPhotoUrl(photoUrl);
        produit.setActive(form.isActive());

        if (form.getCategorieId() != null) {
            Categorie categorie = categorieRepository.findById(form.getCategorieId())
                    .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable"));
            produit.setCategorie(categorie);
        }

        Unite unite = uniteRepository.findById(form.getUniteId())
                .orElseThrow(() -> new IllegalArgumentException("Unité introuvable"));

        PrixUnitaire prixUnitaire = new PrixUnitaire();
        prixUnitaire.setProduit(produit);
        prixUnitaire.setUnite(unite);
        prixUnitaire.setPrix(form.getPrix());
        produit.getPrixUnitaires().add(prixUnitaire);

        return produitRepository.save(produit);
    }
}
