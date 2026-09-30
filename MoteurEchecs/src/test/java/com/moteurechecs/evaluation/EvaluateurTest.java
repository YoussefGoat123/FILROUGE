package com.moteurechecs.evaluation;

import com.moteurechecs.modele.Coup;
import com.moteurechecs.modele.Plateau;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluateurTest {

    @Test
    void positionDepart_materielEgal_scoreNul() {
        assertEquals(0, Evaluateur.evaluer(Plateau.positionDepart()));
    }

    @Test
    void captureDuneDame_deseequilibreLeScoreEnFaveurDuCapteur() {
        // sequence artificielle : on retire une dame noire du plateau en la "capturant"
        // via une tour blanche deplacee dessus (illegal en vraie partie, mais suffisant
        // pour tester que l'evaluateur reagit bien a une perte de materiel)
        Plateau plateau = Plateau.positionDepart();
        Plateau apres = plateau.jouerCoup(new Coup(0, 0, 7, 3)); // tour blanche "capture" la dame noire en d8

        int score = Evaluateur.evaluer(apres);
        assertTrue(score > 0, "les Blancs doivent etre avantages apres avoir gagne une dame");
    }
}
