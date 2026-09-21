# Étape 04 — Stockage contigu vs dispersé (Séance 2, Partie 1)

**Séance associée :** Séance 2 — Localité Spatiale & Lignes de Cache
**Objectif (cours) :** "Générer un lot de candidats en mémoire et comparer deux structures de données : un tableau contigu linéaire en RAM face à une collection de nœuds dispersés par des pointeurs."

## Contexte

Après la Séance 1 (`Make it work`), on entre dans `Make it fast` : première étape, travailler sur la couche la plus basse — comment les données sont rangées en mémoire — avant même de toucher à l'algorithme. Cf. [comprendre-cpu-caches.md](../compréhension/comprendre-cpu-caches.md) pour la théorie (localité spatiale, lignes de cache de 64 octets).

## Piège Java identifié avant l'implémentation

Un `String[]` en Java **n'est pas un bloc contigu** : chaque `String` est un objet séparé sur le tas, et le tableau ne contient que des *références* (pointeurs) vers ces objets. Pour obtenir un vrai bloc mémoire continu comme l'exige le cours, il faut un tableau de **type primitif** (`char[]`), pas un tableau d'objets.

## Ce qui a été fait

Création de [Seance2LocaliteMemoire.java](../src/main/java/com/hashbreaker/Seance2LocaliteMemoire.java), qui génère le même lot de candidats sous deux formes, en réutilisant l'alphabet et le compteur base-N de `Main` (Séance 1) :

- **Structure A — `genererTableauContigu()`** : un seul `char[]` de taille `nombreCandidats × longueur`. Chaque candidat occupe une tranche fixe `[i*longueur, i*longueur+longueur)`. Vrai bloc mémoire continu, aucune indirection.
- **Structure B — `genererListeDispersee()`** : une liste chaînée de `Node` (`candidat` + référence `suivant`). Chaque `new Node(...)` est alloué séparément sur le tas — aucune garantie de contiguïté entre deux nœuds consécutifs de la liste.

Paramètres de démonstration : 1 000 000 candidats, longueur 4 (reste bien dans l'espace de 14 776 336 combinaisons de `Sh3n`, pas de rebouclage du compteur).

## Tests

[Seance2LocaliteMemoireTest.java](../src/test/java/com/hashbreaker/Seance2LocaliteMemoireTest.java) — 7 tests : taille du buffer contigu, premier candidat (`"aaaa"`), ordre des candidats, longueur de la chaîne, dernier nœud sans successeur, et **cohérence entre les deux structures** (mêmes candidats, même ordre, sur les deux représentations).

**Résultat : 7/7 tests passent (20/20 sur l'ensemble du projet).**

## Ce qui n'est PAS encore fait

Cette étape ne fait que **construire** les deux structures — aucune mesure de performance. La comparaison de vitesse de parcours (lecture séquentielle vs accès dispersés, avec chronométrage) est la **Partie 2** de la Séance 2, pas encore réalisée.

## Fichiers concernés

- [src/main/java/com/hashbreaker/Seance2LocaliteMemoire.java](../src/main/java/com/hashbreaker/Seance2LocaliteMemoire.java)
- [src/test/java/com/hashbreaker/Seance2LocaliteMemoireTest.java](../src/test/java/com/hashbreaker/Seance2LocaliteMemoireTest.java)
