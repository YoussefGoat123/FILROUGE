# Étape 01 — Initialisation de l'environnement

**Séance associée :** Séance 1 — Mise en place & baseline
**Objectif (cours) :** "Initialiser un nouveau projet dans le langage de votre choix. Aucun template n'est imposé."

## Constat de départ

La machine ne disposait que d'un **JRE** 1.8.0_321 (pas de JDK, donc pas de `javac`) et d'aucun outil de build (`mvn`, `gradle` absents).

## Ce qui a été fait

1. Installation d'un **JDK 21 LTS (Eclipse Temurin)** via `winget` — choix validé avec l'utilisateur (LTS récente plutôt que rester sur Java 8, pour bénéficier des évolutions du langage sur la suite du projet).
2. Création d'un projet **Maven** standard à la racine du repo :
   - `pom.xml` — Java 21, dépendance `junit-jupiter` (tests), plugin `maven-jar-plugin` (jar exécutable, `mainClass = com.hashbreaker.Main`).
   - Arborescence `src/main/java/com/hashbreaker/` et `src/test/java/com/hashbreaker/`.
3. `.gitignore` pour exclure `target/`, `*.class`, fichiers d'IDE, et l'outillage local (`.tools/`).
4. Vérification que la compilation/exécution fonctionne avec le nouveau JDK.

## Point d'attention rencontré

Après l'installation du JDK, le terminal intégré de VSCode continuait à afficher l'ancienne version Java (1.8). Cause : VSCode capture les variables d'environnement (PATH, JAVA_HOME) **au démarrage du processus**, pas à chaque nouvel onglet de terminal. Un redémarrage complet de VSCode (pas juste le terminal) a été nécessaire pour que `java -version` reflète le JDK 21 fraîchement installé.

## Fichiers concernés

- [pom.xml](../pom.xml)
- [.gitignore](../.gitignore)
