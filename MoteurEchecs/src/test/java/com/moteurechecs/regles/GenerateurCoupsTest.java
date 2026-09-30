package com.moteurechecs.regles;

import com.moteurechecs.modele.Couleur;
import com.moteurechecs.modele.Coup;
import com.moteurechecs.modele.Plateau;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerateurCoupsTest {

    @Test
    void positionDepart_a20CoupsLegauxPourLesBlancs() {
        // Sanite-check classique de tout generateur de coups d'echecs : perft(1) == 20
        // depuis la position de depart (16 coups de pions + 4 coups de cavaliers).
        List<Coup> coups = GenerateurCoups.coupsLegaux(Plateau.positionDepart());
        assertEquals(20, coups.size());
    }

    @Test
    void positionDepart_lePionPeutAvancerDeUneOuDeuxCases() {
        List<Coup> coups = GenerateurCoups.coupsLegaux(Plateau.positionDepart());

        boolean avanceSimple = coups.stream().anyMatch(c ->
                c.ligneDepart() == 1 && c.colonneDepart() == 4 && c.ligneArrivee() == 2 && c.colonneArrivee() == 4);
        boolean avanceDouble = coups.stream().anyMatch(c ->
                c.ligneDepart() == 1 && c.colonneDepart() == 4 && c.ligneArrivee() == 3 && c.colonneArrivee() == 4);

        assertTrue(avanceSimple, "le pion e2 doit pouvoir avancer d'une case");
        assertTrue(avanceDouble, "le pion e2 doit pouvoir avancer de deux cases depuis sa rangee de depart");
    }

    @Test
    void positionDepart_leCavalierPeutSauterParDessusLesPions() {
        List<Coup> coups = GenerateurCoups.coupsLegaux(Plateau.positionDepart());

        // cavalier b1 (ligne 0, colonne 1) peut aller en a3 ou c3
        boolean versA3 = coups.stream().anyMatch(c ->
                c.ligneDepart() == 0 && c.colonneDepart() == 1 && c.ligneArrivee() == 2 && c.colonneArrivee() == 0);
        boolean versC3 = coups.stream().anyMatch(c ->
                c.ligneDepart() == 0 && c.colonneDepart() == 1 && c.ligneArrivee() == 2 && c.colonneArrivee() == 2);

        assertTrue(versA3);
        assertTrue(versC3);
    }

    @Test
    void positionDepart_leFouNePeutPasEncoreBouger() {
        // tous les fous sont bloques par les pions en position de depart
        List<Coup> coups = GenerateurCoups.coupsLegaux(Plateau.positionDepart());
        boolean fouBouge = coups.stream().anyMatch(c -> c.ligneDepart() == 0 && (c.colonneDepart() == 2 || c.colonneDepart() == 5));
        assertFalse(fouBouge);
    }

    @Test
    void roiEnEchec_positionDeDepart_pasDEchec() {
        assertFalse(GenerateurCoups.roiEnEchec(Plateau.positionDepart(), com.moteurechecs.modele.Couleur.BLANC));
    }

    @Test
    void roiEnEchec_matDuFou_detecteLEchecEtMat() {
        // "Fool's Mate" (mat du fou), le mat le plus rapide aux echecs (2 coups) :
        // 1. f3 e5  2. g4 Dh4# -- la diagonale d8-h4 est completement degagee et
        // vise directement le roi blanc en e1 via la diagonale h4-g3-f2-e1.
        Plateau plateau = Plateau.positionDepart();
        plateau = plateau.jouerCoup(new Coup(1, 5, 2, 5)); // f2-f3 (Blancs)
        plateau = plateau.jouerCoup(new Coup(6, 4, 4, 4)); // e7-e5 (Noirs)
        plateau = plateau.jouerCoup(new Coup(1, 6, 3, 6)); // g2-g4 (Blancs)
        plateau = plateau.jouerCoup(new Coup(7, 3, 3, 7)); // Dd8-h4 (Noirs) -- mat

        assertTrue(GenerateurCoups.roiEnEchec(plateau, com.moteurechecs.modele.Couleur.BLANC));
        assertTrue(GenerateurCoups.coupsLegaux(plateau).isEmpty(), "aucun coup legal : c'est bien mat, pas juste echec");
    }

    // ---- caseAttaquee() -- tests directs de la version reecrite (Etape 4) ----

    @Test
    void caseAttaquee_cavalierAttaqueUneCaseEnL() {
        // cavalier blanc en b1 (0,1) attaque a3 (2,0) et c3 (2,2)
        Plateau plateau = Plateau.positionDepart();
        assertTrue(GenerateurCoups.caseAttaquee(plateau, 2, 0, Couleur.BLANC));
        assertTrue(GenerateurCoups.caseAttaquee(plateau, 2, 2, Couleur.BLANC));
    }

    @Test
    void caseAttaquee_pionAttaqueEnDiagonale() {
        // pion blanc en e2 (1,4) attaque d3 (2,3) et f3 (2,5)
        Plateau plateau = Plateau.positionDepart();
        assertTrue(GenerateurCoups.caseAttaquee(plateau, 2, 3, Couleur.BLANC));
        assertTrue(GenerateurCoups.caseAttaquee(plateau, 2, 5, Couleur.BLANC));
    }

    @Test
    void caseAttaquee_avanceToutDroitNestPasUneAttaque() {
        // e4 (3,4) n'est pas attaquee par les Blancs en position de depart : aucun pion
        // (l'avance tout droit du pion e2 n'est pas une "attaque"), aucun cavalier n'y arrive
        Plateau plateau = Plateau.positionDepart();
        assertFalse(GenerateurCoups.caseAttaquee(plateau, 3, 4, Couleur.BLANC));
    }

    @Test
    void caseAttaquee_tourGlissanteBloqueeParUnePiece() {
        // tour blanche en a1 (0,0) : bloquee par son propre pion en a2 (1,0), n'attaque pas plus loin
        Plateau plateau = Plateau.positionDepart();
        assertFalse(GenerateurCoups.caseAttaquee(plateau, 3, 0, Couleur.BLANC), "la tour ne doit pas voir au-dela de son propre pion");
    }

    @Test
    void caseAttaquee_tourGlissanteApresDegagement_attaqueEnLigneDroite() {
        // on retire le pion a2 "a la main" via deux jouerCoup pour degager la colonne a
        Plateau plateau = Plateau.positionDepart();
        plateau = plateau.jouerCoup(new Coup(1, 0, 3, 0)); // a2-a4 (degage la colonne)

        assertTrue(GenerateurCoups.caseAttaquee(plateau, 2, 0, Couleur.BLANC), "la tour a1 doit maintenant attaquer a3");
    }

    @Test
    void caseAttaquee_caseVideNonAttaqueeEnPositionDeDepart() {
        Plateau plateau = Plateau.positionDepart();
        assertFalse(GenerateurCoups.caseAttaquee(plateau, 4, 4, Couleur.BLANC));
        assertFalse(GenerateurCoups.caseAttaquee(plateau, 4, 4, Couleur.NOIR));
    }
}
