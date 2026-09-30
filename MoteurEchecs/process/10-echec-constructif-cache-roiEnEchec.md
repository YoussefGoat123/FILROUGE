# Étape 10 — Échec Constructif : cache naïf de `roiEnEchec()` (Axe 4 du barème)

**Objectif (barème, Axe 4 — 3 pts)** : documenter au moins une tentative d'optimisation ratée, chiffrée. Fait ici volontairement, en s'appuyant sur le vrai Hot Path mesuré à l'Étape 7 plutôt que sur un exemple artificiel.

## Hypothèse d'impact matériel

`roiEnEchec()`/`caseAttaquee()` domine à 59-72% du temps CPU (Étape 7), appelée une fois par coup pseudo-légal testé dans `coupsLegaux()`. Hypothèse plausible : **mémoïser** le résultat dans un cache `HashMap<String, Boolean>` clé sur une sérialisation du plateau devrait éviter de recalculer un résultat déjà connu, réduisant le temps CPU sur le poste dominant.

## Implémentation testée

```java
private static final Map<String, Boolean> CACHE_ECHEC_EXPERIMENTAL = new HashMap<>();

public static boolean roiEnEchec(Plateau plateau, Couleur couleur) {
    String cle = construireCleExperimentale(plateau, couleur); // serialise les 64 cases + couleur
    Boolean enCache = CACHE_ECHEC_EXPERIMENTAL.get(cle);
    if (enCache != null) return enCache;
    boolean resultat = caseAttaquee(plateau, ...);
    CACHE_ECHEC_EXPERIMENTAL.put(cle, resultat);
    return resultat;
}
```

## Commande de profiling pour vérification

```
hyperfine --warmup 1 --runs 5 scripts\run-diagnostic-allocations.cmd
java -XX:StartFlightRecording=filename=echec-diagnostic.jfr,settings=profile ... DiagnosticAllocations
jfr print --events jdk.ObjectAllocationSample echec-diagnostic.jfr | grep objectClass | sort | uniq -c | sort -rn
```

## Bilan mesuré

| Mesure | Avant (Étape 8, sans cache) | Après (avec cache) | Variation |
|---|---|---|---|
| Temps (Hyperfine, 5 essais) | 709,0 ms ± 27,8 ms | 1 481 ms ± 25 ms | **×2,09 plus lent** |
| Positions évaluées | 25 319 | 25 319 | inchangé (décision correcte préservée) |
| Échantillons d'allocation (JFR) | 132 | 462 | **+250 %** |
| Test unitaire `budgetTemps_grandBudget_...` | passe | **échoue** (coup différent, profondeur atteinte réduite dans le budget de 5s) | régression collatérale |

**Répartition des nouvelles allocations (JFR)** : `byte[]` (311, tableaux internes des `String` générées), `String` (28), `StringBuilder` (23), `HashMap$Node` (12) — dominent désormais très largement `Coup` (39, avant : poste principal).

## Cause physique

1. **Coût de construction de la clé supérieur au coût évité** : `roiEnEchec()` est déjà zéro-allocation depuis l'Étape 4 (parcours direct des patterns d'attaque, aucune liste générée). Construire une clé `String` de 65 caractères (boucle sur 64 cases + couleur) sur **chaque appel** réintroduit exactement l'allocation que l'Étape 4 avait supprimée — mais cette fois systématiquement, pas seulement dans le cas qu'elle corrigeait.
2. **Taux de succès du cache quasi nul** : sans hachage de position incrémental (Zobrist hashing, non implémenté), quasiment chaque nœud de l'arbre de recherche correspond à une position **unique** à cette profondeur — le cache est interrogé des dizaines de milliers de fois mais n'apporte presque aucune réutilisation réelle, tout en payant systématiquement le coût de la clé.
3. **Régression collatérale sur l'Étape 9** : le ralentissement modifie la profondeur atteignable dans un budget de temps fixe (iterative deepening), cassant un test qui comparait une recherche à budget à une recherche à profondeur fixe équivalente — une preuve supplémentaire, non anticipée, que le ralentissement est réel et mesurable à plusieurs niveaux du système.

## Verdict d'ingénierie

**Rejeté.** Code entièrement retiré (`git checkout` sur `GenerateurCoups.java`), aucune trace dans le chemin de production. 51/51 tests repassent au vert après retrait.

**Piste correcte pour un cache de positions** (notée pour l'Étape 11, Table de transposition) : un vrai cache de positions nécessite une clé bon marché à calculer — typiquement un **hachage de Zobrist**, mis à jour incrémentalement à chaque `jouer()`/`annuler()` (XOR en O(1) par coup) plutôt que recalculé from scratch par sérialisation complète du plateau à chaque appel. C'est précisément la différence entre cette tentative ratée et une table de transposition bien conçue.

## Fichiers concernés

- Expérimentation non conservée dans le code (retirée par `git checkout`) — reproductible depuis ce document
- [profiling/echec-avant-hyperfine.md](../profiling/echec-avant-hyperfine.md), [profiling/echec-apres-hyperfine.md](../profiling/echec-apres-hyperfine.md), [profiling/echec-diagnostic.jfr](../profiling/echec-diagnostic.jfr)
