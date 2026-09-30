package com.moteurechecs;

import com.moteurechecs.modele.Coup;
import com.moteurechecs.modele.Plateau;
import com.moteurechecs.recherche.Minimax;
import com.moteurechecs.recherche.MinimaxAlphaBeta;

/**
 * Point d'entree du moteur d'echecs.
 *
 * Etape 1 : Minimax naif (baseline, aucun elagage).
 * Etape 2 : Minimax + elagage Alpha-Beta (macro-optimisation) -- compare
 * directement a l'Etape 1, aux memes profondeurs, sur la meme position.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Etape 1 : Minimax naif (sans elagage) ===");
        rechercherNaif(Plateau.positionDepart(), 3);
        rechercherNaif(Plateau.positionDepart(), 4);

        System.out.println("=== Etape 2 : Minimax + elagage Alpha-Beta ===");
        rechercherAlphaBeta(Plateau.positionDepart(), 3);
        rechercherAlphaBeta(Plateau.positionDepart(), 4);
    }

    static void rechercherNaif(Plateau plateau, int profondeur) {
        long debut = System.currentTimeMillis();
        Coup meilleurCoup = Minimax.meilleurCoup(plateau, profondeur);
        long dureeMs = System.currentTimeMillis() - debut;
        afficher(profondeur, meilleurCoup, Minimax.positionsEvaluees(), dureeMs);
    }

    static void rechercherAlphaBeta(Plateau plateau, int profondeur) {
        long debut = System.currentTimeMillis();
        Coup meilleurCoup = MinimaxAlphaBeta.meilleurCoup(plateau, profondeur);
        long dureeMs = System.currentTimeMillis() - debut;
        afficher(profondeur, meilleurCoup, MinimaxAlphaBeta.positionsEvaluees(), dureeMs);
    }

    static void afficher(int profondeur, Coup meilleurCoup, long positions, long dureeMs) {
        double dureeSec = dureeMs / 1000.0;
        long debit = dureeSec > 0 ? (long) (positions / dureeSec) : positions;

        System.out.println("Profondeur " + profondeur + " : " + (meilleurCoup != null ? meilleurCoup.enNotation() : "(mat ou pat)"));
        System.out.println("  Positions evaluees : " + positions);
        System.out.println("  Temps ecoule : " + dureeMs + " ms");
        System.out.println("  Debit : " + debit + " positions/s");
    }
}
