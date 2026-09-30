package com.moteurechecs.experimentation;

import com.moteurechecs.modele.Couleur;
import com.moteurechecs.modele.Piece;
import com.moteurechecs.modele.Plateau;
import com.moteurechecs.modele.PlateauBits;

/**
 * Etape 3 - Localite memoire : compare l'acces "grille d'objets" (Plateau)
 * a l'acces "bitboards" (PlateauBits), sur l'operation qui compte vraiment
 * pour un generateur de coups : "quelles cases sont occupees par telle
 * couleur ?". Meme demarche que Seance2LocaliteMemoire.java sur HashBreaker
 * (tableau contigu vs liste chainee) : construire les deux structures,
 * mesurer un acces pur repete, checksum pour eviter le Dead Code Elimination.
 *
 * Portee assumee : cette etape mesure l'acces pur, PAS encore integre dans
 * le vrai generateur de coups / Minimax (ca reste base sur Plateau). Travail
 * de suite logique, pas fait ici -- meme prudence que HashBreaker Seance 2
 * Partie 3-4, ou le gain memoire pur ne se traduisait pas forcement dans le
 * workload complet.
 */
public class EtapeLocaliteMemoire {

    public static void main(String[] args) {
        Plateau plateauObjets = Plateau.positionDepart();
        PlateauBits plateauBits = PlateauBits.positionDepart();

        // echauffement (non chronometre)
        enumererCasesObjets(plateauObjets, Couleur.BLANC);
        enumererCasesBits(plateauBits, Couleur.BLANC);

        int repetitions = 500_000;
        int essais = 5;

        System.out.println("=== Structure A : grille d'objets (Plateau) ===");
        for (int essai = 1; essai <= essais; essai++) {
            long debut = System.nanoTime();
            long checksum = 0;
            for (int i = 0; i < repetitions; i++) {
                checksum += enumererCasesObjets(plateauObjets, Couleur.BLANC);
            }
            long fin = System.nanoTime();
            System.out.println("  Essai " + essai + " : " + (fin - debut) / 1_000_000 + " ms (checksum=" + checksum + ")");
        }

        System.out.println();
        System.out.println("=== Structure B : bitboards (PlateauBits) ===");
        for (int essai = 1; essai <= essais; essai++) {
            long debut = System.nanoTime();
            long checksum = 0;
            for (int i = 0; i < repetitions; i++) {
                checksum += enumererCasesBits(plateauBits, Couleur.BLANC);
            }
            long fin = System.nanoTime();
            System.out.println("  Essai " + essai + " : " + (fin - debut) / 1_000_000 + " ms (checksum=" + checksum + ")");
        }
    }

    // balaie les 64 cases, filtre par couleur -- cout fixe O(64) quel que soit le nombre de pieces
    static long enumererCasesObjets(Plateau plateau, Couleur couleur) {
        long somme = 0;
        for (int ligne = 0; ligne < 8; ligne++) {
            for (int colonne = 0; colonne < 8; colonne++) {
                Piece piece = plateau.pieceEn(ligne, colonne);
                if (piece != null && piece.couleur() == couleur) {
                    somme += ligne * 8 + colonne;
                }
            }
        }
        return somme;
    }

    // parcourt uniquement les bits poses -- cout O(nombre de pieces), pas O(taille du plateau)
    static long enumererCasesBits(PlateauBits plateau, Couleur couleur) {
        long occupation = plateau.occupationCouleur(couleur);
        long somme = 0;
        while (occupation != 0) {
            int carre = Long.numberOfTrailingZeros(occupation);
            somme += carre;
            occupation &= occupation - 1; // efface le bit le plus bas pose
        }
        return somme;
    }
}
