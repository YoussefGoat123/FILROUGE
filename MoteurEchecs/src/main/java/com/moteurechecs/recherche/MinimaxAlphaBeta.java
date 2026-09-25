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
 *
 * Etape 9 (MACRO, recherche a budget de temps) : meilleurCoupBudgetTemps()
 * enchaine des recherches a profondeur croissante (iterative deepening)
 * jusqu'a epuisement d'un budget de temps, et retourne le meilleur coup de
 * la DERNIERE profondeur COMPLETEMENT terminee -- jamais un resultat
 * partiel. Contrairement aux Etapes 2/8, ce levier n'evite pas de travail
 * (il refait les profondeurs 1..N-1) : son gain est une CAPACITE nouvelle
 * (repondre sous contrainte de temps reelle), pas une acceleration.
 */
public class MinimaxAlphaBeta {

    private static final int SCORE_MAT = 1_000_000;
    private static final int MASQUE_VERIF_TEMPS = 1023; // verifie l'horloge tous les 1024 noeuds, pas a chaque noeud

    private static long positionsEvaluees;
    // Long.MAX_VALUE par defaut : un appel direct a meilleurCoup() (tests,
    // diagnostics, Main.java) sans passer par meilleurCoupBudgetTemps() ne
    // doit JAMAIS declencher d'interruption -- seul un vrai budget de temps
    // pose une deadline reelle.
    private static long deadlineNanos = Long.MAX_VALUE;
    private static long positionsEvalueesTotal;
    private static int profondeurAtteinte;

    // exception de controle de flux : pas de message ni de stack trace (cout de
    // construction quasi nul), instance unique reutilisee -- zero allocation
    // par interruption, meme principe que le zero-allocation de l'Etape 4.
    private static final class RechercheInterrompue extends RuntimeException {
        RechercheInterrompue() {
            super(null, null, false, false);
        }
    }

    private static final RechercheInterrompue INTERRUPTION = new RechercheInterrompue();

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
            int score;
            try {
                score = alphabeta(plateau, profondeur - 1, alpha, beta);
            } finally {
                // toujours annuler, meme si alphabeta() est interrompue par
                // RechercheInterrompue -- sinon le Plateau (mute en place)
                // reste corrompu, a moitie joue
                plateau.annuler(coup, info);
            }

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

    /**
     * Etape 9 : iterative deepening avec budget de temps. Cherche profondeur
     * 1, 2, 3... jusqu'a epuisement du budget ; retourne le coup de la
     * derniere profondeur COMPLETEMENT terminee (une profondeur interrompue
     * en cours de route est jetee, jamais retournee).
     */
    public static Coup meilleurCoupBudgetTemps(Plateau plateau, long budgetMillis) {
        deadlineNanos = System.nanoTime() + budgetMillis * 1_000_000L;
        positionsEvalueesTotal = 0;
        profondeurAtteinte = 0;
        Coup meilleurCoupTotal = null;

        try {
            int profondeur = 1;
            while (System.nanoTime() < deadlineNanos) {
                try {
                    Coup candidat = meilleurCoup(plateau, profondeur);
                    positionsEvalueesTotal += positionsEvaluees;
                    meilleurCoupTotal = candidat;
                    profondeurAtteinte = profondeur;
                    profondeur++;
                } catch (RechercheInterrompue interruption) {
                    positionsEvalueesTotal += positionsEvaluees; // travail reel fait, meme si jete
                    break;
                }
            }
        } finally {
            // remettre "pas de deadline" -- sinon un appel direct a meilleurCoup()
            // juste apres (autre test, autre diagnostic) heriterait d'une
            // deadline perimee et serait interrompu a tort
            deadlineNanos = Long.MAX_VALUE;
        }

        return meilleurCoupTotal;
    }

    static int alphabeta(Plateau plateau, int profondeur, int alpha, int beta) {
        positionsEvaluees++;
        if ((positionsEvaluees & MASQUE_VERIF_TEMPS) == 0 && System.nanoTime() >= deadlineNanos) {
            throw INTERRUPTION;
        }

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
                int score;
                try {
                    score = alphabeta(plateau, profondeur - 1, alpha, beta);
                } finally {
                    plateau.annuler(coup, info);
                }

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
                int score;
                try {
                    score = alphabeta(plateau, profondeur - 1, alpha, beta);
                } finally {
                    plateau.annuler(coup, info);
                }

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

    public static long positionsEvalueesTotal() {
        return positionsEvalueesTotal;
    }

    public static int profondeurAtteinte() {
        return profondeurAtteinte;
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
