# Schémas — État actuel du projet (Séance 1 + Séance 2 Partie 1)

Diagrammes Mermaid de ce qui a été produit jusqu'ici : structure du projet, code de `Main.java` et `Seance2LocaliteMemoire.java`, flux d'exécution et couverture des tests.

> Journal détaillé étape par étape : voir [process/](../process/README.md).

> Pour voir les diagrammes : ouvre ce fichier dans VSCode et fais `Ctrl+Shift+V` (aperçu Markdown). Si les schémas ne s'affichent pas, installe l'extension **"Markdown Preview Mermaid Support"**.

---

## 1. Structure du projet

```mermaid
graph TD
    Root["FILROUGE/"]
    Root --> Pom["pom.xml<br/>(Maven, Java 21, JUnit 5)"]
    Root --> Gitignore[".gitignore"]
    Root --> Readme["README.md<br/>(bases du cours)"]
    Root --> Sujets["sujets.md<br/>(idées projet libre)"]
    Root --> Compr["compréhension/<br/>comprendre-hashbreaker.md<br/>comprendre-cpu-caches.md"]
    Root --> Shemas["shemas/<br/>schemas.md (ce fichier)"]
    Root --> Process["process/<br/>journal étape par étape"]
    Root --> Src["src/"]
    Src --> Main["main/.../Main.java<br/>(Séance 1)"]
    Src --> Seance2["main/.../Seance2LocaliteMemoire.java<br/>(Séance 2 - Partie 1)"]
    Src --> TestMain["test/.../MainTest.java"]
    Src --> TestS2["test/.../Seance2LocaliteMemoireTest.java"]

    style Main fill:#2d6cdf,color:#fff
    style Seance2 fill:#2d6cdf,color:#fff
    style TestMain fill:#2e7d32,color:#fff
    style TestS2 fill:#2e7d32,color:#fff
    style Process fill:#8e24aa,color:#fff
```

---

## 2. Diagramme de classe — `Main`

```mermaid
classDiagram
    class Main {
        -String alphabet$
        +main(args: String[]) void
        +construireCandidat(buffer: int[]) String$
        +incrementer(buffer: int[]) void$
        +sha256(texte: String) String$
    }
    note for Main "$ = méthode statique\nToutes les méthodes utilitaires\nsont statiques et testables\nindépendamment de main()"
```

| Méthode | Rôle |
|---|---|
| `construireCandidat(buffer)` | Transforme le buffer d'indices en chaîne (ex: `[25,55,29]` → `"z3D"`) |
| `incrementer(buffer)` | Fait avancer le buffer d'un cran (compteur base-N avec retenue) |
| `sha256(texte)` | Calcule le hash SHA-256 et le renvoie en hexadécimal |

---

## 3. Flux d'exécution de `main()`

`main()` appelle maintenant `craquer(nomCible, hashCible, longueur)` une fois par cible, à la suite.

```mermaid
flowchart TD
    MainStart(["main()"]) --> Craquer1["craquer('z3D', hash, 3)"]
    Craquer1 --> Craquer2["craquer('Sh3n', hash, 4)"]

    subgraph "craquer(nomCible, hashCible, longueur)"
    Init(["buffer = [0,0,...,0]"]) --> Boucle{"trouve == false ?"}
    Boucle -- "oui" --> Construire["construireCandidat(buffer)<br/>→ ex: 'aaa'"]
    Construire --> Hash["sha256(candidat)<br/>→ hash hexadécimal"]
    Hash --> Compare{"hash == hashCible ?"}
    Compare -- "non" --> Increment["incrementer(buffer)<br/>(retenue droite → gauche)"]
    Increment --> Boucle
    Compare -- "oui" --> Fin(["Afficher candidat + temps écoulé"])
    end
```

---

## 4. Séquence d'une itération (zoom sur un tour de boucle)

```mermaid
sequenceDiagram
    participant M as main()
    participant C as construireCandidat()
    participant H as sha256()
    participant I as incrementer()

    M->>C: buffer = [0,0,1]
    C-->>M: "aab"
    M->>H: "aab"
    H-->>M: "7902699b..." (hash)
    M->>M: hash == hashCible ?
    alt hash différent
        M->>I: buffer
        I-->>M: buffer mis à jour (+1 avec retenue)
    else hash trouvé
        M->>M: afficher résultat + temps
    end
```

---

## 5. Séance 2 — Partie 1 : Stockage contigu vs dispersé

### Diagramme de classe — `Seance2LocaliteMemoire`

```mermaid
classDiagram
    class Seance2LocaliteMemoire {
        +main(args: String[]) void
        +genererTableauContigu(nombreCandidats: int, longueur: int) char[]$
        +extraireCandidat(buffer: char[], index: int, longueur: int) String$
        +genererListeDispersee(nombreCandidats: int, longueur: int) Node$
    }
    class Node {
        +String candidat
        +Node suivant
    }
    Seance2LocaliteMemoire ..> Node : construit une chaîne de
    Seance2LocaliteMemoire ..> Main : réutilise alphabet,\nincrementer(), construireCandidat()
```

### Structure A vs Structure B — ce qui change en mémoire

```mermaid
flowchart TD
    subgraph A["Structure A — char[] contigu"]
    direction LR
    A0["'a'"] --- A1["'a'"] --- A2["'a'"] --- A3["'b'"] --- A4["'a'"] --- A5["'a'"] --- A6["'a'"] --- A7["'c'"]
    end

    subgraph B["Structure B — liste chaînée de Node"]
    direction LR
    B0["Node『aaab』\n@0x7F3A"] -.->|suivant| B1["Node『aaac』\n@0x2C91"]
    B1 -.->|suivant| B2["Node『aaad』\n@0x9E10"]
    end

    A -->|"1 bloc mémoire continu\n→ tient dans peu de lignes de 64 octets"| Note1["Localité spatiale ✅"]
    B -->|"chaque Node alloué séparément\n→ adresses dispersées sur le tas"| Note2["Localité spatiale ❌"]

    style A0 fill:#2e7d32,color:#fff
    style A1 fill:#2e7d32,color:#fff
    style A2 fill:#2e7d32,color:#fff
    style A3 fill:#2e7d32,color:#fff
    style A4 fill:#2e7d32,color:#fff
    style A5 fill:#2e7d32,color:#fff
    style A6 fill:#2e7d32,color:#fff
    style A7 fill:#2e7d32,color:#fff
    style B0 fill:#c62828,color:#fff
    style B1 fill:#c62828,color:#fff
    style B2 fill:#c62828,color:#fff
```

> Rappel important (cf. [comprendre-cpu-caches.md](../compréhension/comprendre-cpu-caches.md)) : un `String[]` Java n'aurait **pas** donné un vrai bloc contigu — seul un tableau de type primitif (`char[]`) garantit l'absence d'indirection. C'est pour ça que la Structure A utilise `char[]` et pas `String[]`.

Les deux structures contiennent **exactement les mêmes candidats, dans le même ordre** (vérifié par les tests) — seule leur disposition physique en mémoire diffère. C'est cette différence, et uniquement elle, que la Partie 2 va mesurer.

---

## 6. Couverture des tests (`MainTest.java` + `Seance2LocaliteMemoireTest.java`)

```mermaid
graph LR
    subgraph "sha256() - 5 tests"
        T1["chaîne vide"]
        T2["'abc' (vecteur connu)"]
        T3["'z3D' == hash cible"]
        T4["déterminisme"]
        T5["toujours 64 caractères"]
    end
    subgraph "construireCandidat() - 3 tests"
        T6["buffer [0,0,0] → 'aaa'"]
        T7["indices de z,3,D → 'z3D'"]
        T8["buffer de taille 1"]
    end
    subgraph "incrementer() - 5 tests"
        T9["incrément simple, sans retenue"]
        T10["une retenue ('aa9' → 'aba')"]
        T11["retenue en chaîne ('a99' → 'baa')"]
        T12["débordement complet (rebouclage)"]
        T13["plusieurs appels dans l'ordre"]
    end
    subgraph "Seance2LocaliteMemoireTest - 7 tests"
        T14["tableau : taille correcte"]
        T15["tableau : premier candidat 'aaaa'"]
        T16["tableau : ordre du compteur base-N"]
        T17["liste : premier nœud 'aaaa'"]
        T18["liste : longueur de chaîne correcte"]
        T19["liste : dernier nœud sans suivant"]
        T20["les deux structures contiennent\nles mêmes candidats, même ordre"]
    end
```

**Résultat actuel : 20/20 tests passent.**

---

## 7. Baseline mesurée — z3D vs Sh3n

```mermaid
xychart-beta
    title "Temps de résolution (version naïve)"
    x-axis ["z3D (238 328 candidats)", "Sh3n (14 776 336 candidats)"]
    y-axis "Temps (ms)" 0 --> 11000
    bar [249, 10536]
```

L'espace de recherche est ~62x plus grand pour `Sh3n` (un caractère de plus) et le temps suit à peu près la même échelle : c'est cohérent avec un algorithme en **O(62^longueur)**, qui teste les candidats un par un sans aucune astuce. C'est exactement cette explosion combinatoire que les prochaines séances vont attaquer (parallélisme, élagage, etc. — pas en changeant l'algo de force brute lui-même mais en réduisant le coût de chaque tentative et en répartissant le travail).

---

## 8. Où on en est dans le TP

```mermaid
flowchart LR
    A["✅ Init projet Java\n(pom.xml, JDK 21)"] --> B["✅ Algo naïf\n(compteur base-N + SHA-256)"]
    B --> C["✅ Résolution z3D + Sh3n\n(baseline: 249 ms / 10 536 ms)"]
    C --> D["✅ Séance 2 Partie 1\nStockage contigu vs dispersé"]
    D --> E["⬜ Séance 2 Partie 2\nParcours linéaire vs aléatoire\n(mesure du goulot mémoire)"]
    E --> F["⬜ Séance 2 Partie 3-4\nObservation + validation\nsympathie matérielle"]
    F --> G["⬜ Séances suivantes :\nzéro-allocation, profiling,\nworkers, gRPC, SQL"]

    style A fill:#2e7d32,color:#fff
    style B fill:#2e7d32,color:#fff
    style C fill:#2e7d32,color:#fff
    style D fill:#2e7d32,color:#fff
    style E fill:#f9a825,color:#000
    style F fill:#9e9e9e,color:#fff
    style G fill:#9e9e9e,color:#fff
```
