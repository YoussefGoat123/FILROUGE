# Étape 09 — Compactage de Structure / Padding (Séance 3, Partie 2)

**Séance associée :** Séance 3 — Zéro-Allocation & Struct Padding
**Objectif (cours) :** "Réorganiser les champs de `Candidate` par ordre de taille décroissante pour éliminer 8 octets de padding invisible et passer de 40 octets à 32 octets nets."

## Constat de départ : cette technique ne se transpose pas telle quelle en Java

Le cours illustre le padding en **Go**, où le compilateur respecte scrupuleusement l'ordre de déclaration des champs — d'où l'astuce "trier du plus grand au plus petit" pour éliminer les trous. En **Java, ce n'est pas le compilateur (`javac`) qui décide de l'agencement mémoire d'un objet, c'est la JVM (HotSpot)** — et elle réordonne déjà les champs elle-même, indépendamment de l'ordre choisi dans le code source (cf. [comprendre-memoire-stack-heap.md](../compréhension/comprendre-memoire-stack-heap.md), section 3).

Plutôt que de supposer que cette astuce est inutile en Java, on l'a **vérifié empiriquement** — même méthode que pour tous les résultats surprenants précédents (Séance 2, Partie 3/4).

## Ce qui a été fait

Outil utilisé : **JOL (Java Object Layout)**, la bibliothèque officielle OpenJDK pour inspecter l'agencement mémoire réel d'un objet — l'équivalent Java de l'outil `fieldalignment` mentionné pour Go dans le cours.

```
curl -o .tools/jol-core.jar https://repo1.maven.org/maven2/org/openjdk/jol/jol-core/0.17/jol-core-0.17.jar
```

Deux classes créées dans [Seance3Padding.java](../src/main/java/com/hashbreaker/Seance3Padding.java), avec les mêmes champs que l'exemple du cours (`trouve`, `tentatives`, `charsetId`, `longueur`, `cible`) mais déclarés dans un ordre différent :

- `CandidatDesordonne` : ordre "désordonné" (comme `CandidateBad` du cours)
- `CandidatOrdonne` : champs triés du plus grand au plus petit (comme `CandidateGood` du cours)

`ClassLayout.parseClass(...)` affiche l'agencement mémoire réel décidé par la JVM pour chacune.

## Résultat mesuré

**Les deux classes produisent EXACTEMENT le même agencement mémoire, au octet près :**

```
CandidatDesordonne                          CandidatOrdonne
OFF  SZ   CHAMP                             OFF  SZ   CHAMP
 0    8   (en-tête objet : mark)             0    8   (en-tête objet : mark)
 8    4   (en-tête objet : classe)           8    4   (en-tête objet : classe)
12    4   longueur (int)                    12    4   longueur (int)
16    8   tentatives (long)                 16    8   tentatives (long)
24    1   trouve (boolean)                  24    1   trouve (boolean)
25    1   charsetId (byte)                  25    1   charsetId (byte)
26    2   (padding)                         26    2   (padding)
28    4   cible (String, reference)         28    4   cible (String, reference)

Taille totale : 32 octets                   Taille totale : 32 octets
Perte : 2 octets (6,25%)                    Perte : 2 octets (6,25%)
```

**Aucune différence.** Peu importe l'ordre dans lequel les champs sont déclarés dans le code source, la JVM produit la même disposition finale.

## Interprétation

Ce résultat **confirme** ce qui était anticipé théoriquement dans `comprendre-memoire-stack-heap.md` : la JVM HotSpot réordonne déjà les champs automatiquement pour minimiser le padding. Contrairement à Go, il n'y a donc **rien à optimiser manuellement ici en Java** — l'astuce "trier par taille décroissante" du cours n'a aucune prise sur le résultat.

Deux points à retenir malgré tout :

1. **L'en-tête d'objet (12 octets)** est un coût que Go n'a pas sur ses structs, et qui est incompressible en Java quelle que soit la classe. C'est un désavantage structurel de Java par rapport à Go sur ce plan précis — pertinent pour comprendre pourquoi nos `Node` (Séance 2) coûtent plus cher que prévu.
2. **La référence `String` ne pèse que 4 octets** ici (et non 8), grâce aux *compressed oops* (pointeurs compressés) activés par défaut sur la JVM pour les tas de moins de 32 Go — un détail d'implémentation qu'on n'aurait pas deviné sans l'outil.
3. La perte réelle mesurée (2 octets sur 32 = 6,25%) est bien plus faible que l'exemple Go du cours (10 octets sur 40 = 25%) — précisément parce que la réorganisation automatique de la JVM fait déjà l'essentiel du travail que Go demande de faire à la main.

## Conséquence pour la suite

Le levier "struct padding" du cours **ne s'applique pas** à HashBreaker en Java de la façon dont le cours le présente. Le vrai levier zéro-allocation à appliquer reste celui identifié en Partie 1 (diagnostic JFR) : éliminer les allocations répétées dans `sha256()`, pas réorganiser des champs.

## Fichiers concernés

- [src/main/java/com/hashbreaker/Seance3Padding.java](../src/main/java/com/hashbreaker/Seance3Padding.java)
- [.tools/jol-core.jar](../.tools/jol-core.jar) (outillage local, non versionné — cf. `.gitignore`)
