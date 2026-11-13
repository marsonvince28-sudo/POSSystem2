package com.pos.views;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import com.pos.models.Product;
import com.pos.models.Sale;
import com.pos.models.SaleItem;
import com.pos.models.User;
import com.pos.database.DBconnection;
import com.pos.utils.DataStore;
import com.pos.utils.ReceiptDialog;
import com.pos.utils.Refreshable;

public class POSPanel extends JPanel implements Refreshable {

    public static final double TAX_RATE = 0.12;
    private final User cashier;
    private final DataStore store = DataStore.getInstance();
    private final DecimalFormat money = new DecimalFormat("#,##0.00");

    private JTextField searchField;
    private JComboBox<String> categoryCombo;
    private DefaultTableModel productModel;
    private JTable productTable;

    private DefaultTableModel cartModel;
    private JTable cartTable;
    private final List<SaleItem> cart = new ArrayList<>();

    private JTextField discountField;
    private JTextField paidField;
    private JLabel subtotalValue, discountValue, taxValue, totalValue, changeValue;
    private JLabel statusLabel;
    private double subtotal, discountAmount, taxAmount, total;

    public POSPanel(User cashier) {
        this.cashier = cashier;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(buildProductPanel(), BorderLayout.CENTER);
        add(buildCartPanel(), BorderLayout.EAST);
        refreshProducts();
        refreshCart();
    }

    private JPanel buildProductPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Product Catalog"));
        JPanel top = new JPanel(new BorderLayout(5, 5));
        searchField = new JTextField();
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { refreshProducts(); }
        });

        categoryCombo = new JComboBox<>();
        categoryCombo.setPreferredSize(new Dimension(160, 26));
        categoryCombo.addActionListener(e -> refreshProducts());
        top.add(new JLabel("Search: "), BorderLayout.WEST);
        top.add(searchField, BorderLayout.CENTER);
        top.add(categoryCombo, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        productModel = new DefaultTableModel(
                new Object[]{"ID", "Product Name", "Category", "Price", "Stock", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        productTable = new JTable(productModel);
        productTable.setRowHeight(24);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        productTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(column == 3 || column == 4 ? SwingConstants.RIGHT : SwingConstants.LEFT);
                if (!isSelected) {
                    Object idObj = table.getModel().getValueAt(row, 0);
                    Product p = idObj == null ? null : store.findProductById(idObj.toString());
                    if (p != null && p.isOutOfStock()) c.setBackground(new Color(255, 205, 205));
                    else if (p != null && p.isLowStock()) c.setBackground(new Color(255, 245, 200));
                    else c.setBackground(Color.WHITE);
                }
                return c;
            }
        });

        productTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) addSelectedToCart();
            }
        });
        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton addBtn = new JButton("Add to Cart  >>");
        addBtn.addActionListener(e -> addSelectedToCart());
        bottom.add(addBtn);
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(0, 120, 0));
        bottom.add(statusLabel);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setPreferredSize(new Dimension(450, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Current Transaction"));

        cartModel = new DefaultTableModel(new Object[]{"Item", "Qty", "Unit Price", "Line Total"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(24);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        for(int i=1; i<=3; i++) cartTable.getColumnModel().getColumn(i).setCellRenderer(right);
        panel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        JPanel btnRow = new JPanel(new GridLayout(1, 4, 5, 5));
        JButton plus = new JButton("+ Qty"), minus = new JButton("- Qty");
        JButton remove = new JButton("Remove"), clear = new JButton("Clear");
        btnRow.add(plus); btnRow.add(minus); btnRow.add(remove); btnRow.add(clear);
        south.add(btnRow); south.add(Box.createVerticalStrut(8));

        JPanel totals = new JPanel(new GridLayout(7, 2, 5, 5));
        totals.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        subtotalValue = new JLabel("0.00", SwingConstants.RIGHT);
        discountValue = new JLabel("0.00", SwingConstants.RIGHT);
        taxValue      = new JLabel("0.00", SwingConstants.RIGHT);
        totalValue    = new JLabel("0.00", SwingConstants.RIGHT);
        changeValue   = new JLabel("0.00", SwingConstants.RIGHT);
        totalValue.setFont(totalValue.getFont().deriveFont(Font.BOLD, 16f));
        totalValue.setForeground(new Color(0, 100, 0));

        discountField = new JTextField("0");
        discountField.setHorizontalAlignment(JTextField.RIGHT);
        discountField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { recalculate(); }
        });

        paidField = new JTextField();
        paidField.setHorizontalAlignment(JTextField.RIGHT);
        paidField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { recalculate(); }
        });

        totals.add(new JLabel("Subtotal:")); totals.add(subtotalValue);
        totals.add(new JLabel("Discount (%):")); totals.add(discountField);
        totals.add(new JLabel("Discount Amount:")); totals.add(discountValue);
        totals.add(new JLabel("VAT (" + (int)(TAX_RATE * 100) + "%):")); totals.add(taxValue);
        totals.add(new JLabel("TOTAL:")); totals.add(totalValue);
        totals.add(new JLabel("Amount Paid:")); totals.add(paidField);
        totals.add(new JLabel("Change:")); totals.add(changeValue);

        south.add(totals);
        south.add(Box.createVerticalStrut(6));
        JButton checkout = new JButton("COMPLETE SALE");
        checkout.setFont(checkout.getFont().deriveFont(Font.BOLD, 15f));
        checkout.setBackground(new Color(50, 150, 70));
        checkout.setForeground(Color.GREEN);
        checkout.setOpaque(true);
        south.add(checkout);
        panel.add(south, BorderLayout.SOUTH);

        plus.addActionListener(e -> changeQty(1));
        minus.addActionListener(e -> changeQty(-1));
        remove.addActionListener(e -> removeSelectedItem());
        clear.addActionListener(e -> clearCart());
        checkout.addActionListener(e -> completeSale());
        return panel;
    }

    @Override public void refresh() { refreshProducts(); refreshCart(); }

    private void refreshProducts() {
        String prevCat = (String) categoryCombo.getSelectedItem();
        categoryCombo.removeAllItems();
        categoryCombo.addItem("All Categories");
        for (String c : store.getCategories()) categoryCombo.addItem(c);
        if (prevCat != null) categoryCombo.setSelectedItem(prevCat);
        if (categoryCombo.getSelectedIndex() < 0) categoryCombo.setSelectedIndex(0);

        String keyword = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        String cat = (String) categoryCombo.getSelectedItem();
        productModel.setRowCount(0);
        for (Product p : store.getProducts()) {
            boolean matchKeyword = keyword.isEmpty() || p.getName().toLowerCase().contains(keyword) || p.getId().toLowerCase().contains(keyword);
            boolean matchCat = cat == null || "All Categories".equals(cat) || p.getCategory().equals(cat);
            if (matchKeyword && matchCat) {
                productModel.addRow(new Object[]{p.getId(), p.getName(), p.getCategory(), money.format(p.getPrice()), p.getStock(), p.getStatus()});
            }
        }
    }

    private void addSelectedToCart() {
        int row = productTable.getSelectedRow();
        if (row < 0) { warn("Please select a product first."); return; }
        String id = String.valueOf(productModel.getValueAt(row, 0));
        Product p = store.findProductById(id);
        if (p == null || p.isOutOfStock()) { warn(p.getName() + " is out of stock."); return; }
        SaleItem existing = null;
        for (SaleItem it : cart) if (it.getProductId().equalsIgnoreCase(p.getId())) { existing = it; break; }
        int inCart = existing == null ? 0 : existing.getQuantity();
        if (inCart + 1 > p.getStock()) { warn("Only " + p.getStock() + " unit(s) available."); return; }
        if (existing == null) cart.add(new SaleItem(p.getId(), p.getName(), p.getPrice(), 1));
        else existing.setQuantity(existing.getQuantity() + 1);
        refreshCart();
        statusLabel.setText("Added: " + p.getName());
    }

    private void changeQty(int delta) {
        int row = cartTable.getSelectedRow();
        if (row < 0) { warn("Select an item in the cart first."); return; }
        SaleItem item = cart.get(row);
        Product p = store.findProductById(item.getProductId());
        int newQty = item.getQuantity() + delta;
        if (newQty <= 0) cart.remove(row);
        else if (p != null && newQty > p.getStock()) { warn("Only " + p.getStock() + " unit(s) available."); return; }
        else item.setQuantity(newQty);
        refreshCart();
    }

    private void removeSelectedItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0) { warn("Select an item in the cart first."); return; }
        cart.remove(row);
        refreshCart();
    }

    private void clearCart() {
        if (cart.isEmpty()) return;
        if (JOptionPane.showConfirmDialog(this, "Clear the current transaction?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            cart.clear(); discountField.setText("0"); paidField.setText(""); refreshCart();
        }
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        for (SaleItem it : cart) cartModel.addRow(new Object[]{it.getProductName(), it.getQuantity(), money.format(it.getUnitPrice()), money.format(it.getLineTotal())});
        recalculate();
    }

    private void recalculate() {
        subtotal = 0;
        for (SaleItem it : cart) subtotal += it.getLineTotal();
        double discPct = parseDouble(discountField.getText());
        if (discPct < 0) discPct = 0;
        if (discPct > 100) discPct = 100;
        discountAmount = subtotal * discPct / 100.0;
        taxAmount = (subtotal - discountAmount) * TAX_RATE;
        total = (subtotal - discountAmount) + taxAmount;
        double change = parseDouble(paidField.getText()) - total;

        subtotalValue.setText(money.format(subtotal)); discountValue.setText(money.format(discountAmount));
        taxValue.setText(money.format(taxAmount)); totalValue.setText(money.format(total));
        changeValue.setText(money.format(change));
        changeValue.setForeground(change < 0 ? Color.RED : new Color(0, 100, 0));
    }

    private void completeSale() {
        if (cart.isEmpty()) { warn("The cart is empty."); return; }
        recalculate();
        double paid = parseDouble(paidField.getText());
        if (paid <= 0 || paid < total) { warn("Invalid amount paid."); return; }

        Sale sale = new Sale();
        sale.setSaleId(store.nextSaleId());
        sale.setTimestamp(System.currentTimeMillis());
        sale.setCashier(cashier.getUsername()); // GAMIT NA ANG USERNAME
        sale.setItems(new ArrayList<>(cart));
        sale.setSubtotal(subtotal);
        sale.setDiscountPercent(parseDouble(discountField.getText()));
        sale.setDiscountAmount(discountAmount);
        sale.setTaxAmount(taxAmount);
        sale.setTotal(total);
        sale.setAmountPaid(paid);
        sale.setChange(paid - total);

        if (!DBconnection.addSale(sale)) {
            JOptionPane.showMessageDialog(this, "Could not save the sale to the database.\nStock may have been insufficient.", "Sale Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (SaleItem it : cart) { Product p = store.findProductById(it.getProductId()); if (p != null) p.setStock(p.getStock() - it.getQuantity()); }
        store.getSales().add(sale);

        ReceiptDialog.showReceipt(this, sale);
        cart.clear(); discountField.setText("0"); paidField.setText(""); refreshCart(); refreshProducts();
        statusLabel.setText("Sale " + sale.getSaleId() + " completed successfully.");
    }

    private double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        try { return Double.parseDouble(s.trim().replace(",", "")); } catch (Exception e) { return 0; }
    }
    private void warn(String msg) { JOptionPane.showMessageDialog(this, msg, "POS", JOptionPane.WARNING_MESSAGE); }
}