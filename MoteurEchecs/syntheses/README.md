# Synthèses — Ce qu'il faut retenir

Une synthèse par étape clé — condensé "à retenir" pour réviser vite ou nourrir le rapport d'audit final. Même principe que pour HashBreaker (`../../syntheses/`).

## Convention (à respecter pour chaque nouvelle étape)

Chaque fichier doit être **autonome** (pas seulement un lien vers `shemas/`) et contenir :

1. **Diagramme(s)** de l'état du système à cette étape (architecture ou flux, en Mermaid).
2. **Description précise** de l'optimisation appliquée (ou "aucune" pour une baseline) et *pourquoi*.
3. **Impact mesuré**, avec un tableau ou graphique comparatif **par rapport à l'étape précédente directement comparable** — pas forcément la toute première baseline, si un changement algorithmique (ex: alpha-beta) rend la comparaison directe trompeuse (cf. Étape 1, section "Résultats mesurés").

## Sommaire

| # | Étape | Synthèse |
|---|---|---|
| 01 | Architecture & Algorithme Naïf | [01-architecture-naive.md](01-architecture-naive.md) |
| 02 | Élagage Alpha-Beta (macro) | [02-elagage-alpha-beta.md](02-elagage-alpha-beta.md) |
| 03 | Localité mémoire : Bitboards (micro) | [03-localite-memoire-bitboards.md](03-localite-memoire-bitboards.md) |
| 04 | Zéro-allocation : caseAttaquee() + Make/Unmake (micro) | [04-zero-allocation.md](04-zero-allocation.md) |
| 05 | Struct Padding / Alignement (JOL) | [05-struct-padding.md](05-struct-padding.md) |
| 06 | Pré-allocation de capacité (listes de Coup) | [06-preallocation.md](06-preallocation.md) |
| 07 | Profiling réel & Hot Path (Axe 2) | [07-profiling-reel.md](07-profiling-reel.md) |
| 08 | Tri des coups (MVV-LVA, macro) | [08-tri-coups.md](08-tri-coups.md) |

## Synthèse consolidée des performances (toutes étapes, position de départ)

Un seul tableau récapitulatif — inspiré de la structure de synthèse utilisée par le formateur ([Chroq/hashbreaker](https://github.com/Chroq/hashbreaker), section "Synthèse des Performances") — plutôt que des tableaux avant/après dispersés par étape. Chaque étape du détail reste dans son fichier dédié ci-dessus ; ce tableau sert de vue d'ensemble unique pour le rapport d'audit final.

| Étape | Stratégie | Profondeur | Temps mesuré (Hyperfine) | Positions évaluées | Allocations (JFR) | Pic mémoire (heap, JFR) | Gain cumulé |
|---|---|---|---|---|---|---|---|
| 1 | Minimax naïf (baseline) | 4 | 8 465 ms | 206 603 | non profilé | ~58-71 MB *(3 runs)* | — *(algorithme différent, non comparable directement)* |
| 2 | + Élagage Alpha-Beta (macro) | 5 | 1 999 ms ± 58 ms | 41 554 | 600 échantillons | ~69 MB *(3 runs, très stable)* | **référence** (nouvelle base de comparaison) |
| 3 | Bitboards (micro, **non intégré** à la recherche) | — | — *(mesure isolée : ×1,3 sur énumération pure)* | — | — | — | non intégré, n'affecte pas la recherche |
| 4 | + Zéro-allocation (`caseAttaquee()` + make/unmake) | 5 | 475,3 ms ± 37,8 ms | 41 554 | 120 échantillons (÷5) | ~52-55 MB *(−22% vs Étape 2)* | **×4,2** |
| 5 | Struct Padding (JOL) — vérifié, rien à corriger | 5 | inchangé | 41 554 | 120 échantillons | inchangé | ×4,2 *(inchangé)* |
| 6 | + Pré-allocation de capacité | 5 | 426,3 ms ± 35,8 ms | 41 554 | 120 échantillons *(inchangé)* | ~46-47 MB *(−32% vs Étape 2 cumulé)* | **×4,7** |
| 7 | Profiling réel (diagnostic, pas d'optimisation) | 6 | ~2,06 s *(fenêtre de profiling)* | 645 199 | dominé par `Coup` (structurel) | non mesuré (diagnostic) | non applicable — diagnostic seul |
| 8 | + Tri des coups (MVV-LVA, **macro**) | 5 | 709,8 ms ± 23,1 ms *(voir note baseline ci-dessous)* | **25 319** (−39 % vs Étape 6) | 132 échantillons *(+10 %, coût du tri)* | non mesuré | **×1,25** *(sur sa propre baseline avant/après)* |

**Lecture clé** : le nombre de positions évaluées reste **identique (41 554)** de l'Étape 2 à l'Étape 6 — preuve que les micro-optimisations (4, 5, 6) accélèrent l'exécution **sans changer l'algorithme ni la décision prise** (même garantie que les tests d'équivalence Minimax/Alpha-Beta). Seul le temps d'exécution, le volume d'allocations et le pic mémoire bougent — et ce pic mémoire **baisse de ~32% entre l'Étape 2 et l'Étape 6**, un gain réel et cohérent avec la baisse d'allocations, complémentaire (pas redondant) avec le gain de vitesse ×4,7. **L'Étape 8 change cette invariance** : c'est le premier levier qui réduit réellement le nombre de positions visitées (41 554 → 25 319, −39 %) plutôt que d'accélérer le traitement d'un nombre fixe de positions — cohérent avec sa nature "macro" (même famille que l'élagage alpha-beta de l'Étape 2), à la différence des leviers micro (4, 5, 6).

⚠️ **Note sur la baseline de l'Étape 8** : la mesure "avant" prise pour l'Étape 8 (887,1 ms) est significativement plus lente que le 426,3 ms documenté à l'Étape 6, sur un code strictement identique — probablement un écart de conditions machine entre les deux sessions de mesure (jours différents), pas une régression du code. Le facteur ×1,25 est valide en interne (même session, mêmes conditions), mais **la colonne "Temps mesuré" de l'Étape 8 n'est pas directement comparable en valeur absolue** à celle des étapes 2-6. La colonne "Positions évaluées" reste fiable dans tous les cas (déterministe, insensible à la charge machine). Détail : [process/08-tri-coups-move-ordering.md](../process/08-tri-coups-move-ordering.md).

### Méthodologie de la colonne "Pic mémoire"

Mesure ajoutée a posteriori, sur demande explicite (empreinte RAM absente de la première version du tableau). Le code de production n'existant qu'en une seule version (modifié en place depuis l'Étape 4), les états historiques ont été reconstruits via un **worktree git isolé**, checkoutant le commit correspondant à chaque étape (`8486680`, `f1c8fe4`, `3c432d3`, `cb8650d`), recompilés et exécutés indépendamment du dépôt de travail principal.

**Deux tentatives, une seule retenue :**
1. ❌ `Runtime.totalMemory()-freeMemory()` lu directement dans le code, juste après la recherche — **rejeté** : sur 3 runs identiques (Étape 6), résultats de 3 081 KB à 27 655 KB, un facteur ×9 d'écart. Lire le tas à un instant choisi arbitrairement dépend totalement du hasard du passage du GC juste avant — inutilisable.
2. ✅ **`jdk.GCHeapSummary`** (événement JFR standard, déjà présent dans nos `.jfr` sans configuration supplémentaire — cf. [compréhension/comprendre-metrologie-profiling.md](../../compréhension/comprendre-metrologie-profiling.md), équivalent Java du `inuse_space` de `pprof` vu en J2_PM) — le pic retenu est le **maximum des valeurs `heapUsed` "Before GC"** sur toute la durée de la recherche (état juste avant chaque collection, le plus représentatif de l'usage réel). Beaucoup plus stable : écarts de 1-3% entre runs au lieu de ×9.

Chaque valeur est la moyenne de 3 runs JFR indépendants par étape (mêmes fichiers `.jfr`, non conservés dans `profiling/` car reconstruits à la volée depuis git, pas issus de l'état actuel du code).
