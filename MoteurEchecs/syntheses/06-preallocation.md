# Synthèse — Pré-allocation de capacité (listes de `Coup`)

## En une phrase

Le deuxième levier oublié du cours (J2_PM, "réallocations de slices") a été rattrapé — gain réel mais modeste (~×1,11), assumé honnêtement comme un résultat d'un ordre de grandeur différent des corrections précédentes, sans forcer une conclusion plus impressionnante qu'elle ne l'est.

## État du système à cette étape

```mermaid
flowchart LR
    subgraph Avant["Avant"]
    A1["new ArrayList<>()\ncapacite 0 -> grandit 10,15,22,33..."]
    A2["2-3 reallocations internes\npar appel (20-40 coups typiques)"]
    end
    subgraph Apres["Apres"]
    B1["coupsLegaux: capacite EXACTE\n(pseudoLegaux.size())"]
    B2["coupsPseudoLegaux: capacite ESTIMEE\n(48, documentee comme telle)"]
    end

    style Avant fill:#c62828,color:#fff
    style Apres fill:#2e7d32,color:#fff
```

## Application littérale de l'exemple du cours (J2_PM)

```mermaid
flowchart TD
    Cours["Cours : make([]Record, 0, len(source))\nvs make([]Record, 0)"] --> Ici["Ici : new ArrayList<>(pseudoLegaux.size())\nvs new ArrayList<>()"]
    Ici --> Nuance["Nuance : coupsLegaux a une taille EXACTE connue,\ncoupsPseudoLegaux seulement une ESTIMATION\n-- distinction assumee et documentee"]

    style Cours fill:#2d6cdf,color:#fff
    style Nuance fill:#f9a825,color:#000
```

## Impact mesuré

```mermaid
xychart-beta
    title "Temps moyen (ms, Hyperfine 5 essais)"
    x-axis ["Avant (fin Etape 4)", "Apres (pre-allocation)"]
    y-axis "Temps (ms)" 0 --> 550
    bar [475.3, 426.3]
```

| Mesure | Avant | Après | Facteur |
|---|---|---|---|
| Temps (Hyperfine) | 475,3 ms ± 37,8 ms | 426,3 ms ± 35,8 ms | **×1,11** |
| Échantillons d'allocation (JFR) | 120 | 120 | inchangé |

## Interprétation honnête : un gain réel, mais modeste — et assumé comme tel

```mermaid
flowchart TD
    Q["Pourquoi le gain est plus petit\nque caseAttaquee() (x4,2) ?"] --> R1["caseAttaquee : eliminait un VRAI travail\ninutile (generer toute une liste pour 1 case)"]
    Q --> R2["Pre-allocation : optimise un DETAIL\nd'implementation d'ArrayList\n(nombre de petits tableaux internes recopies)"]
    R2 --> C["Les echantillons JFR ne bougent pas :\nl'echantillonneur suit le DEBIT D'OCTETS,\npas le nombre brut de petits objets evites"]
    C --> Concl["Intervalles ±1 sigma qui se chevauchent legerement --\ngain honnete, pas surevalue"]

    style R1 fill:#2e7d32,color:#fff
    style R2 fill:#f9a825,color:#000
    style Concl fill:#2d6cdf,color:#fff
```

**Ce résultat n'est pas "décevant"** — c'est la mesure honnête d'un levier dont l'ampleur attendue était d'emblée plus faible que la correction de l'Étape 4. Le documenter tel quel (sans gonfler artificiellement son importance) fait partie de la même discipline que les résultats "aucun gain" de HashBreaker Séance 2 Partie 3-4.

## Pour aller plus loin

- Détail technique complet : [process/06-preallocation-listes-coup.md](../process/06-preallocation-listes-coup.md)
- Étape précédente : [05-struct-padding.md](05-struct-padding.md)
