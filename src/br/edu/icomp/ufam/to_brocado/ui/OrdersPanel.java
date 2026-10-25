package br.edu.icomp.ufam.to_brocado.ui;

import br.edu.icomp.ufam.to_brocado.data.RestaurantData;
import br.edu.icomp.ufam.to_brocado.model.Order;
import br.edu.icomp.ufam.to_brocado.model.OrderItem;
import br.edu.icomp.ufam.to_brocado.model.OrderStatus;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

class OrdersPanel extends ScreenPanel {

    private static final long serialVersionUID = 1L;
	private static final String FILTER_ALL = "Todos os pedidos";
    private static final String FILTER_PENDING = "Pendentes";
    private static final String FILTER_LATE = "Em atraso";
    private static final int TOTAL_COLUMN = 4;
    private static final int REFRESH_INTERVAL_MILLIS = 30_000;
    private static final Color PENDING_COLOR = new Color(0xFFF3B0);
    private static final Color LATE_COLOR = new Color(0xFFC9C9);

    private final RestaurantData data;
    private final DefaultTableModel model =
            createModel("Pedido", "Mesa", "Garçom", "Itens", "Total", "Situação", "Tempo", "Prazo");
    private final JTable table = createTable(model);
    private final JComboBox<String> filterBox =
            new JComboBox<>(new String[]{FILTER_ALL, FILTER_PENDING, FILTER_LATE});
    private final JLabel summaryLabel = new JLabel();
    private final JButton advanceButton = new JButton("Avançar situação");
    private List<Order> shownOrders = List.of();

    OrdersPanel(RestaurantData data) {
        super("Pedidos");
        this.data = data;

        table.setDefaultRenderer(Object.class, new OrderRowRenderer());
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(320);
        table.getSelectionModel().addListSelectionListener(_ -> updateAdvanceButton());
        filterBox.addActionListener(_ -> refresh());
        advanceButton.addActionListener(_ -> advanceSelectedOrder());

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.add(new JLabel("Exibir:"));
        filters.add(filterBox);
        filters.add(summaryLabel);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.add(filters, BorderLayout.WEST);
        toolbar.add(advanceButton, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.add(toolbar, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        new Timer(REFRESH_INTERVAL_MILLIS, _ -> refresh()).start();
        refresh();
    }

    @Override
    protected void refresh() {
        Order previous = getSelectedOrder();
        List<Order> all = data.getOrders();
        summaryLabel.setText("Pendentes: " + all.stream().filter(Order::isPending).count()
                + "   |   Em atraso: " + all.stream().filter(Order::isLate).count());

        Comparator<Order> comparator = Comparator
                .comparing((Order order) -> order.getStatus().isFinal())
                .thenComparing(Order::getCreatedAt);
        shownOrders = all.stream().filter(this::matchesFilter).sorted(comparator).toList();

        model.setRowCount(0);
        for (Order order : shownOrders) {
            model.addRow(new Object[]{
                    "#" + order.getId(),
                    order.getTable().toString(),
                    order.getTable().getWaiter() == null ? "—" : order.getTable().getWaiter().getName(),
                    describeItems(order),
                    formatCurrency(order.getTotal()),
                    order.getStatus().getLabel(),
                    order.getMinutesElapsed() + " min",
                    describeDeadline(order)
            });
        }

        int index = previous == null ? -1 : shownOrders.indexOf(previous);
        if (index >= 0) {
            table.setRowSelectionInterval(index, index);
        }
        updateAdvanceButton();
    }

    private boolean matchesFilter(Order order) {
        Object filter = filterBox.getSelectedItem();
        if (FILTER_PENDING.equals(filter)) {
            return order.isPending();
        }
        if (FILTER_LATE.equals(filter)) {
            return order.isLate();
        }
        return true;
    }

    private Order getSelectedOrder() {
        int row = table.getSelectedRow();
        return row >= 0 && row < shownOrders.size() ? shownOrders.get(row) : null;
    }

    private void updateAdvanceButton() {
        Order order = getSelectedOrder();
        boolean canAdvance = order != null && !order.getStatus().isFinal();
        advanceButton.setEnabled(canAdvance);
        advanceButton.setText(canAdvance
                ? "Avançar para: " + order.getStatus().next().getLabel()
                : "Avançar situação");
    }

    private void advanceSelectedOrder() {
        Order order = getSelectedOrder();
        if (order == null) {
            return;
        }
        order.advance();
        showStatus("Pedido #" + order.getId() + " atualizado para: " + order.getStatus().getLabel() + ".");
        refresh();
    }

    private static String describeItems(Order order) {
        return order.getItems().stream()
                .map((OrderItem item) -> item.getQuantity() + "x " + item.getMenuItem().getName())
                .collect(Collectors.joining(", "));
    }

    private static String describeDeadline(Order order) {
        if (order.isLate()) {
            return "EM ATRASO";
        }
        return order.getStatus().isInProduction() ? "No prazo" : "—";
    }

    private class OrderRowRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

		@Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focused, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, selected, focused, row, column);
            setHorizontalAlignment(column == TOTAL_COLUMN ? SwingConstants.RIGHT : SwingConstants.LEFT);
            Order order = row < shownOrders.size() ? shownOrders.get(row) : null;
            boolean highlighted = order != null && (order.isLate() || order.isPending());
            component.setFont(table.getFont().deriveFont(highlighted ? Font.BOLD : Font.PLAIN));
            if (!selected) {
                component.setBackground(rowColor(order, table.getBackground()));
            }
            return component;
        }

        private Color rowColor(Order order, Color defaultColor) {
            if (order == null) {
                return defaultColor;
            }
            if (order.isLate()) {
                return LATE_COLOR;
            }
            return order.getStatus() == OrderStatus.PENDING ? PENDING_COLOR : defaultColor;
        }
    }
}
