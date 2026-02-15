package com.magasin.multi_magasin.backoffice.api;

import com.magasin.multi_magasin.domain.entity.Unite;
import com.magasin.multi_magasin.repository.UniteRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/backOffice/api/unites")
public class UniteApiController {

    private final UniteRepository uniteRepository;

    public UniteApiController(UniteRepository uniteRepository) {
        this.uniteRepository = uniteRepository;
    }

    public record CreateUniteRequest(
            @NotBlank @Size(max = 50) String libelle,
            @NotBlank @Size(max = 10) String symbole
    ) {
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateUniteRequest req) {
        String libelle = req.libelle() == null ? "" : req.libelle().trim();
        String symbole = req.symbole() == null ? "" : req.symbole().trim();

        if (libelle.isBlank() || symbole.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Libellé et symbole requis"));
        }

        return uniteRepository.findBySymboleIgnoreCase(symbole)
                .<ResponseEntity<?>>map(existing -> ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "message", "Ce symbole existe déjà",
                                "id", existing.getId(),
                                "libelle", existing.getLibelle(),
                                "symbole", existing.getSymbole()
                        )))
                .orElseGet(() -> {
                    Unite u = new Unite();
                    u.setLibelle(libelle);
                    u.setSymbole(symbole);
                    Unite saved = uniteRepository.save(u);
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(Map.of("id", saved.getId(), "libelle", saved.getLibelle(), "symbole", saved.getSymbole()));
                });
    }
}
