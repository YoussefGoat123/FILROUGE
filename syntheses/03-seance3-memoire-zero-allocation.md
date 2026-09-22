# Synthèse — Séance 3 : Modèle Mémoire, Zéro-Allocation & Struct Padding

## En une phrase

Un diagnostic mesuré (pas deviné) a révélé que le vrai coupable des allocations n'était pas celui qu'on soupçonnait — et corriger précisément ce point-là a fait chuter les cycles GC de 406 à 1, avec un gain de ×3,76 sur le temps d'exécution.

## Les 5 idées à retenir absolument

### 1. Pile (Stack) vs Tas (Heap) : deux coûts radicalement différents

La pile est quasi gratuite (déplacement de pointeur, O(1), libération instantanée, zéro GC). Le tas coûte cher : recherche de bloc libre, synchronisation, et surtout **pression sur le Garbage Collector** — chaque objet doit être tracé, scanné, libéré. Moins un objet "s'échappe" sur le tas, plus la latence reste stable.

### 2. En Java, l'échappement se décide au runtime, pas à la compilation

Contrairement à Go (`go build -gcflags="-m"`, décision statique et prévisible du compilateur), Java n'offre pas de contrôle direct : c'est le JIT qui décide dynamiquement, au runtime, si une allocation peut être éliminée. **Conséquence pratique** : on ne peut pas compter dessus — il faut coder directement en style zéro-allocation (réutiliser les buffers) plutôt qu'espérer que la JVM l'élimine toute seule.

### 3. Le struct padding à la Go ne s'applique pas en Java (vérifié avec JOL)

Testé empiriquement : deux classes identiques, seul l'ordre de déclaration des champs change → **layout mémoire strictement identique** (32 octets, même agencement). La JVM HotSpot réordonne déjà les champs automatiquement. L'astuce "trier du plus grand au plus petit" du cours (utile en Go) n'a **aucun effet** en Java. Découverte au passage : l'en-tête d'objet Java (12 octets, incompressible) est un coût que Go n'a pas.

### 4. Toujours profiler avant de corriger — l'intuition s'est trompée

Le diagnostic JFR a révélé que **`sha256()` (conversion hexadécimale) représentait ~94% des allocations**, alors qu'on soupçonnait `construireCandidat()` (qui ne pesait en réalité que ~4,4%). Sans le profiling, on aurait probablement optimisé la mauvaise fonction en premier.

### 5. Le résultat de la correction est spectaculaire et mesuré, pas supposé

| Mesure | Avant | Après | Facteur |
|---|---|---|---|
| Cycles Young GC | 406 | 1 | ÷406 |
| Pauses GC (temps total) | 448,5 ms | 10,7 ms | ÷42 |
| Allocations attribuables au code | 6610 échantillons | 0 | -100% |
| Temps `Sh3n` | 10 536 ms | 2 803 ms | ×3,76 |

La technique appliquée : décoder le hash cible en `byte[]` une seule fois (pas de comparaison hex par tentative), réutiliser une seule instance de `MessageDigest`, construire le candidat dans un `byte[]` muté par indice (jamais de `String`), et écrire le résultat du hash dans un buffer réutilisé plutôt que d'en allouer un nouveau à chaque appel.

## Vocabulaire à savoir définir à l'oral

- **Pile (Stack) / Tas (Heap)** : deux régions mémoire aux coûts radicalement différents.
- **Escape Analysis** : décision de placer une variable sur la pile ou le tas — statique en Go, dynamique (JIT) en Java.
- **Struct padding / field alignment** : octets de remplissage insérés pour respecter les contraintes d'alignement matériel.
- **False Sharing** : ralentissement causé par deux cœurs qui écrivent sur la même ligne de cache sans le savoir.
- **0 B/op, 0 allocs/op** : objectif de benchmark prouvant qu'une boucle critique n'alloue plus rien sur le tas.
- **JFR (Java Flight Recorder)** : profileur intégré au JDK, utilisé ici comme équivalent des Flamegraphs/pprof.

## Piège méthodologique confirmé une nouvelme fois

Comme en Séance 2 (loi d'Amdahl), l'intuition non vérifiée s'est trompée ici aussi (le coupable supposé n'était pas le vrai coupable). **Deux séances de suite, la mesure a contredit l'intuition de départ** — la meilleure preuve possible que la règle scientifique du cours ("toujours profiler avant d'optimiser") n'est pas un simple conseil théorique, mais une nécessité pratique démontrée sur notre propre code.

## Pour aller plus loin

- Explications détaillées avec analogies et schémas : [comprendre-memoire-stack-heap.md](../compréhension/comprendre-memoire-stack-heap.md)
- Journal technique complet : [process/08](../process/08-seance3-partie1-diagnostic-escape-analysis.md) à [11](../process/11-seance3-partie4-validation-zero-allocation.md)
- Diagrammes de l'implémentation : [shemas/schemas.md](../shemas/schemas.md)
