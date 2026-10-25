package br.edu.icomp.ufam.to_brocado.ui;

import br.edu.icomp.ufam.to_brocado.data.RestaurantData;
import br.edu.icomp.ufam.to_brocado.model.Waiter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

class WaitersPanel extends ScreenPanel {

    private static final long serialVersionUID = 1L;
	private final RestaurantData data;
    private final DefaultTableModel model = createModel("Código", "Nome", "Telefone", "Mesas atendidas");
    private final JTable table = createTable(model);
    private final JTextField searchField = new JTextField(20);
    private final JButton editButton = new JButton("Editar");
    private final JButton removeButton = new JButton("Remover");
    private List<Waiter> shownWaiters = List.of();

    WaitersPanel(RestaurantData data) {
        super("Garçons");
        this.data = data;

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                refresh();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refresh();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refresh();
            }
        });

        JButton newButton = new JButton("Novo garçom");
        newButton.addActionListener(_ -> openForm(null));
        editButton.addActionListener(_ -> openForm(getSelectedWaiter()));
        removeButton.addActionListener(_ -> removeSelectedWaiter());
        table.getSelectionModel().addListSelectionListener(_ -> updateButtons());
        alignRight(table, 0, 3);
        table.getColumnModel().getColumn(0).setMaxWidth(80);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.add(new JLabel("Buscar por nome:"));
        searchBar.add(searchField);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.add(newButton);
        actions.add(editButton);
        actions.add(removeButton);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.add(searchBar, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.add(toolbar, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        refresh();
    }

    @Override
    protected void refresh() {
        String filter = searchField.getText().trim().toLowerCase();
        shownWaiters = data.getWaiters().stream()
                .filter(waiter -> waiter.getName().toLowerCase().contains(filter))
                .toList();
        model.setRowCount(0);
        for (Waiter waiter : shownWaiters) {
            model.addRow(new Object[]{
                    waiter.getId(),
                    waiter.getName(),
                    formatPhone(waiter.getPhone()),
                    data.countTablesOf(waiter)
            });
        }
        updateButtons();
    }

    private Waiter getSelectedWaiter() {
        int row = table.getSelectedRow();
        return row >= 0 && row < shownWaiters.size() ? shownWaiters.get(row) : null;
    }

    private void updateButtons() {
        boolean selected = getSelectedWaiter() != null;
        editButton.setEnabled(selected);
        removeButton.setEnabled(selected);
    }

    private void openForm(Waiter waiter) {
        JTextField nameField = new JTextField(waiter == null ? "" : waiter.getName(), 20);
        JTextField phoneField = new JTextField(waiter == null ? "" : formatPhone(waiter.getPhone()), 20);
        JLabel errorLabel = createErrorLabel();
        JPanel form = buildForm(errorLabel, new String[]{"Nome:", "Telefone (com DDD):"},
                new JComponent[]{nameField, phoneField});

        String title = waiter == null ? "Novo garçom" : "Editar garçom";
        boolean confirmed = showForm(title, form, "Salvar", errorLabel,
                () -> validate(nameField.getText(), phoneField.getText()));
        if (!confirmed) {
            return;
        }

        String name = nameField.getText().trim();
        String phone = digitsOf(phoneField.getText());
        if (waiter == null) {
            data.addWaiter(name, phone);
            showStatus("Garçom cadastrado com sucesso.");
        } else {
            waiter.update(name, phone);
            showStatus("Dados do garçom atualizados com sucesso.");
        }
        refresh();
    }

    private void removeSelectedWaiter() {
        Waiter waiter = getSelectedWaiter();
        if (waiter == null) {
            return;
        }
        if (data.countTablesOf(waiter) > 0) {
            showMessage("O garçom " + waiter.getName() + " atende mesas ocupadas e não pode ser removido agora.");
            return;
        }
        if (!confirm("Deseja remover o garçom " + waiter.getName() + "?", "Remover")) {
            return;
        }
        data.removeWaiter(waiter);
        showStatus("Garçom removido com sucesso.");
        refresh();
    }

    private String validate(String name, String phone) {
        if (name.isBlank()) {
            return "Informe o nome do garçom.";
        }
        int digits = digitsOf(phone).length();
        if (digits != 10 && digits != 11) {
            return "Informe o telefone com DDD (10 ou 11 dígitos).";
        }
        return null;
    }

    private static String digitsOf(String text) {
        return text.replaceAll("\\D", "");
    }

    private static String formatPhone(String digits) {
        if (digits.length() == 11) {
            return "(" + digits.substring(0, 2) + ") " + digits.substring(2, 7) + "-" + digits.substring(7);
        }
        if (digits.length() == 10) {
            return "(" + digits.substring(0, 2) + ") " + digits.substring(2, 6) + "-" + digits.substring(6);
        }
        return digits;
    }
}
