package br.edu.icomp.ufam.to_brocado.data;

import br.edu.icomp.ufam.to_brocado.model.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

public class RestaurantData {
    private final RestaurantDao dao = new RestaurantDao();
    private final List<Waiter> waiters = new ArrayList<>();
    private final List<RestaurantTable> tables = new ArrayList<>();
    private final List<MenuItem> menuItems = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();

    public RestaurantData() {
        try {
            RestaurantDao.Data data = dao.load();
            waiters.addAll(data.waiters()); tables.addAll(data.tables()); menuItems.addAll(data.menuItems()); orders.addAll(data.orders());
        } catch (SQLException e) { throw failure("Não foi possível carregar os dados. Verifique o MySQL e a configuração JDBC.", e); }
    }
    public List<Waiter> getWaiters() { return Collections.unmodifiableList(waiters); }
    public List<Waiter> getActiveWaiters() { return waiters.stream().filter(Waiter::isActive).toList(); }
    public Waiter addWaiter(String name,String phone){try{Waiter w=new Waiter(dao.insertWaiter(name,phone),name,phone);waiters.add(w);return w;}catch(SQLException e){throw failure("Não foi possível cadastrar o garçom.",e);}}
    public void updateWaiter(Waiter w,String name,String phone){try{dao.updateWaiter(w,name,phone);w.update(name,phone);}catch(SQLException e){throw failure("Não foi possível atualizar o garçom.",e);}}
    public void setWaiterActive(Waiter w, boolean active){if(!active&&countTablesOf(w)>0)throw new IllegalStateException("O garçom atende mesas ocupadas e não pode ser inativado agora.");try{dao.setWaiterActive(w,active);w.setActive(active);}catch(SQLException e){throw failure("Não foi possível atualizar a situação do garçom.",e);}}
    public RestaurantTable addTable(int number,int seats){try{RestaurantTable t=new RestaurantTable(dao.insertTable(number,seats),number,seats);tables.add(t);sortTables();return t;}catch(SQLException e){throw failure("Não foi possível cadastrar a mesa.",e);}}
    public void updateTable(RestaurantTable t,int number,int seats){try{dao.updateTable(t,number,seats);t.update(number,seats);sortTables();}catch(SQLException e){throw failure("Não foi possível atualizar a mesa.",e);}}
    public void removeTable(RestaurantTable t){try{dao.deleteTable(t);tables.remove(t);}catch(SQLException e){throw failure("Não foi possível remover a mesa. Ela possui histórico associado.",e);}}
    public long countTablesOf(Waiter w){return tables.stream().filter(t->t.getWaiter()!=null&&t.getWaiter().getId()==w.getId()).count();}
    public List<RestaurantTable> getTables(){return Collections.unmodifiableList(tables);}
    public List<MenuItem> getMenuItems(){return Collections.unmodifiableList(menuItems);}
    public MenuItem addMenuItem(String name,MenuCategory category,BigDecimal price,boolean available){try{MenuItem i=new MenuItem(dao.insertMenuItem(name,category,price,available),name,category,price,available);menuItems.add(i);return i;}catch(SQLException e){throw failure("Não foi possível cadastrar o item.",e);}}
    public void updateMenuItem(MenuItem i,String name,MenuCategory category,BigDecimal price,boolean available){try{dao.updateMenuItem(i,name,category,price,available);i.update(name,category,price,available);}catch(SQLException e){throw failure("Não foi possível atualizar o item.",e);}}
    public void removeMenuItem(MenuItem i){try{dao.deleteMenuItem(i);menuItems.remove(i);}catch(SQLException e){throw failure("O item não pode ser removido porque já está em pedidos.",e);}}
    public Order addOrder(RestaurantTable t,List<OrderItem> items){if(t==null||!t.isOccupied())throw new IllegalArgumentException("Uma mesa ocupada é necessária.");if(items==null||items.isEmpty())throw new IllegalArgumentException("Adicione itens ao pedido.");try{Order o=new Order(dao.insertOrder(t,items),t,OrderStatus.PENDING,java.time.LocalDateTime.now(),items);orders.add(o);return o;}catch(SQLException e){throw failure("Não foi possível registrar o pedido.",e);}}
    public void removeOrder(Order o){try{dao.cancelOrder(o);o.setStatus(OrderStatus.CANCELLED);}catch(SQLException e){throw failure("Não foi possível cancelar o pedido.",e);}}
    public void advanceOrder(Order o){OrderStatus old=o.getStatus();o.advance();try{dao.updateOrder(o);}catch(SQLException e){o.setStatus(old);throw failure("Não foi possível atualizar o pedido.",e);}}
    public List<Order> getOrders(){return Collections.unmodifiableList(orders);}
    public List<Order> getOrdersForTable(RestaurantTable t){return orders.stream().filter(o->o.getTable().getId()==t.getId()).toList();}
    public boolean hasUndeliveredOrders(RestaurantTable t){return getOrdersForTable(t).stream().anyMatch(o->!o.getStatus().isFinal());}
    public void occupyTable(RestaurantTable t,Waiter w){if(w==null||!w.isActive())throw new IllegalArgumentException("Selecione um garçom ativo.");try{dao.setTableWaiter(t,w);t.occupy(w);}catch(SQLException e){throw failure("Não foi possível ocupar a mesa.",e);}}
    public Bill closeAccount(RestaurantTable t,boolean includeService,int people){if(t==null||!t.isOccupied())throw new IllegalArgumentException("Uma mesa ocupada é necessária.");if(hasUndeliveredOrders(t))throw new IllegalStateException("Há pedidos não entregues.");BigDecimal subtotal=getOrdersForTable(t).stream().filter(o->o.getStatus()==OrderStatus.DELIVERED).map(Order::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);Bill bill=new Bill(subtotal,includeService,people);try{dao.closeAccount(t,includeService,people);t.release();return bill;}catch(SQLException e){throw failure("Não foi possível fechar a conta.",e);}}
    private void sortTables(){tables.sort(Comparator.comparingInt(RestaurantTable::getNumber));}
    private static IllegalStateException failure(String message,SQLException cause){return new IllegalStateException(message+" Detalhe: "+cause.getMessage(),cause);}
}
