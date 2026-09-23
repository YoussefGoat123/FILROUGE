package com.moteurechecs.experimentation;

import com.moteurechecs.modele.Coup;
import com.moteurechecs.modele.Plateau;
import com.moteurechecs.recherche.MinimaxAlphaBeta;

/**
 * Etape 7 : profiling CPU reel (Flamegraph), pense pour etre execute sous
 * JFR. Profondeur 6 (et non 5 comme DiagnosticAllocations de l'Etape 4) pour
 * obtenir une fenetre d'execution de plusieurs secondes -- necessaire pour
 * un echantillonnage jdk.ExecutionSample statistiquement exploitable
 * (profondeur 5 ne dure qu'environ 426 ms, trop court).
 */
public class DiagnosticProfilingReel {
    public static void main(String[] args) {
        Coup coup = MinimaxAlphaBeta.meilleurCoup(Plateau.positionDepart(), 6);
        System.out.println("Coup trouve : " + coup.enNotation());
        System.out.println("Positions evaluees : " + MinimaxAlphaBeta.positionsEvaluees());
    }
}
