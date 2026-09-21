package com.hashbreaker;

import java.security.NoSuchAlgorithmException;

/**
 * Seance 2 - Localite Spatiale & Lignes de Cache.
 *
 * Partie 1 : generer le meme lot de candidats sous deux structures differentes :
 *   - Structure A : tableau contigu (char[]) -> vrai bloc memoire continu
 *   - Structure B : liste chainee (Node) -> objets disperses sur le tas, relies par pointeurs
 *
 * Partie 2 : parcourir chacune des deux structures et chronometrer le temps d'acces,
 * pour observer l'effet de la localite spatiale (lecture sequentielle vs pointer-chasing).
 *
 * Partie 3 : refaire le meme parcours mais en hachant chaque candidat (SHA-256),
 * pour observer le debit de calcul (hachages/seconde) sous charge CPU reelle.
 *
 * Partie 4 : valider (ou infirmer) sur un plus grand nombre d'essais si la disposition
 * contigue maximise reellement le debit de hachage, sans modifier sha256() ni les
 * methodes de hachage elles-memes -- uniquement la structure qui les alimente.
 *
 * Reutilise le compteur base-N et l'alphabet de Main (Seance 1).
 */
public class Seance2LocaliteMemoire {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        int nombreCandidats = 1_000_000;
        int longueur = 4;

        System.out.println("Generation de " + nombreCandidats + " candidats (longueur " + longueur + ")");

        char[] tableauContigu = genererTableauContigu(nombreCandidats, longueur);
        System.out.println("Structure A (tableau contigu) : " + tableauContigu.length + " chars alloues d'un bloc");

        Node listeDispersee = genererListeDispersee(nombreCandidats, longueur);
        System.out.println("Structure B (liste chainee)   : " + nombreCandidats + " objets Node disperses sur le tas");

        // petite verification que les deux structures contiennent bien les memes candidats
        System.out.println();
        System.out.println("Verification (5 premiers candidats) :");
        Node courant = listeDispersee;
        for (int i = 0; i < 5; i++) {
            String candidatTableau = extraireCandidat(tableauContigu, i, longueur);
            String candidatListe = courant.candidat;
            System.out.println("  [" + i + "] tableau=" + candidatTableau + "  liste=" + candidatListe);
            courant = courant.suivant;
        }

        // ---- Partie 2 : parcours (mesure du temps d'acces) ----
        System.out.println();
        System.out.println("=== Partie 2 : parcours sequentiel, temps d'acces ===");

        // 1 tour d'echauffement (non chronometre) pour laisser le JIT compiler les boucles a chaud
        parcourirTableau(tableauContigu, nombreCandidats, longueur);
        parcourirListe(listeDispersee);

        int nombreRepetitions = 5;

        System.out.println();
        System.out.println("-- Structure A : tableau contigu --");
        for (int rep = 1; rep <= nombreRepetitions; rep++) {
            long debut = System.nanoTime();
            long checksum = parcourirTableau(tableauContigu, nombreCandidats, longueur);
            long fin = System.nanoTime();
            System.out.println("  Essai " + rep + " : " + (fin - debut) / 1_000_000 + " ms (checksum=" + checksum + ")");
        }

        System.out.println();
        System.out.println("-- Structure B : liste chainee --");
        for (int rep = 1; rep <= nombreRepetitions; rep++) {
            long debut = System.nanoTime();
            long checksum = parcourirListe(listeDispersee);
            long fin = System.nanoTime();
            System.out.println("  Essai " + rep + " : " + (fin - debut) / 1_000_000 + " ms (checksum=" + checksum + ")");
        }

        // ---- Partie 3 : goulot memoire (hachage SHA-256 de chaque candidat) ----
        System.out.println();
        System.out.println("=== Partie 3 : hachage complet, observation du debit de calcul ===");

        // echauffement (non chronometre)
        hacherTableau(tableauContigu, nombreCandidats, longueur);
        hacherListe(listeDispersee);

        int nombreRepetitionsHachage = 3;

        System.out.println();
        System.out.println("-- Structure A : tableau contigu (hachage) --");
        for (int rep = 1; rep <= nombreRepetitionsHachage; rep++) {
            long debut = System.nanoTime();
            hacherTableau(tableauContigu, nombreCandidats, longueur);
            long fin = System.nanoTime();
            afficherDebit(rep, debut, fin, nombreCandidats);
        }

        System.out.println();
        System.out.println("-- Structure B : liste chainee (hachage) --");
        for (int rep = 1; rep <= nombreRepetitionsHachage; rep++) {
            long debut = System.nanoTime();
            hacherListe(listeDispersee);
            long fin = System.nanoTime();
            afficherDebit(rep, debut, fin, nombreCandidats);
        }

        // ---- Partie 4 : validation de la sympathie materielle ----
        System.out.println();
        System.out.println("=== Partie 4 : validation de la sympathie materielle ===");

        int essaisValidation = 10;
        double debitTableau = debitMoyenHachageTableau(tableauContigu, nombreCandidats, longueur, essaisValidation);
        double debitListe = debitMoyenHachageListe(listeDispersee, nombreCandidats, essaisValidation);
        double ecartPourcent = ((debitTableau - debitListe) / debitListe) * 100;

        System.out.println("Debit moyen tableau contigu (n=" + essaisValidation + ") : " + (long) debitTableau + " candidats/s");
        System.out.println("Debit moyen liste chainee   (n=" + essaisValidation + ") : " + (long) debitListe + " candidats/s");
        System.out.println("Ecart : " + String.format("%.2f", ecartPourcent) + " %");
        System.out.println();

        // seuil de 5% : en-dessous, on considere l'ecart comme du bruit de mesure, pas un vrai gain
        if (Math.abs(ecartPourcent) < 5.0) {
            System.out.println("Verdict : PAS de gain mesurable sur le debit de hachage a ce stade.");
            System.out.println("          Le cout du hachage naif (conversion hexadecimale par concatenation)");
            System.out.println("          domine largement le cout d'acces memoire (cf. Partie 3, loi d'Amdahl).");
            System.out.println("          La disposition memoire reste optimale en soi (Parties 1-2 le prouvent),");
            System.out.println("          mais son effet est masque tant que sha256() reste aussi couteux en CPU.");
        } else {
            System.out.println("Verdict : la disposition contigue offre un gain mesurable de "
                    + String.format("%.1f", ecartPourcent) + "% sur le debit de hachage.");
        }
    }

    // moyenne le debit de hachage (candidats/seconde) du tableau contigu sur plusieurs essais
    static double debitMoyenHachageTableau(char[] buffer, int nombreCandidats, int longueur, int essais) throws NoSuchAlgorithmException {
        long dureeTotaleNs = 0;
        for (int i = 0; i < essais; i++) {
            long debut = System.nanoTime();
            hacherTableau(buffer, nombreCandidats, longueur);
            long fin = System.nanoTime();
            dureeTotaleNs += (fin - debut);
        }
        double dureeMoyenneSec = (dureeTotaleNs / (double) essais) / 1_000_000_000.0;
        return nombreCandidats / dureeMoyenneSec;
    }

    // moyenne le debit de hachage (candidats/seconde) de la liste chainee sur plusieurs essais
    static double debitMoyenHachageListe(Node tete, int nombreCandidats, int essais) throws NoSuchAlgorithmException {
        long dureeTotaleNs = 0;
        for (int i = 0; i < essais; i++) {
            long debut = System.nanoTime();
            hacherListe(tete);
            long fin = System.nanoTime();
            dureeTotaleNs += (fin - debut);
        }
        double dureeMoyenneSec = (dureeTotaleNs / (double) essais) / 1_000_000_000.0;
        return nombreCandidats / dureeMoyenneSec;
    }

    // affiche le temps ecoule et le debit (candidats/seconde) d'un essai de hachage
    static void afficherDebit(int rep, long debutNs, long finNs, int nombreCandidats) {
        long dureeMs = (finNs - debutNs) / 1_000_000;
        double dureeSec = (finNs - debutNs) / 1_000_000_000.0;
        long debit = (long) (nombreCandidats / dureeSec);
        System.out.println("  Essai " + rep + " : " + dureeMs + " ms  (" + debit + " candidats/s)");
    }

    // ---- Structure A : tableau contigu ----

    // un seul bloc memoire continu, chaque candidat occupe une tranche fixe [i*longueur, i*longueur+longueur)
    static char[] genererTableauContigu(int nombreCandidats, int longueur) {
        char[] buffer = new char[nombreCandidats * longueur];
        int[] compteur = new int[longueur]; // commence a [0,0,...,0] = "aaa..."

        for (int i = 0; i < nombreCandidats; i++) {
            for (int j = 0; j < longueur; j++) {
                buffer[i * longueur + j] = Main.alphabet.charAt(compteur[j]);
            }
            Main.incrementer(compteur);
        }

        return buffer;
    }

    // relit le candidat n°index dans le buffer contigu
    static String extraireCandidat(char[] buffer, int index, int longueur) {
        return new String(buffer, index * longueur, longueur);
    }

    // parcourt le tableau contigu sequentiellement, case par case.
    // le checksum sert juste a empecher le JIT de supprimer la boucle (dead code elimination)
    static long parcourirTableau(char[] buffer, int nombreCandidats, int longueur) {
        long somme = 0;
        for (int i = 0; i < nombreCandidats * longueur; i++) {
            somme += buffer[i];
        }
        return somme;
    }

    // relit chaque candidat du tableau contigu et calcule son SHA-256 (vraie charge CPU)
    static long hacherTableau(char[] buffer, int nombreCandidats, int longueur) throws NoSuchAlgorithmException {
        long somme = 0;
        for (int i = 0; i < nombreCandidats; i++) {
            String candidat = extraireCandidat(buffer, i, longueur);
            String hash = Main.sha256(candidat);
            somme += hash.charAt(0); // empeche le JIT de supprimer l'appel comme code mort
        }
        return somme;
    }

    // ---- Structure B : liste chainee dispersee ----

    static class Node {
        String candidat;
        Node suivant;

        Node(String candidat) {
            this.candidat = candidat;
        }
    }

    // chaque candidat est un objet Node alloue separement (new Node(...)),
    // potentiellement n'importe ou sur le tas : aucune garantie de contiguite
    static Node genererListeDispersee(int nombreCandidats, int longueur) {
        int[] compteur = new int[longueur];

        Node tete = new Node(Main.construireCandidat(compteur));
        Main.incrementer(compteur);

        Node dernier = tete;
        for (int i = 1; i < nombreCandidats; i++) {
            Node nouveau = new Node(Main.construireCandidat(compteur));
            dernier.suivant = nouveau;
            dernier = nouveau;
            Main.incrementer(compteur);
        }

        return tete;
    }

    // parcourt la liste chainee en suivant les pointeurs "suivant" un par un (pointer-chasing).
    // meme calcul que parcourirTableau, seule la structure parcourue change.
    static long parcourirListe(Node tete) {
        long somme = 0;
        Node courant = tete;
        while (courant != null) {
            for (int i = 0; i < courant.candidat.length(); i++) {
                somme += courant.candidat.charAt(i);
            }
            courant = courant.suivant;
        }
        return somme;
    }

    // suit la liste chainee et calcule le SHA-256 de chaque candidat (vraie charge CPU)
    static long hacherListe(Node tete) throws NoSuchAlgorithmException {
        long somme = 0;
        Node courant = tete;
        while (courant != null) {
            String hash = Main.sha256(courant.candidat);
            somme += hash.charAt(0);
            courant = courant.suivant;
        }
        return somme;
    }
}
