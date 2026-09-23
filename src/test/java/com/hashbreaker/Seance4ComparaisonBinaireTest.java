package com.hashbreaker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests de la Seance 4 - Partie 3 (comparaison binaire 64-bit).
 */
class Seance4ComparaisonBinaireTest {

    // ---- octetsVersLongs() ----

    @Test
    void octetsVersLongs_convertitUnMotDe8Octets() {
        byte[] octets = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
        long[] mots = Seance4ComparaisonBinaire.octetsVersLongs(octets);
        assertArrayEquals(new long[]{1L}, mots);
    }

    @Test
    void octetsVersLongs_hashDe32Octets_donne4Mots() {
        byte[] octets = Seance3ZeroAllocation.hexVersOctets(
                "a532ca5e11e2b06ccc911e0d962a4864cdb87da05723f3a050a376d0f0895e63");
        long[] mots = Seance4ComparaisonBinaire.octetsVersLongs(octets);
        assertEquals(4, mots.length);
    }

    // ---- egalise64bit() ----

    @Test
    void egalise64bit_hashesIdentiques_renvoieTrue() {
        byte[] octets = Seance3ZeroAllocation.hexVersOctets(
                "a532ca5e11e2b06ccc911e0d962a4864cdb87da05723f3a050a376d0f0895e63");
        long[] mots = Seance4ComparaisonBinaire.octetsVersLongs(octets);
        assertTrue(Seance4ComparaisonBinaire.egalise64bit(octets, mots));
    }

    @Test
    void egalise64bit_premierOctetDifferent_renvoieFalse() {
        byte[] cible = Seance3ZeroAllocation.hexVersOctets(
                "a532ca5e11e2b06ccc911e0d962a4864cdb87da05723f3a050a376d0f0895e63");
        long[] motsCible = Seance4ComparaisonBinaire.octetsVersLongs(cible);

        byte[] autre = cible.clone();
        autre[0] = (byte) (autre[0] ^ 0xFF); // change le tout premier octet

        assertFalse(Seance4ComparaisonBinaire.egalise64bit(autre, motsCible));
    }

    @Test
    void egalise64bit_dernierOctetDifferent_renvoieFalse() {
        byte[] cible = Seance3ZeroAllocation.hexVersOctets(
                "a532ca5e11e2b06ccc911e0d962a4864cdb87da05723f3a050a376d0f0895e63");
        long[] motsCible = Seance4ComparaisonBinaire.octetsVersLongs(cible);

        byte[] autre = cible.clone();
        autre[autre.length - 1] = (byte) (autre[autre.length - 1] ^ 0xFF); // change le tout dernier octet

        assertFalse(Seance4ComparaisonBinaire.egalise64bit(autre, motsCible));
    }

    // ---- coherence avec les hashs reels du projet (via un vrai calcul SHA-256) ----

    @Test
    void egalise64bit_coherentAvecUnVraiCalculSha256() throws Exception {
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hashReel = digest.digest("z3D".getBytes(java.nio.charset.StandardCharsets.US_ASCII));

        String hashHexAttendu = Main.sha256("z3D");
        byte[] hashCibleOctets = Seance3ZeroAllocation.hexVersOctets(hashHexAttendu);
        long[] hashCibleMots = Seance4ComparaisonBinaire.octetsVersLongs(hashCibleOctets);

        assertTrue(Seance4ComparaisonBinaire.egalise64bit(hashReel, hashCibleMots));
    }
}
