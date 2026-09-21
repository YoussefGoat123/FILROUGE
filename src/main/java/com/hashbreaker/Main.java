package com.hashbreaker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Point d'entrée du craqueur HashBreaker.
 * Séance 1 : générateur combinatoire (compteur base-N) + SHA-256, résolution des cibles z3D et Sh3n.
 *
 * Version NAIVE volontairement : on recrée des String à chaque essai.
 * Ce sera optimisé dans les prochaines séances (zéro-allocation, etc.).
 */
public class Main {

    static String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static void main(String[] args) throws NoSuchAlgorithmException {

        // Niveau 1 - Validation
        craquer("z3D", "a532ca5e11e2b06ccc911e0d962a4864cdb87da05723f3a050a376d0f0895e63", 3);

        // Niveau 2 - Benchmark (baseline chronometree)
        craquer("Sh3n", "bd7d0ea8cf7ade4a446ba4efc46fd99071ec3f423770991ac51f70ec5a894dc7", 4);
    }

    // tente de retrouver le mot de passe correspondant a hashCible par force brute
    static void craquer(String nomCible, String hashCible, int longueur) throws NoSuchAlgorithmException {
        System.out.println("Recherche de la cible : " + nomCible);

        long debut = System.currentTimeMillis();

        // buffer d'indices dans l'alphabet, commence a [0,0,...,0] = "aaa..."
        int[] buffer = new int[longueur];

        boolean trouve = false;

        while (!trouve) {

            // 1. on construit le mot candidat a partir du buffer
            String candidat = construireCandidat(buffer);

            // 2. on calcule son hash SHA-256
            String hashCandidat = sha256(candidat);

            // 3. on compare au hash cible
            if (hashCandidat.equals(hashCible)) {
                trouve = true;
                long fin = System.currentTimeMillis();
                System.out.println("Mot de passe trouve : " + candidat);
                System.out.println("Temps ecoule : " + (fin - debut) + " ms");
            } else {
                // 4. sinon on passe au candidat suivant (compteur base-N avec retenue)
                incrementer(buffer);
            }
        }

        System.out.println();
    }

    // construit le mot candidat en piochant chaque caractere dans l'alphabet
    static String construireCandidat(int[] buffer) {
        String candidat = "";
        for (int i = 0; i < buffer.length; i++) {
            candidat = candidat + alphabet.charAt(buffer[i]);
        }
        return candidat;
    }

    // ajoute 1 au buffer, position la plus a droite d'abord, avec retenue si depassement
    static void incrementer(int[] buffer) {
        int position = buffer.length - 1;

        while (position >= 0) {
            buffer[position] = buffer[position] + 1;

            if (buffer[position] < alphabet.length()) {
                // pas de depassement sur cette position, on s'arrete la
                return;
            }

            // depassement : cette position repasse a 0 et on propage la retenue a gauche
            buffer[position] = 0;
            position = position - 1;
        }
    }

    // calcule le sha256 d'un texte et le renvoie en hexadecimal
    static String sha256(String texte) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] resultat = digest.digest(texte.getBytes());

        // conversion des octets en chaine hexadecimale, octet par octet
        String hex = "";
        for (byte b : resultat) {
            String morceau = Integer.toHexString(0xff & b);
            if (morceau.length() == 1) {
                morceau = "0" + morceau;
            }
            hex = hex + morceau;
        }
        return hex;
    }
}
