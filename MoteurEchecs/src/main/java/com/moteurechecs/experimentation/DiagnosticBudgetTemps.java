package com.moteurechecs.experimentation;

import com.moteurechecs.modele.Coup;
import com.moteurechecs.modele.Plateau;
import com.moteurechecs.recherche.MinimaxAlphaBeta;

/**
 * Etape 9 : mesure la profondeur reellement atteinte, le nombre total de
 * positions evaluees (cumule sur toutes les profondeurs, y compris la
 * derniere si interrompue) et le temps reel ecoule, pour plusieurs budgets.
 */
public class DiagnosticBudgetTemps {
    public static void main(String[] args) {
        long[] budgets = {200, 500, 1000, 2000, 5000};
        for (long budget : budgets) {
            Plateau plateau = Plateau.positionDepart();
            long debut = System.nanoTime();
            Coup coup = MinimaxAlphaBeta.meilleurCoupBudgetTemps(plateau, budget);
            long dureeMs = (System.nanoTime() - debut) / 1_000_000;

            System.out.printf(
                "budget=%5d ms | reel=%5d ms | profondeur=%2d | positions_total=%8d | coup=%s%n",
                budget, dureeMs, MinimaxAlphaBeta.profondeurAtteinte(),
                MinimaxAlphaBeta.positionsEvalueesTotal(),
                coup != null ? coup.enNotation() : "null"
            );
        }
    }
}
