package com.pos.models;

import java.io.Serializable;

public class SaleItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String productId;
    private String productName;
    private double unitPrice;
    private int quantity;

    public SaleItem(String productId, String productName, double unitPrice, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getLineTotal() { return unitPrice * quantity; }
}