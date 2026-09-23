# Schémas — État actuel du projet (Étape 1 : Naïf, Étape 2 : Alpha-Beta)

Diagrammes Mermaid de l'architecture et du fonctionnement du moteur. Même conventions que HashBreaker (`../../shemas/schemas.md`).

> Ouvrir dans VSCode avec `Ctrl+Shift+V` pour l'aperçu Markdown (extension "Markdown Preview Mermaid Support" si besoin).

---

## 1. Architecture par packages

```mermaid
flowchart TD
    Main["Main\n(orchestration uniquement)"] --> Recherche["recherche.Minimax\n(Minimax naif, sans elagage)"]
    Recherche --> Regles["regles.GenerateurCoups\n(coups pseudo-legaux + legaux)"]
    Recherche --> Evaluation["evaluation.Evaluateur\n(score materiel)"]
    Regles --> Modele["modele\nPlateau, Coup, Piece, Couleur, TypePiece"]
    Evaluation --> Modele

    style Main fill:#2d6cdf,color:#fff
    style Recherche fill:#8e24aa,color:#fff
    style Regles fill:#2e7d32,color:#fff
    style Evaluation fill:#f9a825,color:#000
    style Modele fill:#9e9e9e,color:#fff
```

**Principe** : chaque futur levier d'optimisation touche un seul package. Bitboards → `modele`. Élagage alpha-beta → `recherche`. Table de transposition → périphérie de `recherche`. Tables de position → `evaluation`.

---

## 2. Diagramme de classes — `modele`

```mermaid
classDiagram
    class Couleur {
        <<enum>>
        BLANC
        NOIR
        adverse() Couleur
    }
    class TypePiece {
        <<enum>>
        PION
        CAVALIER
        FOU
        TOUR
        DAME
        ROI
    }
    class Piece {
        <<record>>
        +Couleur couleur
        +TypePiece type
        +lettre() char
    }
    class Coup {
        <<record>>
        +int ligneDepart
        +int colonneDepart
        +int ligneArrivee
        +int colonneArrivee
        +TypePiece promotion
        +enNotation() String
    }
    class Plateau {
        -Piece[][] cases
        -Couleur trait
        +positionDepart() Plateau$
        +pieceEn(ligne, colonne) Piece
        +jouerCoup(Coup) Plateau
        +positionRoi(Couleur) int[]
    }
    Piece --> Couleur
    Piece --> TypePiece
    Coup --> TypePiece
    Plateau --> Piece
    Plateau --> Couleur
    note for Plateau "jouerCoup() retourne un NOUVEAU\nPlateau (copie complete) -- naif,\nvolontairement, comme la concatenation\nde String en Seance 1 de HashBreaker"
```

---

## 3. Flux : de `Main` au meilleur coup

```mermaid
sequenceDiagram
    participant M as Main
    participant M2 as Minimax
    participant G as GenerateurCoups
    participant E as Evaluateur
    participant P as Plateau

    M->>M2: meilleurCoup(plateau, profondeur)
    M2->>G: coupsLegaux(plateau)
    G-->>M2: liste de Coup

    loop pour chaque coup candidat
        M2->>P: jouerCoup(coup)
        P-->>M2: nouveau Plateau
        M2->>M2: minimax(nouveauPlateau, profondeur-1)
        Note over M2: recursif : coupsLegaux -> jouerCoup -> minimax...\njusqu'a profondeur 0 ou mat/pat
        M2->>E: evaluer(plateau) (si profondeur 0)
        E-->>M2: score materiel
    end

    M2-->>M: meilleur Coup trouve
```

---

## 4. Détection légalité d'un coup (échec au roi)

```mermaid
flowchart TD
    A["Coup pseudo-legal genere\n(respecte le deplacement de la piece)"] --> B["Jouer le coup sur une copie\ndu plateau (jouerCoup)"]
    B --> C["Roi du joueur qui vient de jouer\nest-il attaque ? (roiEnEchec)"]
    C -- "Oui" --> D["Coup ILLEGAL -- rejete"]
    C -- "Non" --> E["Coup LEGAL -- conserve"]

    style D fill:#c62828,color:#fff
    style E fill:#2e7d32,color:#fff
```

**Un seul mécanisme** couvre tous les cas : clouages, roi qui se met lui-même en echec, roi qui doit sortir d'échec — pas de logique spéciale par cas.

---

## 5. Couverture des tests

```mermaid
graph LR
    subgraph "PlateauTest - 8 tests"
        T1["placement initial des pieces"]
        T2["jouerCoup deplace + libere la case"]
        T3["immutabilite du plateau original"]
    end
    subgraph "GenerateurCoupsTest - 6 tests"
        T4["perft(1) == 20 depuis le depart"]
        T5["pion : avance simple + double"]
        T6["cavalier saute par-dessus les pions"]
        T7["mat du fou : echec + 0 coup legal"]
    end
    subgraph "EvaluateurTest - 2 tests"
        T8["position de depart : score nul"]
        T9["perte de dame : score deseequilibre"]
    end
    subgraph "MinimaxTest - 3 tests"
        T10["coup choisi fait partie des coups legaux"]
        T11["mat en 1 trouve meme a profondeur 1"]
        T12["positions evaluees croit avec la profondeur"]
    end
    subgraph "MinimaxAlphaBetaTest - 5 tests"
        T13["coup choisi fait partie des coups legaux"]
        T14["mat en 1 trouve meme a profondeur 1"]
        T15["meme coup que Minimax pur (position depart)"]
        T16["meme coup que Minimax pur (apres quelques coups)"]
        T17["visite strictement moins de positions"]
    end
```

**Résultat actuel : 24/24 tests passent.**

---

## 6. Baseline mesurée (Minimax naïf, sans élagage)

```mermaid
xychart-beta
    title "Temps de recherche par profondeur (ms)"
    x-axis ["Profondeur 3\n(9 322 positions)", "Profondeur 4\n(206 603 positions)"]
    y-axis "Temps (ms)" 0 --> 9000
    bar [421, 8465]
```

Débit stable autour de **~22 000 à 24 000 positions/seconde** — c'est cette valeur qui servira de référence pour mesurer les gains des futures micro-optimisations (l'élagage alpha-beta, lui, change l'algorithme : voir section suivante).

---

## 7. Étape 2 : Élagage Alpha-Beta (macro-optimisation)

`MinimaxAlphaBeta` — mêmes structures (`modele`, `regles`, `evaluation`), seule `recherche/Minimax` est étendue avec des bornes `alpha`/`beta` qui coupent les branches mathématiquement inutiles.

```mermaid
flowchart TD
    Check{"alpha >= beta ?"} -- "Oui" --> Coupe["COUPURE : branche entiere ignoree"]
    Check -- "Non" --> Continue["Exploration normale"]

    style Coupe fill:#c62828,color:#fff
    style Continue fill:#2e7d32,color:#fff
```

### Impact mesuré (comparaison directe contre l'Étape 1, même position, même profondeur)

```mermaid
xychart-beta
    title "Positions evaluees : Naif vs Alpha-Beta"
    x-axis ["Profondeur 3", "Profondeur 4"]
    y-axis "Positions evaluees" 0 --> 210000
    bar [9322, 206603]
    bar [585, 1416]
```

| Profondeur | Naïf | Alpha-Beta | Facteur | Coup trouvé |
|---|---|---|---|---|
| 3 | 9 322 pos. / 437 ms | 585 pos. / 22 ms | ÷15,9 / ÷19,9 | `b1c3` (identique) |
| 4 | 206 603 pos. / 8 791 ms | 1 416 pos. / 53 ms | **÷145,9 / ÷165,9** | `b1c3` (identique) |

**Équivalence mathématique validée par tests** : même coup choisi à chaque fois — le gain est "gratuit", zéro perte de qualité de décision. Débloque des profondeurs 5-6 auparavant hors de portée (1,8 s et 29,2 s respectivement). Détails : [process/02-elagage-alpha-beta.md](../process/02-elagage-alpha-beta.md) et [syntheses/02-elagage-alpha-beta.md](../syntheses/02-elagage-alpha-beta.md).

---

## 8. Où on en est

```mermaid
flowchart LR
    A["✅ Architecture par packages\n(modele/regles/evaluation/recherche)"] --> B["✅ Algorithme naif\n(Minimax complet, sans elagage)"]
    B --> C["✅ Validation\n(perft=20, mat du fou, 19/19 tests)"]
    C --> D["✅ Baseline mesuree\n(~22-24K positions/s)"]
    D --> E["✅ Elagage Alpha-Beta (macro)\n÷145,9 positions a profondeur 4"]
    E --> F["⬜ Prochains leviers :\nlocalite memoire (bitboards),\nzero-allocation (make/unmake),\nprofiling, workers,\nI/O & persistance"]

    style A fill:#2e7d32,color:#fff
    style B fill:#2e7d32,color:#fff
    style C fill:#2e7d32,color:#fff
    style D fill:#2e7d32,color:#fff
    style E fill:#2e7d32,color:#fff
    style F fill:#9e9e9e,color:#fff
```
