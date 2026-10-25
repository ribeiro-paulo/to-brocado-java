package br.edu.icomp.ufam.to_brocado.ui;

import br.edu.icomp.ufam.to_brocado.data.RestaurantData;
import br.edu.icomp.ufam.to_brocado.model.Bill;
import br.edu.icomp.ufam.to_brocado.model.Order;
import br.edu.icomp.ufam.to_brocado.model.OrderItem;
import br.edu.icomp.ufam.to_brocado.model.RestaurantTable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

class BillPanel extends ScreenPanel {

    private static final long serialVersionUID = 1L;
	private static final int MAX_PEOPLE = 99;
    private static final Color NOTICE_COLOR = new Color(0xB71C1C);

    private final RestaurantData data;
    private final JComboBox<RestaurantTable> tableBox = new JComboBox<>();
    private final DefaultTableModel model = createModel("Pedido", "Item", "Qtd.", "Valor unitário", "Subtotal");
    private final JTable itemsTable = createTable(model);
    private final JCheckBox serviceFeeBox = new JCheckBox(
            "Incluir taxa de serviço (" + Bill.SERVICE_RATE.movePointRight(2).intValue() + "%)", true);
    private final JSpinner peopleSpinner = new JSpinner(new SpinnerNumberModel(1, 1, MAX_PEOPLE, 1));
    private final JLabel subtotalValue = createValueLabel(false);
    private final JLabel serviceFeeValue = createValueLabel(false);
    private final JLabel totalValue = createValueLabel(true);
    private final JLabel perPersonValue = createValueLabel(true);
    private final JLabel noticeLabel = new JLabel(" ");
    private final JButton closeButton = new JButton("Fechar conta e liberar mesa");

    BillPanel(RestaurantData data) {
        super("Fechamento de conta");
        this.data = data;

        tableBox.addActionListener(_ -> updateBill());
        serviceFeeBox.addActionListener(_ -> updateBill());
        peopleSpinner.addChangeListener(_ -> updateBill());
        closeButton.addActionListener(_ -> closeAccount());
        alignRight(itemsTable, 2, 3, 4);
        itemsTable.getColumnModel().getColumn(0).setMaxWidth(80);
        itemsTable.getColumnModel().getColumn(2).setMaxWidth(80);

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        selector.add(new JLabel("Mesa:"));
        selector.add(tableBox);

        JPanel values = new JPanel(new GridLayout(0, 2, 8, 8));
        values.add(new JLabel("Subtotal:"));
        values.add(subtotalValue);
        values.add(new JLabel("Taxa de serviço:"));
        values.add(serviceFeeValue);
        values.add(new JLabel("Total da conta:"));
        values.add(totalValue);
        values.add(new JLabel("Valor por pessoa:"));
        values.add(perPersonValue);

        JPanel people = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        people.add(new JLabel("Número de pessoas:"));
        people.add(peopleSpinner);

        JPanel options = new JPanel(new GridLayout(0, 1, 0, 8));
        options.add(serviceFeeBox);
        options.add(people);

        noticeLabel.setForeground(NOTICE_COLOR);

        JPanel summaryBody = new JPanel(new BorderLayout(0, 16));
        summaryBody.add(options, BorderLayout.NORTH);
        summaryBody.add(values, BorderLayout.CENTER);
        summaryBody.add(noticeLabel, BorderLayout.SOUTH);

        JPanel summary = new JPanel(new BorderLayout(0, 12));
        summary.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Resumo da conta"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        summary.setPreferredSize(new Dimension(340, 0));
        summary.add(summaryBody, BorderLayout.NORTH);
        summary.add(closeButton, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(16, 8));
        content.add(selector, BorderLayout.NORTH);
        content.add(new JScrollPane(itemsTable), BorderLayout.CENTER);
        content.add(summary, BorderLayout.EAST);
        add(content, BorderLayout.CENTER);

        refresh();
    }

    @Override
    protected void refresh() {
        RestaurantTable previous = (RestaurantTable) tableBox.getSelectedItem();
        DefaultComboBoxModel<RestaurantTable> tables = new DefaultComboBoxModel<>();
        data.getTables().stream().filter(RestaurantTable::isOccupied).forEach(tables::addElement);
        tableBox.setModel(tables);
        if (previous != null && tables.getIndexOf(previous) >= 0) {
            tableBox.setSelectedItem(previous);
        }
        updateBill();
    }

    private void updateBill() {
        RestaurantTable table = (RestaurantTable) tableBox.getSelectedItem();
        List<Order> orders = table == null ? List.of() : data.getOrdersForTable(table);

        model.setRowCount(0);
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                model.addRow(new Object[]{
                        "#" + order.getId(),
                        item.getMenuItem().getName(),
                        item.getQuantity(),
                        formatCurrency(item.getUnitPrice()),
                        formatCurrency(item.getSubtotal())
                });
            }
            subtotal = subtotal.add(order.getTotal());
        }

        Bill bill = new Bill(subtotal, serviceFeeBox.isSelected(), (Integer) peopleSpinner.getValue());
        subtotalValue.setText(formatCurrency(bill.getSubtotal()));
        serviceFeeValue.setText(formatCurrency(bill.getServiceFee()));
        totalValue.setText(formatCurrency(bill.getTotal()));
        perPersonValue.setText(formatCurrency(bill.getPerPerson()));

        boolean hasUndelivered = table != null && data.hasUndeliveredOrders(table);
        noticeLabel.setText(hasUndelivered ? "<html>Há pedidos ainda não entregues nesta mesa.</html>" : " ");
        closeButton.setEnabled(table != null);
    }

    private void closeAccount() {
        RestaurantTable table = (RestaurantTable) tableBox.getSelectedItem();
        if (table == null) {
            return;
        }
        if (data.hasUndeliveredOrders(table)) {
            showMessage("Existem pedidos ainda não entregues. Entregue todos os pedidos antes de fechar a conta.");
            return;
        }
        if (!confirm("Fechar a conta da " + table + " e liberar a mesa?", "Fechar conta")) {
            return;
        }
        data.closeTable(table);
        showStatus("Conta da " + table + " fechada. A mesa foi liberada.");
        refresh();
    }

    private static JLabel createValueLabel(boolean emphasized) {
        JLabel label = new JLabel("", SwingConstants.RIGHT);
        if (emphasized) {
            label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
        }
        return label;
    }
}
