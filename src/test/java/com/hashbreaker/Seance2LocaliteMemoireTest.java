package com.hashbreaker;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests de la Seance 2 - Partie 1 (stockage contigu vs disperse).
 */
class Seance2LocaliteMemoireTest {

    // ---- genererTableauContigu() / extraireCandidat() ----

    @Test
    void genererTableauContigu_tailleCorrecte() {
        char[] buffer = Seance2LocaliteMemoire.genererTableauContigu(10, 4);
        assertEquals(10 * 4, buffer.length);
    }

    @Test
    void genererTableauContigu_premierCandidatEstAaaa() {
        char[] buffer = Seance2LocaliteMemoire.genererTableauContigu(5, 4);
        assertEquals("aaaa", Seance2LocaliteMemoire.extraireCandidat(buffer, 0, 4));
    }

    @Test
    void genererTableauContigu_candidatsSuiventLOrdreDuCompteurBaseN() {
        char[] buffer = Seance2LocaliteMemoire.genererTableauContigu(5, 4);
        assertEquals("aaaa", Seance2LocaliteMemoire.extraireCandidat(buffer, 0, 4));
        assertEquals("aaab", Seance2LocaliteMemoire.extraireCandidat(buffer, 1, 4));
        assertEquals("aaac", Seance2LocaliteMemoire.extraireCandidat(buffer, 2, 4));
    }

    // ---- genererListeDispersee() ----

    @Test
    void genererListeDispersee_premierNoeudEstAaaa() {
        Seance2LocaliteMemoire.Node tete = Seance2LocaliteMemoire.genererListeDispersee(5, 4);
        assertEquals("aaaa", tete.candidat);
    }

    @Test
    void genererListeDispersee_chaineALaBonneLongueur() {
        Seance2LocaliteMemoire.Node tete = Seance2LocaliteMemoire.genererListeDispersee(5, 4);

        int compte = 0;
        Seance2LocaliteMemoire.Node courant = tete;
        while (courant != null) {
            compte++;
            courant = courant.suivant;
        }

        assertEquals(5, compte);
    }

    @Test
    void genererListeDispersee_dernierNoeudNaPasDeSuivant() {
        Seance2LocaliteMemoire.Node tete = Seance2LocaliteMemoire.genererListeDispersee(3, 4);

        Seance2LocaliteMemoire.Node courant = tete;
        while (courant.suivant != null) {
            courant = courant.suivant;
        }

        assertNull(courant.suivant);
    }

    // ---- coherence entre les deux structures ----

    @Test
    void lesDeuxStructuresContiennentLesMemesCandidatsDansLeMemeOrdre() {
        int nombreCandidats = 20;
        int longueur = 4;

        char[] tableau = Seance2LocaliteMemoire.genererTableauContigu(nombreCandidats, longueur);
        Seance2LocaliteMemoire.Node courant = Seance2LocaliteMemoire.genererListeDispersee(nombreCandidats, longueur);

        for (int i = 0; i < nombreCandidats; i++) {
            assertNotNull(courant);
            String candidatTableau = Seance2LocaliteMemoire.extraireCandidat(tableau, i, longueur);
            assertEquals(candidatTableau, courant.candidat);
            courant = courant.suivant;
        }
    }

    // ---- Partie 2 : parcourirTableau() / parcourirListe() ----

    @Test
    void parcourirTableau_sommeLesCodesDeTousLesCaracteres() {
        // 2 candidats de longueur 2 : "aa" (97,97) puis "ab" (97,98) -> somme = 389
        char[] buffer = Seance2LocaliteMemoire.genererTableauContigu(2, 2);
        assertEquals(97 + 97 + 97 + 98, Seance2LocaliteMemoire.parcourirTableau(buffer, 2, 2));
    }

    @Test
    void parcourirTableauEtParcourirListe_donnentLeMemeChecksum() {
        int nombreCandidats = 50;
        int longueur = 4;

        char[] tableau = Seance2LocaliteMemoire.genererTableauContigu(nombreCandidats, longueur);
        Seance2LocaliteMemoire.Node liste = Seance2LocaliteMemoire.genererListeDispersee(nombreCandidats, longueur);

        long checksumTableau = Seance2LocaliteMemoire.parcourirTableau(tableau, nombreCandidats, longueur);
        long checksumListe = Seance2LocaliteMemoire.parcourirListe(liste);

        assertEquals(checksumTableau, checksumListe);
    }

    // ---- Partie 3 : hacherTableau() / hacherListe() ----

    @Test
    void hacherTableau_sommeLePremierCaractereDeChaqueHash() throws NoSuchAlgorithmException {
        // "aa" -> sha256 commence par '9' (57), "ab" -> sha256 commence par 'f' (102)
        char[] buffer = Seance2LocaliteMemoire.genererTableauContigu(2, 2);
        assertEquals(57 + 102, Seance2LocaliteMemoire.hacherTableau(buffer, 2, 2));
    }

    @Test
    void hacherTableauEtHacherListe_donnentLeMemeResultat() throws NoSuchAlgorithmException {
        int nombreCandidats = 30;
        int longueur = 4;

        char[] tableau = Seance2LocaliteMemoire.genererTableauContigu(nombreCandidats, longueur);
        Seance2LocaliteMemoire.Node liste = Seance2LocaliteMemoire.genererListeDispersee(nombreCandidats, longueur);

        long resultatTableau = Seance2LocaliteMemoire.hacherTableau(tableau, nombreCandidats, longueur);
        long resultatListe = Seance2LocaliteMemoire.hacherListe(liste);

        assertEquals(resultatTableau, resultatListe);
    }
}
