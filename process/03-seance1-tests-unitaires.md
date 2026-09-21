# Étape 03 — Tests unitaires de la version naïve

**Séance associée :** Séance 1 — Mise en place & baseline
**Objectif (cours) :** "Make it right" — "Code testé, propre et modulaire. Ne jamais optimiser sur une base instable ou confuse." (règle d'or du développement, étape 2/3).

## Pourquoi maintenant

Avant de commencer à optimiser (Séance 2 et suivantes), il faut un filet de sécurité qui garantit que chaque optimisation **ne casse pas le comportement fonctionnel**. Les tests ci-dessous verrouillent le comportement des trois briques de l'algorithme naïf, indépendamment les unes des autres.

## Ce qui a été fait

Création de [MainTest.java](../src/test/java/com/hashbreaker/MainTest.java) avec JUnit 5, 13 tests répartis sur les 3 méthodes utilitaires de `Main` :

- **`sha256()`** (5 tests) : vecteurs de test connus (`""`, `"abc"`), hash de `"z3D"`, déterminisme, longueur toujours 64 caractères.
- **`construireCandidat()`** (3 tests) : buffer `[0,0,0]` → `"aaa"`, indices exacts de `z`/`3`/`D` → `"z3D"`, buffer de taille 1.
- **`incrementer()`** (5 tests) : incrément simple, une retenue, retenue en chaîne sur deux positions, débordement complet (rebouclage silencieux — comportement documenté, pas un bug à corriger à ce stade), parcours de plusieurs candidats consécutifs.

## Outillage

`mvn` n'étant pas encore disponible sur le PATH de la session de travail au moment des tests, le lanceur `junit-platform-console-standalone.jar` a été téléchargé dans `.tools/` (ignoré par git) pour compiler et exécuter les tests en ligne de commande sans dépendre de Maven. À terme, `mvn test` (ou le bouton "Run Tests" de l'IDE) remplace cette étape.

## Résultat

**13/13 tests passent.**

## Fichiers concernés

- [src/test/java/com/hashbreaker/MainTest.java](../src/test/java/com/hashbreaker/MainTest.java)
