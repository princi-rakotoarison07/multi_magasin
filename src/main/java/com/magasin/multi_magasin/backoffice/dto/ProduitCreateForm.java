package com.magasin.multi_magasin.backoffice.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ProduitCreateForm {

    @NotBlank
    @Size(max = 150)
    private String nom;

    @Size(max = 100)
    private String codeBarre;

    private Long categorieId;

    private String designation;

    private boolean active = true;

    @NotNull
    private Long uniteId;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    private BigDecimal prix;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getCodeBarre() {
        return codeBarre;
    }

    public void setCodeBarre(String codeBarre) {
        this.codeBarre = codeBarre;
    }

    public Long getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(Long categorieId) {
        this.categorieId = categorieId;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getUniteId() {
        return uniteId;
    }

    public void setUniteId(Long uniteId) {
        this.uniteId = uniteId;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }
}
