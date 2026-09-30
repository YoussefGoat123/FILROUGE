package com.moteurechecs.modele;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlateauTest {

    @Test
    void positionDepart_placeCorrectementLesTours() {
        Plateau plateau = Plateau.positionDepart();
        assertEquals(new Piece(Couleur.BLANC, TypePiece.TOUR), plateau.pieceEn(0, 0));
        assertEquals(new Piece(Couleur.BLANC, TypePiece.TOUR), plateau.pieceEn(0, 7));
        assertEquals(new Piece(Couleur.NOIR, TypePiece.TOUR), plateau.pieceEn(7, 0));
        assertEquals(new Piece(Couleur.NOIR, TypePiece.TOUR), plateau.pieceEn(7, 7));
    }

    @Test
    void positionDepart_placeCorrectementLesRois() {
        Plateau plateau = Plateau.positionDepart();
        assertEquals(new Piece(Couleur.BLANC, TypePiece.ROI), plateau.pieceEn(0, 4));
        assertEquals(new Piece(Couleur.NOIR, TypePiece.ROI), plateau.pieceEn(7, 4));
    }

    @Test
    void positionDepart_placeCorrectementLesPions() {
        Plateau plateau = Plateau.positionDepart();
        for (int colonne = 0; colonne < 8; colonne++) {
            assertEquals(new Piece(Couleur.BLANC, TypePiece.PION), plateau.pieceEn(1, colonne));
            assertEquals(new Piece(Couleur.NOIR, TypePiece.PION), plateau.pieceEn(6, colonne));
        }
    }

    @Test
    void positionDepart_centreDuPlateauEstVide() {
        Plateau plateau = Plateau.positionDepart();
        assertNull(plateau.pieceEn(3, 3));
        assertNull(plateau.pieceEn(4, 4));
    }

    @Test
    void positionDepart_leTraitEstAuxBlancs() {
        assertEquals(Couleur.BLANC, Plateau.positionDepart().trait());
    }

    @Test
    void jouerCoup_deplaceLaPieceEtLibereLaCaseDeDepart() {
        Plateau apres = Plateau.positionDepart().jouerCoup(new Coup(1, 4, 3, 4)); // e2-e4

        assertEquals(new Piece(Couleur.BLANC, TypePiece.PION), apres.pieceEn(3, 4));
        assertNull(apres.pieceEn(1, 4));
    }

    @Test
    void jouerCoup_alterneLeTraitEntreLesDeuxCouleurs() {
        Plateau apres = Plateau.positionDepart().jouerCoup(new Coup(1, 4, 3, 4));
        assertEquals(Couleur.NOIR, apres.trait());
    }

    @Test
    void jouerCoup_neModifiePasLePlateauOriginal() {
        Plateau avant = Plateau.positionDepart();
        avant.jouerCoup(new Coup(1, 4, 3, 4));

        // le plateau d'origine doit rester intact (Plateau est immuable)
        assertEquals(new Piece(Couleur.BLANC, TypePiece.PION), avant.pieceEn(1, 4));
        assertNull(avant.pieceEn(3, 4));
    }

    // ---- jouer() / annuler() (make/unmake, chemin chaud) ----

    @Test
    void jouer_deplaceLaPieceEnPlace() {
        Plateau plateau = Plateau.positionDepart();
        plateau.jouer(new Coup(1, 4, 3, 4)); // e2-e4

        assertEquals(new Piece(Couleur.BLANC, TypePiece.PION), plateau.pieceEn(3, 4));
        assertNull(plateau.pieceEn(1, 4));
    }

    @Test
    void jouer_alterneLeTrait() {
        Plateau plateau = Plateau.positionDepart();
        plateau.jouer(new Coup(1, 4, 3, 4));
        assertEquals(Couleur.NOIR, plateau.trait());
    }

    @Test
    void annuler_restaureExactementLEtatDavant() {
        Plateau plateau = Plateau.positionDepart();
        Coup coup = new Coup(1, 4, 3, 4); // e2-e4

        Plateau.InfoAnnulation info = plateau.jouer(coup);
        plateau.annuler(coup, info);

        assertEquals(new Piece(Couleur.BLANC, TypePiece.PION), plateau.pieceEn(1, 4));
        assertNull(plateau.pieceEn(3, 4));
        assertEquals(Couleur.BLANC, plateau.trait());
    }

    @Test
    void annuler_restaureUnePieceCapturee() {
        // sequence artificielle (comme dans EvaluateurTest) : la tour blanche "capture" la dame noire en d8
        Plateau plateau = Plateau.positionDepart();
        Coup coup = new Coup(0, 0, 7, 3);

        Plateau.InfoAnnulation info = plateau.jouer(coup);
        assertEquals(new Piece(Couleur.BLANC, TypePiece.TOUR), plateau.pieceEn(7, 3));

        plateau.annuler(coup, info);

        assertEquals(new Piece(Couleur.NOIR, TypePiece.DAME), plateau.pieceEn(7, 3), "la dame noire doit reapparaitre");
        assertEquals(new Piece(Couleur.BLANC, TypePiece.TOUR), plateau.pieceEn(0, 0), "la tour doit revenir a sa case de depart");
    }

    @Test
    void jouerPuisAnnuler_plusieursFoisDeSuite_neCorromptPasLePlateau() {
        Plateau plateau = Plateau.positionDepart();

        for (int i = 0; i < 100; i++) {
            Coup coup = new Coup(1, 4, 3, 4);
            Plateau.InfoAnnulation info = plateau.jouer(coup);
            plateau.annuler(coup, info);
        }

        assertEquals(new Piece(Couleur.BLANC, TypePiece.PION), plateau.pieceEn(1, 4));
        assertEquals(Couleur.BLANC, plateau.trait());
    }

    @Test
    void jouerEtJouerCoup_produisentLeMemeResultat() {
        // les deux facons de jouer un coup doivent etre coherentes entre elles
        Plateau viaCopie = Plateau.positionDepart().jouerCoup(new Coup(1, 4, 3, 4));

        Plateau viaMutation = Plateau.positionDepart();
        viaMutation.jouer(new Coup(1, 4, 3, 4));

        for (int ligne = 0; ligne < 8; ligne++) {
            for (int colonne = 0; colonne < 8; colonne++) {
                assertEquals(viaCopie.pieceEn(ligne, colonne), viaMutation.pieceEn(ligne, colonne));
            }
        }
        assertEquals(viaCopie.trait(), viaMutation.trait());
    }
}
