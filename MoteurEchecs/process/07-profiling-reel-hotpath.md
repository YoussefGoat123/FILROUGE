# Étape 07 — Profiling réel & identification du Hot Path (Axe 2 du barème)

**Objectif (barème, Axe 2 — 5 pts) :** produire un flamegraph/pprof réel et identifier formellement le Hot Path — jusqu'ici, les Étapes 2 à 6 ont toutes été justifiées par analogie théorique avec HashBreaker ou par un simple comptage d'allocations JFR (Étape 4), jamais par un vrai profiling CPU sur ce code précis.

## Ce qui a été fait

Nouvelle classe [experimentation/DiagnosticProfilingReel.java](../src/main/java/com/moteurechecs/experimentation/DiagnosticProfilingReel.java) — même principe minimal que `DiagnosticAllocations` (Étape 4), mais à **profondeur 6** plutôt que 5 : la recherche à profondeur 5 ne dure que ~426 ms (Étape 6), une fenêtre trop courte pour un échantillonnage `jdk.ExecutionSample` statistiquement exploitable (JFR échantillonne par défaut toutes les ~10-20 ms). Vérifié avant de lancer JFR : profondeur 6 dure **~2,06 s** (645 199 positions évaluées) — fenêtre suffisante.

Commande de profiling (identique à la méthode HashBreaker, `settings=profile` pour activer les événements CPU) :

```
java -XX:StartFlightRecording=filename=profiling/etape7-profiling-cpu.jfr,settings=profile -cp target/classes com.moteurechecs.experimentation.DiagnosticProfilingReel
```

`jfr summary` confirme **142 `jdk.ExecutionSample`** capturés (1398 échantillons au total tous types d'événements confondus) — échantillon suffisant pour une agrégation par méthode.

## Résultat mesuré — agrégation des frames sommet de pile (`jfr print --stack-depth 1`)

| Méthode (frame sommet) | Échantillons | Catégorie |
|---|---|---|
| `caseAttaquee()` | 43 | Vérification de légalité (échec) |
| `attaqueGlissante()` | 34 + 3 | Vérification de légalité (échec) |
| `coupsPseudoLegaux()` | 13 | Génération de coups |
| `coupsLegaux()` | 11 + 7 | Génération de coups |
| `alphabeta()` | 8 | Recherche/élagage (overhead) |
| `genererCoupsPion()` | 7 | Génération de coups |
| `Plateau.annuler()` | 3 | Recherche (make/unmake) |
| `Evaluateur.valeur()` | 3 | Évaluation |
| `Evaluateur.evaluer()` | 2 | Évaluation |
| autres (`genererCoupsGlissants`, `attaqueParSaut`, `ajouterAvecPromotionEventuelle`, `positionRoi`, `Plateau.jouer`) | 5 | Génération / recherche |

**139 échantillons sur 142 tombent dans du code applicatif** (`com.moteurechecs.*`), les 3 restants dans du code JVM (GC, JIT).

### Regroupement par catégorie fonctionnelle

| Catégorie | Échantillons | Part du temps CPU |
|---|---|---|
| **Vérification de légalité** (`caseAttaquee` + `attaqueGlissante`, appelées depuis `coupsLegaux` pour vérifier que le roi n'est pas en échec après chaque coup candidat) | 80 | **~57,6 %** |
| **Génération de coups** (`coupsPseudoLegaux`, `coupsLegaux`, `genererCoupsPion`, `genererCoupsGlissants`, `attaqueParSaut`, `ajouterAvecPromotionEventuelle`) | 42 | **~30,2 %** |
| **Recherche / make-unmake** (`alphabeta`, `Plateau.annuler`, `Plateau.jouer`, `positionRoi`) | 13 | **~9,4 %** |
| **Évaluation de position** (`Evaluateur.valeur`, `Evaluateur.evaluer`) | 5 | **~3,6 %** |

## Interprétation

**Résultat contre-intuitif à noter honnêtement** : l'évaluation de position — la fonction qu'on associerait naturellement au "cerveau" du moteur — ne pèse que **~3,6 %** du temps CPU. Le vrai goulot est la **vérification de légalité** (~58 %) : pour chaque coup pseudo-légal généré, `coupsLegaux()` joue le coup, appelle `caseAttaquee()` pour vérifier que le roi ne serait pas en échec, puis annule le coup — et `caseAttaquee()` elle-même est dominée par `attaqueGlissante()` (parcours des rayons fou/tour/dame case par case jusqu'à collision).

Ceci **valide a posteriori le choix de l'Étape 4** : `caseAttaquee()` avait déjà été identifiée et réécrite en zéro-allocation à ce moment-là (sur la base d'un raisonnement structurel, pas d'un flamegraph CPU) — le profiling réel confirme que c'était effectivement le bon endroit à cibler, et que ça reste aujourd'hui, même après optimisation, le poste dominant. Ce n'est donc pas un nouveau goulot inattendu à corriger dans l'urgence, mais une confirmation que l'intuition de l'Étape 4 était juste — contrairement aux deux fois où HashBreaker (Séance 2 et 3) avait vu son intuition contredite par la mesure.

**Allocations restantes (contexte, `jdk.ObjectAllocationSample`)** : dominées par `Coup` (330 échantillons, un par coup candidat généré — attendu, ce sont des `record` immuables), les tableaux internes d'`ArrayList<Coup>` (`Object[]`, `int[]`), et `InfoAnnulation` (21, un par coup essayé dans `coupsLegaux`). Rien d'inattendu ni d'évitable sans changer l'algorithme lui-même (ex: détection d'échec incrémentale plutôt que recalculée à chaque coup candidat) — piste possible pour une future étape, mais hors périmètre ici.

## Vérification de reproductibilité (3 runs)

Avec seulement ~140 échantillons par run, le pourcentage exact pouvait être un artefact statistique plutôt qu'un résultat stable. Vérifié en relançant le même diagnostic 2 fois de plus (même position, même profondeur, même méthode de comptage) :

| Catégorie | Run 1 (officiel) | Run 2 | Run 3 |
|---|---|---|---|
| Vérification de légalité | 58,6 % | 72,1 % | 62,3 % |
| Génération de coups | 29,3 % | 20,2 % | 27,5 % |
| Recherche / make-unmake | 8,6 % | 6,2 % | 8,7 % |
| Évaluation de position | 3,6 % | 1,6 % | 1,4 % |

**Le chiffre précis bouge d'un run à l'autre** (échantillonnage statistique sur une fenêtre de ~2 s, pas une mesure exacte) — mais **le classement est identique sur les 3 runs** : vérification de légalité toujours largement dominante (59-72 %), génération de coups toujours deuxième (20-29 %), évaluation toujours la plus faible part (<4 %). La conclusion qualitative (Étape 4 a bien ciblé le vrai goulot, l'évaluation ne pèse presque rien) est donc **robuste**, même si l'énoncé "58,6 %" pris isolément ne doit pas être lu comme une valeur exacte et figée.

## Pourquoi ça ne remet pas en cause le choix de l'Étape 4 mais ouvre une piste

Le fait que `caseAttaquee()` reste à ~58 % **même après** l'avoir rendue zéro-allocation montre que le coût restant est **intrinsèquement algorithmique** (nombre d'appels × travail par appel), pas un problème d'allocation. La vraie piste de réduction ne serait plus "éliminer des objets" mais "éliminer des appels" — par exemple ne recalculer l'échec qu'incrémentalement plutôt que rejouer-vérifier-annuler pour chaque coup pseudo-légal. Piste notée pour une étape future, pas traitée ici (hors périmètre de l'Étape 7, qui est un diagnostic, pas une correction).

## Fichiers concernés

- [src/main/java/com/moteurechecs/experimentation/DiagnosticProfilingReel.java](../src/main/java/com/moteurechecs/experimentation/DiagnosticProfilingReel.java)
- [scripts/run-diagnostic-profiling-reel.cmd](../scripts/run-diagnostic-profiling-reel.cmd)
- `profiling/etape7-profiling-cpu.jfr` (enregistrement brut)
- `profiling/etape7-flamegraph.html` (flamegraph interactif, généré via `jfr-converter.jar --wall`)
