package br.edu.icomp.ufam.to_brocado.model;

import java.math.BigDecimal;

public class MenuItem {

    private final int id;
    private String name;
    private MenuCategory category;
    private BigDecimal price;
    private boolean available;

    public MenuItem(int id, String name, MenuCategory category, BigDecimal price, boolean available) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.available = available;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MenuCategory getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void update(String name, MenuCategory category, BigDecimal price, boolean available) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.available = available;
    }
}
