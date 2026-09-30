# Étape 04 — Zéro-Allocation : `caseAttaquee()` directe + Make/Unmake

**Type de levier :** micro-optimisation (aucun changement de résultat, seulement de la façon dont il est calculé) — mais composée de **deux corrections de nature différente**, décidées après diagnostic, pas devinées à l'avance.

**Décision de conception importante** : une seule version du code (pas de classe parallèle "avant/après" comme sur HashBreaker). `Plateau.java`, `GenerateurCoups.java` et `MinimaxAlphaBeta.java` sont modifiés **en place**. La comparaison avant/après repose sur des mesures prises juste avant modification (Hyperfine + JFR), conservées dans `profiling/` et documentées ici — pas sur du code dupliqué à maintenir indéfiniment. Choix assumé en vue d'un dépôt Git propre pour la remise du projet noté.

## Diagnostic préalable (avant tout code)

Profil JFR sur `MinimaxAlphaBeta` seul (profondeur 5, 41 554 positions, ~1,8-2 s) : voir conversation — le coupable principal n'était **pas** la copie de `Plateau` comme supposé au départ, mais `caseAttaquee()` qui régénérait la **liste complète des coups pseudo-légaux** d'une couleur juste pour savoir si **une seule case** était attaquée.

| Ligne de code | Échantillons (avant) | Ce que c'est |
|---|---|---|
| `caseAttaquee()` | 240 (~40%) | Génère toute la liste de coups pour une seule question oui/non |
| `coupsPseudoLegaux()` (construction) | ~189 | Les `new Coup(...)` de cette génération |
| `coupsLegaux()` (filtrage) | 152 | Rejoue chaque coup via `jouerCoup()` |
| `Plateau.jouerCoup()` (copie de grille) | 152 | La copie complète du plateau |

## Correction 1 — `caseAttaquee()` réécrite (PAS un buffer réutilisé)

Testée directement contre la case ciblée, pattern par pattern (pion, cavalier, roi, glissantes fou/tour/dame), **sans jamais construire de `Coup` ni de `List`**. Ce n'est pas une "réutilisation de buffer" au sens du cours — c'est l'élimination pure et simple d'un travail qui n'était pas nécessaire pour répondre à la question posée.

```java
// avant : genere TOUTE la liste des coups pour verifier UNE case
static boolean caseAttaquee(Plateau plateau, int ligne, int colonne, Couleur parCouleur) {
    for (Coup coup : coupsPseudoLegaux(plateau, parCouleur)) {
        if (coup.ligneArrivee() == ligne && coup.colonneArrivee() == colonne) return true;
    }
    return false;
}

// apres : teste directement chaque pattern d'attaque, zero allocation
static boolean caseAttaquee(Plateau plateau, int ligne, int colonne, Couleur parCouleur) {
    if (attaquePion(...)) return true;
    if (attaqueParSaut(..., SAUTS_CAVALIER, CAVALIER)) return true;
    if (attaqueParSaut(..., DIRECTIONS_DAME_ROI, ROI)) return true;
    if (attaqueGlissante(..., DIRECTIONS_FOU, FOU)) return true;
    if (attaqueGlissante(..., DIRECTIONS_TOUR, TOUR)) return true;
    return attaqueGlissante(..., DIRECTIONS_DAME_ROI, DAME);
}
```

## Correction 2 — Make/Unmake sur `Plateau` (VRAI buffer réutilisé)

Ici, application littérale du principe du cours (J2_AM, Partie 3 : "buffers fixes réutilisés, mutation directe d'index"). `Plateau` gagne deux nouvelles méthodes :

- **`jouer(coup)`** : mute la grille **en place**, renvoie un petit `record InfoAnnulation` (pièce d'origine + pièce éventuellement capturée) — à comparer à la copie complète d'une grille 8x8.
- **`annuler(coup, info)`** : restaure exactement l'état d'avant.

`jouerCoup(coup)` (copie complète, immuable) **reste disponible** — utilisée par les tests et la mise en place de positions, mais **retirée du chemin chaud**. `GenerateurCoups.coupsLegaux()` et `MinimaxAlphaBeta` utilisent désormais `jouer()`/`annuler()` exclusivement pour la recherche.

## Validation

- **Équivalence fonctionnelle** : `MinimaxAlphaBetaTest` (5 tests d'équivalence avec `Minimax` pur) passe toujours sans modification — la recherche produit exactement les mêmes résultats.
- **Nouveaux tests dédiés** : 6 tests sur `jouer()`/`annuler()` (déplacement, restauration après capture, cent cycles jouer/annuler sans corruption, cohérence avec `jouerCoup()`), 5 tests directs sur `caseAttaquee()` (cavalier, pion, glissante bloquée/débloquée, case non attaquée).

Un test avait une hypothèse fausse au départ (`caseAttaquee(e3) == false` pour les Blancs) — corrigé : e3 est en réalité attaquée en diagonale par les pions **voisins** (d2 et f2), pas par le pion e2 lui-même. Le code était juste, c'est le test qui devait être ajusté — bonne vérification que la réécriture n'a pas introduit de régression silencieuse.

**Résultat : 47/47 tests passent** (12 nouveaux).

## Résultat mesuré

### Allocations (JFR, profondeur 5)

| Mesure | Avant | Après | Facteur |
|---|---|---|---|
| Échantillons d'allocation | 600 | 120 | ÷5 |
| Cycles Young GC | 25 | 2 | ÷12,5 |
| `Piece[][]`/`Piece[]` (copie de grille) | 152 | 1 | quasi disparu |

### Temps (Hyperfine, 5 essais, warmup 1)

| Mesure | Avant | Après |
|---|---|---|
| Temps moyen | 1,999 s ± 0,058 s | 475,3 ms ± 37,8 ms |

**Gain : ×4,2**, coup trouvé identique (`b2b3`) dans les deux cas — confirmé par JFR avant/après ET par les tests d'équivalence.

## Ce qui reste pour plus tard (limite assumée)

Les listes de `Coup` (`coupsPseudoLegaux()`, un `new ArrayList<>()` à chaque appel) ne sont **pas encore réutilisées** — `Coup` reste le premier allocateur après correction (42/120 échantillons). C'est le 3ème levier "buffer réutilisé" identifié dès le départ, volontairement reporté pour ne pas trop élargir cette étape.

## Fichiers concernés

- [src/main/java/com/moteurechecs/modele/Plateau.java](../src/main/java/com/moteurechecs/modele/Plateau.java)
- [src/main/java/com/moteurechecs/regles/GenerateurCoups.java](../src/main/java/com/moteurechecs/regles/GenerateurCoups.java)
- [src/main/java/com/moteurechecs/recherche/MinimaxAlphaBeta.java](../src/main/java/com/moteurechecs/recherche/MinimaxAlphaBeta.java)
- [src/main/java/com/moteurechecs/experimentation/DiagnosticAllocations.java](../src/main/java/com/moteurechecs/experimentation/DiagnosticAllocations.java) (nouveau, outil de mesure réutilisable)
- [src/test/java/com/moteurechecs/modele/PlateauTest.java](../src/test/java/com/moteurechecs/modele/PlateauTest.java) (+6 tests)
- [src/test/java/com/moteurechecs/regles/GenerateurCoupsTest.java](../src/test/java/com/moteurechecs/regles/GenerateurCoupsTest.java) (+6 tests)
- [profiling/etape4-diagnostic-naif.jfr](../profiling/etape4-diagnostic-naif.jfr), [profiling/etape4-diagnostic-apres.jfr](../profiling/etape4-diagnostic-apres.jfr), [profiling/etape4-avant-hyperfine.md](../profiling/etape4-avant-hyperfine.md), [profiling/etape4-apres-hyperfine.md](../profiling/etape4-apres-hyperfine.md)
