# Étape 09 — Recherche à budget de temps (Iterative Deepening)

**Nature du levier** : MACRO, mais différente des précédentes (Étapes 2 et 8) — celles-là réduisaient le nombre de positions visitées pour une profondeur donnée. Celle-ci **rajoute** du travail (les profondeurs 1 à N-1 sont refaites) en échange d'une **capacité nouvelle** : répondre sous une vraie contrainte de temps, pas une profondeur fixe arbitraire choisie à l'avance.

**Pourquoi maintenant** : trou identifié en analysant les cours déjà traités (J3_PM, pattern `context.WithCancel`/`ctx.Done()`) — signalé comme absent à plusieurs reprises mais jamais formalisé en étape propre. Contrairement à la table de transposition (dépend d'un cache externe) ou la recherche parallèle (dépend de threads), celui-ci ne demande aucun prérequis et peut se faire immédiatement.

## Ce qui a été fait

Nouvelle méthode `MinimaxAlphaBeta.meilleurCoupBudgetTemps(Plateau, long budgetMillis)` : boucle sur des profondeurs croissantes (1, 2, 3...), retourne le coup de la **dernière profondeur complètement terminée** — jamais un résultat partiel.

```java
public static Coup meilleurCoupBudgetTemps(Plateau plateau, long budgetMillis) {
    deadlineNanos = System.nanoTime() + budgetMillis * 1_000_000L;
    ...
    while (System.nanoTime() < deadlineNanos) {
        try {
            Coup candidat = meilleurCoup(plateau, profondeur);
            meilleurCoupTotal = candidat; // profondeur complete, adoptee
            profondeur++;
        } catch (RechercheInterrompue interruption) {
            break; // profondeur incomplete, jetee -- garde le resultat precedent
        }
    }
    return meilleurCoupTotal;
}
```

Vérification du temps à l'intérieur de `alphabeta()` (pas seulement entre deux profondeurs), tous les 1024 nœuds (`positionsEvaluees & 1023`) pour limiter le coût de l'appel `System.nanoTime()` lui-même — pas à chaque nœud.

### Trois bugs trouvés et corrigés avant que le code soit correct

1. **`deadlineNanos` par défaut à `0`** : `System.nanoTime() >= 0` est presque toujours vrai, donc **tout appel direct à `meilleurCoup()`** (tests existants, `DiagnosticAllocations`, `Main.java`) déclenchait une interruption immédiate. Trouvé par les tests existants (47/47 → 1 échec dès la première compilation). Corrigé : valeur par défaut `Long.MAX_VALUE` ("pas de deadline").
2. **Deadline non réinitialisée après un budget de temps** : un appel à `meilleurCoup()` juste après un `meilleurCoupBudgetTemps()` héritait d'une deadline périmée (dans le passé) et échouait à tort. Corrigé avec un `finally` qui remet `deadlineNanos = Long.MAX_VALUE`.
3. **Risque de corruption du `Plateau`** : `jouer()`/`annuler()` mutent le plateau en place (Étape 4) — si `RechercheInterrompue` est levée entre un `jouer()` et son `annuler()` correspondant (à n'importe quel niveau de récursion), l'`annuler()` est sauté et le plateau reste à moitié joué. Corrigé en entourant chaque `alphabeta()` récursif d'un `try/finally` garantissant l'annulation, à tous les niveaux (`meilleurCoup()` et les deux branches de `alphabeta()`).

Ces trois bugs (surtout le 3ème) auraient été très difficiles à détecter sans les tests dédiés écrits pour cette étape — exactement le genre de piège qu'une suite de tests solide est censée attraper.

**Exception de contrôle de flux zéro-allocation** : `RechercheInterrompue` désactive son message et sa stack trace (`super(null, null, false, false)`), et une **seule instance** est réutilisée pour toutes les interruptions — même principe que le zéro-allocation de l'Étape 4, appliqué ici au mécanisme d'interruption plutôt qu'à `caseAttaquee()`.

## Tests ajoutés (4)

- Grand budget (5 s) → converge vers le même coup qu'une recherche directe à profondeur fixe équivalente.
- Budget très court (1 ms) → renvoie quand même un coup valide.
- Interruption forcée → le plateau est revenu **exactement** à son état initial (`coupsLegaux()` renvoie le même nombre de coups avant/après).
- Appel direct à `meilleurCoup()` juste après un budget de temps → ne subit pas la deadline périmée (bug #2 ci-dessus, testé explicitement pour ne pas régresser).

51/51 tests passent (47 existants + 4 nouveaux).

## Résultat mesuré

`experimentation/DiagnosticBudgetTemps.java`, position de départ, 5 budgets, 3 runs pour vérifier la reproductibilité :

| Budget | Temps réel | Profondeur atteinte | Positions (total, cumulé) | Coup |
|---|---|---|---|---|
| 200 ms | ~211-216 ms | **4** | 7 224 - 10 296 | `b1c3` |
| 500 ms | ~502-505 ms | **5** | 48 927 - 62 239 | `b2b3` |
| 1000 ms | ~1003-1006 ms | **5** | 131 871 - 135 967 | `b2b3` |
| 2000 ms | ~2002-2008 ms | **5** | 242 463 - 263 967 | `b2b3` |
| 5000 ms | ~5000-5008 ms | **6** | 564 836 - 569 956 | `b1c3` |

**Stable sur 3 runs** : mêmes profondeurs atteintes, mêmes coups trouvés à chaque budget, temps réel toujours à 0,3-8% du budget demandé (jamais un gros dépassement).

## Interprétation honnête

**Le coup trouvé à profondeur 5 (`b2b3`) diffère de celui à profondeur 6 (`b1c3`)** — ce n'est pas un bug : avec une évaluation purement matérielle (aucune table de position), le meilleur coup peut légitimement changer d'une profondeur à l'autre. C'est justement l'intérêt de l'iterative deepening : plus le budget de temps est long, plus la décision devient fiable.

**Entre 500 ms et 2000 ms, la profondeur reste bloquée à 5** malgré un budget 4x plus long — parce que la profondeur 6 est commencée mais jamais terminée dans ce budget (confirmé par le nombre de positions qui grimpe fortement d'un budget à l'autre : 48 927 → 242 463, cette différence est le travail de profondeur 6 fait puis **jeté**). C'est le vrai coût de ce levier, documenté honnêtement plutôt que masqué : entre 500 ms et 2000 ms, une part significative du temps sert à explorer une profondeur 6 qui sera finalement abandonnée.

**Ce n'est donc pas un levier "gratuit"** comme l'alpha-beta (Étape 2) — c'est un compromis assumé : accepter de perdre du travail sur les profondeurs interrompues, en échange de la garantie de toujours avoir un coup valide disponible dans un budget de temps donné.

## Fichiers concernés

- [src/main/java/com/moteurechecs/recherche/MinimaxAlphaBeta.java](../src/main/java/com/moteurechecs/recherche/MinimaxAlphaBeta.java) (modifié en place)
- [src/test/java/com/moteurechecs/recherche/MinimaxAlphaBetaTest.java](../src/test/java/com/moteurechecs/recherche/MinimaxAlphaBetaTest.java) (4 tests ajoutés)
- [src/main/java/com/moteurechecs/experimentation/DiagnosticBudgetTemps.java](../src/main/java/com/moteurechecs/experimentation/DiagnosticBudgetTemps.java) (nouveau, outil de mesure)
