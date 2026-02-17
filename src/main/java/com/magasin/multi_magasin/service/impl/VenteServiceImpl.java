package com.magasin.multi_magasin.service.impl;

import com.magasin.multi_magasin.domain.entity.*;
import com.magasin.multi_magasin.frontoffice.dto.VenteCreateForm;
import com.magasin.multi_magasin.repository.*;
import com.magasin.multi_magasin.service.VenteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VenteServiceImpl implements VenteService {

    private final VenteRepository venteRepository;
    private final ProduitRepository produitRepository;
    private final TypePaiementRepository typePaiementRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final TypeMouvementRepository typeMouvementRepository;
    private final ClientRepository clientRepository;

    public VenteServiceImpl(
            VenteRepository venteRepository,
            ProduitRepository produitRepository,
            TypePaiementRepository typePaiementRepository,
            MouvementStockRepository mouvementStockRepository,
            TypeMouvementRepository typeMouvementRepository,
            ClientRepository clientRepository
    ) {
        this.venteRepository = venteRepository;
        this.produitRepository = produitRepository;
        this.typePaiementRepository = typePaiementRepository;
        this.mouvementStockRepository = mouvementStockRepository;
        this.typeMouvementRepository = typeMouvementRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vente> getAllVentes() {
        return venteRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vente> getVentesByDateRange(java.time.LocalDate dateDebut, java.time.LocalDate dateFin) {
        if (dateDebut == null || dateFin == null) {
             return getAllVentes();
        }
        return venteRepository.findByCreatedAtBetween(dateDebut.atStartOfDay(), dateFin.atTime(23, 59, 59));
    }

    @Override
    @Transactional
    public Vente createVente(VenteCreateForm form) {
        // 1. Create Vente
        Vente vente = new Vente();
        vente.setTotal(form.getTotal());
        vente.setStatut("PAYE"); // Assuming immediate payment
        
        // Assign Client
        if (form.getClientId() != null) {
            Client client = clientRepository.findById(form.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client introuvable avec l'ID: " + form.getClientId()));
            vente.setClient(client);
        } else {
            // Assign a default client (e.g., "Client Passage") or null
            // For now, we'll try to find the first client or leave null
            List<Client> clients = clientRepository.findAll();
            if (!clients.isEmpty()) {
                vente.setClient(clients.get(0));
            }
        }

        // 2. Process Items (Details & Stock)
        TypeMouvement typeMouvementSortie = typeMouvementRepository.findByLibelleIgnoreCase("SORTIE")
                .orElseThrow(() -> new RuntimeException("Type de mouvement 'SORTIE' introuvable"));

        for (VenteCreateForm.LigneVenteDto itemDto : form.getItems()) {
            Produit produit = produitRepository.findById(itemDto.getProduitId())
                    .orElseThrow(() -> new RuntimeException("Produit introuvable: " + itemDto.getProduitId()));

            // Find unit (assuming first available price's unit for now as simplified logic)
            Unite unite = null;
            if (!produit.getPrixUnitaires().isEmpty()) {
                unite = produit.getPrixUnitaires().get(0).getUnite();
            } else {
                 throw new RuntimeException("Produit sans unité définie: " + produit.getNom());
            }

            // Create DetailVente
            DetailVente detail = new DetailVente();
            detail.setVente(vente);
            detail.setProduit(produit);
            detail.setUnite(unite);
            detail.setQuantite(itemDto.getQuantite());
            detail.setPrix(itemDto.getPrix());
            
            // Add to Vente (via getter)
            vente.getDetails().add(detail);
            
            // Create Stock Movement (Sortie)
            MouvementStock mouvement = new MouvementStock();
            mouvement.setProduit(produit);
            mouvement.setTypeMouvement(typeMouvementSortie);
            mouvement.setQuantite(itemDto.getQuantite());
            mouvement.setReferenceVente(vente); // Will be saved after vente is persisted? No, Vente needs to be saved first for ID? 
                                                // Actually, if mapped properly, JPA handles it, but MouvementStock.referenceVente is ManyToOne.
                                                // If we save MouvementStock before Vente has ID, it might fail if referenceVente_id is nullable=false (it is nullable=true in Entity).
            
            // We'll save movements after saving Vente to be sure, or rely on Hibernate to delay SQL if added to a list (but Vente doesn't have list of Mouvements)
            // So we must save Vente first or save movements later.
            // Let's collect movements and save them after Vente.
            // But wait, DetailVente is cascaded.
        }

        // 3. Process Payment
        if (form.getPayment() != null) {
            TypePaiement typePaiement = typePaiementRepository.findById(form.getPayment().getTypePaiementId())
                    .orElseThrow(() -> new RuntimeException("Type de paiement introuvable"));

            Paiement paiement = new Paiement();
            paiement.setVente(vente);
            paiement.setTypePaiement(typePaiement);
            paiement.setMontant(form.getPayment().getMontant());
            // paiement.setDatePaiement(LocalDateTime.now()); // Using createdAt from DB
            // reference logic if needed (e.g. mobile money ref)
            
            vente.getPaiements().add(paiement);
        }

        // 4. Save Vente (cascades Details and Paiements)
        Vente savedVente = venteRepository.save(vente);
        
        // 5. Create and Save Stock Movements (now that Vente has ID)
        for (VenteCreateForm.LigneVenteDto itemDto : form.getItems()) {
             Produit produit = produitRepository.findById(itemDto.getProduitId()).orElseThrow();
             
             MouvementStock mouvement = new MouvementStock();
             mouvement.setProduit(produit);
             mouvement.setTypeMouvement(typeMouvementSortie);
             mouvement.setQuantite(itemDto.getQuantite());
             mouvement.setReferenceVente(savedVente);
             mouvementStockRepository.save(mouvement);
        }

        return savedVente;
    }
}
