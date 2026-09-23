package com.moteurechecs.evaluation;

import com.moteurechecs.modele.Couleur;
import com.moteurechecs.modele.Piece;
import com.moteurechecs.modele.Plateau;
import com.moteurechecs.modele.TypePiece;

/**
 * Evaluation naive V1 : uniquement la valeur materielle des pieces.
 * Pas de tables de position (piece-square tables), pas de bonus structurel
 * (paires de fous, structure de pions, securite du roi...) -- volontairement
 * simple pour ce premier jet mesurable.
 *
 * Score positif = avantage aux Blancs, negatif = avantage aux Noirs.
 */
public class Evaluateur {

    public static int evaluer(Plateau plateau) {
        int score = 0;

        for (int ligne = 0; ligne < 8; ligne++) {
            for (int colonne = 0; colonne < 8; colonne++) {
                Piece piece = plateau.pieceEn(ligne, colonne);
                if (piece == null) {
                    continue;
                }
                int valeur = valeur(piece.type());
                score += (piece.couleur() == Couleur.BLANC) ? valeur : -valeur;
            }
        }

        return score;
    }

    private static int valeur(TypePiece type) {
        return switch (type) {
            case PION -> 100;
            case CAVALIER -> 320;
            case FOU -> 330;
            case TOUR -> 500;
            case DAME -> 900;
            case ROI -> 0; // le roi ne s'echange jamais ; sa "valeur" est geree via mat/pat, pas le materiel
        };
    }
}
