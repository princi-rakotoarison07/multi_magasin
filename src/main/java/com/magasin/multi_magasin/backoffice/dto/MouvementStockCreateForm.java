package com.magasin.multi_magasin.backoffice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MouvementStockCreateForm {

    @NotNull(message = "Le type de mouvement est obligatoire")
    private Long typeMouvementId;

    @NotNull(message = "La date est obligatoire")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateMouvement;

    @NotEmpty(message = "Vous devez ajouter au moins une ligne")
    @Valid
    private List<LigneMouvementCreateForm> lignes = new ArrayList<>();

    public Long getTypeMouvementId() {
        return typeMouvementId;
    }

    public void setTypeMouvementId(Long typeMouvementId) {
        this.typeMouvementId = typeMouvementId;
    }

    public LocalDate getDateMouvement() {
        return dateMouvement;
    }

    public void setDateMouvement(LocalDate dateMouvement) {
        this.dateMouvement = dateMouvement;
    }

    public List<LigneMouvementCreateForm> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneMouvementCreateForm> lignes) {
        this.lignes = lignes;
    }

    public static class LigneMouvementCreateForm {
        @NotNull(message = "Le produit est obligatoire")
        private Long produitId;

        @NotNull(message = "La quantité est obligatoire")
        @DecimalMin(value = "0.001", message = "La quantité doit être supérieure à 0")
        private BigDecimal quantite;

        public Long getProduitId() {
            return produitId;
        }

        public void setProduitId(Long produitId) {
            this.produitId = produitId;
        }

        public BigDecimal getQuantite() {
            return quantite;
        }

        public void setQuantite(BigDecimal quantite) {
            this.quantite = quantite;
        }
    }
}
