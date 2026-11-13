package com.pos.main;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.pos.views.LoginFrame;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}