# Étape 12 — Installation de Maven & Hyperfine

**Contexte :** pas une étape de séance à proprement parler, mais une mise à niveau d'outillage nécessaire pour continuer proprement — en particulier pour la Séance 4 (métrologie/profiling) et pour se rapprocher des exigences du barème du projet noté (axe 1 : "Rigueur du protocole de mesure (Hyperfine)").

## Constat de départ

Depuis le début du projet, deux limitations d'outillage :
1. **Pas de `mvn`** : chaque dépendance (JUnit, puis JOL en Séance 3) devait être téléchargée manuellement en `.jar` dans `.tools/` et référencée à la main sur le classpath — fragile, et bloquant pour de futures dépendances plus complexes (gRPC/Protobuf ont besoin de plugins Maven avec génération de code, pas juste un `.jar`).
2. **Pas de Hyperfine** : nos mesures de temps reposaient uniquement sur `System.currentTimeMillis()`/`nanoTime()` internes au code — une seule exécution, aucun préchauffage séparé, aucune statistique (écart-type, variance).

## Ce qui a été fait

### 1. Installation de Maven

Pas de paquet officiel Apache Maven sur `winget` — installation manuelle depuis le mirroir officiel Apache :

```
https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip
```

Extrait dans `%LOCALAPPDATA%\Programs\apache-maven-3.9.16` (pas de droits admin sur `Program Files`), et `bin/` ajouté au `PATH` utilisateur (`JAVA_HOME` déjà positionné lors de l'installation du JDK en Séance 1).

**Vérification :** `mvn -version` fonctionne, et surtout `mvn test` compile et exécute les 30 tests du projet avec succès (`BUILD SUCCESS`).

### 2. Migration de JOL vers une vraie dépendance Maven

La dépendance `org.openjdk.jol:jol-core:0.17`, jusque-là téléchargée à la main dans `.tools/` pour la Séance 3 Partie 2, est désormais déclarée dans [pom.xml](../pom.xml). Le dossier `.tools/` (JUnit standalone + JOL) a été vidé — Maven gère maintenant tout.

### 3. Installation de Hyperfine

```
winget install --id sharkdp.hyperfine -e
```

Installation réussie, mais un piège rencontré à l'usage : **le nom du dossier du projet contient des espaces** (`OPTIMISATIONS PERFORMANCES BACKEND`), ce qui casse l'invocation par défaut de Hyperfine sur Windows (il transmet la commande brute à `cmd /C` sans la re-quoter correctement, donc `cmd` la découpe au premier espace).

**Solution appliquée** : un script portable [scripts/run-benchmark.cmd](../scripts/run-benchmark.cmd) (utilise `%JAVA_HOME%` et un chemin relatif via `%~dp0`, versionné dans le projet), invoqué via un petit lanceur local **sans espace dans son propre chemin** (`C:\Users\elbaz\hb-launch.cmd`, hors du repo, propre à cette machine) qui appelle le vrai script du projet.

## Résultat — premier vrai benchmark Hyperfine sur le projet

```
hyperfine --warmup 1 --runs 3 hb-launch.cmd   # (execute Main.java : z3D + Sh3n)

Time (mean ± σ):     24.618 s ±  0.121 s    [User: 24.743 s, System: 0.415 s]
Range (min … max):   24.480 s … 24.708 s    3 runs
```

## Observation intéressante : le chiffre diffère de nos mesures précédentes

Nos `println` internes de la Séance 1 rapportaient `z3D` = 249 ms + `Sh3n` = 10 536 ms ≈ **10,8 s**. Hyperfine mesure ici le process complet à **~24,6 s**. L'écart s'explique par :

- Le temps de démarrage de la JVM (chargement de classes, JIT pas encore chaud) — non compté par nos `println` internes, qui ne chronomètrent que la boucle de recherche.
- La charge de la machine au moment de la mesure (biais DVFS/throttling, décrits dans [comprendre-metrologie-profiling.md](../compréhension/comprendre-metrologie-profiling.md)) — nos essais précédents à des moments différents montrent déjà des variations (ex: Séance 2 Partie 2, essais à 1-2 ms puis plus tard 10-12 ms pour la même liste chaînée).

**C'est exactement pour cette raison que le cours (et le barème) exige Hyperfine plutôt qu'une mesure isolée** : une seule exécution peut être trompeuse, et ce résultat le démontre sur notre propre projet.

## Fichiers concernés

- [pom.xml](../pom.xml) (dépendance JOL ajoutée)
- [scripts/run-benchmark.cmd](../scripts/run-benchmark.cmd) (nouveau, portable)
- `.tools/` (vidé — Maven gère désormais les dépendances)
