# Synthèse — Struct Padding / Alignement (JOL), un levier manqué rattrapé

## En une phrase

Un levier du cours oublié en cours de route (vérifié sur HashBreaker, jamais refait ici) a été rattrapé — résultat : aucun gain manuel possible (comme sur HashBreaker), mais confirmé par la mesure et non supposé par analogie, avec un piège technique inédit découvert au passage (JOL et les `record` Java).

## État du système à cette étape

```mermaid
flowchart LR
    P["Piece\n2 references enum"] --> J["JOL ClassLayout.parseClass()"]
    C["Coup\n4 int + 1 enum"] --> J
    I["Plateau.InfoAnnulation\n2 references Piece"] --> J
    J --> R["Layout memoire reel\n(offsets, tailles, padding)"]

    style J fill:#2d6cdf,color:#fff
```

## Piège technique découvert : JOL bloque sur les `record`

```
UnsupportedOperationException: can't get field offset on a record class
```

Les `record` Java (utilisés partout dans ce projet : `Piece`, `Coup`, `InfoAnnulation`) sont protégés par la JVM contre l'introspection d'offset via `Unsafe` — une restriction liée aux garanties d'immuabilité des records. HashBreaker n'avait jamais rencontré ce problème car ses structures testées (`CandidateBad`/`CandidateGood`) étaient des `class` classiques. Contournement : `-Djol.magicFieldOffset=true`.

## Résultat mesuré

```mermaid
xychart-beta
    title "Taille totale par structure (octets)"
    x-axis ["Piece", "Coup", "InfoAnnulation"]
    y-axis "Octets" 0 --> 35
    bar [24, 32, 24]
```

| Structure | Taille | Padding perdu |
|---|---|---|
| `Piece` | 24 octets | 4 octets (~16,7%) |
| `Coup` | 32 octets | **0 octet** |
| `InfoAnnulation` | 24 octets | 4 octets (~16,7%) |

## Interprétation

```mermaid
flowchart TD
    Q["4 octets perdus sur Piece --\nun mauvais ordre de champs ?"] --> R["NON : Piece n'a que 2 champs\nde 4B chacun -- aucun reordonnancement\nne change le resultat"]
    R --> C["C'est un cout structurel de la JVM\n(arrondi obligatoire a 8 octets)\npas une inefficacite corrigible"]
    C --> S["Meme conclusion que HashBreaker Seance 3 :\nseule la suppression de l'objet\n(primitifs empaquetes, ex: bitboards)\nelimine ce padding"]

    style R fill:#f9a825,color:#000
    style S fill:#2d6cdf,color:#fff
```

`Coup` (32 octets) tombe exactement sur un multiple de 8 — zéro perte. `Piece` et `InfoAnnulation` (20 octets utiles) doivent être arrondis à 24 — 4 octets incompressibles, indépendants de l'ordre de déclaration.

## Conclusion : rien à corriger manuellement, mais vérifié, pas supposé

**Différence importante avec un simple "copier-coller" de la conclusion HashBreaker** : la composition de champs de nos structures (`int`/enum) est différente de l'exemple HashBreaker (`boolean`/`long`/`byte`/`String`) — le même résultat ("rien à optimiser") aurait pu ne pas se reproduire. Vérifier plutôt que supposer, une fois de plus la règle qui traverse tout ce projet.

## Pour aller plus loin

- Détail technique complet : [process/05-struct-padding-jol.md](../process/05-struct-padding-jol.md)
- Étape précédente : [04-zero-allocation.md](04-zero-allocation.md)
