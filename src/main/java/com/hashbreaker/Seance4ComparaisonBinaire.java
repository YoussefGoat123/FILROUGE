package com.hashbreaker;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

/**
 * Seance 4 - Partie 3 : Comparaison Binaire 64-bit.
 *
 * Repart de Seance3ZeroAllocation.java (deja zero-allocation) et remplace
 * MessageDigest.isEqual(byte[], byte[]) par une comparaison manuelle par
 * mots de 64 bits (long), avec DEUX gains cumules :
 *
 *   1. Moins d'operations : 4 comparaisons de long au lieu de 32 comparaisons
 *      d'octets (ou d'une boucle interne equivalente).
 *   2. Sortie anticipee (early exit) des le premier mot different.
 *      MessageDigest.isEqual() est volontairement a TEMPS CONSTANT (jamais
 *      de sortie anticipee), une protection anti-timing-attack utile pour
 *      comparer un secret utilisateur -- inutile ici, on compare un hash
 *      qu'on vient de calculer nous-memes, pas un secret externe. La quasi
 *      totalite des candidats echouent des le premier octet : sortir tot
 *      evite de comparer les 31 octets restants pour rien.
 */
public class Seance4ComparaisonBinaire {

    public static void main(String[] args) throws NoSuchAlgorithmException, DigestException {
        craquer("z3D", "a532ca5e11e2b06ccc911e0d962a4864cdb87da05723f3a050a376d0f0895e63", 3);
        craquer("Sh3n", "bd7d0ea8cf7ade4a446ba4efc46fd99071ec3f423770991ac51f70ec5a894dc7", 4);

        // Niveau 3 - Saturation. "@kAl1" est entierement couvert par notre alphabet
        // actuel (63 symboles incluant '@') : pas besoin d'etendre a 70+ symboles
        // pour cette cible precise, meme si le niveau complet le suggere.
        craquer("@kAl1", "b96ec5f74610e96c808a6f062190085adeddeefe085b56cc768f551b4ab641a5", 5);
    }

    static void craquer(String nomCible, String hashCibleHex, int longueur) throws NoSuchAlgorithmException, DigestException {
        System.out.println("Recherche de la cible (comparaison binaire 64-bit) : " + nomCible);

        long debut = System.currentTimeMillis();

        // decode le hash cible en 4 mots de 64 bits, une seule fois avant la boucle
        byte[] hashCibleOctets = Seance3ZeroAllocation.hexVersOctets(hashCibleHex);
        long[] hashCibleMots = octetsVersLongs(hashCibleOctets);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] candidatBuffer = new byte[longueur];
        int[] compteur = new int[longueur];
        Seance3ZeroAllocation.remplirCandidat(candidatBuffer, compteur);

        byte[] hashBuffer = new byte[digest.getDigestLength()];

        boolean trouve = false;

        while (!trouve) {

            digest.update(candidatBuffer);
            digest.digest(hashBuffer, 0, hashBuffer.length);

            if (egalise64bit(hashBuffer, hashCibleMots)) {
                trouve = true;
                long fin = System.currentTimeMillis();
                String candidatTrouve = new String(candidatBuffer, StandardCharsets.US_ASCII);
                System.out.println("Mot de passe trouve : " + candidatTrouve);
                System.out.println("Temps ecoule : " + (fin - debut) + " ms");
            } else {
                Main.incrementer(compteur);
                Seance3ZeroAllocation.remplirCandidat(candidatBuffer, compteur);
            }
        }

        System.out.println();
    }

    // compare un hash calcule (byte[32]) a une cible pre-decodee en 4 mots de 64 bits.
    // sortie anticipee des le premier mot different -- pas de temps constant, volontairement.
    static boolean egalise64bit(byte[] hashCalcule, long[] hashCibleMots) {
        for (int i = 0; i < hashCibleMots.length; i++) {
            long mot = 0;
            int base = i * 8;
            for (int j = 0; j < 8; j++) {
                mot = (mot << 8) | (hashCalcule[base + j] & 0xFFL);
            }
            if (mot != hashCibleMots[i]) {
                return false;
            }
        }
        return true;
    }

    // convertit un tableau d'octets (taille multiple de 8) en mots de 64 bits.
    // appele une seule fois, hors boucle chaude -- l'allocation ici est sans consequence.
    static long[] octetsVersLongs(byte[] octets) {
        long[] mots = new long[octets.length / 8];
        for (int i = 0; i < mots.length; i++) {
            long mot = 0;
            int base = i * 8;
            for (int j = 0; j < 8; j++) {
                mot = (mot << 8) | (octets[base + j] & 0xFFL);
            }
            mots[i] = mot;
        }
        return mots;
    }
}
