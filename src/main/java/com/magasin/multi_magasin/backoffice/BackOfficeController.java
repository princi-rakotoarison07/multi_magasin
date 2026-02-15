package com.magasin.multi_magasin.backoffice;

import com.magasin.multi_magasin.backoffice.dto.ProduitCreateForm;
import com.magasin.multi_magasin.domain.entity.PrixUnitaire;
import com.magasin.multi_magasin.domain.entity.Produit;
import com.magasin.multi_magasin.domain.entity.MouvementStock;
import com.magasin.multi_magasin.repository.CategorieRepository;
import com.magasin.multi_magasin.repository.UniteRepository;
import com.magasin.multi_magasin.service.FileStorageService;
import com.magasin.multi_magasin.service.ProduitService;
import com.magasin.multi_magasin.service.StockService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/backOffice")
public class BackOfficeController {

    private final ProduitService produitService;
    private final CategorieRepository categorieRepository;
    private final UniteRepository uniteRepository;
    private final FileStorageService fileStorageService;
    private final StockService stockService;

    public BackOfficeController(
            ProduitService produitService,
            CategorieRepository categorieRepository,
            UniteRepository uniteRepository,
            FileStorageService fileStorageService,
            StockService stockService
    ) {
        this.produitService = produitService;
        this.categorieRepository = categorieRepository;
        this.uniteRepository = uniteRepository;
        this.fileStorageService = fileStorageService;
        this.stockService = stockService;
    }

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("activeMenu", "dashboard");
        return "backoffice/dashboard";
    }

    @GetMapping("/produits")
    public String produits(Model model) {
        List<Produit> produits = produitService.getAllProduits();
        produits.forEach(p -> p.getPrixUnitaires().sort(Comparator.comparing(PrixUnitaire::getId)));

        model.addAttribute("pageTitle", "Produits");
        model.addAttribute("activeMenu", "produits");
        model.addAttribute("produits", produits);
        model.addAttribute("totalProduits", produits.size());
        return "backoffice/produits";
    }

    @GetMapping("/produits/nouveau")
    public String nouveauProduit(Model model) {
        model.addAttribute("pageTitle", "Nouveau produit");
        model.addAttribute("activeMenu", "produits");
        model.addAttribute("form", new ProduitCreateForm());
        model.addAttribute("categories", categorieRepository.findAll());
        model.addAttribute("unites", uniteRepository.findAll());
        return "backoffice/produits_nouveau";
    }

    @PostMapping("/produits")
    public String createProduit(
            @Valid ProduitCreateForm form,
            BindingResult bindingResult,
            @RequestParam(name = "photo", required = false) MultipartFile photo,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Nouveau produit");
            model.addAttribute("activeMenu", "produits");
            model.addAttribute("categories", categorieRepository.findAll());
            model.addAttribute("unites", uniteRepository.findAll());
            return "backoffice/produits_nouveau";
        }

        try {
            String photoUrl = fileStorageService.storeImage(photo);
            produitService.createProduit(form, photoUrl);
            redirectAttributes.addFlashAttribute("successMessage", "Produit créé avec succès.");
            return "redirect:/backOffice/produits";
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("code barre")) {
                bindingResult.rejectValue("codeBarre", "duplicate", e.getMessage());
            }
            model.addAttribute("pageTitle", "Nouveau produit");
            model.addAttribute("activeMenu", "produits");
            model.addAttribute("categories", categorieRepository.findAll());
            model.addAttribute("unites", uniteRepository.findAll());
            model.addAttribute("errorMessage", e.getMessage());
            return "backoffice/produits_nouveau";
        } catch (DataIntegrityViolationException e) {
            String msg = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage();
            if (msg != null && (msg.contains("code_barre") || msg.toLowerCase().contains("code_barre"))) {
                bindingResult.rejectValue("codeBarre", "duplicate", "Ce code barre existe déjà.");
                model.addAttribute("errorMessage", "Ce code barre existe déjà.");
            } else {
                model.addAttribute("errorMessage", "Erreur d'enregistrement. Vérifie les champs.");
            }
            model.addAttribute("pageTitle", "Nouveau produit");
            model.addAttribute("activeMenu", "produits");
            model.addAttribute("categories", categorieRepository.findAll());
            model.addAttribute("unites", uniteRepository.findAll());
            return "backoffice/produits_nouveau";
        } catch (IOException e) {
            model.addAttribute("pageTitle", "Nouveau produit");
            model.addAttribute("activeMenu", "produits");
            model.addAttribute("categories", categorieRepository.findAll());
            model.addAttribute("unites", uniteRepository.findAll());
            model.addAttribute("errorMessage", "Échec de l'upload de l'image.");
            return "backoffice/produits_nouveau";
        }
    }

    @GetMapping("/ventes")
    public String ventes(Model model) {
        model.addAttribute("pageTitle", "Ventes");
        model.addAttribute("activeMenu", "ventes");
        return "backoffice/ventes";
    }

    @GetMapping("/stock")
    public String stock(Model model) {
        List<MouvementStock> mouvements = stockService.getAllMouvements();

        BigDecimal totalEntree = BigDecimal.ZERO;
        BigDecimal totalSortie = BigDecimal.ZERO;
        BigDecimal totalAjustement = BigDecimal.ZERO;

        for (MouvementStock m : mouvements) {
            if (m.getTypeMouvement() == null || m.getTypeMouvement().getLibelle() == null || m.getQuantite() == null) {
                continue;
            }
            String t = m.getTypeMouvement().getLibelle().trim().toUpperCase();
            if ("ENTREE".equals(t)) {
                totalEntree = totalEntree.add(m.getQuantite());
            } else if ("SORTIE".equals(t)) {
                totalSortie = totalSortie.add(m.getQuantite());
            } else {
                totalAjustement = totalAjustement.add(m.getQuantite());
            }
        }

        model.addAttribute("pageTitle", "Stock");
        model.addAttribute("activeMenu", "stock");
        model.addAttribute("mouvements", mouvements);
        model.addAttribute("totalEntree", totalEntree);
        model.addAttribute("totalSortie", totalSortie);
        model.addAttribute("totalAjustement", totalAjustement);
        return "backoffice/stock";
    }

    @GetMapping("/clients")
    public String clients(Model model) {
        model.addAttribute("pageTitle", "Clients");
        model.addAttribute("activeMenu", "clients");
        return "backoffice/clients";
    }

    @GetMapping("/rapports")
    public String rapports(Model model) {
        model.addAttribute("pageTitle", "Rapports");
        model.addAttribute("activeMenu", "rapports");
        return "backoffice/rapports";
    }
}
