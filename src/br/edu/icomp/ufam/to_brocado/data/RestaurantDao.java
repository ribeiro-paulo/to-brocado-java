package br.edu.icomp.ufam.to_brocado.data;

import br.edu.icomp.ufam.to_brocado.database.Database;
import br.edu.icomp.ufam.to_brocado.model.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

final class RestaurantDao {
    record Data(List<Waiter> waiters, List<RestaurantTable> tables, List<MenuItem> menuItems, List<Order> orders) { }
    record WaiterReportRow(long deliveredOrders, long cancelledOrders, long closedBills,
                           BigDecimal subtotal, BigDecimal serviceFee, BigDecimal total) { }

    Map<Integer, WaiterReportRow> loadWaiterReport(LocalDate from, LocalDate through) throws SQLException {
        Timestamp start = Timestamp.valueOf(from.atStartOfDay());
        Timestamp endExclusive = Timestamp.valueOf(through.plusDays(1).atStartOfDay());
        Map<Integer, WaiterReportRow> rows = new HashMap<>();
        String billsSql = "SELECT b.waiter_id, COUNT(DISTINCT b.id) AS bill_count, "
                + "COALESCE(SUM(b.subtotal),0) AS subtotal, COALESCE(SUM(b.service_fee),0) AS service_fee, "
                + "COALESCE(SUM(b.total),0) AS total, COALESCE(SUM(o.delivered_count),0) AS delivered_count "
                + "FROM bill b LEFT JOIN (SELECT bill_id, COUNT(*) AS delivered_count FROM customer_order "
                + "WHERE status='DELIVERED' GROUP BY bill_id) o ON o.bill_id=b.id "
                + "WHERE b.closed_at>=? AND b.closed_at<? GROUP BY b.waiter_id";
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(billsSql)) {
            p.setTimestamp(1, start);
            p.setTimestamp(2, endExclusive);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    rows.put(Math.toIntExact(r.getLong("waiter_id")), new WaiterReportRow(
                            r.getLong("delivered_count"), 0, r.getLong("bill_count"),
                            r.getBigDecimal("subtotal"), r.getBigDecimal("service_fee"), r.getBigDecimal("total")));
                }
            }
        }

        String cancelledSql = "SELECT waiter_id, COUNT(*) AS cancelled_count FROM customer_order "
                + "WHERE status='CANCELLED' AND cancelled_at>=? AND cancelled_at<? GROUP BY waiter_id";
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(cancelledSql)) {
            p.setTimestamp(1, start);
            p.setTimestamp(2, endExclusive);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    int id = Math.toIntExact(r.getLong("waiter_id"));
                    WaiterReportRow current = rows.getOrDefault(id,
                            new WaiterReportRow(0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
                    rows.put(id, new WaiterReportRow(current.deliveredOrders(), r.getLong("cancelled_count"),
                            current.closedBills(), current.subtotal(), current.serviceFee(), current.total()));
                }
            }
        }
        return rows;
    }

    Data load() throws SQLException {
        List<Waiter> waiters = new ArrayList<>();
        Map<Long, Waiter> waiterById = new HashMap<>();
        try (Connection c = Database.connect(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT id,name,phone,active FROM waiter ORDER BY id")) {
            while (r.next()) { Waiter w = new Waiter(Math.toIntExact(r.getLong("id")), r.getString("name"), r.getString("phone"), r.getBoolean("active")); waiters.add(w); waiterById.put(r.getLong("id"), w); }
        }
        List<RestaurantTable> tables = new ArrayList<>(); Map<Long, RestaurantTable> tableById = new HashMap<>();
        try (Connection c = Database.connect(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT id,number,seats,status,waiter_id FROM restaurant_table ORDER BY number")) {
            while (r.next()) {
                RestaurantTable t = new RestaurantTable(r.getLong("id"), r.getInt("number"), r.getInt("seats"));
                if ("OCCUPIED".equals(r.getString("status"))) t.occupy(waiterById.get(r.getLong("waiter_id")));
                tables.add(t); tableById.put(t.getId(), t);
            }
        }
        List<MenuItem> items = new ArrayList<>(); Map<Long, MenuItem> itemById = new HashMap<>();
        try (Connection c = Database.connect(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT id,name,category,price,available FROM menu_item ORDER BY id")) {
            while (r.next()) { MenuItem item = new MenuItem(Math.toIntExact(r.getLong("id")), r.getString("name"), MenuCategory.valueOf(r.getString("category")), r.getBigDecimal("price"), r.getBoolean("available")); items.add(item); itemById.put((long)item.getId(), item); }
        }
        Map<Long, List<OrderItem>> orderItems = new HashMap<>();
        try (Connection c = Database.connect(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT oi.order_id,oi.menu_item_id,oi.item_name,oi.quantity,oi.unit_price FROM order_item oi ORDER BY oi.id")) {
            while (r.next()) {
                MenuItem current = itemById.get(r.getLong("menu_item_id"));
                MenuItem item = new MenuItem(Math.toIntExact(r.getLong("menu_item_id")), r.getString("item_name"),
                        current == null ? MenuCategory.SNACKS : current.getCategory(), r.getBigDecimal("unit_price"), false);
                orderItems.computeIfAbsent(r.getLong("order_id"), _ -> new ArrayList<>()).add(new OrderItem(item, r.getInt("quantity"), r.getBigDecimal("unit_price")));
            }
        }
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT id,table_id,status,created_at FROM customer_order ORDER BY created_at DESC,id DESC";
        try (Connection c = Database.connect(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) { long id = r.getLong("id"); RestaurantTable t = tableById.get(r.getLong("table_id")); if (t != null) orders.add(new Order(Math.toIntExact(id), t, OrderStatus.valueOf(r.getString("status")), r.getTimestamp("created_at").toLocalDateTime(), orderItems.getOrDefault(id, List.of()))); }
        }
        return new Data(waiters, tables, items, orders);
    }

    int insertWaiter(String name, String phone) throws SQLException { return insert("INSERT INTO waiter(name,phone) VALUES(?,?)", name, phone); }
    void updateWaiter(Waiter w, String name, String phone) throws SQLException { execute("UPDATE waiter SET name=?,phone=? WHERE id=?", name, phone, w.getId()); }
    void setWaiterActive(Waiter w, boolean active) throws SQLException { execute("UPDATE waiter SET active=? WHERE id=?", active, w.getId()); }
    long insertTable(int number, int seats) throws SQLException { return insertLong("INSERT INTO restaurant_table(number,seats) VALUES(?,?)", number, seats); }
    void updateTable(RestaurantTable t, int number, int seats) throws SQLException { execute("UPDATE restaurant_table SET number=?,seats=? WHERE id=?", number, seats, t.getId()); }
    void deleteTable(RestaurantTable t) throws SQLException { execute("DELETE FROM restaurant_table WHERE id=?", t.getId()); }
    void setTableWaiter(RestaurantTable t, Waiter waiter) throws SQLException { execute("UPDATE restaurant_table SET status=?,waiter_id=? WHERE id=?", "OCCUPIED", waiter.getId(), t.getId()); }
    int insertMenuItem(String name, MenuCategory category, BigDecimal price, boolean available) throws SQLException { return insert("INSERT INTO menu_item(name,category,price,available) VALUES(?,?,?,?)", name, category.name(), price, available); }
    void updateMenuItem(MenuItem i, String name, MenuCategory category, BigDecimal price, boolean available) throws SQLException { execute("UPDATE menu_item SET name=?,category=?,price=?,available=? WHERE id=?", name, category.name(), price, available, i.getId()); }
    void deleteMenuItem(MenuItem i) throws SQLException { execute("DELETE FROM menu_item WHERE id=?", i.getId()); }

    int insertOrder(RestaurantTable table, List<OrderItem> items) throws SQLException {
        String orderSql = "INSERT INTO customer_order(table_id,waiter_id,status) VALUES(?,?,'PENDING')";
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                p.setLong(1, table.getId()); if (table.getWaiter() == null) p.setNull(2, Types.BIGINT); else p.setInt(2, table.getWaiter().getId()); p.executeUpdate();
                long id; try (ResultSet k = p.getGeneratedKeys()) { k.next(); id = k.getLong(1); }
                try (PreparedStatement item = c.prepareStatement("INSERT INTO order_item(order_id,menu_item_id,item_name,quantity,unit_price) VALUES(?,?,?,?,?)")) {
                    for (OrderItem oi : items) { item.setLong(1,id); item.setInt(2,oi.getMenuItem().getId()); item.setString(3,oi.getMenuItem().getName()); item.setInt(4,oi.getQuantity()); item.setBigDecimal(5,oi.getUnitPrice()); item.addBatch(); }
                    item.executeBatch();
                }
                c.commit(); return Math.toIntExact(id);
            } catch (SQLException e) { c.rollback(); throw e; }
        }
    }
    void updateOrder(Order o) throws SQLException { execute("UPDATE customer_order SET status=?,cancelled_at=IF(?='CANCELLED',CURRENT_TIMESTAMP,NULL) WHERE id=?", o.getStatus().name(), o.getStatus().name(), o.getId()); }
    void cancelOrder(Order o) throws SQLException { execute("UPDATE customer_order SET status='CANCELLED',cancelled_at=CURRENT_TIMESTAMP WHERE id=?", o.getId()); }
    void closeAccount(RestaurantTable t, boolean includeService, int people) throws SQLException {
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            try {
                BigDecimal subtotal;
                try (PreparedStatement p = c.prepareStatement("SELECT id FROM customer_order WHERE table_id=? AND bill_id IS NULL AND status='DELIVERED' FOR UPDATE")) { p.setLong(1,t.getId()); try (ResultSet r=p.executeQuery()) { while (r.next()) { } } }
                try (PreparedStatement p = c.prepareStatement("SELECT COALESCE(SUM(oi.quantity*oi.unit_price),0) FROM customer_order o JOIN order_item oi ON oi.order_id=o.id WHERE o.table_id=? AND o.bill_id IS NULL AND o.status='DELIVERED'")) { p.setLong(1,t.getId()); try (ResultSet r=p.executeQuery()) { r.next(); subtotal=r.getBigDecimal(1); } }
                BigDecimal fee = includeService ? subtotal.multiply(new BigDecimal("0.1000")).setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2);
                BigDecimal total=subtotal.add(fee), per=total.divide(BigDecimal.valueOf(people),2,java.math.RoundingMode.HALF_UP);
                try (PreparedStatement p=c.prepareStatement("INSERT INTO bill(table_id,table_number,waiter_id,subtotal,service_included,service_rate,service_fee,total,people,per_person) SELECT id,number,waiter_id,?,?,?,?,?,?,? FROM restaurant_table WHERE id=?")) {
                    p.setBigDecimal(1,subtotal); p.setBoolean(2,includeService); p.setBigDecimal(3,new BigDecimal("0.1000")); p.setBigDecimal(4,fee); p.setBigDecimal(5,total); p.setInt(6,people); p.setBigDecimal(7,per); p.setLong(8,t.getId()); p.executeUpdate();
                }
                long billId; try (Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT LAST_INSERT_ID()")) { r.next(); billId=r.getLong(1); }
                try (PreparedStatement p=c.prepareStatement("UPDATE customer_order SET bill_id=? WHERE table_id=? AND status='DELIVERED' AND bill_id IS NULL")) { p.setLong(1,billId); p.setLong(2,t.getId()); p.executeUpdate(); }
                try (PreparedStatement p=c.prepareStatement("UPDATE restaurant_table SET status='FREE',waiter_id=NULL WHERE id=?")) { p.setLong(1,t.getId()); p.executeUpdate(); }
                c.commit();
            } catch (SQLException e) { c.rollback(); throw e; }
        }
    }
    private int insert(String sql,Object... args) throws SQLException { return Math.toIntExact(insertLong(sql,args)); }
    private long insertLong(String sql,Object... args) throws SQLException { try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){ bind(p,args);p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){r.next();return r.getLong(1);} } }
    private void execute(String sql,Object...args)throws SQLException{try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement(sql)){bind(p,args);p.executeUpdate();}}
    private void bind(PreparedStatement p,Object...args)throws SQLException{for(int i=0;i<args.length;i++)p.setObject(i+1,args[i]);}
}
