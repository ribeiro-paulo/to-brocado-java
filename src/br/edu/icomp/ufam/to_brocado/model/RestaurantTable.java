package br.edu.icomp.ufam.to_brocado.model;

public class RestaurantTable {

    private final int number;
    private final int seats;
    private TableStatus status = TableStatus.FREE;
    private Waiter waiter;

    public RestaurantTable(int number, int seats) {
        this.number = number;
        this.seats = seats;
    }

    public int getNumber() {
        return number;
    }

    public int getSeats() {
        return seats;
    }

    public TableStatus getStatus() {
        return status;
    }

    public Waiter getWaiter() {
        return waiter;
    }

    public boolean isOccupied() {
        return status == TableStatus.OCCUPIED;
    }

    public void occupy(Waiter waiter) {
        this.waiter = waiter;
        this.status = TableStatus.OCCUPIED;
    }

    public void release() {
        this.waiter = null;
        this.status = TableStatus.FREE;
    }

    @Override
    public String toString() {
        return "Mesa " + number;
    }
}
