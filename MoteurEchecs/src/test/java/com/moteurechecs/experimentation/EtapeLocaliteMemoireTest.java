package com.moteurechecs.experimentation;

import com.moteurechecs.modele.Couleur;
import com.moteurechecs.modele.Plateau;
import com.moteurechecs.modele.PlateauBits;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EtapeLocaliteMemoireTest {

    @Test
    void enumererCasesObjetsEtBits_donnentLeMemeChecksum_positionDepart() {
        Plateau objets = Plateau.positionDepart();
        PlateauBits bits = PlateauBits.positionDepart();

        long checksumObjets = EtapeLocaliteMemoire.enumererCasesObjets(objets, Couleur.BLANC);
        long checksumBits = EtapeLocaliteMemoire.enumererCasesBits(bits, Couleur.BLANC);

        assertEquals(checksumObjets, checksumBits);
    }

    @Test
    void enumererCasesObjetsEtBits_donnentLeMemeChecksum_pourLesNoirs() {
        Plateau objets = Plateau.positionDepart();
        PlateauBits bits = PlateauBits.positionDepart();

        long checksumObjets = EtapeLocaliteMemoire.enumererCasesObjets(objets, Couleur.NOIR);
        long checksumBits = EtapeLocaliteMemoire.enumererCasesBits(bits, Couleur.NOIR);

        assertEquals(checksumObjets, checksumBits);
    }

    @Test
    void enumererCasesObjetsEtBits_coherentsApresUnCoup() {
        Plateau objets = Plateau.positionDepart().jouerCoup(new com.moteurechecs.modele.Coup(1, 4, 3, 4));
        PlateauBits bits = PlateauBits.positionDepart().jouerCoup(new com.moteurechecs.modele.Coup(1, 4, 3, 4));

        assertEquals(
                EtapeLocaliteMemoire.enumererCasesObjets(objets, Couleur.BLANC),
                EtapeLocaliteMemoire.enumererCasesBits(bits, Couleur.BLANC)
        );
    }
}
