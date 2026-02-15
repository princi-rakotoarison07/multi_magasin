package com.magasin.multi_magasin.frontoffice;

import com.magasin.multi_magasin.domain.entity.Produit;
import com.magasin.multi_magasin.service.ProduitService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/frontOffice")
public class FrontOfficeController {

    private final ProduitService produitService;

    public FrontOfficeController(ProduitService produitService) {
        this.produitService = produitService;
    }

    @GetMapping
    public String index(Model model) {
        // We can pass initial products or let the frontend fetch them
        return "frontoffice/index";
    }

    @GetMapping("/api/produits")
    @ResponseBody
    public List<Map<String, Object>> searchProduits(@RequestParam(required = false, defaultValue = "") String keyword) {
        try {
            // Reusing the search logic but mapping to a structure suitable for the front office
            List<Produit> produits = produitService.searchProduits(keyword, null);
            
            return produits.stream()
                    .map(p -> {
                        // Find the best price/unit to display (e.g. Piece or Kg)
                        var prixDefault = p.getPrixUnitaires().stream().findFirst().orElse(null);
                        
                        // Use HashMap to avoid NullPointerException from Map.of if any value is null
                        Map<String, Object> map = new java.util.HashMap<>();
                        map.put("id", p.getId());
                        map.put("nom", p.getNom() != null ? p.getNom() : "Sans nom");
                        map.put("description", p.getDesignation() != null ? p.getDesignation() : "");
                        map.put("image", (p.getPhotoUrl() != null && !p.getPhotoUrl().isBlank()) ? "/multi_magasin/uploads/" + p.getPhotoUrl() : "https://via.placeholder.com/150");
                        map.put("categorie", p.getCategorie() != null ? p.getCategorie().getLibelle() : "Non classé");
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
}
