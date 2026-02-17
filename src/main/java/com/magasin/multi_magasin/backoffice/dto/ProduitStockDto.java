package com.magasin.multi_magasin.backoffice.dto;

import com.magasin.multi_magasin.domain.entity.Produit;
import java.math.BigDecimal;

public class ProduitStockDto {
    private Produit produit;
    private BigDecimal stockActuel;

    public ProduitStockDto(Produit produit, BigDecimal stockActuel) {
        this.produit = produit;
        this.stockActuel = stockActuel;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public BigDecimal getStockActuel() {
        return stockActuel;
    }

    public void setStockActuel(BigDecimal stockActuel) {
        this.stockActuel = stockActuel;
    }
}
