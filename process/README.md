# Journal du processus d'optimisation — HashBreaker

Ce dossier retrace, étape par étape, tout ce qui a été fait sur le projet depuis son initialisation. Il sert de **rapport d'audit** au sens du livrable attendu par le cours (cf. [README.md](../README.md), section "Livrables attendus") : chaque étape explique *quoi*, *pourquoi* (lien avec le cours) et *avec quel résultat mesuré*.

Mis à jour à chaque modification significative du projet — un fichier par étape, dans l'ordre chronologique.

## Sommaire

| # | Étape | Séance | Statut |
|---|---|---|---|
| 01 | [Initialisation de l'environnement](01-initialisation-environnement.md) | Séance 1 | ✅ |
| 02 | [Algorithme naïf (compteur base-N + SHA-256)](02-seance1-algorithme-naif.md) | Séance 1 | ✅ |
| 03 | [Tests unitaires de la version naïve](03-seance1-tests-unitaires.md) | Séance 1 | ✅ |
| 04 | [Stockage contigu vs dispersé](04-seance2-partie1-stockage-contigu-vs-disperse.md) | Séance 2 — Partie 1 | ✅ |
| 05 | [Parcours linéaire vs aléatoire](05-seance2-partie2-parcours-lineaire-vs-aleatoire.md) | Séance 2 — Partie 2 | ✅ |
| 06 | [Observation du goulot mémoire](06-seance2-partie3-observation-goulot-memoire.md) | Séance 2 — Partie 3 | ✅ |
| 07 | [Validation de la sympathie matérielle](07-seance2-partie4-validation-sympathie-materielle.md) | Séance 2 — Partie 4 | ✅ |
| 08 | [Diagnostic Escape Analysis (JFR)](08-seance3-partie1-diagnostic-escape-analysis.md) | Séance 3 — Partie 1 | ✅ |
| 09 | [Compactage de structure / Padding (JOL)](09-seance3-partie2-padding.md) | Séance 3 — Partie 2 | ✅ |
| 10 | [Buffers fixes / Zéro-allocation](10-seance3-partie3-buffers-fixes.md) | Séance 3 — Partie 3 | ✅ |

## Voir aussi

- [README.md](../README.md) — bases du cours et du projet
- [comprendre-hashbreaker.md](../compréhension/comprendre-hashbreaker.md) — pédagogie force brute / SHA-256
- [comprendre-cpu-caches.md](../compréhension/comprendre-cpu-caches.md) — pédagogie architecture CPU & caches
- [schemas.md](../shemas/schemas.md) — diagrammes Mermaid de l'état courant du code (mis à jour à chaque étape)
