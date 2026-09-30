package com.moteurechecs.modele;

/**
 * Etape 3 (MICRO-optimisation) : representation du plateau par bitboards.
 *
 * Au lieu d'une grille d'OBJETS (Plateau.java, Piece[8][8] -- des references
 * vers des Piece dispersees sur le tas, meme piege que Node en Seance 2 de
 * HashBreaker), le plateau est encode dans 12 "long" (64 bits = 64 cases),
 * un par (couleur, type de piece). Bit i pose <=> une piece de ce type/cette
 * couleur occupe la case i (indice = ligne*8 + colonne, meme convention que
 * Plateau).
 *
 * Avantage structurel visible : "quelles cases sont occupees par telle
 * couleur" est en O(popcount) -- proportionnel au nombre REEL de pieces
 * (16 en position de depart), pas a la taille du plateau (64 cases). La
 * grille d'objets doit balayer les 64 cases quoi qu'il arrive.
 */
public class PlateauBits {

    private final long[] bitboards; // taille 12, indice = indice(couleur, type)
    private final Couleur trait;

    private PlateauBits(long[] bitboards, Couleur trait) {
        this.bitboards = bitboards;
        this.trait = trait;
    }

    static int indice(Couleur couleur, TypePiece type) {
        return couleur.ordinal() * 6 + type.ordinal();
    }

    public static PlateauBits positionDepart() {
        long[] bb = new long[12];

        bb[indice(Couleur.BLANC, TypePiece.PION)] = 0x000000000000FF00L;      // rangee 2
        bb[indice(Couleur.BLANC, TypePiece.TOUR)] = 0x0000000000000081L;      // a1, h1
        bb[indice(Couleur.BLANC, TypePiece.CAVALIER)] = 0x0000000000000042L;  // b1, g1
        bb[indice(Couleur.BLANC, TypePiece.FOU)] = 0x0000000000000024L;       // c1, f1
        bb[indice(Couleur.BLANC, TypePiece.DAME)] = 0x0000000000000008L;      // d1
        bb[indice(Couleur.BLANC, TypePiece.ROI)] = 0x0000000000000010L;       // e1

        bb[indice(Couleur.NOIR, TypePiece.PION)] = 0x00FF000000000000L;       // rangee 7
        bb[indice(Couleur.NOIR, TypePiece.TOUR)] = 0x8100000000000000L;       // a8, h8
        bb[indice(Couleur.NOIR, TypePiece.CAVALIER)] = 0x4200000000000000L;   // b8, g8
        bb[indice(Couleur.NOIR, TypePiece.FOU)] = 0x2400000000000000L;        // c8, f8
        bb[indice(Couleur.NOIR, TypePiece.DAME)] = 0x0800000000000000L;       // d8
        bb[indice(Couleur.NOIR, TypePiece.ROI)] = 0x1000000000000000L;        // e8

        return new PlateauBits(bb, Couleur.BLANC);
    }

    public Couleur trait() {
        return trait;
    }

    // relit la piece presente sur une case (utilise pour les tests de coherence avec Plateau)
    public Piece pieceEn(int ligne, int colonne) {
        long masque = 1L << (ligne * 8 + colonne);
        for (Couleur couleur : Couleur.values()) {
            for (TypePiece type : TypePiece.values()) {
                if ((bitboards[indice(couleur, type)] & masque) != 0) {
                    return new Piece(couleur, type);
                }
            }
        }
        return null;
    }

    // union des 6 bitboards d'une couleur -- toutes les cases occupees par cette couleur, en 1 operation
    public long occupationCouleur(Couleur couleur) {
        long occupation = 0;
        for (TypePiece type : TypePiece.values()) {
            occupation |= bitboards[indice(couleur, type)];
        }
        return occupation;
    }

    public PlateauBits jouerCoup(Coup coup) {
        long[] nouveaux = bitboards.clone(); // copie de 12 long -- deja bien plus leger qu'une grille d'objets

        long masqueDepart = 1L << (coup.ligneDepart() * 8 + coup.colonneDepart());
        long masqueArrivee = 1L << (coup.ligneArrivee() * 8 + coup.colonneArrivee());

        int indicePieceJouee = -1;
        for (int i = 0; i < 12; i++) {
            if ((nouveaux[i] & masqueDepart) != 0) {
                indicePieceJouee = i;
                break;
            }
        }

        // retire une eventuelle piece capturee sur la case d'arrivee (toutes bitboards confondues)
        for (int i = 0; i < 12; i++) {
            nouveaux[i] &= ~masqueArrivee;
        }

        nouveaux[indicePieceJouee] &= ~masqueDepart;

        if (coup.promotion() != null) {
            nouveaux[indice(trait, coup.promotion())] |= masqueArrivee;
        } else {
            nouveaux[indicePieceJouee] |= masqueArrivee;
        }

        return new PlateauBits(nouveaux, trait.adverse());
    }
}
