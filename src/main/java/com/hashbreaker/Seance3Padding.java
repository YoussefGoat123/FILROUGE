package com.hashbreaker;

import org.openjdk.jol.info.ClassLayout;

/**
 * Seance 3 - Partie 2 : Compactage de Structure (Padding).
 *
 * Le cours illustre ce concept en Go, ou l'ordre de declaration des champs
 * determine directement le padding insere par le compilateur. En Java, c'est
 * different : c'est la JVM (pas javac) qui decide de l'agencement reel des
 * champs en memoire, independamment de leur ordre de declaration.
 *
 * Ce diagnostic verifie EMPIRIQUEMENT (avec JOL, l'equivalent Java de l'outil
 * "fieldalignment" de Go) si ca change vraiment quelque chose en Java.
 */
public class Seance3Padding {

    // Champs déclarés dans le "mauvais" ordre (comme CandidateBad du cours Go)
    static class CandidatDesordonne {
        boolean trouve;      // 1B
        long tentatives;     // 8B
        byte charsetId;      // 1B
        int longueur;        // 4B
        String cible;        // reference
    }

    // Champs déclarés du plus grand au plus petit (comme CandidateGood du cours Go)
    static class CandidatOrdonne {
        long tentatives;     // 8B
        String cible;        // reference
        int longueur;        // 4B
        boolean trouve;      // 1B
        byte charsetId;      // 1B
    }

    public static void main(String[] args) {
        System.out.println("=== CandidatDesordonne (declaration \"desordonnee\") ===");
        System.out.println(ClassLayout.parseClass(CandidatDesordonne.class).toPrintable());

        System.out.println("=== CandidatOrdonne (declaration triee par taille decroissante) ===");
        System.out.println(ClassLayout.parseClass(CandidatOrdonne.class).toPrintable());
    }
}
