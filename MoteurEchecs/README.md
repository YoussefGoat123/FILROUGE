# MoteurEchecs — Projet Noté (RNCP Bloc 4)

Moteur d'échecs Minimax/Alpha-Beta — sujet libre du projet noté du module *Optimisations & Performances Backend* (Sup de Vinci). Choix et justification détaillés dans [../choix-sujet.md](../choix-sujet.md).

> ⚠️ Rappel barème : **seul le rapport d'audit final compte pour la note** — le code (ici) sert de preuve de reproductibilité. Voir `process/` pour le journal détaillé étape par étape, et `syntheses/` pour les synthèses de résultats à chaque étape clé (mêmes conventions que le TP fil rouge HashBreaker).

## Build & run

```
mvn test                                    # 19 tests
mvn -q compile && java -cp target/classes com.moteurechecs.Main
```

## Architecture (V1 — naïve)

```
src/main/java/com/moteurechecs/
├── Main.java                        point d'entrée : orchestre, chronomètre, affiche
├── modele/
│   ├── Couleur.java                  enum BLANC / NOIR
│   ├── TypePiece.java                enum PION, CAVALIER, FOU, TOUR, DAME, ROI
│   ├── Piece.java                    record (couleur + type)
│   ├── Coup.java                     record (case départ/arrivée + promotion éventuelle)
│   └── Plateau.java                  grille 8x8 immuable, jouerCoup() retourne un nouveau Plateau
├── regles/
│   └── GenerateurCoups.java          coups pseudo-légaux/légaux, détection d'échec/mat/pat
├── evaluation/
│   └── Evaluateur.java               score matériel uniquement
└── recherche/
    └── Minimax.java                  Minimax complet, SANS élagage alpha-beta
```

## Simplifications actées pour la V1 (voir `choix-sujet.md`)

- **Pas de roque, pas de prise en passant** (ajoutables plus tard).
- **Promotion automatique en Dame** (pas de choix de sous-promotion).
- **Pas d'élagage alpha-beta** — Minimax complet, volontairement, pour établir une baseline mesurable avant d'optimiser (même logique que HashBreaker Séance 1).
- **Plateau copié à chaque coup** (pas de pattern make/unmake) — sera revu au levier zéro-allocation.
- **Évaluation matérielle uniquement** — pas de tables de position, pas de bonus structurel.

## Baseline mesurée (naïve, Minimax sans élagage)

| Profondeur | Positions évaluées | Temps | Débit |
|---|---|---|---|
| 3 | 9 322 | 421 ms | ~22 142 positions/s |
| 4 | 206 603 | 8 465 ms | ~24 406 positions/s |

Ces chiffres serviront de référence pour mesurer les gains des futurs leviers (localité mémoire, zéro-allocation, élagage alpha-beta, profiling, parallélisme, I/O & persistance).
