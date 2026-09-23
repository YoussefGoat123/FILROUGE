package com.moteurechecs.regles;

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
}
