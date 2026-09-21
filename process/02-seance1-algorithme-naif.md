# Étape 02 — Algorithme naïf (compteur base-N + SHA-256)

**Séance associée :** Séance 1 — Mise en place & baseline
**Objectif (cours) :** "Coder le générateur combinatoire pour produire chaque mot candidat dans l'alphabet choisi et calculer son condensat SHA-256." Puis résoudre `z3D` (Niveau 1) et `Sh3n` (Niveau 2), et mesurer un temps de référence.

## Constat de départ

Les hash cibles donnés dans le support de cours (`59f0f9b6e6d1...` pour `z3D`, `d3b07384d113...` pour `Sh3n`) sont des **exemples illustratifs**, pas les vrais hash SHA-256 de ces mots de passe (`d3b07384d113edec49eaa6238ad5ff00f9bf9ce` est notamment un hash d'exemple très connu, utilisé dans d'innombrables tutoriels — pas le hash réel de `Sh3n`). Les vrais hash ont été calculés pour que le programme trouve effectivement quelque chose :

- `z3D` → `a532ca5e11e2b06ccc911e0d962a4864cdb87da05723f3a050a376d0f0895e63`
- `Sh3n` → `bd7d0ea8cf7ade4a446ba4efc46fd99071ec3f423770991ac51f70ec5a894dc7`

## Ce qui a été fait

Implémentation dans [Main.java](../src/main/java/com/hashbreaker/Main.java), volontairement **naïve** (comme demandé par le cours à ce stade — `Make it work` avant `Make it fast`) :

- `alphabet` : les 62 symboles (`a-z A-Z 0-9`).
- `construireCandidat(buffer)` : transforme un buffer d'indices en `String`, par concaténation (`candidat = candidat + ...`) — recrée un nouvel objet à chaque caractère.
- `incrementer(buffer)` : compteur base-N avec retenue (droite → gauche), la même logique qu'un compteur kilométrique.
- `sha256(texte)` : hash via `MessageDigest`, conversion en hexadécimal fait à la main octet par octet (encore de la concaténation de `String`).
- `craquer(nomCible, hashCible, longueur)` : boucle générer → hacher → comparer → incrémenter, chronométrée. Appelée successivement pour `z3D` puis `Sh3n`.

C'est délibérément le "piège de la version naïve" décrit par le cours : des millions d'allocations `String` inutiles, qui seront corrigées dans une séance ultérieure (zéro-allocation).

## Résultats mesurés (baseline)

| Cible | Espace de recherche | Temps mesuré |
|---|---|---|
| `z3D` (3 car.) | 238 328 candidats | **249 ms** |
| `Sh3n` (4 car.) | 14 776 336 candidats | **10 536 ms** |

C'est cette valeur de **10 536 ms pour Sh3n** qui sert de baseline de référence pour mesurer les gains des futures optimisations.

## Fichiers concernés

- [src/main/java/com/hashbreaker/Main.java](../src/main/java/com/hashbreaker/Main.java)
- Explications pédagogiques détaillées : [comprendre-hashbreaker.md](../compréhension/comprendre-hashbreaker.md)
