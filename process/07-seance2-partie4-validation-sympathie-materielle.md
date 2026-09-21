# Étape 07 — Validation de la Sympathie Matérielle (Séance 2, Partie 4)

**Séance associée :** Séance 2 — Localité Spatiale & Lignes de Cache (dernière partie)
**Objectif (cours) :** "Vérifier que la disposition linéaire et contiguë des données en mémoire maximise le débit de hachage sans modifier une seule ligne de la logique de calcul."

## Ce qui a été fait

Ajout de `debitMoyenHachageTableau()` et `debitMoyenHachageListe()` dans [Seance2LocaliteMemoire.java](../src/main/java/com/hashbreaker/Seance2LocaliteMemoire.java) : moyenne du débit de hachage sur **10 essais** (au lieu de 3 en Partie 3) pour une mesure statistiquement plus stable, puis calcul de l'écart en pourcentage et affichage d'un **verdict automatique** — avec un seuil de 5% en-dessous duquel l'écart est considéré comme du bruit de mesure plutôt qu'un vrai gain.

Important : **aucune ligne de `sha256()`, `hacherTableau()` ou `hacherListe()` n'a été modifiée** — seule une couche de mesure/moyenne a été ajoutée par-dessus, conformément à l'exigence explicite du cours ("sans modifier une seule ligne de la logique de calcul").

## Résultat mesuré

```
Debit moyen tableau contigu (n=10) : 1 141 690 candidats/s
Debit moyen liste chainee   (n=10) : 1 170 930 candidats/s
Ecart : -2,50 %

Verdict : PAS de gain mesurable sur le debit de hachage a ce stade.
```

## Interprétation honnête

Le titre officiel de cette partie ("Vérifier que la disposition... maximise le débit de hachage") laisse entendre que la validation devrait confirmer un gain. **Ce n'est pas ce qu'on observe ici**, et c'est cohérent avec la Partie 3 : le hachage naïf (`Main.sha256()`, avec sa conversion hexadécimale par concaténation de `String`) domine si largement le temps par candidat que la différence de disposition mémoire (mesurée en Partie 2 : facteur x8-x10 sur le parcours pur) devient statistiquement invisible sur le débit de hachage global — l'écart mesuré (-2,50%) est même dans le sens inverse de ce qu'on attendrait, ce qui confirme qu'il s'agit de bruit de mesure et non d'un effet réel.

**Ce que ce résultat valide réellement :**
- La disposition contigüe **est bien meilleure pour l'accès mémoire pur** (prouvé sans ambiguïté en Partie 2).
- Mais ce gain **n'est pas exploitable tant que le calcul (SHA-256 naïf) reste le goulot dominant** (loi d'Amdahl, déjà observée en Partie 3).
- La "sympathie matérielle" de la disposition mémoire est donc **une condition nécessaire mais pas suffisante** : elle ne se traduit en gain visible qu'une fois le goulot de calcul lui-même réduit.

Ce résultat n'invalide pas la théorie du cours (localité spatiale, lignes de cache) — il montre au contraire, de façon très concrète, **pourquoi le cours insiste sur le profiling avant optimisation** : on ne peut pas savoir a priori quel levier produira un gain mesurable sans avoir d'abord identifié le goulot dominant du système complet.

## Conclusion de la Séance 2

Les 4 parties sont terminées. Résumé de la chaîne de raisonnement complète :

1. **Partie 1** : deux structures équivalentes construites (tableau contigu vs liste chaînée).
2. **Partie 2** : la disposition mémoire seule change le temps d'accès d'un facteur x8-x10 — la théorie de la localité spatiale est validée expérimentalement.
3. **Partie 3** : ce gain disparaît dès qu'on ajoute un calcul CPU coûteux (hachage naïf) — la loi d'Amdahl explique pourquoi.
4. **Partie 4** : confirmation statistique (10 essais) qu'aucun gain n'est exploitable *pour l'instant*, avec un verdict clair sur la cause.

## Fichiers concernés

- [src/main/java/com/hashbreaker/Seance2LocaliteMemoire.java](../src/main/java/com/hashbreaker/Seance2LocaliteMemoire.java)
- Synthèse complète de la séance : [syntheses/02-seance2-cpu-caches-localite-memoire.md](../syntheses/02-seance2-cpu-caches-localite-memoire.md)
