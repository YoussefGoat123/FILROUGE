package com.moteurechecs.recherche;

import com.moteurechecs.evaluation.Evaluateur;
import com.moteurechecs.modele.Couleur;
import com.moteurechecs.modele.Coup;
import com.moteurechecs.modele.Plateau;
import com.moteurechecs.regles.GenerateurCoups;

import java.util.List;

/**
 * Etape 2 (MACRO-optimisation) : Minimax + elagage Alpha-Beta.
 *
 * Coupe les branches de l'arbre qui ne peuvent mathematiquement pas
 * influencer la decision finale -- reduit la complexite de O(b^d) a environ
 * O(b^(d/2)) dans le meilleur cas. Mathematiquement EQUIVALENT a Minimax pur
 * (memes scores, memes coups choisis), juste moins de positions visitees.
 *
 * Limite assumee pour cette V2 : aucun tri des coups (move ordering). L'ordre
 * de parcours est celui de GenerateurCoups (scan du plateau), pas encore
 * optimise pour maximiser les coupures -- amelioration possible plus tard
 * (ex: MVV-LVA, tester les captures en premier).
 */
public class MinimaxAlphaBeta {

    private static final int SCORE_MAT = 1_000_000;

    private static long positionsEvaluees;

    public static Coup meilleurCoup(Plateau plateau, int profondeur) {
        positionsEvaluees = 0;

        List<Coup> coups = GenerateurCoups.coupsLegaux(plateau);
        if (coups.isEmpty()) {
            return null;
        }

        Couleur joueur = plateau.trait();
        Coup meilleurCoup = null;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int meilleurScore = (joueur == Couleur.BLANC) ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        // pas de coupure au niveau racine : il faut comparer tous les coups pour choisir le meilleur,
        // mais alpha/beta se resserrent progressivement pour couper plus tot dans les sous-arbres suivants
        for (Coup coup : coups) {
            Plateau apres = plateau.jouerCoup(coup);
            int score = alphabeta(apres, profondeur - 1, alpha, beta);

            boolean meilleur = (joueur == Couleur.BLANC) ? (score > meilleurScore) : (score < meilleurScore);
            if (meilleur) {
                meilleurScore = score;
                meilleurCoup = coup;
            }

            if (joueur == Couleur.BLANC) {
                alpha = Math.max(alpha, meilleurScore);
            } else {
                beta = Math.min(beta, meilleurScore);
            }
        }

        return meilleurCoup;
    }

    static int alphabeta(Plateau plateau, int profondeur, int alpha, int beta) {
        positionsEvaluees++;

        List<Coup> coups = GenerateurCoups.coupsLegaux(plateau);

        if (coups.isEmpty()) {
            boolean enEchec = GenerateurCoups.roiEnEchec(plateau, plateau.trait());
            if (!enEchec) {
                return 0;
            }
            return (plateau.trait() == Couleur.BLANC) ? -(SCORE_MAT + profondeur) : (SCORE_MAT + profondeur);
        }

        if (profondeur == 0) {
            return Evaluateur.evaluer(plateau);
        }

        if (plateau.trait() == Couleur.BLANC) {
            int max = Integer.MIN_VALUE;
            for (Coup coup : coups) {
                int score = alphabeta(plateau.jouerCoup(coup), profondeur - 1, alpha, beta);
                max = Math.max(max, score);
                alpha = Math.max(alpha, max);
                if (alpha >= beta) {
                    break; // coupure beta : les Noirs ont deja mieux ailleurs, inutile de continuer ici
                }
            }
            return max;
        } else {
            int min = Integer.MAX_VALUE;
            for (Coup coup : coups) {
                int score = alphabeta(plateau.jouerCoup(coup), profondeur - 1, alpha, beta);
                min = Math.min(min, score);
                beta = Math.min(beta, min);
                if (beta <= alpha) {
                    break; // coupure alpha : les Blancs ont deja mieux ailleurs, inutile de continuer ici
                }
            }
            return min;
        }
    }

    public static long positionsEvaluees() {
        return positionsEvaluees;
    }
}
