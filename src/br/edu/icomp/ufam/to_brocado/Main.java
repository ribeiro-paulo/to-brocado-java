package br.edu.icomp.ufam.to_brocado;

import br.edu.icomp.ufam.to_brocado.data.RestaurantData;
import br.edu.icomp.ufam.to_brocado.ui.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        FlatLightLaf.setup();
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("Table.rowHeight", 36);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.selectionBackground", new java.awt.Color(0xFCE8D5));
        UIManager.put("Table.selectionForeground", new java.awt.Color(0x3B2A1A));
        UIManager.put("Component.focusColor", new java.awt.Color(0xD97706));
        UIManager.put("Component.focusedBorderColor", new java.awt.Color(0xD97706));
        SwingUtilities.invokeLater(() -> {
            try {
                new MainFrame(new RestaurantData()).setVisible(true);
            } catch (IllegalStateException exception) {
                javax.swing.JOptionPane.showMessageDialog(null, exception.getMessage(), "Falha na conexão MySQL",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
