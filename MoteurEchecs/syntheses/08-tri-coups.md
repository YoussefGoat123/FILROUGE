# Synthèse — Tri des coups (MVV-LVA, macro-optimisation)

## En une phrase

Premier levier choisi directement à partir d'une donnée de profiling réelle (Étape 7) plutôt que d'un plan générique : le tri MVV-LVA réduit les positions visitées de 39%, mais le gain net en temps (×1,25) est amorti par le coût du tri lui-même — résultat honnête, pas gonflé.

## Pourquoi ce levier, et pourquoi maintenant

```mermaid
flowchart TD
    P7["Etape 7 : profiling reel\nlegalite+generation = ~90% CPU\n(proportionnel au nb de positions visitees)"] --> Choix["Choix : reduire le NOMBRE\nde positions visitees"]
    Choix --> MO["Tri des coups (MVV-LVA)\nmeilleures coupures alpha-beta plus tot"]
    MO --> Vs["Different de l'Etape 4\n(reduisait le COUT par position,\npas leur nombre)"]

    style P7 fill:#2d6cdf,color:#fff
    style MO fill:#2e7d32,color:#fff
```

Roadmap initiale : Étape 8 = table de transposition. **Remplacé** par le tri des coups, qui attaque plus directement le goulot mesuré (le Hot Path ne montre presque aucune évaluation — <4% — donc pas de raison de cacher des évaluations ; ce qui coûte, c'est le nombre de fois où on vérifie la légalité d'un coup).

## Implémentation

```mermaid
flowchart LR
    Coups["Coups legaux\n(ordre arbitraire)"] --> Score["Score MVV-LVA par coup\n10*valeur(cible) - valeur(attaquant)\n(0 si coup calme)"]
    Score --> Tri["Tri descendant\n(captures fortes en premier)"]
    Tri --> AB["alphabeta() explore\ndans ce nouvel ordre"]

    style Score fill:#f9a825,color:#000
    style AB fill:#2e7d32,color:#fff
```

Appliqué à la fois dans `meilleurCoup()` (racine) et `alphabeta()` (tous les nœuds internes) — pas seulement à la racine, pour que les coupures profitent à tout l'arbre, pas juste au premier niveau.

## Flamegraphs réels — avant / après (captures d'écran)

**Avant (Étape 6, référence — même profondeur 5, mêmes 41 554 positions) :**

![Flamegraph avant tri des coups](../profiling/etape8-avant-flamegraph.png)

**Après (Étape 8 — tri MVV-LVA appliqué, 25 319 positions) :**

![Flamegraph après tri des coups](../profiling/etape8-flamegraph.png)

Reconstruit via un worktree git isolé sur le commit de l'Étape 6 (`cb8650d`, code d'avant, jamais modifié) pour obtenir une comparaison visuelle directe sur le même outil. À l'œil, l'arbre "après" est visiblement plus resserré (moins de branches latérales `co..`) — cohérent avec les −39% de positions visitées. Versions interactives : `profiling/etape8-avant-flamegraph.html` et `profiling/etape8-flamegraph.html`.

⚠️ **Ne pas comparer ces images à celle de l'Étape 7** : celle-ci est à profondeur 5 (comme toutes les mesures Hyperfine du projet), l'Étape 7 est à profondeur 6 (fenêtre plus longue, nécessaire pour un échantillonnage CPU stable) — les deux flamegraphs Étape 7/8 ne sont pas directement superposables, seule la paire avant/après de cette page (même profondeur, même outil) l'est. *Pourquoi : le nombre de positions explose avec la profondeur (×15-16 entre 5 et 6) — une image "plus grosse" à profondeur 6 ne refléterait qu'une profondeur différente, pas une régression.*

## Impact mesuré

```mermaid
xychart-beta
    title "Positions evaluees (profondeur 5)"
    x-axis ["Avant (Etape 6)", "Apres tri (Etape 8)"]
    y-axis "Positions" 0 --> 45000
    bar [41554, 25319]
```

| Mesure | Avant | Après | Facteur |
|---|---|---|---|
| Positions évaluées | 41 554 | 25 319 | **−39 %** |
| Temps (Hyperfine) | 887,1 ms ± 43,0 ms | 709,8 ms ± 23,1 ms | **×1,25** |
| Allocations (JFR) | 120 échantillons | 132 échantillons | +10 % |

## Interprétation honnête : pourquoi le temps ne suit pas les positions au même rythme

```mermaid
flowchart TD
    Q["Pourquoi -39% positions\nmais seulement x1,25 sur le temps ?"] --> R1["Le tri lui-meme a un cout\n(execute a CHAQUE noeud, pas juste la racine)"]
    R1 --> R2["Ce cout mange une partie\ndu gain de la reduction de positions"]
    R2 --> R3["Coherent avec +10% d'allocations JFR\n(lambda captant 'plateau' instancie a chaque tri)"]

    style R1 fill:#f9a825,color:#000
    style R3 fill:#2d6cdf,color:#fff
```

**Ce n'est pas un échec** — c'est un vrai gain macro (réduction du volume de travail, même famille que l'Étape 2), documenté avec son vrai coût plutôt que présenté comme un gain "gratuit" comme l'avait été l'alpha-beta. Une piste micro (éviter la capture de `plateau` dans le comparateur) reste possible plus tard, mais seulement si un futur profiling la justifie — cohérent avec la discipline "macro avant micro" du projet.

**Équivalence préservée** : 47/47 tests passent, y compris les tests d'équivalence Minimax/Alpha-Beta — le tri change l'ordre d'exploration, jamais la décision finale.

## Pour aller plus loin

- Détail technique complet, données brutes : [process/08-tri-coups-move-ordering.md](../process/08-tri-coups-move-ordering.md)
- Étape précédente : [07-profiling-reel.md](07-profiling-reel.md)
