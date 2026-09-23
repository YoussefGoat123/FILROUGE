# Guide — Build, Lancement & Tests

Guide pratique, indépendant du journal d'optimisation (`process/`, `syntheses/`) — pour quiconque veut juste **faire tourner le projet** sans lire toute l'histoire des optimisations.

## Prérequis

- **JDK 21** (Eclipse Temurin recommandé)
- **Maven 3.9+** — si `mvn` n'est pas sur le PATH, utiliser le chemin complet, ex : `"$env:LOCALAPPDATA\Programs\apache-maven-3.9.16\bin\mvn"` (Windows, install sans droits admin)
- `JAVA_HOME` doit pointer vers le JDK 21 (nécessaire pour les scripts de `scripts/`)

Vérifier que tout est en place :

```
mvn -version
java -version
```

> 📁 **Toutes les commandes ci-dessous supposent que tu es dans le dossier `MoteurEchecs/`** (`cd MoteurEchecs` depuis la racine du dépôt).

## Build & Tests

```
mvn test          # compile + lance les 47 tests
mvn -q compile     # compile seulement (silencieux), utile avant de lancer une classe manuellement
mvn -q package -DskipTests   # produit target/moteurechecs.jar
```

**Résultat attendu** : `Tests run: 47, Failures: 0, Errors: 0` et `BUILD SUCCESS`.

## Lancer le programme principal

```
mvn -q compile
java -cp target/classes com.moteurechecs.Main
```

Affiche la comparaison Minimax naïf vs Minimax + Alpha-Beta sur la position de départ, à profondeur 3 et 4 (coup trouvé, positions évaluées, temps, débit).

## Lancer les outils d'expérimentation

Ces classes ont leur propre `main()`, indépendant de `Main.java` :

| Classe | Ce qu'elle fait | Commande |
|---|---|---|
| `experimentation.EtapeLocaliteMemoire` | Compare `Plateau` (objets) vs `PlateauBits` (bitboards) sur l'énumération de cases occupées | `java -cp target/classes com.moteurechecs.experimentation.EtapeLocaliteMemoire` |
| `experimentation.DiagnosticAllocations` | Lance `MinimaxAlphaBeta` seul à profondeur 5 — pensé pour être exécuté sous JFR (voir ci-dessous) | `java -cp target/classes com.moteurechecs.experimentation.DiagnosticAllocations` |
| `experimentation.DiagnosticProfilingReel` | Lance `MinimaxAlphaBeta` seul à profondeur 6 (~2 s) — fenêtre plus longue, pensée pour le profiling CPU (Étape 7, Hot Path) | `java -cp target/classes com.moteurechecs.experimentation.DiagnosticProfilingReel` |

## Profiler avec JFR (allocations + CPU)

```
java -XX:StartFlightRecording=filename=profiling/mon-diagnostic.jfr,settings=profile -cp target/classes com.moteurechecs.experimentation.DiagnosticAllocations
```

Analyser le résultat (pas besoin d'outil externe, `jfr` est fourni avec le JDK) :

```
jfr summary profiling/mon-diagnostic.jfr
jfr print --events jdk.ObjectAllocationSample --stack-depth 5 profiling/mon-diagnostic.jfr
```

**Générer un Flamegraph HTML interactif** (réutilise `../.tools/jfr-converter.jar` — un dossier au-dessus de `MoteurEchecs/`, déjà téléchargé pour HashBreaker ; voir [`../../process/13-seance4-partie1-profiling-cpu-flamegraph.md`](../../process/13-seance4-partie1-profiling-cpu-flamegraph.md) pour la procédure détaillée, identique ici — commande ci-dessous toujours lancée depuis `MoteurEchecs/`) :

```
java -jar ../.tools/jfr-converter.jar --wall -o html --title "Mon profil" profiling/mon-diagnostic.jfr profiling/mon-flamegraph.html
```

Ouvrir ensuite le `.html` généré dans un navigateur (double-clic, ou `Start-Process chemin\vers\fichier.html` en PowerShell).

## Benchmarker avec Hyperfine

```
hyperfine --warmup 1 --runs 5 scripts\run-main.cmd
```

**⚠️ Piège connu sur cette machine** : si le chemin du projet contient des espaces (ex: `OPTIMISATIONS PERFORMANCES BACKEND`), Hyperfine casse en découpant la commande au premier espace. Contournement : créer un petit lanceur `.cmd` **sans espace dans son propre chemin** (ex: directement dans `C:\Users\<toi>\`) qui appelle le vrai script :

```bat
@echo off
call "C:\chemin\complet\avec\espaces\MoteurEchecs\scripts\run-main.cmd"
```

Puis pointer Hyperfine vers ce lanceur plutôt que directement vers `scripts\run-main.cmd`.

### Scripts disponibles dans `scripts/`

| Script | Lance |
|---|---|
| `run-main.cmd` | `Main.java` (comparaison naïf vs Alpha-Beta) |
| `run-diagnostic-allocations.cmd` | `DiagnosticAllocations` (Alpha-Beta seul, profondeur 5 — pour profiling/Hyperfine) |
| `run-diagnostic-profiling-reel.cmd` | `DiagnosticProfilingReel` (Alpha-Beta seul, profondeur 6 — pour profiling CPU/Flamegraph) |
| `run-etape-localite-memoire.cmd` | `EtapeLocaliteMemoire` (comparaison objets vs bitboards) |

Tous supposent `JAVA_HOME` défini et `target/classes` déjà compilé (`mvn -q compile` d'abord).

## Où trouver quoi

| Je veux... | Aller dans... |
|---|---|
| Comprendre l'historique complet des optimisations, étape par étape | [`process/`](../process/README.md) |
| Une synthèse rapide "ce qu'il faut retenir" par étape, avec diagrammes | [`syntheses/`](../syntheses/README.md) |
| Voir l'architecture et les diagrammes techniques du projet | [`shemas/schemas.md`](../shemas/schemas.md) |
| Comprendre le choix du sujet et sa cohérence avec le barème | [`../choix-sujet.md`](../../choix-sujet.md) |
| Juste faire tourner le projet | Ce fichier |
