# Étape 10 — Buffers Fixes / Zéro-Allocation (Séance 3, Partie 3)

**Séance associée :** Séance 3 — Zéro-Allocation & Struct Padding
**Objectif (cours) :** "Remplacer les allocations dynamiques et concaténations par des tableaux d'octets contigus fixes sur la Stack (`[8]byte`) avec mutation directe d'index."

## Ce qui a été fait

Création de [Seance3ZeroAllocation.java](../src/main/java/com/hashbreaker/Seance3ZeroAllocation.java), version corrigée de `Main.craquer()`, ciblant précisément les 4 sources d'allocation identifiées par le diagnostic JFR (Étape 08) :

| Problème diagnostiqué (Partie 1) | Correction appliquée |
|---|---|
| Hash cible comparé en `String` hexadécimale à chaque tentative | `hashCibleHex` décodé en `byte[]` **une seule fois**, avant la boucle (`hexVersOctets()`) |
| `MessageDigest.getInstance()` recréé à chaque tentative | Une seule instance, créée avant la boucle |
| `construireCandidat()` concatène une nouvelle `String` à chaque tentative | `byte[] candidatBuffer` réutilisé, **muté par indice** (`remplirCandidat()`) — jamais de `String` pendant la recherche |
| `sha256()` alloue un nouveau `byte[]` de sortie à chaque appel (`digest.digest(input)`) | `byte[] hashBuffer` réutilisé, rempli via `digest.digest(hashBuffer, 0, hashBuffer.length)` |
| Comparaison via `String.equals()` sur de l'hexadécimal | `MessageDigest.isEqual(hashBuffer, hashCibleOctets)` — comparaison directe d'octets |

La conversion en `String` n'a lieu **qu'une seule fois**, uniquement si le mot de passe est trouvé (donc hors de la boucle chaude).

## Résultat mesuré

| Version | Temps `Sh3n` | Gain |
|---|---|---|
| Naïve (Séance 1, `Main.java`) | 10 536 ms | — (baseline) |
| Zéro-allocation (`Seance3ZeroAllocation.java`) | **2 803 ms** | **~x3,76** |

`z3D` : 594 ms (naïf) → 236 ms (zéro-allocation).

## Tests

[Seance3ZeroAllocationTest.java](../src/test/java/com/hashbreaker/Seance3ZeroAllocationTest.java) — 6 tests, dont un qui vérifie explicitement la **cohérence** entre le hash hexadécimal produit par la version naïve (`Main.sha256()`) et les octets bruts utilisés par la version zéro-allocation, pour garantir que les deux versions ciblent bien le même hash.

**Résultat : 6/6 tests passent (30/30 sur l'ensemble du projet).**

## Ce qui n'est pas encore prouvé formellement

Le gain de temps (x3,76) est mesuré, mais l'objectif strict du cours ("0 B/op et 0 allocs/op") n'a pas encore été **prouvé par profiling** — seulement déduit du code. C'est exactement l'objet de la Partie 4 : relancer JFR sur cette nouvelle version et comparer objectivement le nombre d'allocations avant/après, pas seulement le temps.

## Fichiers concernés

- [src/main/java/com/hashbreaker/Seance3ZeroAllocation.java](../src/main/java/com/hashbreaker/Seance3ZeroAllocation.java)
- [src/test/java/com/hashbreaker/Seance3ZeroAllocationTest.java](../src/test/java/com/hashbreaker/Seance3ZeroAllocationTest.java)
