package com.hashbreaker;

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
}
