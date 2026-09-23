package com.moteurechecs.recherche;

import com.moteurechecs.evaluation.Evaluateur;
import com.moteurechecs.modele.Couleur;
import com.moteurechecs.modele.Coup;
import com.moteurechecs.modele.Plateau;
import com.moteurechecs.regles.GenerateurCoups;

import java.util.List;

/**
 * Recherche naive V1 : Minimax complet, SANS elagage alpha-beta.
 * L'elagage (macro-optimisation, reduction de complexite O(b^d) -> ~O(b^(d/2)))
 * est un levier a part entiere, volontairement pas applique ici -- meme
 * logique que HashBreaker Seance 1 : d'abord un algorithme correct et
 * mesurable, l'optimisation vient apres et se mesure contre cette baseline.
 */
public class Minimax {

    private static final int SCORE_MAT = 1_000_000;

    private static long positionsEvaluees;

    // renvoie le meilleur coup trouve a la profondeur donnee (null si mat/pat des la racine)
    public static Coup meilleurCoup(Plateau plateau, int profondeur) {
        positionsEvaluees = 0;

        List<Coup> coups = GenerateurCoups.coupsLegaux(plateau);
        if (coups.isEmpty()) {
            return null;
        }

        Couleur joueur = plateau.trait();
        Coup meilleurCoup = null;
        int meilleurScore = (joueur == Couleur.BLANC) ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Coup coup : coups) {
            Plateau apres = plateau.jouerCoup(coup);
            int score = minimax(apres, profondeur - 1);

            boolean meilleur = (joueur == Couleur.BLANC) ? (score > meilleurScore) : (score < meilleurScore);
            if (meilleur) {
                meilleurScore = score;
                meilleurCoup = coup;
            }
        }

        return meilleurCoup;
    }

    // explore recursivement l'arbre des coups ; renvoie le score de la position du point de vue des Blancs
    static int minimax(Plateau plateau, int profondeur) {
        positionsEvaluees++;

        List<Coup> coups = GenerateurCoups.coupsLegaux(plateau);

        if (coups.isEmpty()) {
            boolean enEchec = GenerateurCoups.roiEnEchec(plateau, plateau.trait());
            if (!enEchec) {
                return 0; // pat : nulle
            }
            // mat : tres mauvais pour le joueur au trait. On ajoute la profondeur restante
            // pour que le moteur prefere un mat trouve plus tot (score plus extreme).
            return (plateau.trait() == Couleur.BLANC) ? -(SCORE_MAT + profondeur) : (SCORE_MAT + profondeur);
        }

        if (profondeur == 0) {
            return Evaluateur.evaluer(plateau);
        }

        if (plateau.trait() == Couleur.BLANC) {
            int max = Integer.MIN_VALUE;
            for (Coup coup : coups) {
                max = Math.max(max, minimax(plateau.jouerCoup(coup), profondeur - 1));
            }
            return max;
        } else {
            int min = Integer.MAX_VALUE;
            for (Coup coup : coups) {
                min = Math.min(min, minimax(plateau.jouerCoup(coup), profondeur - 1));
            }
            return min;
        }
    }

    // nombre de positions explorees lors du dernier appel a meilleurCoup() -- notre metrique
    // "candidats/s" equivalente : ici "positions evaluees/s", le debit de reference de la baseline.
    public static long positionsEvaluees() {
        return positionsEvaluees;
    }
}
