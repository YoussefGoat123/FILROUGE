package com.moteurechecs.modele;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlateauBitsTest {

    @Test
    void positionDepart_placeCorrectementLesTours() {
        PlateauBits plateau = PlateauBits.positionDepart();
        assertEquals(new Piece(Couleur.BLANC, TypePiece.TOUR), plateau.pieceEn(0, 0));
        assertEquals(new Piece(Couleur.BLANC, TypePiece.TOUR), plateau.pieceEn(0, 7));
        assertEquals(new Piece(Couleur.NOIR, TypePiece.TOUR), plateau.pieceEn(7, 0));
        assertEquals(new Piece(Couleur.NOIR, TypePiece.TOUR), plateau.pieceEn(7, 7));
    }

    @Test
    void positionDepart_placeCorrectementLesRois() {
        PlateauBits plateau = PlateauBits.positionDepart();
        assertEquals(new Piece(Couleur.BLANC, TypePiece.ROI), plateau.pieceEn(0, 4));
        assertEquals(new Piece(Couleur.NOIR, TypePiece.ROI), plateau.pieceEn(7, 4));
    }

    @Test
    void positionDepart_centreDuPlateauEstVide() {
        PlateauBits plateau = PlateauBits.positionDepart();
        assertNull(plateau.pieceEn(3, 3));
        assertNull(plateau.pieceEn(4, 4));
    }

    @Test
    void positionDepart_coherenteAvecPlateauObjets() {
        // les deux representations doivent decrire EXACTEMENT la meme position
        Plateau objets = Plateau.positionDepart();
        PlateauBits bits = PlateauBits.positionDepart();

        for (int ligne = 0; ligne < 8; ligne++) {
            for (int colonne = 0; colonne < 8; colonne++) {
                assertEquals(objets.pieceEn(ligne, colonne), bits.pieceEn(ligne, colonne),
                        "difference en ligne " + ligne + ", colonne " + colonne);
            }
        }
    }

    @Test
    void occupationCouleur_positionDepart_16BitsPosesParCouleur() {
        PlateauBits plateau = PlateauBits.positionDepart();
        assertEquals(16, Long.bitCount(plateau.occupationCouleur(Couleur.BLANC)));
        assertEquals(16, Long.bitCount(plateau.occupationCouleur(Couleur.NOIR)));
    }

    @Test
    void jouerCoup_deplaceLaPieceEtLibereLaCaseDeDepart() {
        PlateauBits apres = PlateauBits.positionDepart().jouerCoup(new Coup(1, 4, 3, 4)); // e2-e4

        assertEquals(new Piece(Couleur.BLANC, TypePiece.PION), apres.pieceEn(3, 4));
        assertNull(apres.pieceEn(1, 4));
    }

    @Test
    void jouerCoup_neModifiePasLePlateauOriginal() {
        PlateauBits avant = PlateauBits.positionDepart();
        avant.jouerCoup(new Coup(1, 4, 3, 4));

        assertEquals(new Piece(Couleur.BLANC, TypePiece.PION), avant.pieceEn(1, 4));
        assertNull(avant.pieceEn(3, 4));
    }

    @Test
    void jouerCoup_uneCaptureRetireLaPieceAdverse() {
        // sequence artificielle (comme dans EvaluateurTest) : la tour blanche "capture" la dame noire en d8
        PlateauBits apres = PlateauBits.positionDepart().jouerCoup(new Coup(0, 0, 7, 3));

        assertEquals(new Piece(Couleur.BLANC, TypePiece.TOUR), apres.pieceEn(7, 3));
        assertEquals(15, Long.bitCount(apres.occupationCouleur(Couleur.NOIR)), "la dame noire doit avoir disparu");
    }
}
