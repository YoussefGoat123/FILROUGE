package com.moteurechecs.modele;

// ligne 0..7 = rangee 1..8, colonne 0..7 = colonne a..h.
// promotion == null sauf pour un pion qui atteint la derniere rangee
// (V1 naive : promotion automatique en Dame, pas de choix de sous-promotion).
public record Coup(int ligneDepart, int colonneDepart, int ligneArrivee, int colonneArrivee, TypePiece promotion) {

    public Coup(int ligneDepart, int colonneDepart, int ligneArrivee, int colonneArrivee) {
        this(ligneDepart, colonneDepart, ligneArrivee, colonneArrivee, null);
    }

    public String enNotation() {
        String depart = "" + (char) ('a' + colonneDepart) + (ligneDepart + 1);
        String arrivee = "" + (char) ('a' + colonneArrivee) + (ligneArrivee + 1);
        return depart + arrivee + (promotion == TypePiece.DAME ? "=D" : "");
    }
}
