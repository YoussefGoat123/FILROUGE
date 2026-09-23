# Synthèse — Séance 4 : Métrologie, Micro-benchmarks & Profiling

## En une phrase

Le profiling CPU (pas seulement les allocations) a confirmé et quantifié précisément le même goulot que la Séance 3 (`sha256()`), et une correction ciblée — abandonner une comparaison à temps constant devenue inutile hors contexte sécuritaire — a permis de craquer la cible originale du cours (`@kAl1`, jamais atteinte jusqu'ici) en 77,5 secondes, mono-thread.

## Les 5 idées à retenir absolument

### 1. Toujours mesurer avec rigueur statistique, jamais une seule exécution

Une seule mesure de temps peut être trompeuse (biais DVFS/throttling, cache froid, charge machine variable). **Hyperfine** (`--warmup`, `--runs`) donne moyenne, écart-type et marge d'erreur — on l'a vérifié concrètement : notre toute première mesure Hyperfine (24,6 s) différait significativement de nos `println` internes précédents (~10,8 s), juste à cause du contexte de mesure.

### 2. Le profiling CPU confirme (et quantifie) ce que l'on savait déjà

Le flamegraph de la version naïve a montré que **~51,6% du temps CPU** est passé dans la boucle de conversion hexadécimale de `sha256()` — au-delà du seuil de 35% cité par le cours, et cohérent avec le diagnostic d'allocations de la Séance 3 (~94% des allocations, même fonction). Deux angles de mesure différents (CPU vs mémoire), même conclusion : ça renforce la confiance dans le diagnostic plutôt que de le dupliquer inutilement.

### 3. Le vrai levier n'est pas toujours celui qu'on anticipe

En remplaçant `MessageDigest.isEqual()` (comparaison à temps constant, protection anti-timing-attack) par une comparaison manuelle avec sortie anticipée, le gain (×1,21 supplémentaire) vient principalement de l'abandon du temps constant — pas seulement du passage à des mots de 64 bits comme le suggérait l'énoncé. Une protection de sécurité utile pour comparer un secret utilisateur devient un coût pur quand on compare un hash qu'on a calculé soi-même, hors contexte sécuritaire.

### 4. L'outillage Java diffère de Go mais couvre les mêmes besoins

| Besoin | Go | Java (utilisé ce projet) |
|---|---|---|
| Micro-benchmark rigoureux | `testing.B`, `go test -bench` | JMH (pas encore installé, mais identifié) |
| Empêcher le Dead Code Elimination | variable globale (`BenchmarkSink`) | retourner un checksum/`long` (déjà notre réflexe depuis la Séance 2) |
| Flamegraph interactif | `go tool pprof -http` | JFR + `jfr-converter.jar` (async-profiler), format `--wall` |
| Comparaison statistique | `benchstat` (test de Mann-Whitney, p-value) | Hyperfine (moyenne/écart-type ; pas de p-value formelle, mais la même discipline) |

### 5. La cible originale du cours est enfin atteinte

`@kAl1` (Niveau 3 "Saturation", ~992 millions à 1,68 milliard de candidats selon l'alphabet) a été craqué en **77,5 secondes, en mono-thread**, grâce à l'empilement de toutes les optimisations des Séances 2 à 4. Rappel important : ce chiffre est un test *mono-cœur* — le vrai "stress-test multi-cœurs" annoncé dès le premier document du cours reste à faire (parallélisme, séance future).

## Résultats mesurés (résumé)

| Mesure | Valeur |
|---|---|
| Part du temps CPU dans la conversion hex (flamegraph) | ~51,6% |
| Naïve (Séance 1) | 11,161 s ± 0,306 s |
| Zéro-allocation (Séance 3) | 1,337 s ± 0,025 s (×8,35) |
| Binaire 64-bit (Séance 4) | 1,104 s ± 0,055 s (×10,11) |
| `@kAl1` craqué (mono-thread) | 77,461 s (~12,8M candidats/s) |

## Vocabulaire à savoir définir à l'oral

- **DVFS / Cold vs Warm cache** : sources de biais matériel qui faussent une mesure isolée.
- **Dead Code Elimination (DCE)** : suppression par le compilateur/JIT d'un calcul dont le résultat n'est jamais utilisé.
- **Flamegraph** : visualisation en flammes ; les plateaux larges au sommet indiquent le vrai goulot.
- **Comparaison à temps constant** : protection de sécurité qui compare toujours l'intégralité des données (utile contre les attaques par timing, coûteuse si inutile).
- **Hyperfine / benchstat** : outils de comparaison statistique rigoureuse entre versions.

## Piège méthodologique confirmé pour la 3e fois

Séance 2 (Amdahl), Séance 3 (le vrai coupable des allocations), et maintenant Séance 4 (le vrai levier de la comparaison binaire) : à chaque fois, la mesure a révélé quelque chose de différent de l'intuition de départ. Ce n'est plus une coïncidence — c'est la preuve répétée que la méthode du cours (mesurer, ne jamais supposer) est non négociable.

## Pour aller plus loin

- Explications détaillées avec analogies et schémas : [comprendre-metrologie-profiling.md](../compréhension/comprendre-metrologie-profiling.md)
- Journal technique complet : [process/12](../process/12-installation-maven-hyperfine.md) à [15](../process/15-seance4-partie4-preuve-statistique.md)
- Diagrammes de l'implémentation : [shemas/schemas.md](../shemas/schemas.md)
