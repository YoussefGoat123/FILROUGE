package com.moteurechecs.recherche;

import com.moteurechecs.evaluation.Evaluateur;
import com.moteurechecs.modele.Couleur;
import com.moteurechecs.modele.Coup;
import com.moteurechecs.modele.Piece;
import com.moteurechecs.modele.Plateau;
import com.moteurechecs.regles.GenerateurCoups;

import java.util.List;

/**
 * Etape 2 (MACRO) : elagage Alpha-Beta. Etape 4 (MICRO, zero-allocation) :
 * recherche menee via jouer()/annuler() (make/unmake) -- une seule instance
 * de Plateau reutilisee pour tout l'arbre, au lieu d'un nouveau Plateau
 * (copie de grille) a chaque noeud. Etape 8 (MACRO, tri des coups) : les
 * coups sont tries par score MVV-LVA avant exploration, pour que les
 * coupures alpha-beta arrivent plus tot -- reduit le NOMBRE de positions
 * visitees (contrairement a l'Etape 4, qui reduisait le COUT par position).
 *
 * Mathematiquement equivalent a Minimax pur (memes scores, memes coups
 * choisis en cas d'egalite stricte -- le tri ne change que l'ORDRE
 * d'exploration, jamais le resultat) -- valide par tests.
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
        trierCoups(plateau, coups);

        Couleur joueur = plateau.trait();
        Coup meilleurCoup = null;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        int meilleurScore = (joueur == Couleur.BLANC) ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Coup coup : coups) {
            Plateau.InfoAnnulation info = plateau.jouer(coup);
            int score = alphabeta(plateau, profondeur - 1, alpha, beta);
            plateau.annuler(coup, info);

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
        trierCoups(plateau, coups);

        if (plateau.trait() == Couleur.BLANC) {
            int max = Integer.MIN_VALUE;
            for (Coup coup : coups) {
                Plateau.InfoAnnulation info = plateau.jouer(coup);
                int score = alphabeta(plateau, profondeur - 1, alpha, beta);
                plateau.annuler(coup, info);

                max = Math.max(max, score);
                alpha = Math.max(alpha, max);
                if (alpha >= beta) {
                    break; // coupure beta
                }
            }
            return max;
        } else {
            int min = Integer.MAX_VALUE;
            for (Coup coup : coups) {
                Plateau.InfoAnnulation info = plateau.jouer(coup);
                int score = alphabeta(plateau, profondeur - 1, alpha, beta);
                plateau.annuler(coup, info);

                min = Math.min(min, score);
                beta = Math.min(beta, min);
                if (beta <= alpha) {
                    break; // coupure alpha
                }
            }
            return min;
        }
    }

    public static long positionsEvaluees() {
        return positionsEvaluees;
    }

    /**
     * Etape 8 : tri MVV-LVA (Most Valuable Victim - Least Valuable Aggressor).
     * Les captures sont explorees avant les coups calmes, triees par la
     * valeur de la piece capturee (descendant), pour que les coupures
     * alpha-beta se produisent le plus tot possible. Coups calmes : score 0,
     * ordre relatif inchange (Collections.sort/List.sort est stable).
     */
    private static void trierCoups(Plateau plateau, List<Coup> coups) {
        coups.sort((a, b) -> scoreTri(plateau, b) - scoreTri(plateau, a));
    }

    private static int scoreTri(Plateau plateau, Coup coup) {
        Piece cible = plateau.pieceEn(coup.ligneArrivee(), coup.colonneArrivee());
        if (cible == null) {
            return 0;
        }
        Piece attaquant = plateau.pieceEn(coup.ligneDepart(), coup.colonneDepart());
        return 10 * Evaluateur.valeur(cible.type()) - Evaluateur.valeur(attaquant.type());
    }
}
