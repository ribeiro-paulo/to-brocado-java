package br.edu.icomp.ufam.to_brocado.model;

public class Waiter {

    private final int id;
    private String name;
    private String phone;

    public Waiter(int id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
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

    public void update(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    @Override
    public String toString() {
        return name;
    }
}
