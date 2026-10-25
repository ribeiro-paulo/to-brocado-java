package br.edu.icomp.ufam.to_brocado;

import br.edu.icomp.ufam.to_brocado.data.RestaurantData;
import br.edu.icomp.ufam.to_brocado.ui.MainFrame;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame(new RestaurantData()).setVisible(true));
    }
}
