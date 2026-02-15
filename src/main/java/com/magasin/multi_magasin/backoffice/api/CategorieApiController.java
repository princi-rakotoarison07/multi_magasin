package com.magasin.multi_magasin.backoffice.api;

import com.magasin.multi_magasin.domain.entity.Categorie;
import com.magasin.multi_magasin.repository.CategorieRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/backOffice/api/categories")
public class CategorieApiController {

    private final CategorieRepository categorieRepository;

    public CategorieApiController(CategorieRepository categorieRepository) {
        this.categorieRepository = categorieRepository;
    }

    public record CreateCategorieRequest(
            @NotBlank @Size(max = 100) String libelle
    ) {
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateCategorieRequest req) {
        String libelle = req.libelle() == null ? "" : req.libelle().trim();
        if (libelle.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Libellé requis"));
        }

        return categorieRepository.findByLibelleIgnoreCase(libelle)
                .<ResponseEntity<?>>map(existing -> ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Cette catégorie existe déjà", "id", existing.getId(), "libelle", existing.getLibelle())))
                .orElseGet(() -> {
                    Categorie c = new Categorie();
                    c.setLibelle(libelle);
                    Categorie saved = categorieRepository.save(c);
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(Map.of("id", saved.getId(), "libelle", saved.getLibelle()));
                });
    }
}
