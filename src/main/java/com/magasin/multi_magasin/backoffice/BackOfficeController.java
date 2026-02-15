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
import com.magasin.multi_magasin.service.VenteService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("/backOffice")
public class BackOfficeController {

    private final ProduitService produitService;
    private final CategorieRepository categorieRepository;
    private final UniteRepository uniteRepository;
    private final FileStorageService fileStorageService;
    private final StockService stockService;
    private final VenteService venteService;

    public BackOfficeController(
            ProduitService produitService,
            CategorieRepository categorieRepository,
            UniteRepository uniteRepository,
            FileStorageService fileStorageService,
            StockService stockService,
            VenteService venteService
    ) {
        this.produitService = produitService;
        this.categorieRepository = categorieRepository;
        this.uniteRepository = uniteRepository;
        this.fileStorageService = fileStorageService;
        this.stockService = stockService;
        this.venteService = venteService;
    }

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("activeMenu", "dashboard");
        return "backoffice/dashboard";
    }

    @GetMapping("/produits")
    public String produits(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categorieId,
            Model model
    ) {
        List<Produit> produits = produitService.searchProduits(keyword, categorieId);
        produits.forEach(p -> p.getPrixUnitaires().sort(Comparator.comparing(PrixUnitaire::getId)));

        model.addAttribute("pageTitle", "Produits");
        model.addAttribute("activeMenu", "produits");
        model.addAttribute("produits", produits);
            model.addAttribute("totalProduits", produits.size());
            model.addAttribute("categories", categorieRepository.findAll());
            model.addAttribute("unites", uniteRepository.findAll());
            model.addAttribute("searchKeyword", keyword);
            model.addAttribute("searchCategorieId", categorieId);
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
            @Valid @ModelAttribute("form") ProduitCreateForm form,
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

    @PostMapping("/produits/{id}")
    public String updateProduit(
            @PathVariable Long id,
            @ModelAttribute("form") ProduitCreateForm form,
            BindingResult bindingResult,
            @RequestParam(name = "photo", required = false) MultipartFile photo,
            @RequestParam(name = "currentKeyword", required = false) String currentKeyword,
            @RequestParam(name = "currentCategorieId", required = false) Long currentCategorieId,
            RedirectAttributes redirectAttributes
    ) {
        String redirectUrl = "redirect:/backOffice/produits";
        
        StringBuilder queryParams = new StringBuilder();
        if (currentKeyword != null && !currentKeyword.isBlank()) {
            queryParams.append("?keyword=").append(URLEncoder.encode(currentKeyword, StandardCharsets.UTF_8));
        }
        if (currentCategorieId != null) {
            queryParams.append(queryParams.length() == 0 ? "?" : "&").append("categorieId=").append(currentCategorieId);
        }
        redirectUrl += queryParams.toString();

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur de validation. Vérifie les champs.");
            return redirectUrl;
        }

        try {
            String photoUrl = null;
            if (photo != null && !photo.isEmpty()) {
                photoUrl = fileStorageService.storeImage(photo);
            }
            produitService.updateProduit(id, form, photoUrl);
            redirectAttributes.addFlashAttribute("successMessage", "Produit mis à jour avec succès.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur de contrainte (doublon probable).");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'upload de l'image.");
        }

        return redirectUrl;
    }

    @PostMapping("/produits/delete/{id}")
    public String deleteProduit(
            @PathVariable Long id,
            @RequestParam(name = "currentKeyword", required = false) String currentKeyword,
            @RequestParam(name = "currentCategorieId", required = false) Long currentCategorieId,
            RedirectAttributes redirectAttributes
    ) {
        String redirectUrl = "redirect:/backOffice/produits";
        if (currentKeyword != null && !currentKeyword.isBlank()) {
            redirectUrl += "?keyword=" + URLEncoder.encode(currentKeyword, StandardCharsets.UTF_8);
        }
        if (currentCategorieId != null) {
            redirectUrl += (redirectUrl.contains("?") ? "&" : "?") + "categorieId=" + currentCategorieId;
        }

        try {
            produitService.deleteProduit(id);
            redirectAttributes.addFlashAttribute("successMessage", "Produit supprimé avec succès.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Impossible de supprimer ce produit car il est utilisé ailleurs (ventes, stocks, etc.).");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la suppression.");
        }

        return redirectUrl;
    }

    @GetMapping("/ventes")
    public String ventes(Model model) {
        model.addAttribute("pageTitle", "Ventes");
        model.addAttribute("activeMenu", "ventes");
        model.addAttribute("ventes", venteService.getAllVentes());
        return "backoffice/ventes";
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
