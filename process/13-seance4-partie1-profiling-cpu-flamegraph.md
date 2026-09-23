# Étape 13 — Profiling CPU & Flamegraph (Séance 4, Partie 1)

**Séance associée :** Séance 4 — Métrologie, Micro-benchmarks & Profiling Applicatif
**Objectif (cours) :** "Exécuter le craqueur avec profilage CPU (`go test -cpuprofile cpu.prof`). Générer et inspecter le Flamegraph interactif avec `go tool pprof -http=:8080`."

## Périmètre de cette étape

Uniquement instrumenter, capturer et visualiser — aucune correction de code à ce stade (ça viendra en Parties 2-3).

## Adaptation Java

- **Capture CPU** : pas de nouvel outil — l'enregistrement JFR de la Séance 3 ([profiling/seance3-diagnostic-naif.jfr](../profiling/seance3-diagnostic-naif.jfr)) contenait déjà des échantillons `jdk.ExecutionSample` (profil CPU), simplement pas encore exploités.
- **Flamegraph interactif** : pas de build Windows native pour l'agent `async-profiler`, mais son outil officiel **`jfr-converter.jar`** (pur Java, fonctionne sur toute plateforme) convertit un `.jfr` existant en flamegraph HTML autonome et interactif.

```
curl -o .tools/jfr-converter.jar https://github.com/async-profiler/async-profiler/releases/download/v4.5/jfr-converter.jar
java -jar .tools/jfr-converter.jar --wall -o html --title "..." profiling/seance3-diagnostic-naif.jfr profiling/seance4-flamegraph-cpu-naif.html
```

## Piège rencontré : l'option `--cpu` ne fonctionnait pas

Premier essai avec `--cpu` (censé cibler `jdk.ExecutionSample`) : le flamegraph généré était **vide** (`cpool` ne contenait que `'all'`, aucune frame). Vérifié que les données existaient bien dans le `.jfr` source (`jfr print --events jdk.ExecutionSample` montrait des stack traces complètes et exploitables). Le problème venait spécifiquement du mode `--cpu` de cette version du convertisseur. **Solution** : utiliser `--wall` (profil "temps réel"/wall-clock, aussi basé sur `jdk.ExecutionSample`) — fonctionne correctement et produit un flamegraph riche. Pour un programme mono-thread sans I/O bloquante comme notre craqueur, wall-clock et temps CPU sont de toute façon quasi identiques.

## Résultat produit

Fichier livré : [profiling/seance4-flamegraph-cpu-naif.html](../profiling/seance4-flamegraph-cpu-naif.html) — à ouvrir dans un navigateur, flamegraph interactif (zoom, recherche) montrant `Main.main → Main.craquer → Main.sha256 → ...`.

## Analyse quantitative (anticipe la Partie 2)

| Frame (sommet de pile) | Échantillons CPU | Part |
|---|---|---|
| `Main.sha256()` ligne 94 (boucle de conversion hex) | 488 / 946 | **~51,6%** |
| `Main.craquer()` lignes 43/55 (construction candidat, comparaison) | 39 / 946 | ~4,1% |

**Plus de la moitié du temps CPU est passée dans la boucle de conversion hexadécimale** — un résultat encore plus marqué que le ">35%" cité en exemple dans le cours (illustré sur un cas Go différent). Cette mesure CPU **confirme et renforce** le diagnostic d'allocations de la Séance 3 (Étape 08, où `sha256()` représentait déjà ~94% des allocations) : c'est bien la même fonction qui domine à la fois le temps CPU et la pression mémoire.

## Fichiers concernés

- [profiling/seance4-flamegraph-cpu-naif.html](../profiling/seance4-flamegraph-cpu-naif.html) (nouveau, flamegraph interactif)
- [.tools/jfr-converter.jar](../.tools/jfr-converter.jar) (outillage local, non versionné)
- Réutilise [profiling/seance3-diagnostic-naif.jfr](../profiling/seance3-diagnostic-naif.jfr) (aucun nouveau run nécessaire)
