package com.magasin.multi_magasin.frontoffice;

import com.magasin.multi_magasin.domain.entity.Categorie;
import com.magasin.multi_magasin.domain.entity.Produit;
import com.magasin.multi_magasin.domain.entity.TypePaiement;
import com.magasin.multi_magasin.domain.entity.Vente;
import com.magasin.multi_magasin.frontoffice.dto.VenteCreateForm;
import com.magasin.multi_magasin.repository.CategorieRepository;
import com.magasin.multi_magasin.repository.TypePaiementRepository;
import com.magasin.multi_magasin.service.ProduitService;
import com.magasin.multi_magasin.service.VenteService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/frontOffice")
public class FrontOfficeController {

    private final ProduitService produitService;
    private final VenteService venteService;
    private final TypePaiementRepository typePaiementRepository;
    private final com.magasin.multi_magasin.repository.ClientRepository clientRepository;
    private final CategorieRepository categorieRepository;

    public FrontOfficeController(ProduitService produitService, VenteService venteService, TypePaiementRepository typePaiementRepository, com.magasin.multi_magasin.repository.ClientRepository clientRepository, CategorieRepository categorieRepository) {
        this.produitService = produitService;
        this.venteService = venteService;
        this.typePaiementRepository = typePaiementRepository;
        this.clientRepository = clientRepository;
        this.categorieRepository = categorieRepository;
    }

    @GetMapping("/api/clients")
    @ResponseBody
    public List<com.magasin.multi_magasin.domain.entity.Client> searchClients(@RequestParam(required = false, defaultValue = "") String keyword) {
        if (keyword.isBlank()) {
            return clientRepository.findAll();
        }
        return clientRepository.search(keyword);
    }

    @PostMapping("/api/clients")
    @ResponseBody
    public ResponseEntity<?> createClient(@RequestBody com.magasin.multi_magasin.domain.entity.Client client) {
        try {
            if (client.getNom() == null || client.getNom().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le nom est obligatoire"));
            }
            com.magasin.multi_magasin.domain.entity.Client savedClient = clientRepository.save(client);
            return ResponseEntity.ok(savedClient);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public String index(Model model) {
        // We can pass initial products or let the frontend fetch them
        return "frontoffice/index";
    }

    @GetMapping("/api/produits")
    @ResponseBody
    public List<Map<String, Object>> searchProduits(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) Long categorieId) {
        try {
            // Reusing the search logic but mapping to a structure suitable for the front office
            List<Produit> produits = produitService.searchProduits(keyword, categorieId);
            
            return produits.stream()
                    .map(p -> {
                        // Find the best price/unit to display (e.g. Piece or Kg)
                        var prixDefault = p.getPrixUnitaires().stream().findFirst().orElse(null);
                        
                        // Use HashMap to avoid NullPointerException from Map.of if any value is null
                        Map<String, Object> map = new java.util.HashMap<>();
                        map.put("id", p.getId());
                        map.put("nom", p.getNom() != null ? p.getNom() : "Sans nom");
                        map.put("description", p.getDesignation() != null ? p.getDesignation() : "");
                        String photoUrl = p.getPhotoUrl();
                        String imageUrl = "https://via.placeholder.com/150";
                        if (photoUrl != null && !photoUrl.isBlank()) {
                            if (photoUrl.startsWith("/uploads/")) {
                                imageUrl = "/multi_magasin" + photoUrl;
                            } else if (photoUrl.startsWith("uploads/")) {
                                imageUrl = "/multi_magasin/" + photoUrl;
                            } else {
                                imageUrl = "/multi_magasin/uploads/" + photoUrl;
                            }
                        }
                        map.put("image", imageUrl);
                        map.put("categorie", p.getCategorie() != null ? p.getCategorie().getLibelle() : "Non classé");
                        map.put("categorieId", p.getCategorie() != null ? p.getCategorie().getId() : null);
                        map.put("codeBarre", p.getCodeBarre() != null ? p.getCodeBarre() : "");
                        
                        if (prixDefault != null) {
                            map.put("prix", prixDefault.getPrix());
                            map.put("unite", prixDefault.getUnite() != null ? prixDefault.getUnite().getSymbole() : "");
                        } else {
                            map.put("prix", 0);
                            map.put("unite", "");
                        }
                        
                        return map;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    @GetMapping("/api/categories")
    @ResponseBody
    public List<Categorie> getCategories() {
        return categorieRepository.findAll();
    }

    @GetMapping("/api/payment-types")
    @ResponseBody
    public List<TypePaiement> getPaymentTypes() {
        return typePaiementRepository.findAll();
    }

    @PostMapping("/api/ventes")
    @ResponseBody
    public ResponseEntity<?> createVente(@RequestBody VenteCreateForm form) {
        try {
            Vente vente = venteService.createVente(form);
            return ResponseEntity.ok(Map.of("message", "Vente enregistrée avec succès", "id", vente.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
