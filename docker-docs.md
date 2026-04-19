
# Docker - Documentation (projet `multi_magasin`)

Ce document explique **globalement** le rôle et le fonctionnement des 3 fichiers Docker du projet :

- **`.dockerignore`**
- **`Dockerfile`**
- **`docker-compose.yml`**

L’objectif est de comprendre comment l’application Spring Boot et la base MySQL sont lancées ensemble avec Docker Compose.

## 1) Hypothèses (confirmées par les fichiers)

À partir du contenu réel des fichiers, on peut confirmer :

- **Base de données** : MySQL **8.4** (service `db` dans `docker-compose.yml`).
- **Application** : image construite localement via le **`Dockerfile`** (service `app` utilise `build: .`).
- **Port HTTP** : l’application écoute sur **8080**.
  - Confirmé par `EXPOSE 8080` dans le `Dockerfile`.
  - Confirmé par le mapping `"8080:8080"` dans `docker-compose.yml`.
  - Confirmé par `SERVER_PORT=8080` dans les variables d’environnement du service `app`.
- **Données persistantes** :
  - Les données MySQL sont persistées dans un volume Docker nommé **`db_data`**.
  - Les fichiers uploadés sont persistés dans un volume Docker nommé **`uploads_data`**.
- **Initialisation DB** : le dossier local `./database` est monté dans `/docker-entrypoint-initdb.d`.
  - Cela signifie que les scripts SQL présents dans `./database` peuvent être exécutés automatiquement **au premier démarrage** du conteneur MySQL.

## 2) `.dockerignore` : réduire le contexte de build

### Rôle

Le fichier **`.dockerignore`** indique à Docker quels fichiers/dossiers **ne doivent pas** être envoyés dans le **contexte de build** (c’est-à-dire ce que Docker transfère au daemon lors de `docker build`).

### Contenu et effet

Le projet ignore :

- `target`
  - Dossier Maven contenant les artefacts compilés.
  - Intérêt : éviter de transférer des jars/outputs déjà construits (et éviter des incohérences de build).
- `.git`, `.gitignore`
  - Métadonnées Git inutiles pour construire l’image.
- `.mvn`
  - Le dossier du Maven Wrapper.
  - Dans ce projet, le build Docker utilise une image `maven:...` et lance `mvn ...`, donc ce dossier n’est pas nécessaire à l’intérieur de l’image (tant que `pom.xml` et `src/` suffisent).
- `.idea`, `.vscode`, `*.iml`
  - Fichiers/paramètres d’IDE.

### Pourquoi c’est important

- **Build plus rapide** : moins de données envoyées.
- **Cache Docker plus stable** : de petits changements locaux (IDE/Git) n’invalident pas le cache.
- **Images plus propres** : rien d’inutile ne se retrouve accidentellement dans l’image.

## 3) `Dockerfile` : construire et exécuter l’application (multi-stage)

Le `Dockerfile` construit un jar Spring Boot avec Maven puis crée une image runtime plus légère.

### Étape 1 : build Maven

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests package
```

- **Image de build** : `maven:3.9-eclipse-temurin-17`
  - Maven 3.9 + JDK 17 (Temurin).
- **`WORKDIR /app`** : le build se fait dans `/app`.
- **Copies minimales** : on copie `pom.xml` et `src/` seulement.
- **Build** : `mvn -DskipTests package` produit un jar dans `target/`.
  - Hypothèse confirmée : le jar final est récupéré via `target/*.jar` à l’étape suivante.

### Étape 2 : runtime JRE

```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

- **Image d’exécution** : `eclipse-temurin:17-jre`
  - On exécute uniquement avec un JRE, donc l’image finale est généralement plus petite.
- **Copie du jar** depuis l’étape `build`.
- **Port** : `EXPOSE 8080` documente le port interne de l’app.
- **Commande de démarrage** : `java -jar /app/app.jar`.

### Conséquence importante

- Le `Dockerfile` ne définit pas les variables d’environnement (DB, port, etc.).
  - Elles sont injectées par `docker-compose.yml`.

## 4) `docker-compose.yml` : orchestrer `db` + `app`

Ce fichier lance **deux services** sur le même réseau Docker par défaut :

- `db` (MySQL)
- `app` (Spring Boot)

### Service `db`

```yaml
db:
  image: mysql:8.4
  container_name: multi_magasin_db
  environment:
    MYSQL_DATABASE: multi_magasin
    MYSQL_ROOT_PASSWORD: root
    TZ: UTC
  volumes:
    - db_data:/var/lib/mysql
    - ./database:/docker-entrypoint-initdb.d
  healthcheck:
    test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-proot"]
    interval: 5s
    timeout: 5s
    retries: 20
```

- **Image** : `mysql:8.4`.
- **Variables d’environnement** :
  - `MYSQL_DATABASE` : base à créer.
  - `MYSQL_ROOT_PASSWORD` : mot de passe root.
  - `TZ` : timezone.
- **Volumes** :
  - `db_data:/var/lib/mysql` : persistance des données.
  - `./database:/docker-entrypoint-initdb.d` : scripts SQL d’initialisation.
- **Healthcheck** :
  - Vérifie que MySQL répond (`mysqladmin ping`).

Note confirmée : **le port 3306 n’est pas exposé sur la machine hôte** (pas de `ports:` sur `db`).

### Service `app`

```yaml
app:
  build: .
  container_name: multi_magasin_app
  depends_on:
    db:
      condition: service_healthy
  environment:
    SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/multi_magasin?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    SPRING_DATASOURCE_USERNAME: root
    SPRING_DATASOURCE_PASSWORD: root
    SERVER_PORT: 8080
    FILE_UPLOAD_DIR: /data/uploads
  ports:
    - "8080:8080"
  volumes:
    - uploads_data:/data/uploads
```

- **Build** : `build: .` déclenche la construction via le `Dockerfile`.
- **Ordre de démarrage** : l’app attend que `db` soit **healthy**.
- **Connexion DB** :
  - URL MySQL vers `db:3306`.
  - `db` est le nom du service, résolu par DNS interne Docker.
- **Exposition du port** : `8080:8080` rend l’app accessible sur `localhost:8080`.
- **Uploads persistants** :
  - Chemin interne `/data/uploads`.
  - Volume `uploads_data` monté sur ce chemin.

### Volumes déclarés

```yaml
volumes:
  db_data:
  uploads_data:
```

- `db_data` : persistance MySQL.
- `uploads_data` : persistance des fichiers uploadés.

## 5) Flux de démarrage (résumé)

Quand tu lances :

```bash
docker compose up --build
```

- Docker Compose démarre `db`.
- MySQL initialise la base `multi_magasin` et exécute (au premier démarrage) les scripts de `./database`.
- Le healthcheck passe au vert.
- Compose construit puis démarre `app`.
- L’application devient accessible sur `http://localhost:8080`.

