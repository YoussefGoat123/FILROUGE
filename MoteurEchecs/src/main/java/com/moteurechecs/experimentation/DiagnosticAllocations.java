package com.moteurechecs.experimentation;

import com.moteurechecs.modele.Coup;
import com.moteurechecs.modele.Plateau;
import com.moteurechecs.recherche.MinimaxAlphaBeta;

/**
 * Etape 4 - Partie 1 : diagnostic d'allocation avant correction.
 *
 * Lance MinimaxAlphaBeta seul (pas le Minimax naif, trop lent et hors sujet
 * ici) a une profondeur substantielle, sous JFR, pour identifier OU vont
 * vraiment les allocations avant de coder quoi que ce soit -- meme demarche
 * que HashBreaker Seance 3 Partie 1 (le coupable suppose n'etait pas
 * confirme sans profiler).
 */
public class DiagnosticAllocations {
    public static void main(String[] args) {
        Coup coup = MinimaxAlphaBeta.meilleurCoup(Plateau.positionDepart(), 5);
        System.out.println("Coup trouve : " + coup.enNotation());
        System.out.println("Positions evaluees : " + MinimaxAlphaBeta.positionsEvaluees());
    }
}
