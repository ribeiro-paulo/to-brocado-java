package br.edu.icomp.ufam.to_brocado.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {

    public static final int LATE_AFTER_MINUTES = 15;

    private final int id;
    private final RestaurantTable table;
    private final List<OrderItem> items;
    private final LocalDateTime createdAt;
    private OrderStatus status;

    public Order(int id, RestaurantTable table, OrderStatus status, LocalDateTime createdAt, List<OrderItem> items) {
        this.id = id;
        this.table = table;
        this.status = status;
        this.createdAt = createdAt;
        this.items = new ArrayList<>(items);
    }

    public int getId() {
        return id;
    }

    public RestaurantTable getTable() {
        return table;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getMinutesElapsed() {
        return Duration.between(createdAt, LocalDateTime.now()).toMinutes();
    }

    public boolean isPending() {
        return status == OrderStatus.PENDING;
    }

    public boolean isLate() {
        return status.isInProduction() && getMinutesElapsed() > LATE_AFTER_MINUTES;
    }

    public void advance() {
        status = status.next();
    }
}
