# Journal du processus d'optimisation — MoteurEchecs

Même méthodologie que le TP fil rouge HashBreaker (`../../process/`) : un fichier par étape, quoi/pourquoi/résultat mesuré. Sert de matière première au rapport d'audit final (seul document réellement noté — cf. [../choix-sujet.md](../choix-sujet.md)).

## Sommaire

| # | Étape | Statut |
|---|---|---|
| 01 | [Architecture & Algorithme Naïf](01-architecture-et-algorithme-naif.md) | ✅ |
| 02 | [Élagage Alpha-Beta (macro)](02-elagage-alpha-beta.md) | ✅ |
| 03 | [Localité mémoire : Bitboards (micro)](03-localite-memoire-bitboards.md) | ✅ |
| 04 | [Zéro-allocation : caseAttaquee() + Make/Unmake (micro)](04-zero-allocation-caseattaquee-et-makeunmake.md) | ✅ |
| 05 | [Struct Padding / Alignement (JOL)](05-struct-padding-jol.md) | ✅ |
| 06 | [Pré-allocation de capacité (listes de Coup)](06-preallocation-listes-coup.md) | ✅ |

## Voir aussi

- [../README.md](../README.md) — vue d'ensemble du projet, build & run
- [../syntheses/](../syntheses/README.md) — synthèses "ce qu'il faut retenir" à chaque étape clé
- [../../choix-sujet.md](../../choix-sujet.md) — choix du sujet, cohérence avec le barème, roadmap
