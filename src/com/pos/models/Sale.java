package com.pos.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Sale implements Serializable {
    private static final long serialVersionUID = 1L;

    private String saleId;
    private long timestamp;
    private String cashier;
    private List<SaleItem> items = new ArrayList<>();

    private double subtotal;
    private double discountPercent;
    private double discountAmount;
    private double taxAmount;
    private double total;
    private double amountPaid;
    private double change;

    public String getSaleId() { return saleId; }
    public void setSaleId(String saleId) { this.saleId = saleId; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public Date getDate() { return new Date(timestamp); }
    public String getCashier() { return cashier; }
    public void setCashier(String cashier) { this.cashier = cashier; }
    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(double discountPercent) { this.discountPercent = discountPercent; }
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }
    public double getChange() { return change; }
    public void setChange(double change) { this.change = change; }

    public int getTotalItemCount() {
        int n = 0;
        for (SaleItem i : items) n += i.getQuantity();
        return n;
    }
}