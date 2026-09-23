package com.moteurechecs;

import com.moteurechecs.modele.Coup;
import com.moteurechecs.modele.Plateau;
import com.moteurechecs.recherche.Minimax;

/**
 * Point d'entree du moteur d'echecs.
 * V1 : Minimax naif (sans elagage alpha-beta), position de depart,
 * deux profondeurs -- meme structure que Main.craquer() sur HashBreaker :
 * une recherche rapide de validation, puis une recherche plus lourde
 * qui sert de baseline chronometree pour les futures optimisations.
 */
public class Main {

    public static void main(String[] args) {
        // Niveau 1 - Validation : profondeur faible, verifie que l'algorithme fonctionne
        rechercher(Plateau.positionDepart(), 3);

        // Niveau 2 - Benchmark : profondeur de reference pour la baseline chronometree
        rechercher(Plateau.positionDepart(), 4);
    }

    static void rechercher(Plateau plateau, int profondeur) {
        System.out.println("Recherche a profondeur " + profondeur + " (Minimax naif, sans elagage)");

        long debut = System.currentTimeMillis();
        Coup meilleurCoup = Minimax.meilleurCoup(plateau, profondeur);
        long fin = System.currentTimeMillis();

        long positions = Minimax.positionsEvaluees();
        long dureeMs = fin - debut;
        double dureeSec = dureeMs / 1000.0;
        long debit = dureeSec > 0 ? (long) (positions / dureeSec) : positions;

        System.out.println("Meilleur coup trouve : " + (meilleurCoup != null ? meilleurCoup.enNotation() : "(mat ou pat)"));
        System.out.println("Positions evaluees : " + positions);
        System.out.println("Temps ecoule : " + dureeMs + " ms");
        System.out.println("Debit : " + debit + " positions/s");
        System.out.println();
    }
}
