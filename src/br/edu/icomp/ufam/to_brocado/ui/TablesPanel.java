package br.edu.icomp.ufam.to_brocado.ui;

import br.edu.icomp.ufam.to_brocado.data.RestaurantData;
import br.edu.icomp.ufam.to_brocado.model.Order;
import br.edu.icomp.ufam.to_brocado.model.RestaurantTable;
import br.edu.icomp.ufam.to_brocado.model.Waiter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

class TablesPanel extends ScreenPanel {

    private static final long serialVersionUID = 1L;
    private static final Color FREE_COLOR = new Color(0xE8F5E9);
    private static final Color OCCUPIED_COLOR = new Color(0xFFF0E4);
    private static final Color SELECTED_BORDER_COLOR = new Color(0xD97706);

    private final RestaurantData data;
    private final JPanel grid = new JPanel(new GridLayout(0, 4, 12, 12));
    private final JLabel detailsLabel = new JLabel();
    private final JButton occupyButton = new JButton("Ocupar mesa");
    private final JButton newButton = new JButton("Nova mesa");
    private final JButton editButton = new JButton("Editar mesa");
    private final JButton removeButton = new JButton("Remover mesa");
    private RestaurantTable selectedTable;

    TablesPanel(RestaurantData data) {
        super("Mesas");
        this.data = data;

        occupyButton.addActionListener(_ -> occupySelectedTable());
        newButton.addActionListener(_ -> openTableForm(null));
        editButton.addActionListener(_ -> openTableForm(selectedTable));
        removeButton.addActionListener(_ -> removeSelectedTable());
        detailsLabel.setVerticalAlignment(SwingConstants.TOP);

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.add(grid, BorderLayout.NORTH);

        JPanel details = new JPanel(new BorderLayout(0, 12));
        details.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Detalhes da mesa"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        details.setPreferredSize(new Dimension(280, 0));
        details.add(detailsLabel, BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(0, 1, 0, 6));
        actions.add(occupyButton);
        actions.add(newButton);
        actions.add(editButton);
        actions.add(removeButton);
        details.add(actions, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(16, 0));
        content.add(gridWrapper, BorderLayout.CENTER);
        content.add(details, BorderLayout.EAST);
        add(content, BorderLayout.CENTER);

        refresh();
    }

    @Override
    protected void refresh() {
        grid.removeAll();
        for (RestaurantTable table : data.getTables()) {
            grid.add(createTableButton(table));
        }
        grid.revalidate();
        grid.repaint();
        updateDetails();
    }

    private JButton createTableButton(RestaurantTable table) {
        String text = "<html><div style='text-align:center'><b>" + table + "</b><br>"
                + table.getStatus().getLabel() + "<br>" + table.getSeats() + " lugares</div></html>";
        JButton button = new JButton(text);
        button.setOpaque(true);
        button.setBackground(table.isOccupied() ? OCCUPIED_COLOR : FREE_COLOR);
        button.setPreferredSize(new Dimension(140, 110));
        button.setFocusPainted(false);
        button.setFont(button.getFont().deriveFont(14f));
        boolean selected = table == selectedTable;
        button.setBorder(selected
                ? BorderFactory.createLineBorder(SELECTED_BORDER_COLOR, 4)
                : BorderFactory.createLineBorder(new Color(0xDED8D1), 1));
        button.addActionListener(_ -> {
            selectedTable = table;
            refresh();
        });
        return button;
    }

    private void updateDetails() {
        if (selectedTable == null) {
            detailsLabel.setText("<html>Selecione uma mesa para consultar a situação.</html>");
            occupyButton.setEnabled(false);
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            return;
        }
        List<Order> orders = data.getOrdersForTable(selectedTable);
        long inProgress = orders.stream().filter(order -> !order.getStatus().isFinal()).count();
        String waiterName = selectedTable.getWaiter() == null ? "—" : selectedTable.getWaiter().getName();
        String hint = selectedTable.isOccupied()
                ? "<br><br>Para encerrar o atendimento, use Fechamento de conta."
                : "";
        detailsLabel.setText("<html><h2>" + selectedTable + "</h2>"
                + "Lugares: " + selectedTable.getSeats() + "<br>"
                + "Situação: <b>" + selectedTable.getStatus().getLabel() + "</b><br>"
                + "Garçom: " + waiterName + "<br>"
                + "Pedidos: " + orders.size() + " (" + inProgress + " em andamento)"
                + hint + "</html>");
        occupyButton.setEnabled(!selectedTable.isOccupied());
        editButton.setEnabled(!selectedTable.isOccupied());
        removeButton.setEnabled(!selectedTable.isOccupied() && data.getOrdersForTable(selectedTable).isEmpty());
    }

    private void openTableForm(RestaurantTable table) {
        JSpinner numberField = new JSpinner(new SpinnerNumberModel(table == null ? data.getTables().size() + 1 : table.getNumber(), 1, 999, 1));
        JSpinner seatsField = new JSpinner(new SpinnerNumberModel(table == null ? 2 : table.getSeats(), 1, 99, 1));
        JLabel errorLabel = createErrorLabel();
        JPanel form = buildForm(errorLabel, new String[]{"Número:", "Lugares:"}, new JComponent[]{numberField, seatsField});
        boolean confirmed = showForm(table == null ? "Nova mesa" : "Editar mesa", form, "Salvar", errorLabel, () -> {
            int number = (Integer) numberField.getValue();
            boolean duplicate = data.getTables().stream().anyMatch(other -> other != table && other.getNumber() == number);
            return duplicate ? "Já existe uma mesa com esse número." : null;
        });
        if (!confirmed) return;
        int number = (Integer) numberField.getValue();
        int seats = (Integer) seatsField.getValue();
        if (table == null) {
            selectedTable = data.addTable(number, seats);
            showStatus("Mesa cadastrada com sucesso.");
        } else {
            data.updateTable(table, number, seats);
            showStatus("Mesa atualizada com sucesso.");
        }
        refresh();
    }

    private void removeSelectedTable() {
        if (selectedTable == null || selectedTable.isOccupied() || !data.getOrdersForTable(selectedTable).isEmpty()) return;
        if (!confirm("Deseja remover " + selectedTable + "?", "Remover")) return;
        data.removeTable(selectedTable);
        selectedTable = null;
        showStatus("Mesa removida com sucesso.");
        refresh();
    }

    private void occupySelectedTable() {
        if (data.getActiveWaiters().isEmpty()) {
            showMessage("Cadastre ao menos um garçom antes de ocupar uma mesa.");
            return;
        }
        JComboBox<Waiter> waiterBox = new JComboBox<>(data.getActiveWaiters().toArray(new Waiter[0]));
        waiterBox.setSelectedIndex(-1);
        JLabel errorLabel = createErrorLabel();
        JPanel form = buildForm(errorLabel, new String[]{"Garçom responsável:"}, new JComponent[]{waiterBox});

        boolean confirmed = showForm("Ocupar " + selectedTable, form, "Ocupar", errorLabel,
                () -> waiterBox.getSelectedItem() == null ? "Selecione o garçom responsável." : null);
        if (!confirmed) {
            return;
        }
        data.occupyTable(selectedTable, (Waiter) waiterBox.getSelectedItem());
        showStatus(selectedTable + " ocupada com sucesso.");
        refresh();
    }
}
