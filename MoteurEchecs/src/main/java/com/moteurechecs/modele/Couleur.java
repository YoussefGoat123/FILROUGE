package com.moteurechecs.modele;

public enum Couleur {
    BLANC, NOIR;

    public Couleur adverse() {
        return this == BLANC ? NOIR : BLANC;
    }
}
