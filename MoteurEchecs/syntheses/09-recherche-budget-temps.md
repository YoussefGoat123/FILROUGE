# Synthèse — Recherche à budget de temps (Iterative Deepening)

## En une phrase

Premier levier macro qui n'accélère rien — il rajoute même du travail (profondeurs refaites) — mais donne au moteur une capacité qu'il n'avait pas : répondre sous une vraie contrainte de temps plutôt qu'à une profondeur fixe arbitraire, avec la garantie de toujours renvoyer un coup valide.

## État du système à cette étape

```mermaid
flowchart LR
    Start["Position de depart\n+ budget de temps"] --> P1["Profondeur 1\n(rapide)"]
    P1 --> P2["Profondeur 2"]
    P2 --> P3["Profondeur 3..."]
    P3 --> PN["Profondeur N\n(interrompue si budget epuise)"]
    PN --> Resultat["Retourne le coup\nde la DERNIERE profondeur\nCOMPLETE"]

    style Start fill:#2d6cdf,color:#fff
    style Resultat fill:#2e7d32,color:#fff
```

## Le point technique le plus important : ne jamais corrompre le Plateau muté en place

```mermaid
flowchart TD
    A["jouer(coup) -- mute le Plateau"] --> B["alphabeta() recursif"]
    B -- "Interruption levee ICI" --> C{"annuler() protege\npar try/finally ?"}
    C -- "Non" --> D["Plateau reste corrompu\n(coup jamais annule)"]
    C -- "Oui" --> E["annuler() s'execute quand meme\nPlateau revient a l'etat initial"]

    style D fill:#c62828,color:#fff
    style E fill:#2e7d32,color:#fff
```

Trouvé et corrigé avant tout bench : sans `try/finally` autour de chaque `annuler()`, une interruption en pleine récursion laisse le plateau à moitié joué. Testé explicitement (`budgetTemps_interruption_neCorrompPasLePlateau`).

## Impact mesuré (3 runs, position de départ)

```mermaid
xychart-beta
    title "Profondeur atteinte selon le budget de temps"
    x-axis ["200ms", "500ms", "1000ms", "2000ms", "5000ms"]
    y-axis "Profondeur" 0 --> 7
    bar [4, 5, 5, 5, 6]
```

| Budget | Profondeur | Temps réel | Coup |
|---|---|---|---|
| 200 ms | 4 | ~211-216 ms | `b1c3` |
| 500 ms | 5 | ~502-505 ms | `b2b3` |
| 1000 ms | 5 | ~1003-1006 ms | `b2b3` |
| 2000 ms | 5 | ~2002-2008 ms | `b2b3` |
| 5000 ms | 6 | ~5000-5008 ms | `b1c3` |

## Interprétation honnête : le vrai coût de ce levier

```mermaid
flowchart TD
    Q["Pourquoi la profondeur reste a 5\nde 500ms a 2000ms (x4 de budget) ?"] --> R["Profondeur 6 commencee\nmais jamais terminee dans ce budget"]
    R --> C["Confirme par les positions cumulees :\n48927 (500ms) -> 242463 (2000ms)\nce travail de profondeur 6 est JETE"]
    C --> Concl["Compromis assume : une part significative\ndu temps sert a explorer une profondeur\nqui sera finalement abandonnee"]

    style R fill:#f9a825,color:#000
    style Concl fill:#2d6cdf,color:#fff
```

**Pas un levier "gratuit"** comme l'alpha-beta (Étape 2, ÷145 positions sans aucune perte) — ici on accepte de perdre du travail sur les profondeurs interrompues, en échange de la garantie de toujours avoir un coup valide dans un budget donné. Documenté tel quel, pas présenté comme un gain de vitesse qu'il n'est pas.

**3 bugs trouvés et corrigés avant que les mesures soient fiables** (deadline par défaut à 0, deadline périmée après un budget de temps, corruption du plateau sans `try/finally`) — détail complet dans `process/09-recherche-budget-temps.md`. 51/51 tests passent.

## Pour aller plus loin

- Détail technique complet : [process/09-recherche-budget-temps.md](../process/09-recherche-budget-temps.md)
- Étape précédente : [08-tri-coups.md](08-tri-coups.md)
