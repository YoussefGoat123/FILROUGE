package com.hashbreaker;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

/**
 * Seance 3 - Partie 3 : Buffers Fixes (Zero-Allocation).
 *
 * Version corrigee de Main.craquer(), ciblant precisement ce que le diagnostic
 * JFR (Partie 1) a identifie comme responsable de ~94% des allocations :
 *
 *   1. hashCible decode en byte[] UNE SEULE FOIS avant la boucle
 *      (au lieu de comparer des String hexadecimales a chaque tentative)
 *   2. une seule instance de MessageDigest, reutilisee
 *      (au lieu de MessageDigest.getInstance() a chaque tentative)
 *   3. le candidat est un byte[] mute par indice, jamais une String
 *      (au lieu de construireCandidat() qui concatene)
 *   4. le hash resultat est ecrit dans un buffer reutilise via
 *      digest(byte[], int, int), jamais un nouveau byte[] par tentative
 *      (au lieu de digest(byte[]) qui alloue a chaque appel)
 *
 * La conversion en String/hexadecimal n'a lieu qu'une seule fois,
 * seulement si le mot de passe est trouve (donc hors boucle chaude).
 */
public class Seance3ZeroAllocation {

    public static void main(String[] args) throws NoSuchAlgorithmException, DigestException {
        craquer("z3D", "a532ca5e11e2b06ccc911e0d962a4864cdb87da05723f3a050a376d0f0895e63", 3);
        craquer("Sh3n", "bd7d0ea8cf7ade4a446ba4efc46fd99071ec3f423770991ac51f70ec5a894dc7", 4);
    }

    static void craquer(String nomCible, String hashCibleHex, int longueur) throws NoSuchAlgorithmException, DigestException {
        System.out.println("Recherche de la cible (zero-allocation) : " + nomCible);

        long debut = System.currentTimeMillis();

        // decode le hash cible en octets une seule fois, avant la boucle
        byte[] hashCibleOctets = hexVersOctets(hashCibleHex);

        // une seule instance de MessageDigest, reutilisee a chaque tentative
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // buffer candidat reutilise, mute par indice (jamais de String par tentative)
        byte[] candidatBuffer = new byte[longueur];
        int[] compteur = new int[longueur];
        remplirCandidat(candidatBuffer, compteur);

        // buffer de sortie du hash, reutilise (SHA-256 fait toujours 32 octets)
        byte[] hashBuffer = new byte[digest.getDigestLength()];

        boolean trouve = false;

        while (!trouve) {

            // calcule le hash directement dans hashBuffer, sans nouvelle allocation
            digest.update(candidatBuffer);
            digest.digest(hashBuffer, 0, hashBuffer.length);

            if (MessageDigest.isEqual(hashBuffer, hashCibleOctets)) {
                trouve = true;
                long fin = System.currentTimeMillis();
                String candidatTrouve = new String(candidatBuffer, StandardCharsets.US_ASCII);
                System.out.println("Mot de passe trouve : " + candidatTrouve);
                System.out.println("Temps ecoule : " + (fin - debut) + " ms");
            } else {
                Main.incrementer(compteur);
                remplirCandidat(candidatBuffer, compteur);
            }
        }

        System.out.println();
    }

    // remplit le buffer candidat par indice a partir du compteur base-N, sans allocation
    static void remplirCandidat(byte[] candidatBuffer, int[] compteur) {
        for (int i = 0; i < compteur.length; i++) {
            candidatBuffer[i] = (byte) Main.alphabet.charAt(compteur[i]);
        }
    }

    // decode une chaine hexadecimale en tableau d'octets (appele une seule fois, hors boucle chaude)
    static byte[] hexVersOctets(String hex) {
        byte[] octets = new byte[hex.length() / 2];
        for (int i = 0; i < octets.length; i++) {
            int poidsForts = Character.digit(hex.charAt(i * 2), 16);
            int poidsFaibles = Character.digit(hex.charAt(i * 2 + 1), 16);
            octets[i] = (byte) ((poidsForts << 4) + poidsFaibles);
        }
        return octets;
    }
}
