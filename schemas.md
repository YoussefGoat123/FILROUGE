# Schémas — État actuel du projet (Séance 1)

Diagrammes Mermaid de ce qui a été produit jusqu'ici : structure du projet, code de `Main.java`, flux d'exécution et couverture des tests.

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
    Root --> Comprendre["comprendre-hashbreaker.md<br/>(explications pédagogiques)"]
    Root --> Schemas["schemas.md<br/>(ce fichier)"]
    Root --> Src["src/"]
    Src --> Main["main/java/com/hashbreaker/<br/>Main.java"]
    Src --> Test["test/java/com/hashbreaker/<br/>MainTest.java"]

    style Main fill:#2d6cdf,color:#fff
    style Test fill:#2e7d32,color:#fff
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

```mermaid
flowchart TD
    Init(["Initialisation :<br/>hashCible, longueur = 3<br/>buffer = [0,0,0]"]) --> Boucle{"trouve == false ?"}
    Boucle -- "oui" --> Construire["construireCandidat(buffer)<br/>→ ex: 'aaa'"]
    Construire --> Hash["sha256(candidat)<br/>→ hash hexadécimal"]
    Hash --> Compare{"hash == hashCible ?"}
    Compare -- "non" --> Increment["incrementer(buffer)<br/>(retenue droite → gauche)"]
    Increment --> Boucle
    Compare -- "oui" --> Fin(["trouve = true<br/>Afficher candidat + temps écoulé"])
    Boucle -- "non" --> Fin
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

## 5. Couverture des tests (`MainTest.java`)

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
```

**Résultat actuel : 13/13 tests passent.**

---

## 6. Où on en est dans le TP

```mermaid
flowchart LR
    A["✅ Init projet Java\n(pom.xml, JDK 21)"] --> B["✅ Algo naïf\n(compteur base-N + SHA-256)"]
    B --> C["✅ Résolution z3D\n(550 ms)"]
    C --> D["⬜ Résolution Sh3n\n(baseline chronométrée)"]
    D --> E["⬜ Séances suivantes :\nalignement mémoire,\nzéro-allocation, profiling,\nworkers, gRPC, SQL"]

    style A fill:#2e7d32,color:#fff
    style B fill:#2e7d32,color:#fff
    style C fill:#2e7d32,color:#fff
    style D fill:#f9a825,color:#000
    style E fill:#9e9e9e,color:#fff
```
