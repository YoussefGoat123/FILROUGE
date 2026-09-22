# Étape 08 — Diagnostic Escape Analysis (Séance 3, Partie 1)

**Séance associée :** Séance 3 — Zéro-Allocation & Struct Padding
**Objectif (cours) :** "Inspecter les choix du compilateur avec `go build -gcflags="-m"`. Constater que les conversions en string et concaténations allouent des millions d'objets sur le Tas."

## Adaptation Java

Il n'existe pas d'équivalent direct de `go build -gcflags="-m"` en Java (l'escape analysis Java se fait au runtime par le JIT, pas statiquement à la compilation — cf. [comprendre-memoire-stack-heap.md](../compréhension/comprendre-memoire-stack-heap.md)). Le diagnostic a donc été fait par **profiling réel** avec **JFR (Java Flight Recorder)**, intégré au JDK — l'équivalent Java le plus proche des Flamegraphs/pprof demandés par le barème du projet noté.

## Ce qui a été fait

Compilation et exécution de `Main.java` (version naïve, inchangée depuis la Séance 1) sous enregistrement JFR :

```
java -XX:StartFlightRecording=filename=diagnostic.jfr,settings=profile -cp out com.hashbreaker.Main
```

Analyse de l'enregistrement avec `jfr summary` et `jfr print --events jdk.ObjectAllocationSample --stack-depth 6/8`.

Fichier de preuve conservé : [profiling/seance3-diagnostic-naif.jfr](../profiling/seance3-diagnostic-naif.jfr).

## Résultats mesurés

### Pression GC globale

| Mesure | Valeur |
|---|---|
| Durée totale de l'exécution (z3D + Sh3n) | ~24,3 s |
| Nombre de cycles de Young GC déclenchés | **406** |
| Temps total passé en pause GC | 448,5 ms (~1,8% du temps total) |

Pour un programme qui ne fait "que" chercher un mot de passe par force brute, 406 déclenchements du ramasse-miettes est un signal fort : quelque chose alloue énormément.

### Répartition des allocations par type d'objet

| Type alloué | Échantillons (`ObjectAllocationSample`) | Part |
|---|---|---|
| `byte[]` | 4397 | ~61% |
| `java.lang.String` | 2617 | ~36% |
| `sun.security.provider.SHA2$SHA256` | 91 | ~1% |
| `int[]` | 79 | ~1% |
| `sun.security.jca.GetInstance$Instance` | 33 | <1% |

`byte[]` et `String` représentent à eux seuls **~97%** des allocations échantillonnées.

### Localisation précise (stack traces) — le vrai coupable n'est pas celui qu'on pensait

| Ligne de code | Échantillons | Ligne concernée |
|---|---|---|
| `Main.sha256()` ligne 99 | 3853 | `hex = hex + morceau;` |
| `Main.sha256()` ligne 95 | 2349 | `String morceau = Integer.toHexString(0xff & b);` |
| `Main.sha256()` ligne 89 | 129 | `MessageDigest.getInstance("SHA-256")` — **recréé à chaque appel !** |
| `Main.sha256()` ligne 90 | 118 | `digest.digest(texte.getBytes())` — `getBytes()` alloue aussi |
| `Main.craquer()` ligne 40 / `construireCandidat()` ligne 64 | 291 | `candidat = candidat + alphabet.charAt(...)` |

## Interprétation

**Découverte importante, contre-intuitive par rapport à l'intuition de départ :** on pensait (Séance 1, section "Le piège de la version naïve") que le principal coupable serait `construireCandidat()`, qui concatène caractère par caractère pour former le mot candidat. En réalité, **`construireCandidat()` ne représente que ~4,4%** des allocations échantillonnées.

Le vrai goulot est `sha256()` : sa **conversion hexadécimale** (`hex = hex + morceau`, ligne 99) représente à elle seule plus de la moitié des échantillons, et l'ensemble de `sha256()` (lignes 89, 90, 95, 99) totalise **~94%** des allocations attribuées à notre code.

**Bonus découvert au passage** : `MessageDigest.getInstance("SHA-256")` est **recréé à chaque tentative** (ligne 89), alors qu'une seule instance suffirait (réinitialisée via `digest.reset()` entre deux calculs). C'est une inefficacité supplémentaire, indépendante du problème d'allocation de `String`, qu'on corrigera en même temps.

## Conséquence pour la Partie 3 (Buffers Fixes)

Ce diagnostic change la priorité : la correction "zéro-allocation" doit cibler **en premier `sha256()`** (le vrai goulot, ~94% des allocations), pas seulement `construireCandidat()` comme on l'aurait supposé sans mesurer. C'est une nouvelle illustration de la règle scientifique du cours : *toujours profiler avant d'optimiser, ne jamais deviner*.

## Fichiers concernés

- [src/main/java/com/hashbreaker/Main.java](../src/main/java/com/hashbreaker/Main.java) (code diagnostiqué, pas encore modifié)
- [profiling/seance3-diagnostic-naif.jfr](../profiling/seance3-diagnostic-naif.jfr) (preuve — enregistrement JFR brut)
