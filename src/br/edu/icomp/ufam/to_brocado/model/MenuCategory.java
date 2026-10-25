package br.edu.icomp.ufam.to_brocado.model;

public enum MenuCategory {

    SNACKS("Lanches"),
    SIDES("Porções"),
    DRINKS("Bebidas"),
    DESSERTS("Sobremesas");

    private final String label;

    MenuCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
