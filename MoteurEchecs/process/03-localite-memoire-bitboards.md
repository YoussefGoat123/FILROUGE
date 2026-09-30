# Étape 03 — Localité Mémoire : Bitboards (Micro-optimisation)

**Type de levier :** micro-optimisation — même information représentée différemment en mémoire, aucun changement algorithmique.

**Nouvelle référence pour la comparaison :** cette étape se compare à elle-même (Structure A vs Structure B, même principe que HashBreaker Séance 2), pas à l'Étape 2 (Alpha-Beta) — les deux leviers sont orthogonaux (l'un touche `recherche/`, l'autre `modele/`).

## Ce qui a été fait

Création de [PlateauBits.java](../src/main/java/com/moteurechecs/modele/PlateauBits.java) : le plateau encodé dans **12 `long`** (un par couple couleur/type de pièce), chaque bit indiquant si une pièce occupe la case correspondante — au lieu de `Plateau.java` actuel, une grille `Piece[8][8]` de **références** vers des objets dispersés sur le tas (même piège que `Node` en Séance 2 de HashBreaker).

Expérience de comparaison dans [EtapeLocaliteMemoire.java](../src/main/java/com/moteurechecs/experimentation/EtapeLocaliteMemoire.java), qui mesure — comme `Seance2LocaliteMemoire.java` sur HashBreaker — un accès pur répété, avec checksum pour empêcher le Dead Code Elimination.

## Choix important : quelle opération comparer

Comparer une simple lecture de case (`pieceEn(ligne, colonne)`) aurait été **trompeur** : c'est une des rares opérations où un tableau d'objets reste compétitif (un seul accès indexé, contre jusqu'à 12 tests de bits dans notre implémentation naïve de `pieceEn`). L'opération choisie est celle qui compte vraiment pour un générateur de coups : **"quelles cases sont occupées par telle couleur ?"**

- **Grille d'objets** : balaie les 64 cases, teste chacune — coût fixe **O(64)**, quel que soit le nombre de pièces réellement présentes.
- **Bitboards** : `occupationCouleur()` fait l'union de 6 `long` (1 opération), puis on itère uniquement sur les bits posés via `Long.numberOfTrailingZeros()` — coût **O(nombre de pièces)**, soit 16 en position de départ, pas 64.

## Validation de cohérence

- [PlateauBitsTest.java](../src/test/java/com/moteurechecs/modele/PlateauBitsTest.java) : position de départ identique case par case à `Plateau` (test croisé sur les 64 cases), `jouerCoup` correct (déplacement, capture), popcount = 16 par couleur en position de départ.
- [EtapeLocaliteMemoireTest.java](../src/test/java/com/moteurechecs/experimentation/EtapeLocaliteMemoireTest.java) : les deux méthodes d'énumération renvoient le même checksum, avant et après un coup joué.

**Résultat : 35/35 tests passent** (11 nouveaux).

## Résultat mesuré

| Structure | Essais (ms, 500 000 répétitions) | Moyenne |
|---|---|---|
| A — grille d'objets | 26, 19, 15, 15, 16 | ~18,2 ms |
| B — bitboards | 19, 14, 17, 10, 9 | ~13,8 ms |

**Gain mesuré : ~×1,3** — checksums identiques (60 000 000 dans les deux cas), donc même résultat, juste calculé plus vite.

## Interprétation honnête : ce n'est PAS la même histoire que HashBreaker Séance 2

Sur HashBreaker, le facteur x8-x10 venait de la **localité spatiale en cache** : un grand tableau contigu (des millions de candidats) profitait des lignes de cache de 64 octets, contrairement à une liste chaînée dispersée sur un tas de plusieurs mégaoctets.

Ici, le plateau est **minuscule** (64 cases, 16 pièces) — bien plus petit qu'une seule ligne de cache L1. L'effet "cache froid vs cache chaud" ne joue quasiment aucun rôle à cette échelle : les deux structures tiennent largement en cache L1 en permanence. Le gain mesuré (~×1,3) vient d'un mécanisme différent : **moins d'opérations exécutées** (parcourir 16 bits posés au lieu de tester 64 cases une par une), pas d'un meilleur accès mémoire au sens strict.

**Leçon méthodologique** : ne pas supposer qu'un levier qui a bien fonctionné sur un projet fonctionnera pour la même raison sur un autre — ici, "bitboards" est réellement un gain, mais le *pourquoi* est différent de HashBreaker, et c'est en mesurant qu'on l'a découvert, pas en le devinant à l'avance.

## Ce qui n'est PAS encore fait

Cette étape mesure l'accès pur — **`PlateauBits` n'est pas encore intégré** dans `GenerateurCoups` / `MinimaxAlphaBeta`, qui continuent d'utiliser `Plateau` (objets). Intégrer réellement les bitboards dans la génération de coups (avec tables d'attaque précalculées pour cavalier/roi, décalages de bits pour les pions, etc.) est un chantier plus vaste, laissé pour une étape future — même prudence que HashBreaker Séance 2 Parties 1-2 (mesure pure) vs Parties 3-4 (impact sur le vrai algorithme).

## Fichiers concernés

- [src/main/java/com/moteurechecs/modele/PlateauBits.java](../src/main/java/com/moteurechecs/modele/PlateauBits.java)
- [src/main/java/com/moteurechecs/experimentation/EtapeLocaliteMemoire.java](../src/main/java/com/moteurechecs/experimentation/EtapeLocaliteMemoire.java)
- [src/test/java/com/moteurechecs/modele/PlateauBitsTest.java](../src/test/java/com/moteurechecs/modele/PlateauBitsTest.java)
- [src/test/java/com/moteurechecs/experimentation/EtapeLocaliteMemoireTest.java](../src/test/java/com/moteurechecs/experimentation/EtapeLocaliteMemoireTest.java)
