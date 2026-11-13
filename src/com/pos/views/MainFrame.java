package com.pos.views;

import java.awt.Component;
import javax.swing.*;
import com.pos.models.User;
import com.pos.utils.Refreshable;

public class MainFrame extends JFrame {

    private final User currentUser;

    public MainFrame(User user) {
        this.currentUser = user;
        setTitle("Point of Sale System  -  " + user.getFullName() + "  [" + user.getRole() + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 720);
        setLocationRelativeTo(null);
        setJMenuBar(buildMenuBar());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Cashier / POS", new POSPanel(currentUser));
        tabs.addTab("Inventory",     new InventoryPanel());
        tabs.addTab("Sales Reports", new SalesReportPanel());
        if (user.isAdmin()) {
            tabs.addTab("User Management", new UserPanel(currentUser));
        }

        tabs.addChangeListener(e -> {
            Component c = tabs.getSelectedComponent();
            if (c instanceof Refreshable) ((Refreshable) c).refresh();
        });

        add(tabs);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem logout = new JMenuItem("Logout");
        JMenuItem exit   = new JMenuItem("Exit");
        file.add(logout);
        file.addSeparator();
        file.add(exit);
        bar.add(file);

        JMenu help = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        help.add(about);
        bar.add(help);

        logout.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this, "Log out of the system?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            }
        });

        exit.addActionListener(e -> System.exit(0));
        about.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Java POS System\nVersion 2.0\n\nConnected to MySQL\nDatabase: pos_system",
                "About", JOptionPane.INFORMATION_MESSAGE));
        return bar;
    }
}