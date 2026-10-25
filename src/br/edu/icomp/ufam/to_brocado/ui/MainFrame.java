package br.edu.icomp.ufam.to_brocado.ui;

import br.edu.icomp.ufam.to_brocado.data.RestaurantData;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

	public MainFrame(RestaurantData data) {
        super("Tô Brocado — Gestão de Lanchonete");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        ScreenPanel[] screens = {
                new WaitersPanel(data),
                new TablesPanel(data),
                new MenuPanel(data),
                new OrdersPanel(data),
                new BillPanel(data)
        };

        CardLayout cards = new CardLayout();
        JPanel content = new JPanel(cards);
        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 6));
        ButtonGroup group = new ButtonGroup();
        for (ScreenPanel screen : screens) {
            content.add(screen, screen.getTitle());
            JToggleButton button = new JToggleButton(screen.getTitle());
            button.setPreferredSize(new Dimension(180, 44));
            button.addActionListener(_ -> cards.show(content, screen.getTitle()));
            group.add(button);
            buttons.add(button);
        }
        ((JToggleButton) buttons.getComponent(0)).setSelected(true);

        JLabel brand = new JLabel("Tô Brocado");
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 20f));

        JLabel memoryNotice = new JLabel(
                "<html>Os dados ficam apenas em memória e são perdidos ao fechar o aplicativo.</html>");
        memoryNotice.setFont(memoryNotice.getFont().deriveFont(Font.PLAIN, 11f));

        JPanel navigation = new JPanel(new BorderLayout(0, 16));
        navigation.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));
        navigation.setPreferredSize(new Dimension(204, 0));
        navigation.add(brand, BorderLayout.NORTH);
        JPanel buttonWrapper = new JPanel(new BorderLayout());
        buttonWrapper.add(buttons, BorderLayout.NORTH);
        navigation.add(buttonWrapper, BorderLayout.CENTER);
        navigation.add(memoryNotice, BorderLayout.SOUTH);

        add(navigation, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        setMinimumSize(new Dimension(1000, 620));
        setSize(1150, 700);
        setLocationRelativeTo(null);
    }
}
