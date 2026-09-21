# Étape 05 — Parcours linéaire vs aléatoire (Séance 2, Partie 2)

**Séance associée :** Séance 2 — Localité Spatiale & Lignes de Cache
**Objectif (cours) :** "Mesurer le temps d'accès lors d'une lecture séquentielle (profitant des lignes de 64 octets et du prefetcher matériel) face à un parcours à accès dispersés."

## Contexte

La Partie 1 ([étape 04](04-seance2-partie1-stockage-contigu-vs-disperse.md)) a construit deux structures contenant les mêmes candidats. Cette étape les **parcourt** et chronomètre le temps d'accès, sans encore rien calculer de coûteux (pas de hachage à ce stade — ça viendra en Partie 3).

## Ce qui a été fait

Ajout dans [Seance2LocaliteMemoire.java](../src/main/java/com/hashbreaker/Seance2LocaliteMemoire.java) :

- **`parcourirTableau(buffer, nombreCandidats, longueur)`** : boucle séquentielle sur le `char[]` contigu, case par case. Lecture qui profite pleinement des lignes de cache de 64 octets et du prefetcher matériel (cf. [comprendre-cpu-caches.md](../compréhension/comprendre-cpu-caches.md), section 6).
- **`parcourirListe(tete)`** : suit les pointeurs `suivant` un par un (pointer-chasing). Chaque nœud est un `Node` séparé, et chaque `candidat` est lui-même un objet `String` distinct — donc chaque étape du parcours implique plusieurs sauts mémoire potentiellement non contigus.
- Les deux méthodes retournent un **checksum** (somme des codes de caractères) : ça sert à empêcher le JIT d'éliminer la boucle comme "code mort" puisque le résultat n'est jamais utilisé autrement, et ça permet de vérifier que les deux parcours lisent bien les mêmes données.
- Dans `main()` : 1 passage d'échauffement (non chronométré, pour laisser le JIT compiler les boucles à chaud) puis **5 mesures chronométrées** par structure avec `System.nanoTime()`.

## Résultats mesurés

| Structure | Essais (ms) | Ordre de grandeur |
|---|---|---|
| A — tableau contigu | 4, 1, 1, 2, 1 | ~1-2 ms |
| B — liste chaînée | 12, 11, 10, 11, 10 | ~10-12 ms |

**Checksum identique dans les deux cas** (`360018520`) : même contenu parcouru, seule la disposition mémoire change. Le tableau contigu est **environ 8 à 10x plus rapide** à parcourir que la liste chaînée, pour un accès strictement équivalent en nombre d'éléments.

## Analyse — pourquoi un tel écart en Java spécifiquement

Une inquiétude légitime avant de lancer la mesure : en Java, les objets `Node` sont alloués les uns après les autres dans une boucle serrée, et la JVM alloue en général de façon séquentielle (bump-pointer allocation) dans l'espace mémoire "Eden" — on pourrait donc s'attendre à ce qu'ils finissent malgré tout assez proches en mémoire, réduisant l'écart attendu.

Ce n'est pas ce qu'on observe, et voici pourquoi : contrairement à un tableau contigu de type primitif, chaque nœud de la liste implique **plusieurs indirections** avant d'atteindre la donnée utile :

```
Node (objet sur le tas) --> reference "candidat" --> objet String (ailleurs sur le tas) --> tableau interne de caracteres
```

Le tableau contigu, lui, n'a **aucune indirection** : lire le caractère `i` est un accès mémoire direct. Chaque étape du parcours de la liste ajoute donc plusieurs sauts de pointeurs supplémentaires (en plus de l'en-tête d'objet de 16 octets que la JVM ajoute à chaque `Node` et à chaque `String`), ce qui suffit à expliquer l'écart mesuré, même sans dispersion physique extrême sur le tas.

## Fichiers concernés

- [src/main/java/com/hashbreaker/Seance2LocaliteMemoire.java](../src/main/java/com/hashbreaker/Seance2LocaliteMemoire.java)
- [src/test/java/com/hashbreaker/Seance2LocaliteMemoireTest.java](../src/test/java/com/hashbreaker/Seance2LocaliteMemoireTest.java) (2 tests ajoutés : correction du checksum, cohérence entre les deux parcours)
