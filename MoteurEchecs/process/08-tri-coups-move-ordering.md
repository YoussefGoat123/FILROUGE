# Étape 08 — Tri des coups (Move Ordering, MVV-LVA)

**Décision de séquencement** : levier choisi à la place de la table de transposition initialement prévue à ce rang (roadmap `choix-sujet.md`), parce qu'il cible **sans détour** le Hot Path mesuré à l'Étape 7 (vérification de légalité + génération de coups, ~90% du temps CPU, proportionnel au nombre de positions visitées) — même logique de séquencement que l'Étape 2 (macro avant micro).

## Ce qui a été fait

Ajout d'un tri MVV-LVA (*Most Valuable Victim - Least Valuable Aggressor*) dans `MinimaxAlphaBeta` : avant chaque exploration (`meilleurCoup()` et `alphabeta()`), la liste de coups légaux est triée pour explorer les captures en premier, par valeur de la pièce capturée décroissante.

```java
private static void trierCoups(Plateau plateau, List<Coup> coups) {
    coups.sort((a, b) -> scoreTri(plateau, b) - scoreTri(plateau, a));
}

private static int scoreTri(Plateau plateau, Coup coup) {
    Piece cible = plateau.pieceEn(coup.ligneArrivee(), coup.colonneArrivee());
    if (cible == null) {
        return 0; // coup calme : score neutre, ordre relatif inchange (tri stable)
    }
    Piece attaquant = plateau.pieceEn(coup.ligneDepart(), coup.colonneDepart());
    return 10 * Evaluateur.valeur(cible.type()) - Evaluateur.valeur(attaquant.type());
}
```

`Evaluateur.valeur(TypePiece)` rendue publique pour être réutilisée ici (source unique de vérité sur les valeurs de pièces, pas de duplication).

**Pourquoi MVV-LVA et pas un tri plus simple** : capturer une pièce de forte valeur avec une pièce de faible valeur (ex: Pion prend Dame) doit être exploré avant l'inverse (Dame prend Pion) — le premier cas est presque toujours souhaitable, le second risque de perdre la Dame pour rien. Pondérer la victime ×10 par rapport à l'attaquant reproduit cette priorité standard sans complexité excessive.

**Code de production modifié en place**, conformément à la convention établie depuis l'Étape 4 (pas de classe parallèle).

## Résultat mesuré

Mesuré sur `DiagnosticAllocations` (Alpha-Beta seul, profondeur 5, position de départ), Hyperfine 5 essais.

⚠️ **Incohérence de baseline détectée et documentée** : la mesure "avant" prise maintenant (887,1 ms ± 43,0 ms) diffère significativement de celle documentée à l'Étape 6 (426,3 ms). Code identique entre les deux (aucun changement de `MinimaxAlphaBeta`/`Plateau`/`GenerateurCoups` entre l'Étape 6 et maintenant, seuls des ajouts de classes de diagnostic séparées). Reproduit sur 2 sessions Hyperfine indépendantes (887,1 ms puis 859,3 ms) — **pas du bruit aléatoire**, mais probablement une différence de conditions machine (charge en arrière-plan, alimentation) entre les deux sessions de mesure, à des jours différents. Le facteur relatif Étape 8 (avant/après, mêmes conditions machine) reste valide ; la valeur absolue "avant" n'est **pas comparable en valeur absolue** au 426,3 ms de l'Étape 6.

| Mesure | Avant (Étape 8) | Après (Étape 8) | Facteur |
|---|---|---|---|
| Temps (Hyperfine) | 887,1 ms ± 43,0 ms | 709,8 ms ± 23,1 ms | **×1,25** |
| Positions évaluées | 41 554 | 25 319 | **−39 %** |
| Échantillons d'allocation (JFR) | 120 | 132 | +10 % |

## Interprétation honnête

**Écart entre le gain sur les positions (−39%) et le gain sur le temps (×1,25 seulement)** : le tri lui-même a un coût — un comparateur est exécuté à chaque nœud interne de l'arbre (pas seulement à la racine), et ce coût mange une partie du bénéfice apporté par la réduction du nombre de positions visitées. C'est cohérent avec la légère hausse des échantillons d'allocation JFR (120 → 132) : le lambda capturant `plateau` dans `trierCoups()` est instancié à chaque appel.

**Ce résultat n'est pas décevant** — c'est un vrai levier macro (même famille que l'Étape 2), qui réduit effectivement le volume de travail, mais son bénéfice net est amorti par son propre coût d'exécution. Documenté tel quel, sans gonfler le facteur ×1,25 en le présentant comme équivalent au −39% de positions.

**Équivalence préservée** : 47/47 tests passent, y compris les tests d'équivalence Minimax/Alpha-Beta — le tri ne change que l'**ordre** d'exploration, jamais le résultat final (même coup trouvé : `b2b3`).

**Piste micro pour plus tard** (non traitée ici, cohérent avec le principe "macro d'abord") : le coût du tri lui-même pourrait être réduit (éviter la capture de `plateau` dans le lambda, ou précalculer les scores dans un tableau plutôt que les recalculer à chaque comparaison) — pertinent seulement si un futur profiling montre que `trierCoups()`/`scoreTri()` devient significatif dans le Hot Path.

## Fichiers concernés

- [src/main/java/com/moteurechecs/recherche/MinimaxAlphaBeta.java](../src/main/java/com/moteurechecs/recherche/MinimaxAlphaBeta.java) (modifié en place)
- [src/main/java/com/moteurechecs/evaluation/Evaluateur.java](../src/main/java/com/moteurechecs/evaluation/Evaluateur.java) (`valeur()` rendue publique)
- [profiling/etape8-avant-hyperfine.md](../profiling/etape8-avant-hyperfine.md), [profiling/etape8-apres-hyperfine.md](../profiling/etape8-apres-hyperfine.md), [profiling/etape8-diagnostic-apres.jfr](../profiling/etape8-diagnostic-apres.jfr)
