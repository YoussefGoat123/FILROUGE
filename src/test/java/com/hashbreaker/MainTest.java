package com.hashbreaker;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests unitaires de la version naive du craqueur (Seance 1).
 */
class MainTest {

    // ---- sha256() ----

    @Test
    void sha256_chaineVide_donneLeHashConnu() throws NoSuchAlgorithmException {
        String resultat = Main.sha256("");
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855".substring(0, 64), resultat);
    }

    @Test
    void sha256_abc_donneLeHashConnu() throws NoSuchAlgorithmException {
        String resultat = Main.sha256("abc");
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad".substring(0, 64), resultat);
    }

    @Test
    void sha256_z3D_correspondAuHashCible() throws NoSuchAlgorithmException {
        String resultat = Main.sha256("z3D");
        assertEquals("a532ca5e11e2b06ccc911e0d962a4864cdb87da05723f3a050a376d0f0895e63".substring(0, 64), resultat);
    }

    @Test
    void sha256_estDeterministe() throws NoSuchAlgorithmException {
        assertEquals(Main.sha256("Sh3n"), Main.sha256("Sh3n"));
    }

    @Test
    void sha256_renvoieToujours64Caracteres() throws NoSuchAlgorithmException {
        assertEquals(64, Main.sha256("a").length());
        assertEquals(64, Main.sha256("un texte plus long pour verifier").length());
    }

    // ---- construireCandidat() ----

    @Test
    void construireCandidat_bufferZero_donneAaa() {
        assertEquals("aaa", Main.construireCandidat(new int[]{0, 0, 0}));
    }

    @Test
    void construireCandidat_indicesDeZ3D_donneZ3D() {
        // z -> index 25, 3 -> index 55, D -> index 29 dans l'alphabet du projet
        assertEquals("z3D", Main.construireCandidat(new int[]{25, 55, 29}));
    }

    @Test
    void construireCandidat_bufferDeTailleUn() {
        assertEquals("b", Main.construireCandidat(new int[]{1}));
    }

    // ---- incrementer() ----

    @Test
    void incrementer_sansDepassement_incrementeSeulementLaDerniereCase() {
        int[] buffer = {0, 0, 0};
        Main.incrementer(buffer);
        assertArrayEquals(new int[]{0, 0, 1}, buffer);
    }

    @Test
    void incrementer_avecUneSeuleRetenue() {
        // dernier caractere de l'alphabet en position 2 -> retenue propagee en position 1
        int dernierIndice = Main.alphabet.length() - 1;
        int[] buffer = {0, 0, dernierIndice};
        Main.incrementer(buffer);
        assertArrayEquals(new int[]{0, 1, 0}, buffer);
    }

    @Test
    void incrementer_avecRetenueEnChaine() {
        // deux positions saturees d'un coup -> retenue propagee jusqu'en position 0
        int dernierIndice = Main.alphabet.length() - 1;
        int[] buffer = {0, dernierIndice, dernierIndice};
        Main.incrementer(buffer);
        assertArrayEquals(new int[]{1, 0, 0}, buffer);
    }

    @Test
    void incrementer_debordementComplet_reboucleSansPlanter() {
        // buffer deja au max partout : la retenue depasse la premiere position.
        // Comportement actuel : on rebrouille silencieusement a [0,0,0] ("aaa").
        int dernierIndice = Main.alphabet.length() - 1;
        int[] buffer = {dernierIndice, dernierIndice, dernierIndice};
        Main.incrementer(buffer);
        assertArrayEquals(new int[]{0, 0, 0}, buffer);
    }

    @Test
    void incrementer_plusieursAppels_parcourtLesPremiersCandidatsDansLOrdre() {
        int[] buffer = {0, 0, 0};

        Main.incrementer(buffer); // "aab"
        assertEquals("aab", Main.construireCandidat(buffer));

        Main.incrementer(buffer); // "aac"
        assertEquals("aac", Main.construireCandidat(buffer));
    }
}
