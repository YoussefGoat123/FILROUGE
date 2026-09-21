# Synthèse — Séance 2 : Architecture Matérielle, CPU & Localité Mémoire

## En une phrase

La disposition physique des données en mémoire peut changer la vitesse d'accès d'un facteur x10 — mais ce gain ne se traduit en performance globale visible **que si l'accès mémoire est le goulot dominant du système**. Sinon, il reste réel mais invisible, masqué par un goulot plus lourd (ici, le calcul CPU).

## Les 6 idées à retenir absolument

### 1. Le CPU ne calcule jamais directement en RAM

L'ALU ne travaille que sur des registres (128 octets, accès instantané). Toute donnée doit d'abord être chargée depuis la RAM vers un registre — et ce chargement coûte cher (~200 cycles).

### 2. Le Mur de la Mémoire (Memory Wall)

Le CPU est environ **200x plus rapide** que la RAM. Sans données en cache, le processeur "gèle" pendant ~200 cycles à chaque accès RAM manqué (CPU Stall). En backend, la performance est très souvent limitée par l'attente mémoire, pas par la puissance de calcul brute.

**Repère mental (si 1 cycle = 1 seconde) :** registres = 1s, cache L1 = 4s, RAM = 3min20, SSD = 8h20.

### 3. Localité spatiale : pourquoi un tableau bat une liste chaînée

Le CPU charge toujours des blocs de **64 octets** d'un coup (ligne de cache), pas un seul octet isolé — pari du matériel que "les voisins seront utilisés bientôt aussi". Un tableau contigu en profite pleinement ; une liste chaînée, dont les nœuds peuvent être dispersés sur le tas, casse ce pari à chaque saut de pointeur.

**Mesuré sur le projet : facteur x8 à x10** entre parcours de tableau contigu (`char[]`) et parcours de liste chaînée (`Node`), pour un contenu strictement identique.

### 4. Piège Java spécifique : `String[]` n'est PAS contigu

Un tableau d'objets (`String[]`) ne contient que des *pointeurs* vers des objets dispersés sur le tas — ce n'est pas un bloc mémoire continu. Seul un tableau de type **primitif** (`char[]`, `int[]`, etc.) garantit une vraie contiguïté en Java. Ce piège est spécifique aux langages avec un tas géré (Java, C#, ...) — il ne se poserait pas de la même façon en C ou Rust.

### 5. La loi d'Amdahl explique pourquoi le gain mémoire a disparu à l'usage réel

Dès qu'on ajoute un calcul CPU coûteux (hachage SHA-256 naïf, ~870-900 ns/candidat) par-dessus le parcours, l'écart de x8-x10 dû à la mémoire (~8-10 ns/candidat) devient **statistiquement invisible** sur le débit global — confirmé avec 10 essais mesurés (écart de -2,5%, dans le bruit). Optimiser une portion qui pèse <1% du temps total ne produit jamais de gain visible, même avec une accélération infinie sur cette portion.

**C'est le résultat le plus important de la séance** : la théorie (localité spatiale) est vraie et prouvée expérimentalement (Partie 2), mais un gain réel sur un sous-système ne se traduit en gain global que s'il touche le goulot *dominant* — d'où l'importance de toujours profiler avant d'optimiser.

### 6. Cache Hit vs Cache Miss, et les 3 impératifs

Un programme "sympathique avec le matériel" maximise les Cache Hits en organisant ses données pour la localité spatiale (tableaux contigus) et temporelle (garder les variables chaudes proches dans les boucles serrées) — sans changer l'algorithme lui-même.

## Résultats mesurés (résumé)

| Mesure | Tableau contigu | Liste chaînée | Écart |
|---|---|---|---|
| Parcours pur (Partie 2) | ~1-2 ms | ~10-12 ms | **x8-x10** |
| Débit de hachage, 3 essais (Partie 3) | ~1,10-1,15 M candidats/s | ~1,08-1,16 M candidats/s | ~1-2% (bruit) |
| Débit de hachage moyen, 10 essais (Partie 4) | 1 141 690 candidats/s | 1 170 930 candidats/s | -2,50% (bruit) |

## Vocabulaire à savoir définir à l'oral

- **Registre / ALU** : mémoire instantanée du CPU, seule mémoire sur laquelle l'ALU peut calculer.
- **Ligne de cache (cache line)** : bloc de 64 octets, unité minimale de transfert RAM ↔ cache.
- **Cache Hit / Miss** : donnée trouvée / absente dans le cache.
- **Mur de la mémoire** : écart structurel de vitesse entre CPU et RAM (~200x).
- **Localité spatiale** : les données proches en mémoire sont chargées ensemble.
- **Localité temporelle** : une donnée récemment utilisée sera probablement réutilisée bientôt.
- **Loi d'Amdahl** : le gain global est borné par le poids de la portion optimisée dans le temps total.

## Piège méthodologique à retenir pour la suite

Un résultat expérimental qui contredit l'intuition (ici : "pas de gain mesurable") n'est pas une erreur à corriger en forçant le résultat attendu — c'est une donnée d'audit à part entière, souvent plus instructive que la confirmation attendue. **Toujours mesurer honnêtement et interpréter, plutôt que supposer.**

## Pour aller plus loin

- Explications détaillées avec analogies et schémas : [comprendre-cpu-caches.md](../compréhension/comprendre-cpu-caches.md)
- Journal technique complet : [process/04](../process/04-seance2-partie1-stockage-contigu-vs-disperse.md) à [07](../process/07-seance2-partie4-validation-sympathie-materielle.md)
- Diagrammes de l'implémentation : [shemas/schemas.md](../shemas/schemas.md)
