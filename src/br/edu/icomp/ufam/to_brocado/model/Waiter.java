package br.edu.icomp.ufam.to_brocado.model;

public class Waiter {

    private final int id;
    private String name;
    private String phone;
    private boolean active;

    public Waiter(int id, String name, String phone) {
        this(id, name, phone, true);
    }

    public Waiter(int id, String name, String phone, boolean active) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isActive() {
        return active;
    }

    public void update(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return name;
    }
}
