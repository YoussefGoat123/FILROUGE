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
}
