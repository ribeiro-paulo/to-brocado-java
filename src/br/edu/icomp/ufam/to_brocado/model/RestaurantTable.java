package br.edu.icomp.ufam.to_brocado.model;

public class RestaurantTable {

    private int number;
    private long id;
    private int seats;
    private TableStatus status = TableStatus.FREE;
    private Waiter waiter;

    public RestaurantTable(int number, int seats) {
        this(0, number, seats);
    }

    public RestaurantTable(long id, int number, int seats) {
        this.id = id;
        this.number = number;
        this.seats = seats;
    }

    public int getNumber() {
        return number;
    }

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

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

    public void update(int number, int seats) {
        this.number = number;
        this.seats = seats;
    }

    @Override
    public String toString() {
        return "Mesa " + number;
    }
}
