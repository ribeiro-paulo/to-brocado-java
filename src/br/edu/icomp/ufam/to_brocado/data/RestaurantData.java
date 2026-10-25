package br.edu.icomp.ufam.to_brocado.data;

import br.edu.icomp.ufam.to_brocado.model.MenuCategory;
import br.edu.icomp.ufam.to_brocado.model.MenuItem;
import br.edu.icomp.ufam.to_brocado.model.Order;
import br.edu.icomp.ufam.to_brocado.model.OrderItem;
import br.edu.icomp.ufam.to_brocado.model.OrderStatus;
import br.edu.icomp.ufam.to_brocado.model.RestaurantTable;
import br.edu.icomp.ufam.to_brocado.model.Waiter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RestaurantData {

    private final List<Waiter> waiters = new ArrayList<>();
    private final List<RestaurantTable> tables = new ArrayList<>();
    private final List<MenuItem> menuItems = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private int nextWaiterId = 1;
    private int nextMenuItemId = 1;

    public RestaurantData() {
        loadSampleData();
    }

    public List<Waiter> getWaiters() {
        return Collections.unmodifiableList(waiters);
    }

    public Waiter addWaiter(String name, String phone) {
        Waiter waiter = new Waiter(nextWaiterId++, name, phone);
        waiters.add(waiter);
        return waiter;
    }

    public void removeWaiter(Waiter waiter) {
        waiters.remove(waiter);
    }

    public long countTablesOf(Waiter waiter) {
        return tables.stream().filter(table -> table.getWaiter() == waiter).count();
    }

    public List<RestaurantTable> getTables() {
        return Collections.unmodifiableList(tables);
    }

    public List<MenuItem> getMenuItems() {
        return Collections.unmodifiableList(menuItems);
    }

    public MenuItem addMenuItem(String name, MenuCategory category, BigDecimal price, boolean available) {
        MenuItem item = new MenuItem(nextMenuItemId++, name, category, price, available);
        menuItems.add(item);
        return item;
    }

    public void removeMenuItem(MenuItem item) {
        menuItems.remove(item);
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    public List<Order> getOrdersForTable(RestaurantTable table) {
        return orders.stream().filter(order -> order.getTable() == table).toList();
    }

    public boolean hasUndeliveredOrders(RestaurantTable table) {
        return orders.stream()
                .anyMatch(order -> order.getTable() == table && !order.getStatus().isFinal());
    }

    public void closeTable(RestaurantTable table) {
        orders.removeIf(order -> order.getTable() == table);
        table.release();
    }

    private void addOrder(RestaurantTable table, OrderStatus status, int minutesAgo, OrderItem... items) {
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(minutesAgo);
        orders.add(new Order(orders.size() + 1, table, status, createdAt, List.of(items)));
    }

    private void loadSampleData() {
        Waiter carlos = addWaiter("Carlos Silva", "92991110001");
        Waiter marina = addWaiter("Marina Souza", "92991110002");
        Waiter joao = addWaiter("João Pereira", "92991110003");

        int[] seatsPerTable = {2, 2, 4, 4, 4, 6, 6, 8};
        for (int i = 0; i < seatsPerTable.length; i++) {
            tables.add(new RestaurantTable(i + 1, seatsPerTable[i]));
        }
        tables.get(0).occupy(carlos);
        tables.get(2).occupy(marina);
        tables.get(3).occupy(joao);
        tables.get(5).occupy(carlos);

        MenuItem burger = addMenuItem("X-Burger", MenuCategory.SNACKS, new BigDecimal("22.00"), true);
        MenuItem salad = addMenuItem("X-Salada", MenuCategory.SNACKS, new BigDecimal("24.00"), true);
        MenuItem bacon = addMenuItem("X-Bacon", MenuCategory.SNACKS, new BigDecimal("27.50"), true);
        MenuItem fries = addMenuItem("Batata frita", MenuCategory.SIDES, new BigDecimal("18.00"), true);
        MenuItem onionRings = addMenuItem("Anéis de cebola", MenuCategory.SIDES, new BigDecimal("20.00"), true);
        MenuItem soda = addMenuItem("Refrigerante", MenuCategory.DRINKS, new BigDecimal("7.00"), true);
        MenuItem juice = addMenuItem("Suco natural", MenuCategory.DRINKS, new BigDecimal("9.00"), true);
        addMenuItem("Água mineral", MenuCategory.DRINKS, new BigDecimal("4.00"), true);
        addMenuItem("Pudim", MenuCategory.DESSERTS, new BigDecimal("12.00"), true);
        addMenuItem("Milkshake", MenuCategory.DESSERTS, new BigDecimal("16.00"), false);

        addOrder(tables.get(0), OrderStatus.DELIVERED, 40, new OrderItem(burger, 2), new OrderItem(soda, 2));
        addOrder(tables.get(0), OrderStatus.PREPARING, 18, new OrderItem(fries, 1));
        addOrder(tables.get(2), OrderStatus.PENDING, 4, new OrderItem(salad, 1), new OrderItem(juice, 1));
        addOrder(tables.get(3), OrderStatus.PENDING, 20, new OrderItem(bacon, 2), new OrderItem(juice, 2));
        addOrder(tables.get(5), OrderStatus.READY, 8, new OrderItem(onionRings, 1), new OrderItem(soda, 3));
        addOrder(tables.get(2), OrderStatus.DELIVERED, 25, new OrderItem(soda, 2));
    }
}
