package com.hashbreaker;

import org.junit.jupiter.api.Test;

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
}
