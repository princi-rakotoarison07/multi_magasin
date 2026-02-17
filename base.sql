CREATE DATABASE IF NOT EXISTS multi_magasin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE multi_magasin;

-- ==========================
-- CATEGORIE
-- ==========================
CREATE TABLE IF NOT EXISTS categorie (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- ==========================
-- UNITE
-- ==========================
CREATE TABLE IF NOT EXISTS unite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL,
    symbole VARCHAR(10) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- ==========================
-- CLIENT
-- ==========================
CREATE TABLE IF NOT EXISTS client (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100),
    telephone VARCHAR(20),
    email VARCHAR(150),
    adresse TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ==========================
-- PRODUIT
-- ==========================
CREATE TABLE IF NOT EXISTS produit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    code_barre VARCHAR(100) UNIQUE,
    categorie_id BIGINT,
    designation TEXT,
    photo_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (categorie_id) REFERENCES categorie(id)
) ENGINE=InnoDB;

-- ==========================
-- TYPE_MOUVEMENT
-- ==========================
CREATE TABLE IF NOT EXISTS type_mouvement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- ==========================
-- CAISSE
-- ==========================
CREATE TABLE IF NOT EXISTS caisse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- ==========================
-- TYPE_PAIEMENT
-- ==========================
CREATE TABLE IF NOT EXISTS type_paiement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- ==========================
-- VENTE (AVANT mouvement_stock)
-- ==========================
CREATE TABLE IF NOT EXISTS vente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT,
    total DECIMAL(15,2) DEFAULT 0,
    statut VARCHAR(50) DEFAULT 'EN_ATTENTE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES client(id)
) ENGINE=InnoDB;

-- ==========================
-- PRIX_UNITAIRE
-- ==========================
CREATE TABLE IF NOT EXISTS prix_unitaire (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    produit_id BIGINT NOT NULL,
    unite_id BIGINT NOT NULL,
    prix DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE (produit_id, unite_id),
    FOREIGN KEY (produit_id) REFERENCES produit(id) ON DELETE CASCADE,
    FOREIGN KEY (unite_id) REFERENCES unite(id)
) ENGINE=InnoDB;

-- ==========================
-- DETAIL_VENTE
-- ==========================
CREATE TABLE IF NOT EXISTS detail_vente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vente_id BIGINT NOT NULL,
    produit_id BIGINT NOT NULL,
    unite_id BIGINT NOT NULL,
    quantite DECIMAL(15,3) NOT NULL,
    prix DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (vente_id) REFERENCES vente(id) ON DELETE CASCADE,
    FOREIGN KEY (produit_id) REFERENCES produit(id),
    FOREIGN KEY (unite_id) REFERENCES unite(id)
) ENGINE=InnoDB;

-- ==========================
-- MOUVEMENT_STOCK (MAINTENANT OK)
-- ==========================
CREATE TABLE IF NOT EXISTS mouvement_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    produit_id BIGINT NOT NULL,
    type_mouvement_id BIGINT NOT NULL,
    quantite DECIMAL(15,3) NOT NULL,
    reference_vente_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (produit_id) REFERENCES produit(id) ON DELETE CASCADE,
    FOREIGN KEY (type_mouvement_id) REFERENCES type_mouvement(id),
    FOREIGN KEY (reference_vente_id) REFERENCES vente(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ==========================
-- PAIEMENT
-- ==========================
CREATE TABLE IF NOT EXISTS paiement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_paiement_id BIGINT NOT NULL,
    vente_id BIGINT NOT NULL,
    caisse_id BIGINT,
    montant DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (type_paiement_id) REFERENCES type_paiement(id),
    FOREIGN KEY (vente_id) REFERENCES vente(id),
    FOREIGN KEY (caisse_id) REFERENCES caisse(id)
) ENGINE=InnoDB;

-- ==========================
-- DATA INITIALIZATION
-- ==========================

-- CATEGORIE
INSERT IGNORE INTO categorie (libelle) VALUES 
('Alimentation'), 
('Boissons'), 
('Hygiène'), 
('Entretien');

-- UNITE
INSERT IGNORE INTO unite (libelle, symbole) VALUES 
('Pièce', 'Pce'), 
('Kilogramme', 'Kg'), 
('Litre', 'L'), 
('Boîte', 'Bt'), 
('Paquet', 'Pqt');

-- TYPE_MOUVEMENT
INSERT IGNORE INTO type_mouvement (libelle) VALUES 
('Entrée'), 
('Sortie'), 
('Ajustement');

-- CAISSE
INSERT IGNORE INTO caisse (nom) VALUES 
('Caisse Principale');

-- TYPE_PAIEMENT
INSERT IGNORE INTO type_paiement (libelle) VALUES 
('Espèces'), 
('Mobile Money'), 
('Chèque'), 
('Virement');

-- CLIENT (Sample)
INSERT IGNORE INTO client (nom, prenom, telephone, email, adresse) VALUES 
('Client', 'Passage', '0000000000', NULL, NULL);

-- ==========================
-- DONNÉES DE STOCK INITIALES (PRODUITS + PRIX + STOCK)
-- ==========================

-- 1. Coca-Cola 33cl (Boissons)
INSERT IGNORE INTO produit (nom, code_barre, categorie_id, designation, photo_url, is_active) VALUES 
('Coca-Cola Original 33cl', '5449000000996', 2, 'Boisson gazeuse rafraîchissante aux extraits végétaux.', 'uploads/coca.png', TRUE);

SET @coca_id = (SELECT id FROM produit WHERE code_barre = '5449000000996');

INSERT IGNORE INTO prix_unitaire (produit_id, unite_id, prix) VALUES 
(@coca_id, 1, 2500.00);

INSERT INTO mouvement_stock (produit_id, type_mouvement_id, quantite) VALUES 
(@coca_id, 1, 50.000);


-- 2. Fanta Orange 33cl (Boissons)
INSERT IGNORE INTO produit (nom, code_barre, categorie_id, designation, photo_url, is_active) VALUES 
('Fanta Orange 33cl', '5449000011527', 2, 'Boisson gazeuse pétillante au jus d''orange.', 'uploads/fanta.png', TRUE);

SET @fanta_id = (SELECT id FROM produit WHERE code_barre = '5449000011527');

INSERT IGNORE INTO prix_unitaire (produit_id, unite_id, prix) VALUES 
(@fanta_id, 1, 2500.00);

INSERT INTO mouvement_stock (produit_id, type_mouvement_id, quantite) VALUES 
(@fanta_id, 1, 50.000);


-- 3. Gel Hydroalcoolique (Hygiène)
INSERT IGNORE INTO produit (nom, code_barre, categorie_id, designation, photo_url, is_active) VALUES 
('Gel Hydroalcoolique 100ml', '3700000000001', 3, 'Solution antiseptique pour la désinfection des mains.', 'uploads/hygiene-definition-2751065050.jpg', TRUE);

SET @gel_id = (SELECT id FROM produit WHERE code_barre = '3700000000001');

INSERT IGNORE INTO prix_unitaire (produit_id, unite_id, prix) VALUES 
(@gel_id, 1, 5000.00);

INSERT INTO mouvement_stock (produit_id, type_mouvement_id, quantite) VALUES 
(@gel_id, 1, 20.000);


-- 4. Assiette Santé (Alimentation)
INSERT IGNORE INTO produit (nom, code_barre, categorie_id, designation, photo_url, is_active) VALUES 
('Assiette Santé Équilibrée', '2000000000001', 1, 'Repas complet équilibré (Saumon, Avocat, Légumes).', 'uploads/sante-mentale.jpg', TRUE);

SET @assiette_id = (SELECT id FROM produit WHERE code_barre = '2000000000001');

INSERT IGNORE INTO prix_unitaire (produit_id, unite_id, prix) VALUES 
(@assiette_id, 1, 15000.00);

INSERT INTO mouvement_stock (produit_id, type_mouvement_id, quantite) VALUES 
(@assiette_id, 1, 10.000);

