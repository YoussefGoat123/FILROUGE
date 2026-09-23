package com.moteurechecs.modele;

/**
 * Representation naive du plateau : une grille 8x8 d'objets Piece (ou null).
 *
 * jouerCoup() retourne un NOUVEAU Plateau (copie complete + coup applique),
 * plutot que de muter en place (pattern make/unmake). C'est volontairement
 * naif -- exactement le meme choix que la concatenation de String en
 * Seance 1 de HashBreaker -- et sera corrige lors du futur creneau
 * zero-allocation (equivalent Seance 3).
 */
public class Plateau {

    private final Piece[][] cases; // [ligne][colonne], ligne 0 = rangee 1, colonne 0 = colonne a
    private final Couleur trait;

    private Plateau(Piece[][] cases, Couleur trait) {
        this.cases = cases;
        this.trait = trait;
    }

    public static Plateau positionDepart() {
        Piece[][] grille = new Piece[8][8];

        TypePiece[] rangeeArriere = {
                TypePiece.TOUR, TypePiece.CAVALIER, TypePiece.FOU, TypePiece.DAME,
                TypePiece.ROI, TypePiece.FOU, TypePiece.CAVALIER, TypePiece.TOUR
        };

        for (int colonne = 0; colonne < 8; colonne++) {
            grille[0][colonne] = new Piece(Couleur.BLANC, rangeeArriere[colonne]);
            grille[1][colonne] = new Piece(Couleur.BLANC, TypePiece.PION);
            grille[6][colonne] = new Piece(Couleur.NOIR, TypePiece.PION);
            grille[7][colonne] = new Piece(Couleur.NOIR, rangeeArriere[colonne]);
        }

        return new Plateau(grille, Couleur.BLANC);
    }

    public Piece pieceEn(int ligne, int colonne) {
        if (ligne < 0 || ligne > 7 || colonne < 0 || colonne > 7) {
            return null;
        }
        return cases[ligne][colonne];
    }

    public Couleur trait() {
        return trait;
    }

    // applique un coup et renvoie un nouveau plateau (copie complete de la grille)
    public Plateau jouerCoup(Coup coup) {
        Piece[][] nouvelleGrille = new Piece[8][8];
        for (int ligne = 0; ligne < 8; ligne++) {
            System.arraycopy(cases[ligne], 0, nouvelleGrille[ligne], 0, 8);
        }

        Piece pieceJouee = nouvelleGrille[coup.ligneDepart()][coup.colonneDepart()];

        if (coup.promotion() != null) {
            pieceJouee = new Piece(pieceJouee.couleur(), coup.promotion());
        }

        nouvelleGrille[coup.ligneArrivee()][coup.colonneArrivee()] = pieceJouee;
        nouvelleGrille[coup.ligneDepart()][coup.colonneDepart()] = null;

        return new Plateau(nouvelleGrille, trait.adverse());
    }

    // trouve la case du roi d'une couleur donnee (utilise par la detection d'echec)
    public int[] positionRoi(Couleur couleur) {
        for (int ligne = 0; ligne < 8; ligne++) {
            for (int colonne = 0; colonne < 8; colonne++) {
                Piece piece = cases[ligne][colonne];
                if (piece != null && piece.couleur() == couleur && piece.type() == TypePiece.ROI) {
                    return new int[]{ligne, colonne};
                }
            }
        }
        throw new IllegalStateException("Roi " + couleur + " introuvable sur le plateau");
    }

    public String affichage() {
        StringBuilder sb = new StringBuilder();
        for (int ligne = 7; ligne >= 0; ligne--) {
            for (int colonne = 0; colonne < 8; colonne++) {
                Piece piece = cases[ligne][colonne];
                sb.append(piece == null ? '.' : piece.lettre());
                sb.append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
