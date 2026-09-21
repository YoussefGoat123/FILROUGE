# Étape 06 — Observation du Goulot Mémoire (Séance 2, Partie 3)

**Séance associée :** Séance 2 — Localité Spatiale & Lignes de Cache
**Objectif (cours) :** "Constater expérimentalement l'effondrement du débit de calcul lorsque le processeur subit des Cache Misses à répétition et attend la RAM (~200 cycles d'inactivité)."

## Ce qui a été fait

Ajout de `hacherTableau()` et `hacherListe()` dans [Seance2LocaliteMemoire.java](../src/main/java/com/hashbreaker/Seance2LocaliteMemoire.java) : même parcours qu'en Partie 2, mais en calculant le **vrai SHA-256** de chaque candidat via `Main.sha256()` (réutilisé tel quel, sans modification — conformité à l'exigence de la Partie 4 : "sans modifier une seule ligne de la logique de calcul"). Mesure du **débit** (candidats/seconde) sur 3 essais chronométrés par structure, après 1 tour d'échauffement.

## Résultat mesuré — et il est contre-intuitif

| Structure | Débit mesuré |
|---|---|
| A — tableau contigu | ~1,10 à 1,15 million candidats/s |
| B — liste chaînée | ~1,08 à 1,16 million candidats/s |

**Aucune différence significative** (écart de 1 à 2%, dans la marge de bruit de mesure) — alors que la Partie 2 avait montré un facteur **x8 à x10** sur le parcours pur (sans hachage). Le "goulot mémoire" attendu par le cours **n'est pas observable à cette échelle**, et c'est un résultat honnête à documenter, pas à masquer.

## Analyse — pourquoi la théorie du cours ne s'observe pas ici

Le calcul de `Main.sha256()` est **très coûteux en CPU** : la version naïve reconvertit le résultat en hexadécimal par concaténation de `String` caractère par caractère (le "piège de la version naïve" identifié dès la Séance 1). Sur ce projet, un hachage complet prend environ **~870-900 ns**, alors que l'écart de coût mémoire entre les deux structures (mesuré en Partie 2) est de l'ordre de **8 à 10 ns par candidat**.

Le calcul CPU représente donc plus de 99% du temps total par candidat — l'accès mémoire, même 8 à 10x plus lent sur la liste chaînée, ne pèse qu'une fraction négligeable du temps global.

## Lien direct avec la loi d'Amdahl (vue en J1_AM)

C'est une application concrète de la **règle du maillon faible** du premier cours du module :

> "Si une portion ne pèse que 5% du temps total d'une requête, l'optimiser n'apportera jamais plus de 5% de gain global, même avec une accélération infinie."

Ici, l'accès mémoire pèse largement moins de 5% du temps total par candidat (dominé par le hachage naïf). Améliorer la disposition mémoire (Partie 1-2) ne peut donc, à ce stade, produire aucun gain visible sur le débit de hachage global — **pas parce que l'optimisation mémoire est inutile en soi, mais parce qu'elle n'est pas le goulot dominant sur CE code précis, à CET instant précis.**

## Ce que ça implique pour la suite du projet

Cette observation valide directement la **règle scientifique** du cours : *"Toujours profiler avant d'optimiser pour cibler mathématiquement le goulot prédominant."* Une fois la Séance 3 (zéro-allocation) aura supprimé le coût de la conversion hexadécimale naïve, le poids relatif de l'accès mémoire dans le temps total remontera mécaniquement — et l'écart mesuré entre tableau contigu et liste chaînée redeviendra probablement visible sur le débit de hachage. C'est une prédiction vérifiable à la séance suivante.

## Fichiers concernés

- [src/main/java/com/hashbreaker/Seance2LocaliteMemoire.java](../src/main/java/com/hashbreaker/Seance2LocaliteMemoire.java)
- [src/test/java/com/hashbreaker/Seance2LocaliteMemoireTest.java](../src/test/java/com/hashbreaker/Seance2LocaliteMemoireTest.java) (2 tests ajoutés)
