package com.magasin.multi_magasin.service.impl;

import com.magasin.multi_magasin.backoffice.dto.MouvementStockCreateForm;
import com.magasin.multi_magasin.domain.entity.MouvementStock;
import com.magasin.multi_magasin.domain.entity.Produit;
import com.magasin.multi_magasin.domain.entity.TypeMouvement;
import com.magasin.multi_magasin.domain.entity.Unite;
import com.magasin.multi_magasin.repository.MouvementStockRepository;
import com.magasin.multi_magasin.repository.ProduitRepository;
import com.magasin.multi_magasin.repository.TypeMouvementRepository;
import com.magasin.multi_magasin.repository.UniteRepository;
import com.magasin.multi_magasin.service.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockServiceImpl implements StockService {

    private final MouvementStockRepository mouvementStockRepository;
    private final TypeMouvementRepository typeMouvementRepository;
    private final ProduitRepository produitRepository;

    public StockServiceImpl(
            MouvementStockRepository mouvementStockRepository,
            TypeMouvementRepository typeMouvementRepository,
            ProduitRepository produitRepository
    ) {
        this.mouvementStockRepository = mouvementStockRepository;
        this.typeMouvementRepository = typeMouvementRepository;
        this.produitRepository = produitRepository;
    }

    @Override
    public List<MouvementStock> getAllMouvements() {
        return mouvementStockRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<com.magasin.multi_magasin.backoffice.dto.ProduitStockDto> getEtatStock() {
        return mouvementStockRepository.getEtatStock();
    }

    @Override
    public List<TypeMouvement> getAllTypeMouvements() {
        return typeMouvementRepository.findAll();
    }

    @Override
    @Transactional
    public void createMouvements(MouvementStockCreateForm form) {
        TypeMouvement typeMouvement = typeMouvementRepository.findById(form.getTypeMouvementId())
                .orElseThrow(() -> new IllegalArgumentException("Type de mouvement invalide"));

        LocalDateTime movementDate;
        if (form.getDateMouvement().equals(LocalDateTime.now().toLocalDate())) {
            movementDate = LocalDateTime.now();
        } else {
            movementDate = form.getDateMouvement().atStartOfDay();
        }

        for (MouvementStockCreateForm.LigneMouvementCreateForm ligne : form.getLignes()) {
            Produit produit = produitRepository.findById(ligne.getProduitId())
                    .orElseThrow(() -> new IllegalArgumentException("Produit introuvable: " + ligne.getProduitId()));
            
            MouvementStock mouvement = new MouvementStock();
            mouvement.setProduit(produit);
            mouvement.setTypeMouvement(typeMouvement);
            mouvement.setQuantite(ligne.getQuantite());
            mouvement.setCreatedAt(movementDate);

            mouvementStockRepository.save(mouvement);
        }
    }
}
