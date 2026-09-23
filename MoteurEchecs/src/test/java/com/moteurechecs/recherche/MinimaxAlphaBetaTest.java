package com.moteurechecs.recherche;

import com.moteurechecs.modele.Coup;
import com.moteurechecs.modele.Plateau;
import com.moteurechecs.regles.GenerateurCoups;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinimaxAlphaBetaTest {

    @Test
    void meilleurCoup_positionDepart_renvoieUnCoupParmiLesCoupsLegaux() {
        Plateau plateau = Plateau.positionDepart();
        Coup coup = MinimaxAlphaBeta.meilleurCoup(plateau, 2);

        assertNotNull(coup);
        List<Coup> coupsLegaux = GenerateurCoups.coupsLegaux(plateau);
        assertTrue(coupsLegaux.contains(coup));
    }

    @Test
    void meilleurCoup_matEnUn_trouveLeCoupDeMat() {
        Plateau plateau = Plateau.positionDepart();
        plateau = plateau.jouerCoup(new Coup(1, 5, 2, 5)); // f2-f3
        plateau = plateau.jouerCoup(new Coup(6, 4, 4, 4)); // e7-e5
        plateau = plateau.jouerCoup(new Coup(1, 6, 3, 6)); // g2-g4

        Coup coupDeMat = new Coup(7, 3, 3, 7); // Dd8-h4#
        Coup trouve = MinimaxAlphaBeta.meilleurCoup(plateau, 1);

        assertEquals(coupDeMat, trouve);
    }

    // ---- Equivalence mathematique avec Minimax pur : memes coups choisis, moins de positions visitees ----

    @Test
    void equivalence_memeCoupChoisiQueMinimaxPur_profondeur3() {
        Plateau plateau = Plateau.positionDepart();

        Coup coupNaif = Minimax.meilleurCoup(plateau, 3);
        Coup coupElague = MinimaxAlphaBeta.meilleurCoup(plateau, 3);

        assertEquals(coupNaif, coupElague, "l'elagage alpha-beta ne doit jamais changer le coup choisi");
    }

    @Test
    void equivalence_memeCoupApresQuelquesCoupsJoues() {
        Plateau plateau = Plateau.positionDepart();
        plateau = plateau.jouerCoup(new Coup(1, 4, 3, 4)); // e2-e4
        plateau = plateau.jouerCoup(new Coup(6, 4, 4, 4)); // e7-e5

        Coup coupNaif = Minimax.meilleurCoup(plateau, 3);
        Coup coupElague = MinimaxAlphaBeta.meilleurCoup(plateau, 3);

        assertEquals(coupNaif, coupElague);
    }

    @Test
    void elagage_visiteStrictementMoinsDePositionsQueMinimaxPur() {
        Plateau plateau = Plateau.positionDepart();

        Minimax.meilleurCoup(plateau, 3);
        long positionsNaif = Minimax.positionsEvaluees();

        MinimaxAlphaBeta.meilleurCoup(plateau, 3);
        long positionsElague = MinimaxAlphaBeta.positionsEvaluees();

        assertTrue(positionsElague < positionsNaif,
                "l'elagage doit visiter moins de positions (" + positionsElague + " vs " + positionsNaif + ")");
    }
}
