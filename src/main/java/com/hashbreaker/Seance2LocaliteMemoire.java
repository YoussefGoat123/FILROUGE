package com.hashbreaker;

/**
 * Seance 2 - Partie 1 : Stockage Contigu vs Disperse.
 *
 * Objectif : generer le meme lot de candidats sous deux structures differentes
 * pour pouvoir ensuite (Partie 2) comparer leurs performances de parcours :
 *   - Structure A : tableau contigu (char[]) -> vrai bloc memoire continu
 *   - Structure B : liste chainee (Node) -> objets disperses sur le tas, relies par pointeurs
 *
 * Reutilise le compteur base-N et l'alphabet de Main (Seance 1).
 */
public class Seance2LocaliteMemoire {

    public static void main(String[] args) {
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
}
