package com.pos.models;

import java.io.Serializable;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String category;
    private double price;
    private int stock;
    private int lowStockLevel;

    public Product(String id, String name, String category, double price, int stock, int lowStockLevel) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.lowStockLevel = lowStockLevel;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public int getLowStockLevel() { return lowStockLevel; }
    public void setLowStockLevel(int lowStockLevel) { this.lowStockLevel = lowStockLevel; }

    public boolean isLowStock() { return stock > 0 && stock <= lowStockLevel; }
    public boolean isOutOfStock() { return stock <= 0; }
    public double getInventoryValue() { return price * stock; }

    public String getStatus() {
        if (isOutOfStock()) return "Out of Stock";
        if (isLowStock()) return "Low Stock";
        return "OK";
    }

    @Override public String toString() { return name; }
}