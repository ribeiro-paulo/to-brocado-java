package br.edu.icomp.ufam.to_brocado;

import br.edu.icomp.ufam.to_brocado.data.RestaurantData;
import br.edu.icomp.ufam.to_brocado.ui.MainFrame;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
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
