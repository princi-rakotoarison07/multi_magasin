-- Categories
INSERT IGNORE INTO categorie (libelle) VALUES
('Alimentaire'),
('Boisson'),
('Cosmétique');

-- Unités
INSERT IGNORE INTO unite (libelle, symbole) VALUES
('Kilogramme', 'kg'),
('Litre', 'l'),
('Pièce', 'pc');

-- Type mouvement
INSERT IGNORE INTO type_mouvement (libelle) VALUES
('ENTREE'),
('SORTIE'),
('AJUSTEMENT');

-- Type paiement
INSERT IGNORE INTO type_paiement (libelle) VALUES
('ESPECES'),
('CARTE'),
('MOBILE_MONEY');

-- Caisse
INSERT IGNORE INTO caisse (nom) VALUES
('Caisse Principale');

-- Client test
INSERT IGNORE INTO client (nom, prenom, telephone, email)
VALUES ('Rakoto', 'Jean', '0341122334', 'jean@mail.com');
