# Multi-Magasin - Système de Gestion de Point de Vente (POS)

## Description
Multi-Magasin est une application complète de gestion de point de vente et de stock, développée avec Spring Boot et Thymeleaf. Elle offre une solution intégrée pour gérer les ventes, les clients, le stock et les rapports financiers d'un magasin.

L'application est divisée en deux modules principaux :
*   **FrontOffice (Caisse)** : Interface optimisée pour la prise de commande rapide, la gestion du panier et les paiements multiples.
*   **BackOffice (Administration)** : Tableau de bord de gestion pour le suivi des KPI, la gestion des produits, des stocks et des clients.

## Fonctionnalités Clés

### 🏪 FrontOffice (Caisse)
*   **Point de Vente (POS)** : Interface intuitive pour les caissiers.
*   **Recherche Intelligente** : Recherche rapide de produits (nom, code-barre, catégorie) et de clients (nom, prénom, téléphone, email).
*   **Gestion du Panier** : Ajout/suppression d'articles, calcul automatique des totaux.
*   **Paiements Multiples** : Support pour Espèces, Mobile Money (MVola, Airtel, Orange, Telma), Chèques.
*   **Gestion Clients** : Création rapide de nouveaux clients et assignation aux ventes.
*   **Contrôle de Stock** : Vérification en temps réel de la disponibilité des produits avant validation.

### ⚙️ BackOffice (Administration)
*   **Tableau de Bord (Dashboard)** :
    *   Ventes du jour et évolution vs J-1.
    *   Chiffre d'affaires du jour et évolution.
    *   Nombre de produits actifs.
    *   Liste des 5 dernières ventes en temps réel.
*   **Gestion des Produits** : Ajout, modification, désactivation, gestion des prix unitaires.
*   **Gestion des Stocks** : Suivi des mouvements (Entrées/Sorties), inventaire.
*   **Rapports** : Visualisation des performances de vente.

## Stack Technique

*   **Backend** : Java 17, Spring Boot 3+ (Spring MVC, Spring Data JPA).
*   **Base de Données** : MySQL 8.0.
*   **Frontend** : Thymeleaf, Tailwind CSS (Design), Alpine.js (Interactivité).
*   **Build Tool** : Maven.
*   **Dépendances Clés** : `mysql-connector-j`, `itextpdf` (génération PDF), `spring-boot-starter-validation`.

## Prérequis

*   Java JDK 17 ou supérieur.
*   MySQL Server.
*   Maven (optionnel, le wrapper `mvnw` est inclus).

## Installation et Démarrage (Docker)

### Prérequis

*   Docker Desktop (avec Docker Compose).

### Démarrage

1.  Ouvrez un terminal dans le dossier racine du projet.
2.  Lancez :
    ```bash
    docker compose up --build
    ```

### Accès à l'application

*   **Caisse (FrontOffice)** : [http://localhost:8080/multi_magasin/frontOffice](http://localhost:8080/multi_magasin/frontOffice)
*   **Administration (BackOffice)** : [http://localhost:8080/multi_magasin/backOffice](http://localhost:8080/multi_magasin/backOffice)

### Notes importantes (Docker)

*   La base MySQL est démarrée dans un conteneur `db`.
*   Par défaut, **le port MySQL n'est pas exposé sur ta machine** (pas de conflit avec un MySQL local). L'application y accède via le réseau Docker.
*   Les scripts SQL du dossier `./database` sont exécutés automatiquement au premier démarrage de la base (via `docker-entrypoint-initdb.d`).
*   Les images uploadées sont stockées dans un volume Docker (`uploads_data`) via `FILE_UPLOAD_DIR=/data/uploads`.

### (Optionnel) Exposer MySQL sur ta machine

Si tu veux te connecter à MySQL depuis ton PC (Workbench, DBeaver, etc.), tu peux exposer le port en modifiant `docker-compose.yml` (service `db`) :

```yaml
ports:
  - "3307:3306"
```

Puis, tu te connectes à `localhost:3307`.

## Installation et Démarrage (Local)

1.  **Préparation de l'Environnement**
    *   Assurez-vous d'avoir le dossier du projet sur votre machine.
    *   Installez [Java JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html).
    *   Installez [MySQL Server](https://dev.mysql.com/downloads/installer/).

2.  **Configuration de la Base de Données**
    *   Lancez votre serveur MySQL.
    *   Créez une nouvelle base de données vide nommée `multi_magasin`.
        ```sql
        CREATE DATABASE multi_magasin;
        ```
    *   Initialise le schéma et quelques données (recommandé) :
        *   Exécute `database/base.sql` (tables + données de base)
        *   Puis exécute `database/donnee.sql` (données complémentaires)
    *   Configurez la connexion via variables d'environnement (ou laissez les valeurs par défaut) :
        ```properties
        spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/multi_magasin?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}
        spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}
        spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}
        file.upload-dir=${FILE_UPLOAD_DIR:uploads}
        ```

3.  **Lancement de l'Application**
    *   Ouvrez un terminal (Invite de commandes ou PowerShell) dans le dossier racine du projet.
    *   Lancez l'application via Maven :
        ```bash
        mvn spring-boot:run
        ```
    *   *Si Maven n'est pas installé dans votre PATH, utilisez le wrapper fourni :*
        ```bash
        # Sous Windows
        ./mvnw spring-boot:run
        ```

4.  **Accès à l'Application**
    *   Ouvrez votre navigateur web.
    *   **Caisse (FrontOffice)** : [http://localhost:8080/multi_magasin/frontOffice](http://localhost:8080/multi_magasin/frontOffice)
    *   **Administration (BackOffice)** : [http://localhost:8080/multi_magasin/backOffice](http://localhost:8080/multi_magasin/backOffice)

## Structure du Projet

```
src/main/java/com/magasin/multi_magasin/
├── backoffice/       # Contrôleurs pour l'administration
├── frontoffice/      # Contrôleurs et DTO pour la caisse
├── domain/entity/    # Entités JPA (Produit, Vente, Client, etc.)
├── repository/       # Interfaces Spring Data JPA
├── service/          # Logique métier (VenteService, ProduitService)
└── ...
```

## Auteurs
*   Projet développé pour la gestion de magasin multi-points de vente.
