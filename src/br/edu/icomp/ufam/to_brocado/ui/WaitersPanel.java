package br.edu.icomp.ufam.to_brocado.ui;

import br.edu.icomp.ufam.to_brocado.data.RestaurantData;
import br.edu.icomp.ufam.to_brocado.model.Waiter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

class WaitersPanel extends ScreenPanel {

    private static final long serialVersionUID = 1L;
	private final RestaurantData data;
    private final DefaultTableModel model = createModel("Código", "Nome", "Telefone", "Mesas atendidas", "Situação");
    private final JTable table = createTable(model);
    private final JTextField searchField = new JTextField(20);
    private final JButton editButton = new JButton("Editar");
    private final JButton activeButton = new JButton("Inativar");
    private final JCheckBox showInactiveBox = new JCheckBox("Mostrar inativos");
    private final JTextField fromField = new JTextField(LocalDate.now().withDayOfMonth(1).toString(), 10);
    private final JTextField throughField = new JTextField(LocalDate.now().toString(), 10);
    private final DefaultTableModel reportModel = createModel("Código", "Garçom", "Situação", "Pedidos entregues",
            "Pedidos cancelados", "Contas fechadas", "Subtotal", "Taxa de serviço", "Total");
    private final JTable reportTable = createTable(reportModel);
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
        activeButton.addActionListener(_ -> changeSelectedWaiterStatus());
        showInactiveBox.addActionListener(_ -> refresh());
        table.getSelectionModel().addListSelectionListener(_ -> updateButtons());
        alignRight(table, 0, 3);
        table.getColumnModel().getColumn(0).setMaxWidth(80);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.add(new JLabel("Buscar por nome:"));
        searchBar.add(searchField);
        searchBar.add(showInactiveBox);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.add(newButton);
        actions.add(editButton);
        actions.add(activeButton);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.add(searchBar, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.add(toolbar, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Cadastro", content);
        tabs.addTab("Relatório de desempenho", createReportPanel());
        add(tabs, BorderLayout.CENTER);

        refresh();
    }

    private JPanel createReportPanel() {
        JButton generateButton = new JButton("Gerar relatório");
        generateButton.addActionListener(_ -> generateReport());
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.add(new JLabel("Data inicial (AAAA-MM-DD):"));
        filters.add(fromField);
        filters.add(new JLabel("Data final (AAAA-MM-DD):"));
        filters.add(throughField);
        filters.add(generateButton);

        reportTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        alignRight(reportTable, 0, 3, 4, 5, 6, 7, 8);
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(filters, BorderLayout.NORTH);
        panel.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        return panel;
    }

    private void generateReport() {
        try {
            LocalDate from = LocalDate.parse(fromField.getText().trim());
            LocalDate through = LocalDate.parse(throughField.getText().trim());
            List<RestaurantData.WaiterReport> rows = data.getWaiterReport(from, through);
            reportModel.setRowCount(0);
            for (RestaurantData.WaiterReport report : rows) {
                Waiter waiter = data.getWaiters().stream()
                        .filter(candidate -> candidate.getId() == report.waiterId()).findFirst().orElse(null);
                if (waiter == null) continue;
                reportModel.addRow(new Object[]{waiter.getId(), waiter.getName(), waiter.isActive() ? "Ativo" : "Inativo",
                        report.deliveredOrders(), report.cancelledOrders(), report.closedBills(),
                        formatCurrency(report.subtotal()), formatCurrency(report.serviceFee()), formatCurrency(report.total())});
            }
            showStatus("Relatório atualizado para " + from + " a " + through + ".");
        } catch (DateTimeParseException e) {
            showMessage("Informe as datas no formato AAAA-MM-DD.");
        } catch (IllegalArgumentException e) {
            showMessage(e.getMessage());
        } catch (IllegalStateException e) {
            showMessage(e.getMessage());
        }
    }

    @Override
    protected void refresh() {
        String filter = searchField.getText().trim().toLowerCase();
        shownWaiters = data.getWaiters().stream()
                .filter(waiter -> showInactiveBox.isSelected() || waiter.isActive())
                .filter(waiter -> waiter.getName().toLowerCase().contains(filter))
                .toList();
        model.setRowCount(0);
        for (Waiter waiter : shownWaiters) {
            model.addRow(new Object[]{
                    waiter.getId(),
                    waiter.getName(),
                    formatPhone(waiter.getPhone()),
                    data.countTablesOf(waiter),
                    waiter.isActive() ? "Ativo" : "Inativo"
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
        activeButton.setEnabled(selected);
        Waiter waiter = getSelectedWaiter();
        activeButton.setText(waiter != null && waiter.isActive() ? "Inativar" : "Reativar");
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
            data.updateWaiter(waiter, name, phone);
            showStatus("Dados do garçom atualizados com sucesso.");
        }
        refresh();
    }

    private void changeSelectedWaiterStatus() {
        Waiter waiter = getSelectedWaiter();
        if (waiter == null) {
            return;
        }
        boolean activate = !waiter.isActive();
        String action = activate ? "reativar" : "inativar";
        String buttonLabel = activate ? "Reativar" : "Inativar";
        if (!confirm("Deseja " + action + " o garçom " + waiter.getName() + "?", buttonLabel)) {
            return;
        }
        try {
            data.setWaiterActive(waiter, activate);
            showStatus("Garçom " + (activate ? "reativado" : "inativado") + " com sucesso.");
        } catch (IllegalStateException e) {
            showMessage(e.getMessage());
        }
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
