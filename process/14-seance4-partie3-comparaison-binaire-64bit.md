# Étape 14 — Comparaison Binaire 64-bit (Séance 4, Partie 3)

**Séance associée :** Séance 4 — Métrologie, Micro-benchmarks & Profiling Applicatif
**Objectif (cours) :** "Supprimer le goulot : décoder le hash cible une seule fois au boot et comparer directement les octets bruts en mémoire via des mots de 64 bits (uint64)."

## Point de départ

La Séance 3 avait déjà éliminé la conversion hexadécimale (`MessageDigest.isEqual(byte[], byte[])`, comparaison en octets bruts). Cette étape va plus loin : comparer par **mots de 64 bits** (`long` en Java) plutôt qu'octet par octet.

## Ce qui a été fait

Création de [Seance4ComparaisonBinaire.java](../src/main/java/com/hashbreaker/Seance4ComparaisonBinaire.java), qui repart de `Seance3ZeroAllocation.java` et remplace `MessageDigest.isEqual()` par une comparaison manuelle :

1. **`octetsVersLongs()`** : décode le hash cible (32 octets) en **4 mots de 64 bits**, une seule fois avant la boucle.
2. **`egalise64bit()`** : à chaque tentative, reconstruit le hash calculé en 4 mots de 64 bits et les compare un par un aux mots cible — **avec sortie anticipée** dès le premier mot différent.

## Découverte importante : le vrai gain ne vient pas (que) de la taille des mots

`MessageDigest.isEqual()` est **volontairement conçu en temps constant** — une protection standard contre les attaques par mesure de timing, qui compare *toujours* la totalité des 32 octets, même si le premier octet diffère déjà. C'est indispensable quand on compare un secret utilisateur (mot de passe, token) à une valeur de référence. **Mais ce n'est pas notre cas** : on compare un hash qu'on vient de calculer nous-mêmes à une cible connue, dans un contexte de craquage, pas de sécurité applicative — la protection anti-timing n'a ici aucune utilité et ne fait que coûter du temps pour rien.

Avec un alphabet de 63 symboles, la probabilité qu'un candidat au hasard ait son premier octet de hash correct est d'environ 1/256 — **plus de 99% des tentatives échouent dès le premier octet**. Notre comparaison avec sortie anticipée évite donc de comparer les 31 octets restants dans l'immense majorité des cas, contrairement à `isEqual()` qui les compare systématiquement.

## Résultat mesuré — preuve statistique via Hyperfine

Première vraie comparaison à 3 versions avec rigueur statistique (`--warmup 1 --runs 5`), export dans [profiling/seance4-hyperfine-comparatif.md](../profiling/seance4-hyperfine-comparatif.md) :

| Version | Temps moyen | Facteur vs naïf | Facteur vs Séance 3 |
|---|---|---|---|
| Naïve (Séance 1, `Main.java`) | 11,161 s ± 0,306 s | — | — |
| Zéro-allocation (Séance 3) | 1,337 s ± 0,025 s | ×8,35 | — |
| **Binaire 64-bit (Séance 4)** | **1,104 s ± 0,055 s** | **×10,11** | **×1,21** |

*(Mesure sur l'exécution complète `z3D` + `Sh3n`, via `java -cp target/classes com.hashbreaker.<Classe>`.)*

## Interprétation

Le gain de ×1,21 par rapport à la Séance 3 est net et statistiquement significatif (les intervalles ne se chevauchent pas : 1,337±0,025 vs 1,104±0,055). Il est attribuable principalement à la sortie anticipée (early exit) plutôt qu'à la seule différence "4 comparaisons de `long` vs 32 comparaisons d'octets" — un exemple concret que la micro-optimisation la plus efficace n'est pas toujours celle qu'on anticipe en premier (même schéma pédagogique que Séance 3 : mesurer plutôt que supposer).

## Tests

[Seance4ComparaisonBinaireTest.java](../src/test/java/com/hashbreaker/Seance4ComparaisonBinaireTest.java) — 6 tests, dont un qui vérifie la cohérence avec un vrai calcul SHA-256 indépendant (`MessageDigest` standard) pour garantir que la sortie anticipée ne casse pas la correction.

**Résultat : 6/6 tests passent (36/36 sur l'ensemble du projet).**

## Fichiers concernés

- [src/main/java/com/hashbreaker/Seance4ComparaisonBinaire.java](../src/main/java/com/hashbreaker/Seance4ComparaisonBinaire.java)
- [src/test/java/com/hashbreaker/Seance4ComparaisonBinaireTest.java](../src/test/java/com/hashbreaker/Seance4ComparaisonBinaireTest.java)
- [scripts/run-main.cmd](../scripts/run-main.cmd), [scripts/run-seance3-zeroalloc.cmd](../scripts/run-seance3-zeroalloc.cmd), [scripts/run-seance4-binaire64.cmd](../scripts/run-seance4-binaire64.cmd) (nouveaux, pour Hyperfine)
- [profiling/seance4-hyperfine-comparatif.md](../profiling/seance4-hyperfine-comparatif.md) (preuve brute exportée)
