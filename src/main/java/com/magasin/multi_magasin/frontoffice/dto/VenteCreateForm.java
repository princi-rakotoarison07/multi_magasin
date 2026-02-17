package com.magasin.multi_magasin.frontoffice.dto;

import java.math.BigDecimal;
import java.util.List;

public class VenteCreateForm {
    private List<LigneVenteDto> items;
    private BigDecimal total;
    private PaymentDto payment;
    private Long clientId;

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public List<LigneVenteDto> getItems() {
        return items;
    }

    public void setItems(List<LigneVenteDto> items) {
        this.items = items;
    }

    public PaymentDto getPayment() {
        return payment;
    }

    public void setPayment(PaymentDto payment) {
        this.payment = payment;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public static class LigneVenteDto {
        private Long produitId;
        private BigDecimal quantite;
        private BigDecimal prix;

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

        public BigDecimal getPrix() {
            return prix;
        }

        public void setPrix(BigDecimal prix) {
            this.prix = prix;
        }
    }

    public static class PaymentDto {
        private Long typePaiementId;
        private BigDecimal montant;
        private String reference;

        public Long getTypePaiementId() {
            return typePaiementId;
        }

        public void setTypePaiementId(Long typePaiementId) {
            this.typePaiementId = typePaiementId;
        }

        public BigDecimal getMontant() {
            return montant;
        }

        public void setMontant(BigDecimal montant) {
            this.montant = montant;
        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }
    }
}
