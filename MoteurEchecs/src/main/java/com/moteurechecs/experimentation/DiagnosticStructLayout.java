package com.moteurechecs.experimentation;

import com.moteurechecs.modele.Coup;
import com.moteurechecs.modele.Piece;
import com.moteurechecs.modele.Plateau;
import org.openjdk.jol.info.ClassLayout;

/**
 * Etape 5 (levier manque, ajoute apres coup) : verification du struct padding
 * sur les structures creees en masse pendant la recherche -- meme demarche
 * que Seance3Padding.java sur HashBreaker, mais jamais refaite ici alors que
 * Piece, Coup et Plateau.InfoAnnulation ont une composition de champs
 * differente (int/enum plutot que boolean/long/byte/String) : le meme
 * resultat ("rien a optimiser en Java, la JVM reordonne deja") n'etait pas
 * garanti d'avance, il fallait verifier, pas supposer.
 */
public class DiagnosticStructLayout {

    public static void main(String[] args) {
        System.out.println("=== Piece (couleur, type) ===");
        System.out.println(ClassLayout.parseClass(Piece.class).toPrintable());

        System.out.println("=== Coup (4 int + 1 enum promotion) ===");
        System.out.println(ClassLayout.parseClass(Coup.class).toPrintable());

        System.out.println("=== Plateau.InfoAnnulation (2 references Piece) ===");
        System.out.println(ClassLayout.parseClass(Plateau.InfoAnnulation.class).toPrintable());
    }
}
