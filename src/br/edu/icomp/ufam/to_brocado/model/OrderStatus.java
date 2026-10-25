package br.edu.icomp.ufam.to_brocado.model;

public enum OrderStatus {

    PENDING("Pendente"),
    PREPARING("Em preparo"),
    READY("Pronto"),
    DELIVERED("Entregue");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isInProduction() {
        return this == PENDING || this == PREPARING;
    }

    public boolean isFinal() {
        return this == DELIVERED;
    }

    public OrderStatus next() {
        OrderStatus[] all = values();
        return all[Math.min(ordinal() + 1, all.length - 1)];
    }
}
