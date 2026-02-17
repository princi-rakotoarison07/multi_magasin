package com.magasin.multi_magasin.backoffice;

import com.magasin.multi_magasin.backoffice.dto.ProduitCreateForm;
import com.magasin.multi_magasin.domain.entity.PrixUnitaire;
import com.magasin.multi_magasin.domain.entity.Produit;
import com.magasin.multi_magasin.domain.entity.MouvementStock;
import com.magasin.multi_magasin.domain.entity.Client;
import com.magasin.multi_magasin.repository.CategorieRepository;
import com.magasin.multi_magasin.repository.UniteRepository;
import com.magasin.multi_magasin.repository.ClientRepository;
import com.magasin.multi_magasin.repository.ProduitRepository;
import com.magasin.multi_magasin.service.FileStorageService;
import com.magasin.multi_magasin.service.ProduitService;
import com.magasin.multi_magasin.service.StockService;
import com.magasin.multi_magasin.service.VenteService;
import com.magasin.multi_magasin.domain.entity.Vente;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.itextpdf.text.DocumentException;

@Controller
@RequestMapping("/backOffice")
public class BackOfficeController {

    private final ProduitService produitService;
    private final CategorieRepository categorieRepository;
    private final UniteRepository uniteRepository;
    private final ClientRepository clientRepository;
    private final FileStorageService fileStorageService;
    private final StockService stockService;
    private final VenteService venteService;

    private final ProduitRepository produitRepository;

    public BackOfficeController(
            ProduitService produitService,
            CategorieRepository categorieRepository,
            UniteRepository uniteRepository,
            ClientRepository clientRepository,
            FileStorageService fileStorageService,
            StockService stockService,
            VenteService venteService,
            ProduitRepository produitRepository
    ) {
        this.produitService = produitService;
        this.categorieRepository = categorieRepository;
        this.uniteRepository = uniteRepository;
        this.clientRepository = clientRepository;
        this.fileStorageService = fileStorageService;
        this.stockService = stockService;
        this.venteService = venteService;
        this.produitRepository = produitRepository;
    }

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        // KPI: Ventes du jour
        LocalDateTime today = LocalDateTime.now();
        long ventesDuJour = venteService.countVentesByDate(today);
        
        // KPI: Évolution vs Hier (Ventes)
        LocalDateTime yesterday = today.minusDays(1);
        long ventesHier = venteService.countVentesByDate(yesterday);
        
        double evolutionVentes = 0;
        if (ventesHier > 0) {
            evolutionVentes = ((double) (ventesDuJour - ventesHier) / ventesHier) * 100;
        } else if (ventesDuJour > 0) {
            evolutionVentes = 100; // Si hier 0 et auj > 0, +100% (ou infini)
        }

        // KPI: Chiffre d'affaires du jour
        BigDecimal caDuJour = venteService.sumTotalByDate(today);
        if (caDuJour == null) caDuJour = BigDecimal.ZERO;

        // KPI: Évolution CA vs Hier
        BigDecimal caHier = venteService.sumTotalByDate(yesterday);
        if (caHier == null) caHier = BigDecimal.ZERO;
        
        double evolutionCA = 0;
        if (caHier.compareTo(BigDecimal.ZERO) > 0) {
            evolutionCA = caDuJour.subtract(caHier).divide(caHier, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100)).doubleValue();
        } else if (caDuJour.compareTo(BigDecimal.ZERO) > 0) {
            evolutionCA = 100;
        }

        // KPI: Produits actifs
            long produitsActifs = produitRepository.countByActiveTrue();

        // Dernières ventes
        List<Vente> dernieresVentes = venteService.getRecentVentes();

        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("ventesDuJour", ventesDuJour);
        model.addAttribute("evolutionVentes", evolutionVentes);
        model.addAttribute("caDuJour", caDuJour);
        model.addAttribute("evolutionCA", evolutionCA);
        model.addAttribute("produitsActifs", produitsActifs);
        model.addAttribute("dernieresVentes", dernieresVentes);
        
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
            return "redirect:/multi_magasin/backOffice/produits";
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
        String redirectUrl = "redirect:/multi_magasin/backOffice/produits";
        
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
        String redirectUrl = "redirect:/multi_magasin/backOffice/produits";
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

    /*
    // Moved to VenteController
    @GetMapping("/ventes")
    public String ventes(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin,
            Model model
    ) {
        if (dateDebut == null && dateFin == null) {
            dateDebut = LocalDate.now();
            dateFin = LocalDate.now();
        } else if (dateDebut == null) {
            dateDebut = dateFin;
        } else if (dateFin == null) {
            dateFin = dateDebut;
        }

        model.addAttribute("pageTitle", "Ventes");
        model.addAttribute("activeMenu", "ventes");
        model.addAttribute("ventes", venteService.getVentesByDateRange(dateDebut, dateFin));
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);
        return "backoffice/ventes";
    }

    @GetMapping("/ventes/export/pdf")
    public ResponseEntity<byte[]> exportVentesPdf(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin,
            @RequestParam(defaultValue = "simple") String type
    ) throws DocumentException {
        if (dateDebut == null && dateFin == null) {
            dateDebut = LocalDate.now();
            dateFin = LocalDate.now();
        } else if (dateDebut == null) {
            dateDebut = dateFin;
        } else if (dateFin == null) {
            dateFin = dateDebut;
        }

        List<Vente> ventes = venteService.getVentesByDateRange(dateDebut, dateFin);
        String dateRange = dateDebut.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " + dateFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        // Mock username for now, or get from SecurityContext
        String username = "Admin"; 

        byte[] pdfBytes = pdfService.generateVentesPdf(ventes, type, dateRange, username);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "ventes_" + type + "_" + LocalDate.now().toString() + ".pdf";
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
    */

    @GetMapping("/clients")
    public String clients(
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        List<Client> clients;
        if (keyword != null && !keyword.trim().isEmpty()) {
            clients = clientRepository.findByNomContainingIgnoreCase(keyword.trim());
        } else {
            clients = clientRepository.findAll();
        }
        
        model.addAttribute("pageTitle", "Clients");
        model.addAttribute("activeMenu", "clients");
        model.addAttribute("clients", clients);
        model.addAttribute("totalClients", clients.size());
        model.addAttribute("searchKeyword", keyword);
        return "backoffice/clients";
    }

    @GetMapping("/clients/modifier/{id}")
    public String modifierClient(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Client> clientOpt = clientRepository.findById(id);
            if (clientOpt.isPresent()) {
                model.addAttribute("pageTitle", "Modifier un client");
                model.addAttribute("activeMenu", "clients");
                model.addAttribute("client", clientOpt.get());
                return "backoffice/clients_modifier";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Client non trouvé.");
                return "redirect:/multi_magasin/backOffice/clients";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors du chargement du client: " + e.getMessage());
            return "redirect:/multi_magasin/backOffice/clients";
        }
    }

    @PostMapping("/clients/modifier/{id}")
    public String updateClient(
            @PathVariable Long id,
            @RequestParam String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String adresse,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Optional<Client> clientOpt = clientRepository.findById(id);
            if (clientOpt.isPresent()) {
                Client client = clientOpt.get();
                client.setNom(nom.trim());
                
                if (prenom != null && !prenom.trim().isEmpty()) {
                    client.setPrenom(prenom.trim());
                } else {
                    client.setPrenom(null);
                }
                
                if (telephone != null && !telephone.trim().isEmpty()) {
                    client.setTelephone(telephone.trim());
                } else {
                    client.setTelephone(null);
                }
                
                if (email != null && !email.trim().isEmpty()) {
                    client.setEmail(email.trim());
                } else {
                    client.setEmail(null);
                }
                
                if (adresse != null && !adresse.trim().isEmpty()) {
                    client.setAdresse(adresse.trim());
                } else {
                    client.setAdresse(null);
                }
                
                clientRepository.save(client);
                redirectAttributes.addFlashAttribute("successMessage", "Client modifié avec succès.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Client non trouvé.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la modification du client: " + e.getMessage());
        }
        
        return "redirect:/multi_magasin/backOffice/clients";
    }

    @GetMapping("/clients/supprimer/{id}")
    public String supprimerClient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Client> clientOpt = clientRepository.findById(id);
            if (clientOpt.isPresent()) {
                clientRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Client supprimé avec succès.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Client non trouvé.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la suppression du client: " + e.getMessage());
        }
        
        return "redirect:/multi_magasin/backOffice/clients";
    }

    @GetMapping("/clients/nouveau")
    public String nouveauClient(Model model) {
        model.addAttribute("pageTitle", "Nouveau client");
        model.addAttribute("activeMenu", "clients");
        return "backoffice/clients_nouveau";
    }

    @PostMapping("/clients/nouveau")
    public String createClientFromForm(
            @RequestParam String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String adresse,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Client client = new Client();
            client.setNom(nom.trim());
            
            if (prenom != null && !prenom.trim().isEmpty()) {
                client.setPrenom(prenom.trim());
            }
            
            if (telephone != null && !telephone.trim().isEmpty()) {
                client.setTelephone(telephone.trim());
            }
            
            if (email != null && !email.trim().isEmpty()) {
                client.setEmail(email.trim());
            }
            
            if (adresse != null && !adresse.trim().isEmpty()) {
                client.setAdresse(adresse.trim());
            }
            
            clientRepository.save(client);
            redirectAttributes.addFlashAttribute("successMessage", "Client créé avec succès.");
            return "redirect:/multi_magasin/backOffice/clients";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la création du client: " + e.getMessage());
            return "redirect:/multi_magasin/backOffice/clients/nouveau";
        }
    }

    @PostMapping("/clients")
    @ResponseBody
    public Map<String, Object> createClient(@RequestBody Map<String, String> clientData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Client client = new Client();
            client.setNom(clientData.get("nom"));
            
            String prenom = clientData.get("prenom");
            if (prenom != null && !prenom.trim().isEmpty()) {
                client.setPrenom(prenom.trim());
            }
            
            String telephone = clientData.get("telephone");
            if (telephone != null && !telephone.trim().isEmpty()) {
                client.setTelephone(telephone.trim());
            }
            
            String email = clientData.get("email");
            if (email != null && !email.trim().isEmpty()) {
                client.setEmail(email.trim());
            }
            
            String adresse = clientData.get("adresse");
            if (adresse != null && !adresse.trim().isEmpty()) {
                client.setAdresse(adresse.trim());
            }
            
            clientRepository.save(client);
            
            response.put("success", true);
            response.put("message", "Client créé avec succès");
            response.put("clientId", client.getId());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la création du client: " + e.getMessage());
        }
        
        return response;
    }
}
