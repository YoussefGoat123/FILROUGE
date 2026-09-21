# Synthèse — Séance 1 : Mise en place & Craqueur Naïf

## En une phrase

SHA-256 est irréversible, donc la seule façon de retrouver un mot de passe à partir de son hash est d'essayer **tous les candidats possibles** dans un ordre systématique (compteur base-N) jusqu'à trouver une correspondance.

## Les 5 idées à retenir absolument

### 1. Pourquoi la force brute est la SEULE option

Une fonction de hachage cryptographique est **à sens unique** : facile à calculer dans un sens (texte → hash), impossible à inverser mathématiquement (hash → texte). Il n'existe aucune formule pour "décoder" un hash. La seule stratégie possible est donc l'énumération exhaustive : générer un candidat, le hacher, comparer, recommencer.

### 2. Le compteur base-N (le cœur de l'algorithme)

Générer "tous les mots possibles sans en oublier aucun" se fait exactement comme un compteur kilométrique : chaque position est un chiffre dans une base égale à la taille de l'alphabet (62 ici). On incrémente la position la plus à droite ; si elle dépasse le dernier symbole de l'alphabet, elle repasse à 0 et **propage une retenue** vers la gauche.

```
"aa9" (dernier caractère de l'alphabet en dernière position)
  → +1
"aba" (retenue propagée : la position du milieu passe de 'a' à 'b')
```

**Si tu ne devais retenir qu'une chose de cette séance, c'est ce mécanisme de retenue.** C'est lui qui garantit qu'on énumère bien 100% de l'espace de recherche, sans doublon et sans oubli.

### 3. La règle d'or : Make it work avant Make it fast

Le code de cette séance est **volontairement naïf** : concaténation de `String` à chaque itération (`candidat = candidat + ...`), conversion hexadécimale faite "à la main" caractère par caractère. C'est inefficace, et c'est normal — l'objectif de la Séance 1 n'est pas la performance mais l'exactitude. Optimiser un code qui ne fonctionne pas encore correctement (ou qu'on ne comprend pas encore) est le meilleur moyen de perdre du temps.

### 4. L'explosion combinatoire (pourquoi la longueur compte plus que tout)

L'espace de recherche croît en **`alphabet_size ^ longueur`** — une exponentielle, pas une droite. Un seul caractère de plus multiplie l'espace par 62 :

| Cible | Longueur | Espace de recherche |
|---|---|---|
| `z3D` | 3 | 238 328 |
| `Sh3n` | 4 | 14 776 336 (×62) |
| `@kAl1` | 5 | 1,68 milliard (×62 encore) |

### 5. La baseline sert de référence, pas de résultat final

Le temps mesuré sur la version naïve (**10 536 ms pour `Sh3n`**) n'est pas "le" résultat du projet — c'est le point de départ chronométré contre lequel chaque optimisation future (séances suivantes) sera comparée pour prouver un gain réel.

## Résultats mesurés (baseline officielle)

| Cible | Espace de recherche | Temps mesuré |
|---|---|---|
| `z3D` | 238 328 candidats | 249 ms |
| `Sh3n` | 14 776 336 candidats | **10 536 ms** ← baseline de référence |

## Piège à ne jamais refaire (dès la prochaine séance)

Le hash cible donné dans un énoncé n'est pas forcément le vrai hash du mot de passe affiché en exemple — toujours vérifier en calculant soi-même le hash réel avant de coder dessus (`sha256sum` en ligne de commande, ou équivalent).

## Vocabulaire à savoir définir à l'oral

- **Fonction de hachage à sens unique** : transformation facile dans un sens, impossible à inverser.
- **Espace de recherche** : nombre total de candidats possibles (`alphabet_size ^ longueur`).
- **Compteur base-N avec retenue** : technique d'énumération exhaustive sans doublon.
- **Baseline** : temps de référence non optimisé, servant de point de comparaison.
- **Make it work / Make it right / Make it fast** : la règle d'or, dans cet ordre, jamais inversée.

## Pour aller plus loin

- Explication pas à pas avec analogies et schémas : [comprendre-hashbreaker.md](../compréhension/comprendre-hashbreaker.md)
- Détail technique de l'implémentation : [process/02-seance1-algorithme-naif.md](../process/02-seance1-algorithme-naif.md)
