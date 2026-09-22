# Étape 11 — Validation 0 allocs/op (Séance 3, Partie 4)

**Séance associée :** Séance 3 — Zéro-Allocation & Struct Padding (dernière partie)
**Objectif (cours) :** "Valider avec `go test -bench . -benchmem` que la boucle critique atteint strictement 0 B/op et 0 allocs/op avec un débit maximal."

## Adaptation Java

Pas d'équivalent direct à `go test -bench . -benchmem` (qui rapporte nativement B/op et allocs/op). La validation a donc été faite en **reproduisant le même protocole JFR** que pour le diagnostic initial (Étape 08), sur la version corrigée (`Seance3ZeroAllocation.java`), pour comparer objectivement les deux enregistrements plutôt que de se fier uniquement au gain de temps.

## Ce qui a été fait

```
java -XX:StartFlightRecording=filename=diagnostic-optimise.jfr,settings=profile -cp out com.hashbreaker.Seance3ZeroAllocation
```

Même analyse que l'Étape 08 (`jfr print --events jdk.YoungGarbageCollection / jdk.GCPhasePause / jdk.ObjectAllocationSample`), appliquée cette fois à la version zéro-allocation.

Preuve conservée : [profiling/seance3-diagnostic-zero-allocation.jfr](../profiling/seance3-diagnostic-zero-allocation.jfr) (227 Ko, contre 2,77 Mo pour l'enregistrement naïf — signal indirect qu'il y a beaucoup moins d'évènements à journaliser).

## Résultat mesuré — comparaison directe avant / après

| Mesure | Naïf (Étape 08) | Zéro-allocation | Amélioration |
|---|---|---|---|
| Cycles de Young GC | 406 | **1** | ÷406 |
| Temps total en pause GC | 448,5 ms | **10,7 ms** | ÷42 |
| Échantillons d'allocation totaux | 7221 | **4** | ÷1805 |
| Échantillons provenant de `com.hashbreaker` | 6610 (~94% pour `sha256()` seul) | **0** | -100% |
| Temps de résolution `Sh3n` | 10 536 ms | 2 803 ms | ×3,76 |

## Interprétation

Les **4 échantillons résiduels** ne proviennent pas de notre code — vérifié en inspectant leurs stack traces complètes :

1. `ConcurrentHashMap$Node[]` — alloué par **JFR lui-même** (`jdk.jfr.internal.StringPool`), pour son propre fonctionnement interne d'enregistrement.
2. `Object[]` — `ArrayList.grow()`, probablement lié à l'initialisation du JVM/JFR.
3. `byte[]` et `String` — construction de chaînes internes à la JVM (une lors du démarrage, une sur le thread `C1 CompilerThread0` du JIT lui-même).

**Aucun de ces 4 échantillons ne pointe vers `com.hashbreaker`.** La boucle critique (`craquer()`) est donc, au sens strict, **conforme à l'objectif "0 allocs/op"** — le seul bruit résiduel vient de l'outil de mesure et de la JVM elle-même, pas du code métier.

Ce résultat valide directement l'hypothèse formulée à l'Étape 08 : en ciblant précisément `sha256()` (identifié comme responsable de ~94% des allocations) plutôt que de deviner, la correction a éliminé la quasi-totalité de la pression GC — pas seulement réduit le temps d'exécution.

## Conclusion de la Séance 3

Les 4 parties sont terminées :

1. **Partie 1** : diagnostic JFR — `sha256()` responsable de ~94% des allocations (pas `construireCandidat()` comme supposé initialement).
2. **Partie 2** : padding vérifié avec JOL — sans effet en Java (la JVM réordonne déjà les champs automatiquement, contrairement à Go).
3. **Partie 3** : correction zéro-allocation ciblée sur `sha256()` — gain de ×3,76 sur `Sh3n`.
4. **Partie 4** : validation JFR — 406 → 1 cycle GC, 0 allocation attribuable au code métier.

## Fichiers concernés

- [src/main/java/com/hashbreaker/Seance3ZeroAllocation.java](../src/main/java/com/hashbreaker/Seance3ZeroAllocation.java)
- [profiling/seance3-diagnostic-zero-allocation.jfr](../profiling/seance3-diagnostic-zero-allocation.jfr)
- Synthèse complète de la séance : [syntheses/03-seance3-memoire-zero-allocation.md](../syntheses/03-seance3-memoire-zero-allocation.md)
