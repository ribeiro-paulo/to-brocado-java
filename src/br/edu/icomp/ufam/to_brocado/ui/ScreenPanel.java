package br.edu.icomp.ufam.to_brocado.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Supplier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

abstract class ScreenPanel extends JPanel {

    private static final long serialVersionUID = 1L;
	private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
    private static final Color SUCCESS_COLOR = new Color(0x1B5E20);
    private static final Color ERROR_COLOR = new Color(0xB71C1C);

    private final String title;
    private final JLabel statusLabel = new JLabel(" ");

    protected ScreenPanel(String title) {
        super(new BorderLayout(0, 12));
        this.title = title;
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        add(titleLabel, BorderLayout.NORTH);
        add(statusLabel, BorderLayout.SOUTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent event) {
                statusLabel.setText(" ");
                refresh();
            }
        });
    }

    protected abstract void refresh();

    String getTitle() {
        return title;
    }

    protected void showStatus(String message) {
        statusLabel.setForeground(SUCCESS_COLOR);
        statusLabel.setText(message);
    }

    protected void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    protected boolean confirm(String message, String confirmLabel) {
        Object[] options = {confirmLabel, "Cancelar"};
        int choice = JOptionPane.showOptionDialog(this, message, "Confirmação", JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null, options, options[1]);
        return choice == 0;
    }

    protected boolean showForm(String title, JPanel form, String confirmLabel, JLabel errorLabel,
                               Supplier<String> validator) {
        Object[] options = {confirmLabel, "Cancelar"};
        while (true) {
            int choice = JOptionPane.showOptionDialog(this, form, title, JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (choice != 0) {
                return false;
            }
            String error = validator.get();
            if (error == null) {
                return true;
            }
            errorLabel.setText(error);
        }
    }

    protected static JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");
        label.setForeground(ERROR_COLOR);
        return label;
    }

    protected static JPanel buildForm(JLabel errorLabel, String[] labels, JComponent[] fields) {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < labels.length; i++) {
            constraints.gridy = i;
            constraints.gridx = 0;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.NONE;
            form.add(new JLabel(labels[i]), constraints);
            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            form.add(fields[i], constraints);
        }
        constraints.gridy = labels.length;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        form.add(errorLabel, constraints);
        return form;
    }

    protected static DefaultTableModel createModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    protected static JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        return table;
    }

    protected static void alignRight(JTable table, int... columns) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int column : columns) {
            table.getColumnModel().getColumn(column).setCellRenderer(renderer);
        }
    }

    protected static String formatCurrency(BigDecimal value) {
        return CURRENCY_FORMAT.format(value);
    }
}
