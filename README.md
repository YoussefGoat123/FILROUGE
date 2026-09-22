# HashBreaker — TP Fil Rouge

**Craqueur cryptographique haute performance** — Sup de Vinci, RNCP Bloc 4, module *Optimisations & Performances Backend* (formateur : Christophe Lecroq).

Moteur combinatoire de craquage de condensats SHA-256. On part d'une version naïve (Séance 1) puis on applique, créneau après créneau, chaque levier d'optimisation (alignement mémoire, zéro-allocation, profiling, workers bornés, streaming binaire gRPC, indexation SQL) pour franchir plusieurs ordres de grandeur de performance.

Ce fil conducteur sert de **démonstrateur guidé** : les mêmes leviers (zéro-allocation, profiling, workers, SQL) et la même méthode d'audit s'appliquent ensuite au projet noté (libre ou parmi les 10 sujets suggérés : WorldGen, Sudoku, RayTracer, Bot d'Échecs, Jeu de la Vie, Order Book HFT, PixelWar, SIEM Détection, Mini-Vector DB, Spatial Game Server).

## Cibles à craquer

| Niveau | Mot de passe | Longueur / charset | Espace de recherche | Usage |
|---|---|---|---|---|
| 1 — Validation | `z3D` | 3 car. — 62 symboles | 238 328 candidats | Valider l'algo, résolution en ms |
| 2 — Benchmark | `Sh3n` | 4 car. — casse mixte + chiffres | 14 776 336 candidats | Calibrer les benchmarks séquentiels, struct padding, suppression du GC |
| 3 — Saturation | `@kAl1` | 5 car. — 70+ symboles | 1,68 milliard | Preuve du parallélisme multi-cœurs, streaming gRPC, indexation SQL |

## Algorithme — compteur base-N

SHA-256 est à sens unique (irréversible) : on ne peut retrouver le mot de passe que par force brute.

1. **Compteur en base-N (avec retenue)** : chaque caractère est un index dans l'alphabet (`0 -> 'a'`, `61 -> '9'`). Le candidat est un buffer `[0, 0, 0]` ("aaa"). À chaque tour, on incrémente la dernière case ; à la valeur max, elle repasse à 0 et propage une retenue (+1) à gauche.
2. **Flux** : hash cible + alphabet + longueur max en entrée → boucle *candidat suivant → sha256() → test d'égalité* → mot de passe en clair, débit et temps total en sortie.
3. **Piège de la version naïve** : créer un nouvel objet string et le convertir en hexadécimal à chaque essai alloue des millions d'objets sur le tas, ce qui étouffe le Garbage Collector.

## Règle d'or du développement

1. **Make it work** — exactitude d'abord ; un code rapide qui donne un résultat faux est inutile.
2. **Make it right** — architecture lisible, code testé et modulaire ; ne jamais optimiser une base instable.
3. **Make it fast** — optimiser après mesure, cibler uniquement les goulots prouvés.

> Loi absolue : ne jamais inverser l'ordre. L'optimisation prématurée est la première cause de complexité accidentelle.

## Macro vs micro-optimisation

- **Macro** (échelle globale) : complexité algorithmique (O(n²) → O(n log n)), structures de données (tableaux contigus vs listes chaînées, hachage), architecture système (async, streaming, cache, distribution de charge).
- **Micro** (échelle locale, sur le Hot Path) : primitives vectorisées (`copy()` en Go), suppression des copies/allocations temporaires, aide au compilateur (inlining, déroulement de boucles).

## Complexité — repères Grand O (N = 50)

| Notation | Nom | Opérations | Exemple |
|---|---|---|---|
| O(1) | Constant | 1 | Accès indexé, hash map idéale |
| O(log N) | Logarithmique | 6 | Dichotomie, B-Tree SQL |
| O(N) | Linéaire | 50 | Parcours de slice, table scan |
| O(N log N) | Quasi-linéaire | 282 | MergeSort, QuickSort |
| O(N²) | Quadratique | 2 500 | Boucles imbriquées naïves, jointure produit |

## Compromis espace-temps (RAM/CPU)

- **Économiser le CPU (+ RAM)** : mémoïsation/caching, dénormalisation, tables de correspondance.
- **Économiser la RAM (+ CPU)** : compression à la volée (gzip, zstd), streaming par blocs.
- Attention : trop de RAM cause des cache misses CPU et sature le GC — l'excès de mémoire finit par dégrader le CPU aussi.

## Goulots d'étranglement & loi d'Amdahl

La vitesse globale est dictée par le maillon le plus lent. Sources courantes : I/O & réseau (latence, sockets bloquantes), base de données (requêtes N+1, tables non indexées), concurrence (contention sur verrous), calcul CPU (allocations continues, boucles critiques).

```
Sglobal = 1 / ((1 - P) + (P / S))
```
- `P` : fraction du temps global optimisable/parallélisable
- `S` : facteur d'accélération obtenu sur cette fraction

> Règle du maillon faible : optimiser une portion qui ne pèse que 5 % du temps total n'apportera jamais plus de 5 % de gain global, même avec `S -> ∞`. **Toujours profiler avant d'optimiser.**

## Hot Path vs Cold Path

- **Cold Path (~90 % du code)** : démarrage, injection de dépendances, config, erreurs rares → privilégier la lisibilité, aucun gain mesurable à optimiser ici.
- **Hot Path (~10 % du code)** : boucles d'ingestion, parsing réseau, logique exécutée des millions de fois → zéro allocation sur le tas, zéro copie inutile, inlining maximal.

> On ne devine jamais le Hot Path, on le **mesure** avec un profileur (Flamegraph, pprof).

## Représentation binaire & encodage texte

- **Bit** : état logique 0/1. **Octet** : 8 bits contigus, 256 combinaisons (0–255), poids binaires `128·64·32·16·8·4·2·1`.
- **ASCII** : 1 octet fixe (7 bits utiles), valeurs 0–127 (`'A'` = 65 = `0x41`).
- **UTF-8** : 1 à 4 octets variables, rétrocompatible ASCII (`'A'` = 1 octet, `'é'` = 2 octets, `'€'` = 3 octets, emoji = 4 octets).

**Piège mémoire des chaînes** : une chaîne est un tableau d'octets, pas de caractères.
- `len("Go") == 2` (octets = caractères)
- `len("Café") == 5` octets pour 4 caractères (`'é'` pèse 2 octets)
- Indexer directement un octet (`s[3]`) peut extraire un demi-octet isolé et corrompre la donnée.

> Règle backend : toujours itérer sur les points de code décodés (runes en Go, `.chars()` en Rust), jamais sur les octets bruts.

## Livrables attendus

- Code source versionné
- Mesures de temps avant/après (baseline incluse)
- Profils d'exécution (Flamegraph / pprof ou équivalent)
- Rapport d'audit comparatif

## Séance 1 — Mise en place & baseline (3h30)

Objectif : initialiser le projet et obtenir une référence de performance avant optimisation.

1. Initialiser un projet dans le langage de son choix (Go, Rust, C++, C#, Java…) — aucun template imposé.
2. Coder le générateur combinatoire (compteur base-N) + calcul SHA-256 par candidat.
3. Résoudre les cibles Niveau 1 (`z3D`) puis Niveau 2 (`Sh3n`).
4. Chronométrer l'exécution et consigner le temps de référence (**baseline**) pour mesurer les gains futurs.

## Prochaines étapes (créneaux suivants)

- [x] Localité mémoire & lignes de cache (Séance 2 — terminée, cf. [syntheses/02-seance2-cpu-caches-localite-memoire.md](syntheses/02-seance2-cpu-caches-localite-memoire.md))
- [x] Alignement mémoire & struct padding, zéro-allocation (Séance 3 — terminée, cf. [syntheses/03-seance3-memoire-zero-allocation.md](syntheses/03-seance3-memoire-zero-allocation.md))
- [ ] Profiling (Flamegraph / pprof)
- [ ] Workers bornés (parallélisme multi-cœurs)
- [ ] Streaming binaire gRPC
- [ ] Indexation SQL
