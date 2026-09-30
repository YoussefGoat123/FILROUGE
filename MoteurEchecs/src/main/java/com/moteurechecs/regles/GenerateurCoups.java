package com.moteurechecs.regles;

import com.moteurechecs.modele.Couleur;
import com.moteurechecs.modele.Coup;
import com.moteurechecs.modele.Piece;
import com.moteurechecs.modele.Plateau;
import com.moteurechecs.modele.TypePiece;

import java.util.ArrayList;
import java.util.List;

/**
 * Generation de coups.
 *
 * Perimetre volontairement simplifie (decision actee) : PAS de roque, PAS de
 * prise en passant. Promotion geree, mais automatiquement en Dame (pas de
 * choix de sous-promotion).
 *
 * Etape 4 (zero-allocation) : deux corrections apportees suite a un
 * diagnostic JFR qui a revele que le vrai goulot n'etait ni celui suppose --
 * voir process/04-zero-allocation-caseattaquee-et-makeunmake.md pour le
 * detail complet du diagnostic.
 *
 *   1. caseAttaquee() teste desormais DIRECTEMENT les patterns d'attaque
 *      (pion, cavalier, roi, glissantes) contre la case ciblee, au lieu de
 *      generer la liste complete des coups pseudo-legaux d'une couleur pour
 *      n'en garder qu'un match -- zero Coup, zero List alloues pour cette
 *      operation (elimination du besoin, pas reutilisation de buffer).
 *
 *   2. coupsLegaux() utilise desormais jouer()/annuler() (make/unmake) au
 *      lieu de jouerCoup() (copie de grille) pour son filtrage -- vrai
 *      "buffer fixe reutilise, mutation directe d'index" au sens du cours.
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

    // coups legaux : coups pseudo-legaux qui ne laissent pas son propre roi en echec.
    // utilise jouer()/annuler() (make/unmake) : une seule instance de Plateau mutee et restauree,
    // au lieu d'une copie de grille par coup teste.
    public static List<Coup> coupsLegaux(Plateau plateau) {
        Couleur joueur = plateau.trait();
        List<Coup> pseudoLegaux = coupsPseudoLegaux(plateau, joueur);
        // capacite exacte, pas une estimation : legaux ne peut jamais depasser pseudoLegaux (filtrage pur)
        List<Coup> legaux = new ArrayList<>(pseudoLegaux.size());

        for (Coup coup : pseudoLegaux) {
            Plateau.InfoAnnulation info = plateau.jouer(coup);
            if (!roiEnEchec(plateau, joueur)) {
                legaux.add(coup);
            }
            plateau.annuler(coup, info);
        }

        return legaux;
    }

    public static boolean roiEnEchec(Plateau plateau, Couleur couleur) {
        int[] positionRoi = plateau.positionRoi(couleur);
        return caseAttaquee(plateau, positionRoi[0], positionRoi[1], couleur.adverse());
    }

    // vrai si une piece de 'parCouleur' attaque directement (ligne, colonne).
    // teste chaque pattern d'attaque un par un, sans jamais generer de liste de coups.
    static boolean caseAttaquee(Plateau plateau, int ligne, int colonne, Couleur parCouleur) {
        if (attaquePion(plateau, ligne, colonne, parCouleur)) {
            return true;
        }
        if (attaqueParSaut(plateau, ligne, colonne, parCouleur, SAUTS_CAVALIER, TypePiece.CAVALIER)) {
            return true;
        }
        if (attaqueParSaut(plateau, ligne, colonne, parCouleur, DIRECTIONS_DAME_ROI, TypePiece.ROI)) {
            return true;
        }
        if (attaqueGlissante(plateau, ligne, colonne, parCouleur, DIRECTIONS_FOU, TypePiece.FOU)) {
            return true;
        }
        if (attaqueGlissante(plateau, ligne, colonne, parCouleur, DIRECTIONS_TOUR, TypePiece.TOUR)) {
            return true;
        }
        return attaqueGlissante(plateau, ligne, colonne, parCouleur, DIRECTIONS_DAME_ROI, TypePiece.DAME);
    }

    // un pion de parCouleur attaque en diagonale avant -- donc l'attaquant potentiel
    // se trouve en diagonale ARRIERE de la case ciblee, vue depuis parCouleur
    private static boolean attaquePion(Plateau plateau, int ligne, int colonne, Couleur parCouleur) {
        int direction = (parCouleur == Couleur.BLANC) ? 1 : -1;
        int ligneAttaquant = ligne - direction;

        for (int deltaColonne : new int[]{-1, 1}) {
            Piece piece = plateau.pieceEn(ligneAttaquant, colonne + deltaColonne);
            if (piece != null && piece.couleur() == parCouleur && piece.type() == TypePiece.PION) {
                return true;
            }
        }
        return false;
    }

    // cavalier ou roi : vrai si une piece du type donne est a l'un des sauts/pas depuis (ligne, colonne)
    private static boolean attaqueParSaut(Plateau plateau, int ligne, int colonne, Couleur parCouleur, int[][] sauts, TypePiece type) {
        for (int[] saut : sauts) {
            Piece piece = plateau.pieceEn(ligne + saut[0], colonne + saut[1]);
            if (piece != null && piece.couleur() == parCouleur && piece.type() == type) {
                return true;
            }
        }
        return false;
    }

    // fou/tour/dame : glisse dans chaque direction jusqu'a une piece ; attaque si c'est le bon type/couleur
    private static boolean attaqueGlissante(Plateau plateau, int ligne, int colonne, Couleur parCouleur, int[][] directions, TypePiece type) {
        for (int[] direction : directions) {
            int l = ligne + direction[0];
            int c = colonne + direction[1];

            while (estDansPlateau(l, c)) {
                Piece piece = plateau.pieceEn(l, c);
                if (piece != null) {
                    if (piece.couleur() == parCouleur && piece.type() == type) {
                        return true;
                    }
                    break; // bloque par une piece (alliee ou adverse) : on arrete cette direction
                }
                l += direction[0];
                c += direction[1];
            }
        }
        return false;
    }

    // capacite estimee (pas garantie) : une position a rarement plus de coups pseudo-legaux que ca
    // en jeu reel -- evite 2-3 reallocations internes par appel (10 -> 20 -> 40, capacite par defaut)
    private static final int CAPACITE_COUPS_ESTIMEE = 48;

    static List<Coup> coupsPseudoLegaux(Plateau plateau, Couleur couleur) {
        List<Coup> coups = new ArrayList<>(CAPACITE_COUPS_ESTIMEE);

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
