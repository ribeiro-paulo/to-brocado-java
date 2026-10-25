package br.edu.icomp.ufam.to_brocado.ui;

import br.edu.icomp.ufam.to_brocado.data.RestaurantData;
import br.edu.icomp.ufam.to_brocado.model.MenuCategory;
import br.edu.icomp.ufam.to_brocado.model.MenuItem;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

class MenuPanel extends ScreenPanel {

    private static final long serialVersionUID = 1L;
	private final RestaurantData data;
    private final DefaultTableModel model = createModel("Código", "Nome", "Categoria", "Preço", "Situação");
    private final JTable table = createTable(model);
    private final JButton editButton = new JButton("Editar");
    private final JButton removeButton = new JButton("Remover");
    private List<MenuItem> shownItems = List.of();

    MenuPanel(RestaurantData data) {
        super("Cardápio");
        this.data = data;

        JButton newButton = new JButton("Novo item");
        newButton.addActionListener(_ -> openForm(null));
        editButton.addActionListener(_ -> openForm(getSelectedItem()));
        removeButton.addActionListener(_ -> removeSelectedItem());
        table.getSelectionModel().addListSelectionListener(_ -> updateButtons());
        alignRight(table, 0, 3);
        table.getColumnModel().getColumn(0).setMaxWidth(80);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.add(newButton);
        actions.add(editButton);
        actions.add(removeButton);

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.add(actions, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        refresh();
    }

    @Override
    protected void refresh() {
        shownItems = List.copyOf(data.getMenuItems());
        model.setRowCount(0);
        for (MenuItem item : shownItems) {
            model.addRow(new Object[]{
                    item.getId(),
                    item.getName(),
                    item.getCategory().getLabel(),
                    formatCurrency(item.getPrice()),
                    item.isAvailable() ? "Disponível" : "Indisponível"
            });
        }
        updateButtons();
    }

    private MenuItem getSelectedItem() {
        int row = table.getSelectedRow();
        return row >= 0 && row < shownItems.size() ? shownItems.get(row) : null;
    }

    private void updateButtons() {
        boolean selected = getSelectedItem() != null;
        editButton.setEnabled(selected);
        removeButton.setEnabled(selected);
    }

    private void openForm(MenuItem item) {
        JTextField nameField = new JTextField(item == null ? "" : item.getName(), 20);
        JComboBox<MenuCategory> categoryBox = new JComboBox<>(MenuCategory.values());
        JTextField priceField = new JTextField(item == null ? "" : item.getPrice().toPlainString().replace('.', ','), 20);
        JCheckBox availableBox = new JCheckBox("Disponível para pedidos", item == null || item.isAvailable());
        if (item != null) {
            categoryBox.setSelectedItem(item.getCategory());
        }
        JLabel errorLabel = createErrorLabel();
        JPanel form = buildForm(errorLabel, new String[]{"Nome:", "Categoria:", "Preço (R$):", "Situação:"},
                new JComponent[]{nameField, categoryBox, priceField, availableBox});

        String title = item == null ? "Novo item do cardápio" : "Editar item do cardápio";
        boolean confirmed = showForm(title, form, "Salvar", errorLabel,
                () -> validate(nameField.getText(), priceField.getText()));
        if (!confirmed) {
            return;
        }

        String name = nameField.getText().trim();
        MenuCategory category = (MenuCategory) categoryBox.getSelectedItem();
        BigDecimal price = parsePrice(priceField.getText());
        if (item == null) {
            data.addMenuItem(name, category, price, availableBox.isSelected());
            showStatus("Item cadastrado com sucesso.");
        } else {
            item.update(name, category, price, availableBox.isSelected());
            showStatus("Item atualizado com sucesso.");
        }
        refresh();
    }

    private void removeSelectedItem() {
        MenuItem item = getSelectedItem();
        if (item == null) {
            return;
        }
        if (!confirm("Deseja remover o item " + item.getName() + " do cardápio?", "Remover")) {
            return;
        }
        data.removeMenuItem(item);
        showStatus("Item removido com sucesso.");
        refresh();
    }

    private String validate(String name, String price) {
        if (name.isBlank()) {
            return "Informe o nome do item.";
        }
        BigDecimal parsed = parsePrice(price);
        if (parsed == null) {
            return "Informe um preço válido, por exemplo 12,50.";
        }
        if (parsed.signum() <= 0) {
            return "O preço deve ser maior que zero.";
        }
        return null;
    }

    private static BigDecimal parsePrice(String text) {
        try {
            return new BigDecimal(text.trim().replace(',', '.')).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
