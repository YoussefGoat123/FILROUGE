package com.moteurechecs.recherche;

import com.moteurechecs.modele.Coup;
import com.moteurechecs.modele.Plateau;
import com.moteurechecs.regles.GenerateurCoups;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinimaxTest {

    @Test
    void meilleurCoup_positionDepart_renvoieUnCoupParmiLesCoupsLegaux() {
        Plateau plateau = Plateau.positionDepart();
        Coup coup = Minimax.meilleurCoup(plateau, 2);

        assertNotNull(coup);
        List<Coup> coupsLegaux = GenerateurCoups.coupsLegaux(plateau);
        assertTrue(coupsLegaux.contains(coup), "le coup choisi doit faire partie des coups legaux");
    }

    @Test
    void meilleurCoup_matEnUn_trouveLeCoupDeMat() {
        // Position a un coup du mat du fou : les Blancs viennent de jouer f3 et g4,
        // c'est aux Noirs de jouer Dh4# -- Minimax doit le trouver meme a profondeur 1.
        Plateau plateau = Plateau.positionDepart();
        plateau = plateau.jouerCoup(new Coup(1, 5, 2, 5)); // f2-f3
        plateau = plateau.jouerCoup(new Coup(6, 4, 4, 4)); // e7-e5
        plateau = plateau.jouerCoup(new Coup(1, 6, 3, 6)); // g2-g4

        Coup coupDeMat = new Coup(7, 3, 3, 7); // Dd8-h4#
        Coup trouve = Minimax.meilleurCoup(plateau, 1);

        assertTrue(GenerateurCoups.coupsLegaux(plateau).contains(coupDeMat), "Dh4 doit etre un coup legal ici");
        assertTrue(trouve.equals(coupDeMat), "Minimax doit choisir le mat immediat plutot qu'un autre coup");
    }

    @Test
    void positionsEvaluees_augmenteAvecLaProfondeur() {
        Plateau plateau = Plateau.positionDepart();

        Minimax.meilleurCoup(plateau, 1);
        long positionsProfondeur1 = Minimax.positionsEvaluees();

        Minimax.meilleurCoup(plateau, 2);
        long positionsProfondeur2 = Minimax.positionsEvaluees();

        assertTrue(positionsProfondeur2 > positionsProfondeur1,
                "explorer un coup de plus doit visiter davantage de positions");
    }
}
