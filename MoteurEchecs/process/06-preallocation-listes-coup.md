# Étape 06 — Pré-allocation de capacité (listes de `Coup`) — levier rattrapé

**Type de levier :** micro-optimisation — application littérale de l'exemple du cours J2_PM (section 9, "Goulot : Réallocations de Slices").

**Contexte** : ce levier avait été identifié dès l'Étape 4 comme "reporté" (les listes de `Coup` restaient le premier poste d'allocation, 42/120 échantillons), puis oublié en pratique en enchaînant sur autre chose. Rattrapé après relecture explicite du cours.

## Ce qui a été fait

Deux `ArrayList<Coup>` dans `GenerateurCoups.java`, toutes deux créées sans capacité initiale (`new ArrayList<>()`, capacité effective 0, grandissant par paliers 10→15→22→33... à chaque dépassement, soit 2-3 réallocations internes par appel pour une position typique à 20-40 coups) :

```java
// AVANT
List<Coup> legaux = new ArrayList<>();
List<Coup> coups = new ArrayList<>();

// APRES
List<Coup> legaux = new ArrayList<>(pseudoLegaux.size());  // capacite EXACTE (filtrage pur)
List<Coup> coups = new ArrayList<>(CAPACITE_COUPS_ESTIMEE); // capacite ESTIMEE (48, documentee comme telle)
```

**Distinction importante entre les deux cas** :
- `coupsLegaux()` : `legaux` ne peut **jamais** dépasser `pseudoLegaux.size()` (on ne fait que filtrer) — capacité exacte, pas un pari.
- `coupsPseudoLegaux()` : la taille finale n'est pas connue à l'avance — `48` est une **estimation documentée** (une position dépasse rarement 40-50 coups pseudo-légaux en jeu réel ; le maximum théorique aux échecs est 218, mais dans une position jamais rencontrée en pratique).

## Validation

**47/47 tests passent**, sans modification — le pré-dimensionnement de capacité ne change ni le contenu ni l'ordre d'une `ArrayList`, seulement son mode d'allocation interne. Même coup trouvé (`b2b3`) qu'avant la correction.

## Résultat mesuré

| Mesure | Avant (fin Étape 4) | Après | Facteur |
|---|---|---|---|
| Temps (Hyperfine, 5 essais) | 475,3 ms ± 37,8 ms | 426,3 ms ± 35,8 ms | **×1,11** |
| Échantillons d'allocation (JFR) | 120 | 120 | inchangé (bruit statistique) |
| Cycles Young GC | 2 | 2 | inchangé |

## Interprétation honnête

Le gain de temps (~11%) est réel dans sa tendance centrale, mais **les intervalles ±1σ se chevauchent légèrement** (avant : [437,5 ; 513,1] ms, après : [390,5 ; 462,1] ms) — avec seulement 5 essais, ce n'est pas une preuve statistique aussi solide que les gains massifs des étapes précédentes (×4,2, ×145,9...). Les échantillons d'allocation JFR n'ont montré aucune baisse visible non plus — attendu, puisque ce levier réduit le **nombre** de petits tableaux internes recopiés (2-3 par appel), pas le volume total d'octets alloués, et l'échantillonneur JFR est sensible au débit d'octets, pas au nombre brut d'objets.

**Ce résultat illustre honnêtement une limite du levier** : contrairement à `caseAttaquee()` (Étape 4, ×4,2) qui éliminait un vrai travail inutile, la pré-allocation de capacité optimise un détail d'implémentation d'`ArrayList` — un gain réel mais d'un ordre de grandeur différent, cohérent avec ce qu'on pouvait raisonnablement attendre.

## Fichiers concernés

- [src/main/java/com/moteurechecs/regles/GenerateurCoups.java](../src/main/java/com/moteurechecs/regles/GenerateurCoups.java)
- [profiling/etape6-diagnostic-apres.jfr](../profiling/etape6-diagnostic-apres.jfr), [profiling/etape6-apres-hyperfine.md](../profiling/etape6-apres-hyperfine.md)
