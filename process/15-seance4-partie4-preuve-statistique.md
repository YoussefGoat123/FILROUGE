# Étape 15 — Preuve Statistique & Validation Finale (Séance 4, Partie 4)

**Séance associée :** Séance 4 — Métrologie, Micro-benchmarks & Profiling Applicatif (dernière partie)
**Objectif (cours) :** "Mesurer le gain net avant/après avec benchstat et intégrer les captures Flamegraph annotées dans le rapport d'audit."

## Ce qui a été fait

### 1. Preuve statistique (déjà produite en Partie 3, consolidée ici)

La comparaison Hyperfine à 3 versions ([profiling/seance4-hyperfine-comparatif.md](../profiling/seance4-hyperfine-comparatif.md)) sert de substitut Java à `benchstat` — même principe (plusieurs essais, warmup, statistiques avec marge d'erreur) :

| Version | Temps moyen | vs Naïf |
|---|---|---|
| Naïve (Séance 1) | 11,161 s ± 0,306 s | — |
| Zéro-allocation (Séance 3) | 1,337 s ± 0,025 s | ×8,35 |
| Binaire 64-bit (Séance 4) | 1,104 s ± 0,055 s | ×10,11 |

### 2. Captures Flamegraph (déjà produites en Partie 1)

[profiling/seance4-flamegraph-cpu-naif.html](../profiling/seance4-flamegraph-cpu-naif.html) — flamegraph interactif de la version naïve, montrant le plateau large (~51,6% du temps CPU) sur la conversion hexadécimale de `sha256()`.

### 3. Validation finale : attaque réelle de `@kAl1`

La mission originale du tout premier document du cours (J1_AM) visait `@kAl1` — Niveau 3 "Saturation", jamais attaqué jusqu'ici. Constat : les caractères `@`, `k`, `A`, `l`, `1` sont **tous déjà couverts par notre alphabet actuel** (63 symboles, `@` ajouté en préparation de séance) — pas besoin d'étendre à 70+ symboles pour cette cible précise, même si le niveau complet du jeu le suggérait à l'origine.

Hash réel calculé : `b96ec5f74610e96c808a6f062190085adeddeefe085b56cc768f551b4ab641a5`.

Ajout d'un 3ème appel `craquer("@kAl1", ..., 5)` dans [Seance4ComparaisonBinaire.java](../src/main/java/com/hashbreaker/Seance4ComparaisonBinaire.java) (notre version la plus optimisée à date : zéro-allocation + comparaison binaire 64-bit).

```
Recherche de la cible (comparaison binaire 64-bit) : @kAl1
Mot de passe trouve : @kAl1
Temps ecoule : 77 461 ms   (~1 min 17 s)
```

**Contexte de cette durée** : `@` est le **dernier caractère** de notre alphabet (le plus grand index). Le compteur base-N énumère `a, b, ... z, A, ... Z, 0, ... 9, @` sur la première position — donc tout candidat commençant par `@` n'est atteint qu'après avoir épuisé les 62 autres valeurs possibles de cette position, soit ~98,4% de l'espace de recherche total (63⁵ ≈ 992 millions de candidats) parcouru avant de le trouver. C'est un cas quasiment pire-cas pour notre ordre d'énumération — pas un hasard malchanceux, une conséquence directe de la position de `@` dans l'alphabet.

**Débit réel obtenu** : ~992 millions / 77,461 s ≈ **12,8 millions de candidats/seconde**, en simple thread — cohérent avec le débit mesuré sur `Sh3n` (14,7M candidats en 0,9s ≈ 16,3M candidats/s, la petite différence s'expliquant par le calcul additionnel du buffer 5 caractères vs 4).

## Bilan de la Séance 4

| Partie | Résultat |
|---|---|
| 1. Profiling CPU & Flamegraph | Flamegraph interactif généré, `sha256()` identifié comme goulot dominant |
| 2. Détection du Goulet Hex | ~51,6% du temps CPU dans la conversion hex (> seuil de 35% du cours) |
| 3. Comparaison Binaire 64-bit | Sortie anticipée + mots de 64 bits → ×1,21 supplémentaire (×10,11 cumulé vs naïf) |
| 4. Preuve statistique + validation | Hyperfine (3 versions, marges d'erreur) + `@kAl1` craqué en 77,5 s |

**Note pour la suite** : `@kAl1` a été craqué en **mono-thread**. Le PDF original le désignait comme "stress-test multi-cœurs" — le vrai test de parallélisme (workers bornés) reste à faire dans une séance future, où l'on pourra comparer ce temps de 77,5 s à une version parallélisée.

## Fichiers concernés

- [src/main/java/com/hashbreaker/Seance4ComparaisonBinaire.java](../src/main/java/com/hashbreaker/Seance4ComparaisonBinaire.java) (ajout de l'attaque `@kAl1`)
- [profiling/seance4-hyperfine-comparatif.md](../profiling/seance4-hyperfine-comparatif.md)
- [profiling/seance4-flamegraph-cpu-naif.html](../profiling/seance4-flamegraph-cpu-naif.html)
- Synthèse complète de la séance : [syntheses/04-seance4-metrologie-profiling.md](../syntheses/04-seance4-metrologie-profiling.md)
