package com.hashbreaker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests de la Seance 3 - Partie 3 (buffers fixes, zero-allocation).
 */
class Seance3ZeroAllocationTest {

    // ---- hexVersOctets() ----

    @Test
    void hexVersOctets_convertitCorrectement() {
        byte[] resultat = Seance3ZeroAllocation.hexVersOctets("a532ca5e");
        assertArrayEquals(new byte[]{(byte) 0xa5, (byte) 0x32, (byte) 0xca, (byte) 0x5e}, resultat);
    }

    @Test
    void hexVersOctets_tailleCorrecte() {
        // un hash SHA-256 fait 64 caracteres hex -> 32 octets
        byte[] resultat = Seance3ZeroAllocation.hexVersOctets(
                "a532ca5e11e2b06ccc911e0d962a4864cdb87da05723f3a050a376d0f0895e63");
        assertEquals(32, resultat.length);
    }

    // ---- remplirCandidat() ----

    @Test
    void remplirCandidat_bufferZero_donneAaa() {
        byte[] buffer = new byte[3];
        Seance3ZeroAllocation.remplirCandidat(buffer, new int[]{0, 0, 0});
        assertEquals("aaa", new String(buffer, java.nio.charset.StandardCharsets.US_ASCII));
    }

    @Test
    void remplirCandidat_indicesDeZ3D_donneZ3D() {
        byte[] buffer = new byte[3];
        Seance3ZeroAllocation.remplirCandidat(buffer, new int[]{25, 55, 29});
        assertEquals("z3D", new String(buffer, java.nio.charset.StandardCharsets.US_ASCII));
    }

    @Test
    void remplirCandidat_reutiliseLeMemeBuffer_sansNouvelleAllocation() {
        byte[] buffer = new byte[3];

        Seance3ZeroAllocation.remplirCandidat(buffer, new int[]{0, 0, 0});
        assertEquals("aaa", new String(buffer, java.nio.charset.StandardCharsets.US_ASCII));

        Seance3ZeroAllocation.remplirCandidat(buffer, new int[]{0, 0, 1});
        assertEquals("aab", new String(buffer, java.nio.charset.StandardCharsets.US_ASCII));
    }

    // ---- coherence avec la version naive (Main) ----

    @Test
    void hexVersOctets_estCoherentAvecLeHashCibleDeZ3D() throws Exception {
        // le hash hex de "z3D" (Main) reconverti en octets doit correspondre
        // au vrai digest SHA-256 de "z3D"
        String hashHex = Main.sha256("z3D");
        byte[] octetsAttendus = Seance3ZeroAllocation.hexVersOctets(hashHex);

        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] octetsReels = digest.digest("z3D".getBytes(java.nio.charset.StandardCharsets.US_ASCII));

        assertArrayEquals(octetsAttendus, octetsReels);
    }
}
