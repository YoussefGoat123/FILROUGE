# Sujet libre — Idées de projet (thème échecs)

Le module impose une grille d'évaluation technique commune (macro/micro-optimisation, Hot Path mesuré par profiling, workers bornés, streaming, indexation SQL...). Voici plusieurs pistes centrées sur les échecs qui s'y prêtent bien, dans la continuité du démonstrateur HashBreaker.

## 1. Moteur d'échecs (engine) — Minimax / Alpha-Beta

Option la plus alignée avec la grille RNCP. Le Hot Path est la génération de coups + l'évaluation de position, exécutée des millions de fois.

Leviers naturels :
- **Bitboards** : zéro-allocation, alignement mémoire.
- **Transposition table** (hash map) : mémoïsation / caching.
- **Recherche parallèle sur plusieurs branches** : parallélisme multi-cœurs, workers bornés.
- **Profiling** pour identifier le vrai goulot (génération de coups vs évaluation vs tri des coups).

## 2. Solveur de mats / puzzles (mat en N coups) à force brute optimisée

Proche du HashBreaker dans l'esprit : exploration combinatoire d'un espace immense, mais sur un plateau d'échecs.

Leviers naturels :
- **Élagage alpha-beta** : réduction de complexité algorithmique (macro-optimisation).
- **Zéro-allocation** sur la recherche en profondeur.
- Comparaison avant/après très parlante (temps de résolution d'un mat en 4 coups avant/après élagage).

## 3. Serveur de parties d'échecs multi-joueurs temps réel

Le plus « backend pur » : ici le Hot Path est réseau/I/O plutôt que calcul CPU pur.

Leviers naturels :
- **Streaming binaire** (WebSocket ou gRPC au lieu de JSON).
- **Indexation SQL** (recherche de parties/historique par ELO, date...).
- **Pool de connexions**, concurrence (contention sur les parties en cours).
- Terrain riche pour les axes I/O & Réseau et Base de Données de la grille.

## 4. Analyseur PGN massif / stats sur bases de parties

Ingestion et parsing de millions de parties au format PGN pour calculer des statistiques (fréquence d'ouvertures, taux de victoire par position...).

Leviers naturels :
- **Parsing zéro-copie**.
- **Streaming par blocs** (RAM constante sur un fichier énorme).
- **Indexation SQL** pour requêtes rapides.
- Avant/après mesurable sur le débit de parsing (Mo/s).

## Recommandation

- Option **1 (moteur d'échecs)** : la plus proche du modèle HashBreaker suivi en cours, coche le plus de cases de la grille (calcul CPU intensif, structures de données, parallélisme, profiling).
- Option **3 (serveur multi-joueurs)** : meilleur choix pour un profil plus « backend/API » que « algo pur ».
