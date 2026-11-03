package br.edu.icomp.ufam.to_brocado.ui;

import br.edu.icomp.ufam.to_brocado.data.RestaurantData;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Color;
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
            button.setHorizontalAlignment(JToggleButton.LEFT);
            button.setFocusPainted(false);
            button.putClientProperty("JButton.buttonType", "roundRect");
            button.putClientProperty("JComponent.minimumWidth", 172);
            button.addActionListener(_ -> cards.show(content, screen.getTitle()));
            group.add(button);
            buttons.add(button);
        }
        ((JToggleButton) buttons.getComponent(0)).setSelected(true);

        JLabel brand = new JLabel("Tô Brocado");
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 20f));
        brand.setForeground(new Color(0x9A4D0B));

        JLabel subtitle = new JLabel("GESTÃO DO RESTAURANTE");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.BOLD, 10f));
        subtitle.setForeground(new Color(0x77716B));
        JPanel brandBlock = new JPanel(new GridLayout(0, 1, 0, 4));
        brandBlock.setOpaque(false);
        brandBlock.add(brand);
        brandBlock.add(subtitle);

        JPanel navigation = new JPanel(new BorderLayout(0, 22));
        navigation.setBorder(BorderFactory.createEmptyBorder(24, 16, 20, 16));
        navigation.setPreferredSize(new Dimension(224, 0));
        navigation.add(brandBlock, BorderLayout.NORTH);
        buttons.setOpaque(false);
        JPanel buttonWrapper = new JPanel(new BorderLayout());
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(buttons, BorderLayout.NORTH);
        navigation.add(buttonWrapper, BorderLayout.CENTER);

        add(navigation, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        getRootPane().putClientProperty("JRootPane.titleBarBackground", new Color(0xFFF9F2));

        setMinimumSize(new Dimension(1080, 680));
        setSize(1240, 780);
        setLocationRelativeTo(null);
    }
}
