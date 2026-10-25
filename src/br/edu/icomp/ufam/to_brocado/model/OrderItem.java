package br.edu.icomp.ufam.to_brocado.model;

import java.math.BigDecimal;

public class OrderItem {

    private final MenuItem menuItem;
    private final int quantity;
    private final BigDecimal unitPrice;

    public OrderItem(MenuItem menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.unitPrice = menuItem.getPrice();
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
