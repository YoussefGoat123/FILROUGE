# MoteurEchecs — Projet Noté (RNCP Bloc 4)

Moteur d'échecs Minimax/Alpha-Beta — sujet libre du projet noté du module *Optimisations & Performances Backend* (Sup de Vinci). Choix et justification détaillés dans [../choix-sujet.md](../choix-sujet.md).

> ⚠️ Rappel barème : **seul le rapport d'audit final compte pour la note** — le code (ici) sert de preuve de reproductibilité. Voir `process/` pour le journal détaillé étape par étape, et `syntheses/` pour les synthèses de résultats à chaque étape clé (mêmes conventions que le TP fil rouge HashBreaker).
>
> 📖 **[guide/README.md](guide/README.md)** — guide pratique complet (build, tests, lancement, profiling JFR, benchmarks Hyperfine) si tu veux juste faire tourner le projet sans lire tout l'historique des optimisations.

## Build & run

```
mvn test                                                        # 47 tests
mvn -q compile && java -cp target/classes com.moteurechecs.Main
```

Détails complets (profiling, Hyperfine, tous les outils d'expérimentation) : [guide/README.md](guide/README.md).

## Architecture

```
src/main/java/com/moteurechecs/
├── Main.java                        point d'entrée : orchestre, chronomètre, affiche
├── modele/
│   ├── Couleur.java                  enum BLANC / NOIR
│   ├── TypePiece.java                enum PION, CAVALIER, FOU, TOUR, DAME, ROI
│   ├── Piece.java                    record (couleur + type)
│   ├── Coup.java                     record (case départ/arrivée + promotion éventuelle)
│   ├── Plateau.java                  grille 8x8 — jouerCoup() (copie, tests) + jouer()/annuler() (make/unmake, chemin chaud)
│   └── PlateauBits.java              représentation bitboards (12 long) — expérimentation, pas encore intégrée à la recherche
├── regles/
│   └── GenerateurCoups.java          coups pseudo-légaux/légaux, détection d'échec/mat/pat (caseAttaquee directe, zéro-allocation)
├── evaluation/
│   └── Evaluateur.java               score matériel uniquement
├── recherche/
│   ├── Minimax.java                  Minimax complet, SANS élagage — baseline permanente de référence
│   └── MinimaxAlphaBeta.java         Minimax + élagage Alpha-Beta + make/unmake — version courante
└── experimentation/
    ├── EtapeLocaliteMemoire.java      comparaison Plateau (objets) vs PlateauBits
    ├── DiagnosticAllocations.java     point d'entrée pour profiler MinimaxAlphaBeta sous JFR
    └── DiagnosticStructLayout.java    inspection JOL du layout mémoire de Piece/Coup/InfoAnnulation
```

## Simplifications actées (voir `choix-sujet.md`)

- **Pas de roque, pas de prise en passant** (ajoutables plus tard).
- **Promotion automatique en Dame** (pas de choix de sous-promotion).
- **Évaluation matérielle uniquement** — pas de tables de position, pas de bonus structurel.
- **`PlateauBits` (bitboards) pas encore intégré** à la génération de coups/recherche — mesure d'accès pur uniquement pour l'instant.
- **Pas encore de table de transposition** (cache LRU des positions déjà évaluées) — prévue à l'Étape 9.

## Progression mesurée (position de départ, Minimax + Alpha-Beta, profondeur 5)

| Étape | Temps moyen (Hyperfine, 5 essais) | Gain cumulé |
|---|---|---|
| Naïf (Étape 1, sans élagage) | — (profondeur 4 max mesurée : 8,465 s) | — |
| Alpha-Beta (Étape 2, macro) | 1,999 s ± 0,058 s (profondeur 5) | référence |
| + Zéro-allocation (Étape 4, micro) | 475,3 ms ± 37,8 ms | ×4,2 |
| + Pré-allocation capacité (Étape 6, micro) | 426,3 ms ± 35,8 ms | **×4,7** |
| + Tri des coups MVV-LVA (Étape 8, macro) | 709,8 ms ± 23,1 ms *(session de mesure différente, voir note)* | ×1,25 sur sa propre baseline ; **−39 % de positions évaluées** (41 554 → 25 319) |

Étape 5 (struct padding, JOL) : vérifiée, aucun gain accessible manuellement en Java (voir synthèse dédiée).

⚠️ La ligne Étape 8 a été mesurée dans une session Hyperfine distincte, où la baseline "avant" (887,1 ms) différait du 426,3 ms ci-dessus sur un code pourtant identique (écart de conditions machine, pas une régression — voir [process/08-tri-coups-move-ordering.md](process/08-tri-coups-move-ordering.md)). Le gain cumulé en temps n'est donc pas directement chaînable ici ; **le nombre de positions évaluées** (déterministe, insensible à la machine) reste la mesure la plus fiable pour comparer l'Étape 8 au reste.

Détail complet, diagrammes et interprétation : [syntheses/](syntheses/README.md).
