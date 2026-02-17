package com.magasin.multi_magasin.backoffice;

import com.itextpdf.text.DocumentException;
import com.magasin.multi_magasin.domain.entity.Vente;
import com.magasin.multi_magasin.service.PdfService;
import com.magasin.multi_magasin.service.VenteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/backOffice/ventes")
public class VenteController {

    private final VenteService venteService;
    private final PdfService pdfService;

    public VenteController(VenteService venteService, PdfService pdfService) {
        this.venteService = venteService;
        this.pdfService = pdfService;
    }

    @GetMapping
    public String indexRoot(Model model) {
        return "redirect:/multi_magasin/backOffice/ventes/historique";
    }

    @GetMapping("/historique")
    public String index(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFin,
            Model model
    ) {
        List<Vente> ventes;
        if (dateDebut != null || dateFin != null) {
            if (dateDebut == null) dateDebut = dateFin;
            if (dateFin == null) dateFin = dateDebut;
            ventes = venteService.getVentesByDateRange(dateDebut, dateFin);
        } else {
            ventes = venteService.getAllVentes();
        }

        model.addAttribute("activeMenu", "ventes");
        model.addAttribute("ventes", ventes);
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);
        return "backoffice/ventes_historique";
    }

    @GetMapping("/export/pdf")
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
        
        String username = "Admin"; 

        byte[] pdfBytes = pdfService.generateVentesPdf(ventes, type, dateRange, username);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "ventes_" + type + "_" + LocalDate.now().toString() + ".pdf";
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("activeMenu", "ventes");
        Vente vente = venteService.getVenteById(id);
        model.addAttribute("vente", vente);
        return "backoffice/vente_details"; 
    }
}
