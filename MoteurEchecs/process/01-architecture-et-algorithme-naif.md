# Étape 01 — Architecture & Algorithme Naïf (V1)

**Objectif :** poser une architecture propre dès le départ (contrairement à HashBreaker où tout partait d'un seul `Main.java`), et un algorithme Minimax naïf correct et mesurable — base de comparaison pour tous les leviers d'optimisation à venir.

## Décisions actées avant l'implémentation

- **Langage : Java**, pour rester cohérent avec HashBreaker et réutiliser directement la toolchain déjà opérationnelle (Maven, JUnit, JFR, JOL, Hyperfine).
- **Règles simplifiées pour la V1** : pas de roque, pas de prise en passant (ajoutables plus tard), promotion automatique en Dame. Objectif : réduire la complexité initiale pour valider l'algorithme rapidement — même philosophie "Make it work" que la Séance 1 de HashBreaker.

## Architecture mise en place

Contrairement à HashBreaker (où `Main.java` concentrait tout au départ), le moteur est structuré en packages dès la V1 :

- **`modele`** : structures de données pures (`Couleur`, `TypePiece`, `Piece`, `Coup`, `Plateau`). `Piece` et `Coup` sont des `record` Java (immuables, concis). `Plateau` est une grille 8x8 immuable — chaque coup produit un **nouveau** `Plateau` (copie complète), pattern volontairement naïf.
- **`regles`** : `GenerateurCoups`, qui sépare clairement coups *pseudo-légaux* (respectent le déplacement de la pièce, ignorent l'échec au roi) des coups *légaux* (pseudo-légaux qui ne laissent pas son propre roi en échec — filtrage en rejouant chaque coup et en vérifiant l'échec après coup).
- **`evaluation`** : `Evaluateur`, isolé pour pouvoir être enrichi plus tard (tables de position, structure de pions...) sans toucher au reste.
- **`recherche`** : `Minimax`, qui ne connaît que `Plateau`, `Coup`, `GenerateurCoups` et `Evaluateur` — aucune dépendance vers `Main`.
- **`Main`** : uniquement l'orchestration (créer la position, lancer la recherche, chronométrer, afficher) — exactement le rôle que jouait `craquer()` dans HashBreaker, mais ici la logique métier elle-même est répartie dans les bons packages plutôt que dans la classe d'entrée.

## Algorithme

**Génération de coups** : pour chaque pièce, génère ses coups selon son type (sauts pour cavalier/roi, glissades pour fou/tour/dame, règles spécifiques pour le pion — avance simple/double, capture diagonale, promotion). Un coup pseudo-légal est légal si, après l'avoir joué, le roi du joueur qui vient de jouer n'est pas en échec.

**Minimax** (sans élagage) : explore récursivement l'arbre des coups jusqu'à une profondeur donnée. Aux nœuds terminaux (profondeur 0, ou plus aucun coup légal), retourne soit le score matériel (`Evaluateur`), soit un score de mat/pat. Le score de mat est ajusté par la profondeur restante pour que le moteur préfère un mat trouvé plus tôt.

## Validation

Deux vérifications classiques et non négociables pour tout générateur de coups d'échecs, toutes deux passées :

1. **`perft(1) == 20`** depuis la position de départ (16 coups de pions + 4 coups de cavaliers) — le sanity-check standard de l'industrie.
2. **Détection du "mat du fou"** (*Fool's Mate*, le mat le plus rapide possible aux échecs, en 2 coups : `1.f3 e5 2.g4 Dh4#`) — vérifie à la fois la détection d'échec et l'absence de coups légaux en position de mat.

**Résultat : 19/19 tests passent** (`PlateauTest`, `GenerateurCoupsTest`, `EvaluateurTest`, `MinimaxTest`).

## Baseline mesurée

| Profondeur | Positions évaluées | Temps | Débit |
|---|---|---|---|
| 3 | 9 322 | 421 ms | ~22 142 positions/s |
| 4 | 206 603 | 8 465 ms | ~24 406 positions/s |

Même structure de mesure que HashBreaker (`craquer()` sur deux niveaux : une résolution rapide de validation, puis une résolution plus lourde qui sert de référence chronométrée).

## Fichiers concernés

- `src/main/java/com/moteurechecs/` (toute l'arborescence)
- `src/test/java/com/moteurechecs/` (4 classes de test, 19 tests)
- `pom.xml`, `README.md`
