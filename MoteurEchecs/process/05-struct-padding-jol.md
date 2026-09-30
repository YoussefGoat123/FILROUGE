# Étape 05 — Struct Padding / Alignement (JOL) — levier manqué, rattrapé

**Type de levier :** micro-optimisation potentielle — vérification, pas supposition.

**Contexte** : cette vérification avait été faite sur HashBreaker (Séance 3, Étape 09) mais jamais refaite sur MoteurEchecs, alors que `Piece`, `Coup` et `Plateau.InfoAnnulation` sont créés en masse pendant la recherche. Composition de champs différente de l'exemple HashBreaker (`int`/enum ici, vs `boolean`/`long`/`byte`/`String` là-bas) — le même résultat n'était pas garanti d'avance.

## Ce qui a été fait

Ajout de `org.openjdk.jol:jol-core:0.17` au `pom.xml`, et création de [DiagnosticStructLayout.java](../src/main/java/com/moteurechecs/experimentation/DiagnosticStructLayout.java) qui inspecte le layout mémoire réel de `Piece`, `Coup` et `Plateau.InfoAnnulation` via `ClassLayout.parseClass(...)`.

## Piège technique rencontré : JOL ne peut pas analyser les `record` par défaut

```
Caused by: java.lang.UnsupportedOperationException: can't get field offset on a record class
```

`Piece`, `Coup` et `InfoAnnulation` sont tous des `record` Java — le JVM restreint volontairement l'accès aux offsets de champs des records via `Unsafe` (protection liée aux garanties d'immuabilité des records, absente sur HashBreaker où les structures testées étaient des `static class` classiques, pas des records). Contournement : flag `-Djol.magicFieldOffset=true`.

## Résultat mesuré

| Structure | Composition | Taille totale | Perte (padding) |
|---|---|---|---|
| `Piece` | 2 références enum (`Couleur`, `TypePiece`) | 24 octets | 4 octets (~16,7%) |
| `Coup` | 4 `int` + 1 référence enum (`promotion`) | 32 octets | **0 octet** |
| `Plateau.InfoAnnulation` | 2 références `Piece` | 24 octets | 4 octets (~16,7%) |

## Interprétation

`Coup` (32 octets, 12B en-tête + 16B de 4 `int` + 4B de référence) tombe **exactement** sur un multiple de 8 — aucune perte. `Piece` et `InfoAnnulation` (12B en-tête + 2×4B de références = 20B) doivent être arrondis à 24 octets pour respecter l'alignement 8 octets de la JVM — 4 octets de padding **incompressibles**.

**Point important, cohérent avec la découverte de HashBreaker** : ce padding **ne vient pas d'un mauvais ordre de champs** — `Piece` n'a que 2 champs de taille identique (4 octets chacun, ce sont des références), aucun réordonnancement possible ne changerait le résultat. C'est un coût structurel de la JVM (arrondi à 8 octets), pas une inefficacité corrigible manuellement. Même conclusion que HashBreaker Séance 3 : en Java, on ne peut pas éliminer ce genre de padding en réorganisant les champs — seule la suppression de l'objet lui-même (ex : le remplacer par des primitifs empaquetés, comme les bitboards de l'Étape 3) le pourrait.

## Conclusion : rien à corriger manuellement

Comme sur HashBreaker, cette vérification confirme qu'**il n'y a pas de gain accessible par réorganisation de champs** en Java — mais contrairement à HashBreaker, on ne le supposait pas par analogie, on l'a **mesuré spécifiquement** sur nos propres structures, avec un résultat qui aurait pu être différent (composition de champs différente) et un piège technique inédit (records) découvert au passage.

## Fichiers concernés

- [pom.xml](../pom.xml) (dépendance JOL ajoutée)
- [src/main/java/com/moteurechecs/experimentation/DiagnosticStructLayout.java](../src/main/java/com/moteurechecs/experimentation/DiagnosticStructLayout.java)
