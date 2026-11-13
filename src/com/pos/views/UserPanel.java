package com.pos.views;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.pos.models.User;
import com.pos.database.DBconnection;
import com.pos.utils.DataStore;
import com.pos.utils.Refreshable;

public class UserPanel extends JPanel implements Refreshable {

    private final DataStore store = DataStore.getInstance();
    private final User currentUser;
    private DefaultTableModel model;
    private JTable table;

    public UserPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        refresh();
    }

    private void buildUI() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton addBtn = new JButton("Add User"), editBtn = new JButton("Edit User"), passBtn = new JButton("Reset Password");
        JButton toggleBtn = new JButton("Activate / Deactivate"), deleteBtn = new JButton("Delete User"), refreshBtn = new JButton("Refresh");
        buttons.add(addBtn); buttons.add(editBtn); buttons.add(passBtn); buttons.add(toggleBtn); buttons.add(deleteBtn); buttons.add(refreshBtn);
        add(buttons, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"Username", "Full Name", "Role", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JLabel note = new JLabel("  Only administrators can manage user accounts."); note.setForeground(Color.GRAY);
        add(note, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> showUserDialog(null));
        editBtn.addActionListener(e -> { User u = getSelectedUser(); if (u == null) warn("Select a user."); else showUserDialog(u); });
        passBtn.addActionListener(e -> resetPassword());
        toggleBtn.addActionListener(e -> toggleActive());
        deleteBtn.addActionListener(e -> deleteUser());
        refreshBtn.addActionListener(e -> refresh());
    }

    @Override
    public void refresh() {
        store.getUsers().clear(); store.getUsers().addAll(DBconnection.getAllUsers());
        model.setRowCount(0);
        for (User u : store.getUsers()) model.addRow(new Object[]{u.getUsername(), u.getFullName(), u.getRole(), u.isActive() ? "Active" : "Inactive"});
    }

    private User getSelectedUser() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return store.findUser(String.valueOf(model.getValueAt(row, 0)));
    }

    private void showUserDialog(User existing) {
        JDialog dlg = new JDialog();
        dlg.setTitle(existing == null ? "Add User" : "Edit User");
        dlg.setModal(true); dlg.setSize(380, 300); dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        JTextField usernameField = new JTextField(existing == null ? "" : existing.getUsername());
        JTextField fullNameField = new JTextField(existing == null ? "" : existing.getFullName());
        JPasswordField passField = new JPasswordField(existing == null ? "" : existing.getPassword());
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{User.ROLE_CASHIER, User.ROLE_ADMIN});
        if (existing != null) roleCombo.setSelectedItem(existing.getRole());
        if (existing != null) usernameField.setEditable(false);

        form.add(new JLabel("Username:")); form.add(usernameField);
        form.add(new JLabel("Full Name:")); form.add(fullNameField);
        form.add(new JLabel("Password:")); form.add(passField);
        form.add(new JLabel("Role:")); form.add(roleCombo);
        dlg.add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = new JButton("Save"), cancel = new JButton("Cancel");
        btns.add(save); btns.add(cancel); dlg.add(btns, BorderLayout.SOUTH);
        cancel.addActionListener(e -> dlg.dispose());

        save.addActionListener(e -> {
            String username = usernameField.getText().trim(), fullName = fullNameField.getText().trim();
            String password = new String(passField.getPassword()), role = (String) roleCombo.getSelectedItem();
            if (username.isEmpty() || fullName.isEmpty() || password.isEmpty()) { warn("All fields required."); return; }

            if (existing == null) {
                if (store.findUser(username) != null) { warn("Username exists."); return; }
                User newUser = new User(username, password, fullName, role, true);
                if (DBconnection.addUser(newUser)) store.getUsers().add(newUser); else warn("MySQL Error.");
            } else {
                existing.setFullName(fullName); existing.setPassword(password); existing.setRole(role);
                if (!DBconnection.updateUser(existing)) { warn("MySQL Error."); return; }
            }
            dlg.dispose(); refresh();
        });
        dlg.setVisible(true);
    }

    private void resetPassword() {
        User u = getSelectedUser();
        if (u == null) { warn("Select a user."); return; }
        String np = JOptionPane.showInputDialog(this, "New password for " + u.getUsername() + ":");
        if (np != null && !np.trim().isEmpty()) { u.setPassword(np.trim()); if (DBconnection.updateUser(u)) JOptionPane.showMessageDialog(this, "Updated."); }
    }

    private void toggleActive() {
        User u = getSelectedUser();
        if (u == null || u.getUsername().equalsIgnoreCase(currentUser.getUsername())) { warn("Cannot deactivate yourself."); return; }
        boolean newState = !u.isActive();
        if (DBconnection.setUserActive(u.getUsername(), newState)) { u.setActive(newState); refresh(); }
    }

    private void deleteUser() {
        User u = getSelectedUser();
        if (u == null || u.getUsername().equalsIgnoreCase(currentUser.getUsername())) { warn("Cannot delete yourself."); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete user " + u.getUsername() + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (DBconnection.deleteUser(u.getUsername())) { store.getUsers().remove(u); refresh(); }
        }
    }
    private void warn(String msg) { JOptionPane.showMessageDialog(this, msg, "User Management", JOptionPane.WARNING_MESSAGE); }
}