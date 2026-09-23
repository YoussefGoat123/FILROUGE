# Synthèses — Ce qu'il faut retenir

Une synthèse par étape clé — condensé "à retenir" pour réviser vite ou nourrir le rapport d'audit final. Même principe que pour HashBreaker (`../../syntheses/`).

## Convention (à respecter pour chaque nouvelle étape)

Chaque fichier doit être **autonome** (pas seulement un lien vers `shemas/`) et contenir :

1. **Diagramme(s)** de l'état du système à cette étape (architecture ou flux, en Mermaid).
2. **Description précise** de l'optimisation appliquée (ou "aucune" pour une baseline) et *pourquoi*.
3. **Impact mesuré**, avec un tableau ou graphique comparatif **par rapport à l'étape précédente directement comparable** — pas forcément la toute première baseline, si un changement algorithmique (ex: alpha-beta) rend la comparaison directe trompeuse (cf. Étape 1, section "Résultats mesurés").

## Sommaire

| # | Étape | Synthèse |
|---|---|---|
| 01 | Architecture & Algorithme Naïf | [01-architecture-naive.md](01-architecture-naive.md) |
| 02 | Élagage Alpha-Beta (macro) | [02-elagage-alpha-beta.md](02-elagage-alpha-beta.md) |
