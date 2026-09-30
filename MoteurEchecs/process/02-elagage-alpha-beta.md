# Étape 02 — Élagage Alpha-Beta (Macro-optimisation)

**Type de levier :** macro-optimisation — réduction de la complexité algorithmique elle-même, pas une exécution plus rapide du même travail.

**Décision de séquencement :** appliquée avant les micro-optimisations (localité mémoire, zéro-allocation), car sur un moteur d'échecs le facteur dominant est l'explosion combinatoire de l'arbre de recherche (O(b^d)) — même principe que la loi d'Amdahl vue en Séance 2 de HashBreaker : optimiser un facteur secondaire (vitesse d'exécution) ne se voit pas tant que le facteur dominant (nombre de positions visitées) n'est pas traité.

## Ce qui a été fait

Création de [MinimaxAlphaBeta.java](../src/main/java/com/moteurechecs/recherche/MinimaxAlphaBeta.java), qui reprend exactement la structure de `Minimax.java` en ajoutant les bornes `alpha`/`beta` :

- **Coupure beta** (branche maximisante) : dès que `alpha >= beta`, on arrête d'explorer les coups restants à ce nœud — l'adversaire a déjà une meilleure option ailleurs, ce sous-arbre ne peut plus influencer la décision finale.
- **Coupure alpha** (branche minimisante) : symétrique.
- **Aucune coupure au niveau racine** (`meilleurCoup()`) : il faut comparer tous les coups candidats pour identifier le meilleur, mais `alpha`/`beta` se resserrent progressivement, ce qui aide à couper plus tôt dans les sous-arbres des coups suivants.

**Limite assumée pour cette V2** : aucun tri des coups (*move ordering*). L'ordre de parcours reste celui de `GenerateurCoups` (scan du plateau), pas optimisé pour maximiser les coupures. Amélioration future possible (ex : tester les captures en premier, heuristique MVV-LVA).

## Validation de l'équivalence mathématique

L'élagage alpha-beta ne doit **jamais** changer la décision — seulement réduire le nombre de positions visitées pour y arriver. Deux tests dédiés vérifient explicitement que `MinimaxAlphaBeta` choisit **exactement le même coup** que `Minimax` (position de départ et après quelques coups joués), plus un test confirmant qu'il visite bien strictement moins de positions.

**Résultat : 24/24 tests passent** (5 nouveaux dans `MinimaxAlphaBetaTest`).

## Résultat mesuré — comparaison directe contre l'Étape 1

| Profondeur | Version | Positions évaluées | Temps | Coup trouvé |
|---|---|---|---|---|
| 3 | Naïf (Étape 1) | 9 322 | 437 ms | `b1c3` |
| 3 | Alpha-Beta | 585 | 22 ms | `b1c3` |
| 4 | Naïf (Étape 1) | 206 603 | 8 791 ms | `b1c3` |
| 4 | Alpha-Beta | 1 416 | 53 ms | `b1c3` |

**Facteurs de réduction à profondeur 4** : ÷145,9 en positions visitées, ÷165,9 en temps d'exécution. Même coup trouvé dans tous les cas — confirme l'équivalence mathématique sur un cas réel, pas seulement en test unitaire.

## Ce que ça débloque

Avec l'arbre drastiquement réduit, des profondeurs jusque-là inaccessibles au Minimax naïf deviennent explorables en quelques secondes :

| Profondeur | Positions évaluées | Temps |
|---|---|---|
| 5 | 41 554 | 1 830 ms |
| 6 | 645 199 | 29 185 ms |

*(mesuré en dehors de `Main.java`, à titre indicatif — le Minimax naïf à ces profondeurs prendrait plusieurs minutes à plusieurs heures, non mesuré)*

**Note curieuse (pas un bug)** : le meilleur coup change entre les profondeurs (`b1c3` à 3-4, `b2b3` à 5, retour à `b1c3` à 6) — normal avec une évaluation purement matérielle : plus la recherche est profonde, plus elle voit loin dans les échanges possibles, ce qui peut faire changer l'appréciation d'un coup (effet d'horizon, phénomène connu des moteurs sans *quiescence search*).

## Fichiers concernés

- [src/main/java/com/moteurechecs/recherche/MinimaxAlphaBeta.java](../src/main/java/com/moteurechecs/recherche/MinimaxAlphaBeta.java)
- [src/test/java/com/moteurechecs/recherche/MinimaxAlphaBetaTest.java](../src/test/java/com/moteurechecs/recherche/MinimaxAlphaBetaTest.java)
- [src/main/java/com/moteurechecs/Main.java](../src/main/java/com/moteurechecs/Main.java) (affiche désormais les deux versions côte à côte)
