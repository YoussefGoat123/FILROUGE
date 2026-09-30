package com.moteurechecs.modele;

// Version naive : un objet (record) par piece. Sera reconsidere lors du
// passage aux bitboards (localite memoire, futur creneau equivalent a la
// Seance 2 de HashBreaker).
public record Piece(Couleur couleur, TypePiece type) {

    public char lettre() {
        char lettre = switch (type) {
            case PION -> 'p';
            case CAVALIER -> 'n';
            case FOU -> 'b';
            case TOUR -> 'r';
            case DAME -> 'q';
            case ROI -> 'k';
        };
        return couleur == Couleur.BLANC ? Character.toUpperCase(lettre) : lettre;
    }
}
