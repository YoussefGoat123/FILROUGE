# Synthèse — Échec Constructif : cache naïf de `roiEnEchec()` (Axe 4)

## En une phrase

Une tentative plausible (mémoïser la fonction qui domine 59-72% du CPU) a été implémentée, mesurée, et rejetée : ×2,09 plus lent, +250% d'allocations, cause physique identifiée et comprise, code entièrement retiré.

## Hypothèse testée

```mermaid
flowchart LR
    H["Hypothese : roiEnEchec() domine le CPU (Etape 7)\n-> la memoiser devrait accelerer"] --> I["Implementation :\nHashMap<String,Boolean>\ncle = serialisation du plateau"]
    I --> M["Mesure Hyperfine + JFR"]

    style H fill:#2d6cdf,color:#fff
```

## Résultat mesuré

```mermaid
xychart-beta
    title "Temps (ms) - avec vs sans le cache naif"
    x-axis ["Sans cache (Etape 8)", "Avec cache (echec)"]
    y-axis "Temps (ms)" 0 --> 1600
    bar [709, 1481]
```

| Mesure | Sans cache | Avec cache | Verdict |
|---|---|---|---|
| Temps (Hyperfine) | 709,0 ms ± 27,8 ms | 1 481 ms ± 25 ms | **×2,09 plus lent** |
| Allocations (JFR) | 132 échantillons | 462 échantillons | **+250%** |
| Positions évaluées | 25 319 | 25 319 | inchangé (décisions correctes) |
| Test iterative deepening | passe | échoue (régression collatérale) | signal supplémentaire |

## Cause physique

```mermaid
flowchart TD
    A["Cle = String de 65 caracteres\nreconstruite a CHAQUE appel"] --> B["Cout de construction > cout evite\n(roiEnEchec deja zero-allocation depuis Etape 4)"]
    C["Sans hachage incremental (Zobrist)\npositions quasi toujours uniques"] --> D["Taux de succes du cache ~nul\npaye le cout, jamais le benefice"]
    B --> V["Verdict : REJETE"]
    D --> V

    style V fill:#c62828,color:#fff
```

**Diagnostic JFR** : les nouvelles allocations dominantes sont `byte[]`/`String`/`StringBuilder` (362 échantillons cumulés) — réintroduction directe de l'anti-pattern éliminé à l'Étape 4, plus `HashMap$Node` (le cache lui-même).

## Verdict & piste correcte

**Rejeté**, code retiré (`git checkout`), 51/51 tests repassent au vert. Un vrai cache de positions nécessite un **hachage de Zobrist** (mise à jour incrémentale en O(1) par coup, pas une sérialisation complète recalculée à chaque appel) — piste correcte notée pour la future table de transposition (Étape 11), pas traitée ici.

## Pour aller plus loin

- Détail technique complet, données JFR brutes : [process/10-echec-constructif-cache-roiEnEchec.md](../process/10-echec-constructif-cache-roiEnEchec.md)
- Étape précédente : [09-recherche-budget-temps.md](09-recherche-budget-temps.md)
