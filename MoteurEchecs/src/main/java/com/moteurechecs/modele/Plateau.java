package com.moteurechecs.modele;

/**
 * Representation du plateau : une grille 8x8 d'objets Piece (ou null).
 *
 * Etape 4 (zero-allocation) : deux facons de jouer un coup coexistent
 * volontairement, pour deux usages differents -- ce n'est pas "deux
 * versions" de l'optimisation, une seule reste utilisee dans le chemin
 * chaud :
 *
 *   - jouerCoup(coup)      : copie complete de la grille, renvoie un NOUVEAU
 *                            Plateau. Pratique et lisible pour les tests et
 *                            la mise en place de positions. Plus utilise
 *                            dans la recherche (retire du chemin chaud).
 *
 *   - jouer(coup)/annuler(...) : MUTE ce Plateau en place (pattern
 *                            make/unmake), une seule instance reutilisee
 *                            pour tout l'arbre de recherche. C'est le
 *                            chemin utilise par GenerateurCoups et
 *                            MinimaxAlphaBeta -- equivalent chess du
 *                            "buffer fixe reutilise, mutation directe
 *                            d'index" du cours J2_AM.
 */
public class Plateau {

    private final Piece[][] cases; // [ligne][colonne], ligne 0 = rangee 1, colonne 0 = colonne a
    private Couleur trait;

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

    // copie complete de la grille + coup applique -- pour les tests et la mise en place de positions.
    // PAS utilise dans le chemin chaud de la recherche (voir jouer()/annuler() ci-dessous).
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

    // information necessaire pour annuler un coup joue avec jouer() -- petit record,
    // sans commune mesure avec la copie complete d'une grille 8x8
    public record InfoAnnulation(Piece pieceOriginale, Piece pieceCapturee) {}

    // MUTE ce plateau en place (chemin chaud de la recherche). Renvoie l'info pour annuler().
    public InfoAnnulation jouer(Coup coup) {
        Piece pieceOriginale = cases[coup.ligneDepart()][coup.colonneDepart()];
        Piece pieceCapturee = cases[coup.ligneArrivee()][coup.colonneArrivee()];

        Piece pieceFinale = (coup.promotion() != null)
                ? new Piece(pieceOriginale.couleur(), coup.promotion())
                : pieceOriginale;

        cases[coup.ligneArrivee()][coup.colonneArrivee()] = pieceFinale;
        cases[coup.ligneDepart()][coup.colonneDepart()] = null;
        trait = trait.adverse();

        return new InfoAnnulation(pieceOriginale, pieceCapturee);
    }

    // annule le coup joue par jouer() -- remet le plateau EXACTEMENT dans l'etat d'avant
    public void annuler(Coup coup, InfoAnnulation info) {
        cases[coup.ligneDepart()][coup.colonneDepart()] = info.pieceOriginale();
        cases[coup.ligneArrivee()][coup.colonneArrivee()] = info.pieceCapturee();
        trait = trait.adverse();
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
