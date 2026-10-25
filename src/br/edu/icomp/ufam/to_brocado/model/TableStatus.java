package br.edu.icomp.ufam.to_brocado.model;

public enum TableStatus {

    FREE("Livre"),
    OCCUPIED("Ocupada");

    private final String label;

    TableStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
