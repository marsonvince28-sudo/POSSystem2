package com.pos.views;

import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import com.pos.models.Sale;
import com.pos.database.DBconnection;
import com.pos.utils.DataStore;
import com.pos.utils.ReceiptDialog;
import com.pos.utils.Refreshable;

public class SalesReportPanel extends JPanel implements Refreshable {

    private final DataStore store = DataStore.getInstance();
    private final DecimalFormat money = new DecimalFormat("#,##0.00");
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private JComboBox<String> filterCombo;
    private DefaultTableModel model;
    private JTable table;
    private JLabel txLabel, revenueLabel, itemsLabel, avgLabel;

    public SalesReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buildUI();
        refresh();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout(8, 8));
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filterPanel.add(new JLabel("Period:"));
        filterCombo = new JComboBox<>(new String[]{"All Sales", "Today", "This Week", "This Month"});
        filterCombo.addActionListener(e -> refresh());
        filterPanel.add(filterCombo);

        JButton refreshBtn = new JButton("Refresh"); refreshBtn.addActionListener(e -> refresh()); filterPanel.add(refreshBtn);
        JButton viewBtn = new JButton("View Receipt"); viewBtn.addActionListener(e -> viewReceipt()); filterPanel.add(viewBtn);
        JButton exportBtn = new JButton("Export CSV"); exportBtn.addActionListener(e -> exportCsv()); filterPanel.add(exportBtn);
        top.add(filterPanel, BorderLayout.WEST);

        JPanel summary = new JPanel(new GridLayout(1, 4, 10, 0));
        txLabel = makeSummaryLabel("Transactions: 0"); revenueLabel = makeSummaryLabel("Total Revenue: 0.00");
        itemsLabel = makeSummaryLabel("Items Sold: 0"); avgLabel = makeSummaryLabel("Average Sale: 0.00");
        summary.add(txLabel); summary.add(revenueLabel); summary.add(itemsLabel); summary.add(avgLabel);
        top.add(summary, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"Sale ID", "Date & Time", "Cashier", "Items", "Subtotal", "Discount", "VAT", "Total"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 3; i <= 7; i++) table.getColumnModel().getColumn(i).setCellRenderer(right);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private JLabel makeSummaryLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setOpaque(true); l.setBackground(new Color(235, 242, 250));
        l.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 215, 230)), BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        l.setFont(l.getFont().deriveFont(Font.BOLD)); return l;
    }

    @Override
    public void refresh() {
        store.getSales().clear(); store.getSales().addAll(DBconnection.getAllSales());
        model.setRowCount(0);
        int txCount = 0, itemCount = 0; double revenue = 0;
        for (Sale s : store.getSales()) {
            if (!matchesFilter(s)) continue;
            model.addRow(new Object[]{s.getSaleId(), dateFmt.format(s.getDate()), s.getCashier(), s.getTotalItemCount(), money.format(s.getSubtotal()), money.format(s.getDiscountAmount()), money.format(s.getTaxAmount()), money.format(s.getTotal())});
            txCount++; itemCount += s.getTotalItemCount(); revenue += s.getTotal();
        }
        txLabel.setText("Transactions: " + txCount); revenueLabel.setText("Total Revenue: " + money.format(revenue));
        itemsLabel.setText("Items Sold: " + itemCount); avgLabel.setText("Average Sale: " + money.format(txCount == 0 ? 0 : revenue / txCount));
    }

    private boolean matchesFilter(Sale s) {
        String f = (String) filterCombo.getSelectedItem();
        if (f == null || "All Sales".equals(f)) return true;
        Calendar now = Calendar.getInstance(), sc = Calendar.getInstance(); sc.setTime(s.getDate());
        switch (f) {
            case "Today": return now.get(Calendar.YEAR) == sc.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == sc.get(Calendar.DAY_OF_YEAR);
            case "This Week": return now.get(Calendar.YEAR) == sc.get(Calendar.YEAR) && now.get(Calendar.WEEK_OF_YEAR) == sc.get(Calendar.WEEK_OF_YEAR);
            case "This Month": return now.get(Calendar.YEAR) == sc.get(Calendar.YEAR) && now.get(Calendar.MONTH) == sc.get(Calendar.MONTH);
            default: return true;
        }
    }

    private void viewReceipt() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select transaction first."); return; }
        String saleId = String.valueOf(model.getValueAt(row, 0));
        for (Sale s : store.getSales()) if (s.getSaleId().equals(saleId)) { ReceiptDialog.showReceipt(this, s); return; }
    }

    private void exportCsv() {
        if (model.getRowCount() == 0) { JOptionPane.showMessageDialog(this, "Nothing to export."); return; }
        JFileChooser chooser = new JFileChooser(); chooser.setSelectedFile(new File("sales_report.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(chooser.getSelectedFile())) {
                for (int c = 0; c < model.getColumnCount(); c++) { if (c > 0) pw.print(","); pw.print(model.getColumnName(c)); }
                pw.println();
                for (int r = 0; r < model.getRowCount(); r++) {
                    for (int c = 0; c < model.getColumnCount(); c++) { if (c > 0) pw.print(","); pw.print(String.valueOf(model.getValueAt(r, c)).replace(",", "")); }
                    pw.println();
                }
                JOptionPane.showMessageDialog(this, "Exported successfully.");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Export failed."); }
        }
    }
}