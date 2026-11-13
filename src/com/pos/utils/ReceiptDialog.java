package com.pos.utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import com.pos.models.Sale;
import com.pos.models.SaleItem;

public class ReceiptDialog {

    private static final int WIDTH = 48;

    public static String buildReceiptText(Sale sale) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        String line = repeat('=', WIDTH);
        String dash = repeat('-', WIDTH);

        sb.append(line).append("\n");
        sb.append(center("YOLLS FRUIT & VEGETABLE STALL", WIDTH)).append("\n");
        sb.append(center("Uptown Bypass road, Cagayan De Oro City", WIDTH)).append("\n");
        sb.append(center("Tel: 0912-345-6789", WIDTH)).append("\n");
        sb.append(line).append("\n");
        sb.append(String.format("Receipt No : %s%n", sale.getSaleId()));
        sb.append(String.format("Date       : %s%n", sdf.format(sale.getDate())));
        sb.append(String.format("Cashier    : %s%n", sale.getCashier()));
        sb.append(dash).append("\n");

        for (SaleItem it : sale.getItems()) {
            sb.append(String.format("  %-18s %3d x %8.2f = %10.2f%n",
                    trim(it.getProductName(), 18),
                    it.getQuantity(), it.getUnitPrice(), it.getLineTotal()));
        }

        sb.append(dash).append("\n");
        sb.append(String.format("%-35s = %10.2f%n", "Subtotal", sale.getSubtotal()));
        if (sale.getDiscountAmount() > 0) {
            sb.append(String.format("%-35s = %10.2f%n",
                    String.format("Discount (%.2f%%)", sale.getDiscountPercent()),
                    sale.getDiscountAmount()));
        }
        sb.append(String.format("%-35s = %10.2f%n", "VAT", sale.getTaxAmount()));
        sb.append(line).append("\n");
        sb.append(String.format("%-35s = %10.2f%n", "TOTAL", sale.getTotal()));
        sb.append(String.format("%-35s = %10.2f%n", "Amount Paid", sale.getAmountPaid()));
        sb.append(String.format("%-35s = %10.2f%n", "Change", sale.getChange()));
        sb.append(line).append("\n");
        sb.append(center("Thank you for your purchase!", WIDTH)).append("\n");
        sb.append(center("Please come again.", WIDTH)).append("\n");
        sb.append(line).append("\n");

        return sb.toString();
    }

    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    private static String center(String s, int width) {
        if (s.length() >= width) return s;
        int pad = (width - s.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pad; i++) sb.append(' ');
        sb.append(s);
        return sb.toString();
    }

    private static String trim(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }

    public static void showReceipt(Component parent, Sale sale) {
        String text = buildReceiptText(sale);

        final JDialog dialog = new JDialog();
        dialog.setTitle("Receipt - " + sale.getSaleId());
        dialog.setModal(true);
        dialog.setSize(540, 620);
        dialog.setLocationRelativeTo(parent);

        JTextArea area = new JTextArea(text);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        area.setMargin(new Insets(8, 8, 8, 8));
        dialog.add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton saveBtn  = new JButton("Save to File");
        JButton printBtn = new JButton("Print");
        JButton closeBtn = new JButton("Close");
        btns.add(saveBtn); btns.add(printBtn); btns.add(closeBtn);
        dialog.add(btns, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("receipt_" + sale.getSaleId() + ".txt"));
            if (chooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                try (FileWriter fw = new FileWriter(chooser.getSelectedFile())) {
                    fw.write(text);
                    JOptionPane.showMessageDialog(dialog, "Receipt saved.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Could not save: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        printBtn.addActionListener(e -> {
            try {
                area.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Printing failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        closeBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }
}