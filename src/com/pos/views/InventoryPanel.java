package com.pos.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.pos.database.DBconnection;
import com.pos.models.Product;
import com.pos.utils.DataStore;
import com.pos.utils.Refreshable;

public class InventoryPanel extends JPanel implements Refreshable {

    private final DataStore store = DataStore.getInstance();
    private final DecimalFormat money = new DecimalFormat("#,##0.00");
    private JTextField searchField;
    private DefaultTableModel model;
    private JTable table;
    private JLabel summaryLabel;

    public InventoryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        refresh();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout(8, 8));
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchField = new JTextField();
        searchField.addKeyListener(new KeyAdapter() { @Override public void keyReleased(KeyEvent e) { refresh(); } });
        searchPanel.add(searchField, BorderLayout.CENTER);
        top.add(searchPanel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton addBtn = new JButton("Add Product"), editBtn = new JButton("Edit"), deleteBtn = new JButton("Delete");
        JButton restockBtn = new JButton("Restock"), refreshBtn = new JButton("Refresh");
        buttons.add(addBtn); buttons.add(editBtn); buttons.add(deleteBtn); buttons.add(restockBtn); buttons.add(refreshBtn);
        top.add(buttons, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"ID", "Name", "Category", "Price", "Stock", "Low Level", "Status", "Value"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment((column >= 3 && column != 6) ? SwingConstants.RIGHT : SwingConstants.LEFT);
                if (!isSelected) {
                    Object idObj = t.getModel().getValueAt(row, 0);
                    Product p = idObj == null ? null : store.findProductById(idObj.toString());
                    if (p != null && p.isOutOfStock()) c.setBackground(new Color(255, 205, 205));
                    else if (p != null && p.isLowStock()) c.setBackground(new Color(255, 245, 200));
                    else c.setBackground(Color.WHITE);
                }
                return c;
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        summaryLabel = new JLabel(" ");
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(summaryLabel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> showProductDialog(null));
        editBtn.addActionListener(e -> { Product p = getSelectedProduct(); if (p == null) warn("Select a product to edit."); else showProductDialog(p); });
        deleteBtn.addActionListener(e -> deleteProduct());
        restockBtn.addActionListener(e -> restockProduct());
        refreshBtn.addActionListener(e -> refresh());
    }

    @Override
    public void refresh() {
        store.getProducts().clear();
        store.getProducts().addAll(DBconnection.getAllProducts());
        String keyword = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        model.setRowCount(0);
        int lowCount = 0, outCount = 0;
        double totalValue = 0;
        for (Product p : store.getProducts()) {
            if (!keyword.isEmpty() && !p.getName().toLowerCase().contains(keyword) && !p.getId().toLowerCase().contains(keyword) && !p.getCategory().toLowerCase().contains(keyword)) continue;
            model.addRow(new Object[]{p.getId(), p.getName(), p.getCategory(), money.format(p.getPrice()), p.getStock(), p.getLowStockLevel(), p.getStatus(), money.format(p.getInventoryValue())});
            if (p.isOutOfStock()) outCount++; else if (p.isLowStock()) lowCount++;
            totalValue += p.getInventoryValue();
        }
        summaryLabel.setText(String.format("  Total Products: %d      Low Stock: %d      Out of Stock: %d      Inventory Value: %s", store.getProducts().size(), lowCount, outCount, money.format(totalValue)));
    }

    private Product getSelectedProduct() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return store.findProductById(String.valueOf(model.getValueAt(row, 0)));
    }

    private void showProductDialog(Product existing) {
        JDialog dlg = new JDialog();
        dlg.setTitle(existing == null ? "Add Product" : "Edit Product");
        dlg.setModal(true);
        dlg.setSize(400, 340);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        JTextField idField = new JTextField(existing == null ? "" : existing.getId());
        JTextField nameField = new JTextField(existing == null ? "" : existing.getName());
        JComboBox<String> catCombo = new JComboBox<>();
        catCombo.setEditable(true);
        for (String c : store.getCategories()) catCombo.addItem(c);
        if (existing != null) catCombo.setSelectedItem(existing.getCategory());
        JTextField priceField = new JTextField(existing == null ? "" : String.valueOf(existing.getPrice()));
        JTextField stockField = new JTextField(existing == null ? "" : String.valueOf(existing.getStock()));
        JTextField lowField = new JTextField(existing == null ? "10" : String.valueOf(existing.getLowStockLevel()));

        form.add(new JLabel("Product ID:")); form.add(idField);
        form.add(new JLabel("Name:")); form.add(nameField);
        form.add(new JLabel("Category:")); form.add(catCombo);
        form.add(new JLabel("Price:")); form.add(priceField);
        form.add(new JLabel("Stock:")); form.add(stockField);
        form.add(new JLabel("Low Stock Alert:")); form.add(lowField);
        dlg.add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = new JButton("Save"), cancel = new JButton("Cancel");
        btns.add(save); btns.add(cancel);
        dlg.add(btns, BorderLayout.SOUTH);
        cancel.addActionListener(e -> dlg.dispose());

        save.addActionListener(e -> {
            try {
                String id = idField.getText().trim(), name = nameField.getText().trim();
                if (id.isEmpty() || name.isEmpty()) throw new IllegalArgumentException("Fields required.");
                Object catObj = catCombo.isEditable() ? catCombo.getEditor().getItem() : catCombo.getSelectedItem();
                String cat = catObj == null ? "Uncategorized" : catObj.toString().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int stock = Integer.parseInt(stockField.getText().trim()), low = Integer.parseInt(lowField.getText().trim());

                if (existing == null) {
                    if (store.findProductById(id) != null) throw new IllegalArgumentException("ID exists.");
                    Product newProduct = new Product(id, name, cat, price, stock, low);
                    if (DBconnection.addProduct(newProduct)) store.getProducts().add(newProduct);
                    else throw new IllegalArgumentException("Failed to add to MySQL.");
                } else {
                    if (!existing.getId().equalsIgnoreCase(id)) throw new IllegalArgumentException("Cannot change ID.");
                    Product updatedProduct = new Product(id, name, cat, price, stock, low);
                    if (DBconnection.updateProduct(updatedProduct)) {
                        existing.setName(name); existing.setCategory(cat); existing.setPrice(price); existing.setStock(stock); existing.setLowStockLevel(low);
                    } else throw new IllegalArgumentException("Failed to update MySQL.");
                }
                dlg.dispose(); refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dlg.setVisible(true);
    }

    private void deleteProduct() {
        Product p = getSelectedProduct();
        if (p == null) { warn("Select product."); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete \"" + p.getName() + "\"?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (DBconnection.deleteProduct(p.getId())) { store.getProducts().remove(p); refresh(); }
            else warn("Failed to delete. May have existing sales.");
        }
    }

    private void restockProduct() {
        Product p = getSelectedProduct();
        if (p == null) { warn("Select product."); return; }
        String input = JOptionPane.showInputDialog(this, "Current stock: " + p.getStock() + "\nEnter qty to add:", "Restock", JOptionPane.QUESTION_MESSAGE);
        if (input != null) {
            try {
                int qty = Integer.parseInt(input.trim());
                if (qty <= 0) throw new NumberFormatException();
                if (DBconnection.restockProduct(p.getId(), qty)) { p.setStock(p.getStock() + qty); refresh(); }
                else warn("Failed to restock MySQL.");
            } catch (Exception ex) { warn("Invalid number."); }
        }
    }
    private void warn(String msg) { JOptionPane.showMessageDialog(this, msg, "Inventory", JOptionPane.WARNING_MESSAGE); }
}