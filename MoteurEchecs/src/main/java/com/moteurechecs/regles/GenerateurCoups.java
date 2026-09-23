package com.moteurechecs.regles;

import com.moteurechecs.modele.Couleur;
import com.moteurechecs.modele.Coup;
import com.moteurechecs.modele.Piece;
import com.moteurechecs.modele.Plateau;
import com.moteurechecs.modele.TypePiece;

import java.util.ArrayList;
import java.util.List;

/**
 * Generation de coups - version naive V1.
 *
 * Perimetre volontairement simplifie (decision actee) : PAS de roque, PAS de
 * prise en passant. Promotion geree, mais automatiquement en Dame (pas de
 * choix de sous-promotion). A completer dans une iteration ulterieure si
 * necessaire -- l'objectif de cette V1 est "Make it work" avant "Make it
 * fast", exactement comme la Seance 1 de HashBreaker.
 */
public class GenerateurCoups {

    private static final int[][] DIRECTIONS_FOU = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    private static final int[][] DIRECTIONS_TOUR = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[][] DIRECTIONS_DAME_ROI = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };
    private static final int[][] SAUTS_CAVALIER = {
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
    };

    // coups legaux : coups pseudo-legaux qui ne laissent pas son propre roi en echec
    public static List<Coup> coupsLegaux(Plateau plateau) {
        List<Coup> pseudoLegaux = coupsPseudoLegaux(plateau, plateau.trait());
        List<Coup> legaux = new ArrayList<>();

        for (Coup coup : pseudoLegaux) {
            Plateau apres = plateau.jouerCoup(coup);
            if (!roiEnEchec(apres, plateau.trait())) {
                legaux.add(coup);
            }
        }

        return legaux;
    }

    public static boolean roiEnEchec(Plateau plateau, Couleur couleur) {
        int[] positionRoi = plateau.positionRoi(couleur);
        return caseAttaquee(plateau, positionRoi[0], positionRoi[1], couleur.adverse());
    }

    // vrai si une piece de 'parCouleur' peut atteindre (ligne, colonne) en un coup pseudo-legal
    static boolean caseAttaquee(Plateau plateau, int ligne, int colonne, Couleur parCouleur) {
        for (Coup coup : coupsPseudoLegaux(plateau, parCouleur)) {
            if (coup.ligneArrivee() == ligne && coup.colonneArrivee() == colonne) {
                return true;
            }
        }
        return false;
    }

    static List<Coup> coupsPseudoLegaux(Plateau plateau, Couleur couleur) {
        List<Coup> coups = new ArrayList<>();

        for (int ligne = 0; ligne < 8; ligne++) {
            for (int colonne = 0; colonne < 8; colonne++) {
                Piece piece = plateau.pieceEn(ligne, colonne);
                if (piece == null || piece.couleur() != couleur) {
                    continue;
                }

                switch (piece.type()) {
                    case PION -> genererCoupsPion(plateau, ligne, colonne, couleur, coups);
                    case CAVALIER -> genererCoupsSauts(plateau, ligne, colonne, couleur, SAUTS_CAVALIER, coups);
                    case FOU -> genererCoupsGlissants(plateau, ligne, colonne, couleur, DIRECTIONS_FOU, coups);
                    case TOUR -> genererCoupsGlissants(plateau, ligne, colonne, couleur, DIRECTIONS_TOUR, coups);
                    case DAME -> genererCoupsGlissants(plateau, ligne, colonne, couleur, DIRECTIONS_DAME_ROI, coups);
                    case ROI -> genererCoupsSauts(plateau, ligne, colonne, couleur, DIRECTIONS_DAME_ROI, coups);
                }
            }
        }

        return coups;
    }

    private static void genererCoupsPion(Plateau plateau, int ligne, int colonne, Couleur couleur, List<Coup> coups) {
        int direction = (couleur == Couleur.BLANC) ? 1 : -1;
        int rangeeDepart = (couleur == Couleur.BLANC) ? 1 : 6;
        int derniereRangee = (couleur == Couleur.BLANC) ? 7 : 0;

        // avance simple
        int ligneAvance = ligne + direction;
        if (estVide(plateau, ligneAvance, colonne)) {
            ajouterAvecPromotionEventuelle(ligne, colonne, ligneAvance, colonne, derniereRangee, coups);

            // avance double depuis la rangee de depart
            int ligneDouble = ligne + 2 * direction;
            if (ligne == rangeeDepart && estVide(plateau, ligneDouble, colonne)) {
                coups.add(new Coup(ligne, colonne, ligneDouble, colonne));
            }
        }

        // captures en diagonale
        for (int deltaColonne : new int[]{-1, 1}) {
            int ligneCapture = ligne + direction;
            int colonneCapture = colonne + deltaColonne;
            Piece cible = plateau.pieceEn(ligneCapture, colonneCapture);
            if (cible != null && cible.couleur() != couleur) {
                ajouterAvecPromotionEventuelle(ligne, colonne, ligneCapture, colonneCapture, derniereRangee, coups);
            }
        }
    }

    private static void ajouterAvecPromotionEventuelle(int ligneDepart, int colonneDepart, int ligneArrivee, int colonneArrivee, int derniereRangee, List<Coup> coups) {
        if (ligneArrivee == derniereRangee) {
            coups.add(new Coup(ligneDepart, colonneDepart, ligneArrivee, colonneArrivee, TypePiece.DAME));
        } else {
            coups.add(new Coup(ligneDepart, colonneDepart, ligneArrivee, colonneArrivee));
        }
    }

    // cavalier et roi : un seul pas dans chaque direction/saut de la liste
    private static void genererCoupsSauts(Plateau plateau, int ligne, int colonne, Couleur couleur, int[][] sauts, List<Coup> coups) {
        for (int[] saut : sauts) {
            int nouvelleLigne = ligne + saut[0];
            int nouvelleColonne = colonne + saut[1];
            if (estDansPlateau(nouvelleLigne, nouvelleColonne) && caseJouable(plateau, nouvelleLigne, nouvelleColonne, couleur)) {
                coups.add(new Coup(ligne, colonne, nouvelleLigne, nouvelleColonne));
            }
        }
    }

    // fou, tour, dame : glisse dans chaque direction jusqu'a un bord, une piece alliee, ou une capture
    private static void genererCoupsGlissants(Plateau plateau, int ligne, int colonne, Couleur couleur, int[][] directions, List<Coup> coups) {
        for (int[] direction : directions) {
            int nouvelleLigne = ligne + direction[0];
            int nouvelleColonne = colonne + direction[1];

            while (estDansPlateau(nouvelleLigne, nouvelleColonne)) {
                Piece cible = plateau.pieceEn(nouvelleLigne, nouvelleColonne);

                if (cible == null) {
                    coups.add(new Coup(ligne, colonne, nouvelleLigne, nouvelleColonne));
                } else {
                    if (cible.couleur() != couleur) {
                        coups.add(new Coup(ligne, colonne, nouvelleLigne, nouvelleColonne));
                    }
                    break; // piece rencontree (alliee ou adverse) : on arrete de glisser dans cette direction
                }

                nouvelleLigne += direction[0];
                nouvelleColonne += direction[1];
            }
        }
    }

    private static boolean estDansPlateau(int ligne, int colonne) {
        return ligne >= 0 && ligne <= 7 && colonne >= 0 && colonne <= 7;
    }

    private static boolean estVide(Plateau plateau, int ligne, int colonne) {
        return estDansPlateau(ligne, colonne) && plateau.pieceEn(ligne, colonne) == null;
    }

    // vrai si la case est vide ou occupee par une piece adverse (donc jouable pour cavalier/roi)
    private static boolean caseJouable(Plateau plateau, int ligne, int colonne, Couleur couleur) {
        Piece cible = plateau.pieceEn(ligne, colonne);
        return cible == null || cible.couleur() != couleur;
    }
}
