package com.magasin.multi_magasin.domain.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mouvement_stock")
public class MouvementStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_mouvement_id", nullable = false)
    private TypeMouvement typeMouvement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unite_id", nullable = false)
    private Unite unite;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_vente_id")
    private Vente referenceVente;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public TypeMouvement getTypeMouvement() {
        return typeMouvement;
    }

    public void setTypeMouvement(TypeMouvement typeMouvement) {
        this.typeMouvement = typeMouvement;
    }

    public Unite getUnite() {
        return unite;
    }

    public void setUnite(Unite unite) {
        this.unite = unite;
    }

    public BigDecimal getQuantite() {
        return quantite;
    }

    public void setQuantite(BigDecimal quantite) {
        this.quantite = quantite;
    }

    public Vente getReferenceVente() {
        return referenceVente;
    }

    public void setReferenceVente(Vente referenceVente) {
        this.referenceVente = referenceVente;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
