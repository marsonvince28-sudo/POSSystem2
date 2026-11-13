package com.pos.views;

import java.awt.*;
import javax.swing.*;
import com.pos.models.User;
import com.pos.database.DBconnection;
import com.pos.utils.DataStore;

public class LoginFrame extends JFrame {

    private JTextField userField;
    private JPasswordField passField;

    public LoginFrame() {
        setTitle("POS System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 300);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("POINT OF SALE SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(30, 90, 160));
        main.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 12));
        form.add(new JLabel("Username:"));
        userField = new JTextField("admin");
        form.add(userField);
        form.add(new JLabel("Password:"));
        passField = new JPasswordField("admin123");
        form.add(passField);
        main.add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(5, 5));
        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 8));
        JButton loginBtn = new JButton("Login");
        JButton exitBtn  = new JButton("Exit");
        buttons.add(loginBtn);
        buttons.add(exitBtn);

        JLabel hint = new JLabel("<html><i>Default: admin/admin123 or cashier/cashier123</i></html>", SwingConstants.CENTER);
        hint.setFont(hint.getFont().deriveFont(11f));
        hint.setForeground(Color.GRAY);

        south.add(buttons, BorderLayout.NORTH);
        south.add(hint, BorderLayout.SOUTH);
        main.add(south, BorderLayout.SOUTH);
        add(main);

        loginBtn.addActionListener(e -> doLogin());
        exitBtn.addActionListener(e -> System.exit(0));
        getRootPane().setDefaultButton(loginBtn);
    }

    private void doLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.", "Login", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = DBconnection.login(username, password);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            passField.setText("");
            return;
        }

        dispose();
        DataStore.getInstance().load();
        SwingUtilities.invokeLater(() -> new MainFrame(user).setVisible(true));
    }
}