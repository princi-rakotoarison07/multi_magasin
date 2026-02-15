package com.magasin.multi_magasin.backoffice;

import com.magasin.multi_magasin.backoffice.dto.MouvementStockCreateForm;
import com.magasin.multi_magasin.domain.entity.Produit;
import com.magasin.multi_magasin.domain.entity.TypeMouvement;
import com.magasin.multi_magasin.domain.entity.Unite;
import com.magasin.multi_magasin.service.ProduitService;
import com.magasin.multi_magasin.service.StockService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/backOffice/stock")
public class StockController {

    private final StockService stockService;
    private final ProduitService produitService;

    public StockController(StockService stockService, ProduitService produitService) {
        this.stockService = stockService;
        this.produitService = produitService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("activeMenu", "stock");
        model.addAttribute("mouvements", stockService.getAllMouvements());
        return "backoffice/stock";
    }

    @GetMapping("/nouveau")
    public String nouveau(Model model) {
        model.addAttribute("activeMenu", "stock");
        model.addAttribute("form", new MouvementStockCreateForm());
        model.addAttribute("typesMouvement", stockService.getAllTypeMouvements());
        return "backoffice/stock_nouveau";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") MouvementStockCreateForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "stock");
            model.addAttribute("typesMouvement", stockService.getAllTypeMouvements());
            model.addAttribute("errorMessage", "Veuillez corriger les erreurs dans le formulaire.");
            return "backoffice/stock_nouveau";
        }

        try {
            stockService.createMouvements(form);
            redirectAttributes.addFlashAttribute("successMessage", "Mouvement de stock enregistré avec succès.");
            return "redirect:/backOffice/stock";
        } catch (Exception e) {
            model.addAttribute("activeMenu", "stock");
            model.addAttribute("typesMouvement", stockService.getAllTypeMouvements());
            model.addAttribute("errorMessage", e.getMessage());
            return "backoffice/stock_nouveau";
        }
    }

    @GetMapping("/api/produits")
    @ResponseBody
    public List<Map<String, Object>> searchProduits(@RequestParam String keyword) {
        // We reuse produitService.searchProduits but we need to map to a simple structure including units and price
        // Actually produitService.searchProduits returns entities.
        // We need to avoid infinite recursion if we serialize entities directly.
        // So we map to DTOs or Maps.
        return produitService.searchProduits(keyword, null).stream()
                .map(p -> {
                    // Extract first unit/price or all units
                    List<Map<String, Object>> prixUnitaires = p.getPrixUnitaires().stream()
                            .map(pu -> Map.<String, Object>of(
                                    "uniteId", pu.getUnite().getId(),
                                    "uniteLibelle", pu.getUnite().getLibelle(),
                                    "uniteSymbole", pu.getUnite().getSymbole(),
                                    "prix", pu.getPrix()
                            ))
                            .collect(Collectors.toList());

                    return Map.<String, Object>of(
                            "id", p.getId(),
                            "nom", p.getNom(),
                            "codeBarre", p.getCodeBarre() != null ? p.getCodeBarre() : "",
                            "categorie", p.getCategorie() != null ? p.getCategorie().getLibelle() : "",
                            "prixUnitaires", prixUnitaires
                    );
                })
                .collect(Collectors.toList());
    }
}
