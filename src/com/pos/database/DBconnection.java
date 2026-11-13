package com.pos.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.pos.models.Product;
import com.pos.models.Sale;
import com.pos.models.SaleItem;
import com.pos.models.User;

public class DBconnection {
	

	    private static final String URL =
	        "jdbc:mysql://localhost:3306/pos_system";

	    private static final String USER = "root";
	    private static final String PASSWORD = "";

	    public static Connection getConnection() {
	        try {
	            return DriverManager.getConnection(URL, USER, PASSWORD);
	        } catch (SQLException e) {
	            System.out.println("Database Connection Failed!");
	            e.printStackTrace();
	            return null;
	        }
	    }

	    /* =========================================================
	     *  PHASE 1 — USERS
	     * ========================================================= */

	    public static User login(String username, String password) {
	        String sql = "SELECT * FROM users "
	                   + "WHERE username = ? AND password = ? AND is_active = 1";

	        try (Connection conn = getConnection()) {
	            if (conn == null) return null;

	            try (PreparedStatement ps = conn.prepareStatement(sql)) {
	                ps.setString(1, username);
	                ps.setString(2, password);

	                try (ResultSet rs = ps.executeQuery()) {
	                    if (rs.next()) {
	                        return new User(
	                            rs.getString("username"),
	                            rs.getString("password"),
	                            rs.getString("full_name"),
	                            rs.getString("role"),
	                            rs.getBoolean("is_active")
	                        );
	                    }
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }

	    public static List<User> getAllUsers() {
	        List<User> users = new ArrayList<>();
	        String sql = "SELECT * FROM users ORDER BY username";

	        try (Connection conn = getConnection()) {
	            if (conn == null) return users;

	            try (PreparedStatement ps = conn.prepareStatement(sql);
	                 ResultSet rs = ps.executeQuery()) {

	                while (rs.next()) {
	                    users.add(new User(
	                        rs.getString("username"),
	                        rs.getString("password"),
	                        rs.getString("full_name"),
	                        rs.getString("role"),
	                        rs.getBoolean("is_active")
	                    ));
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return users;
	    }

	    public static boolean addUser(User u) {
	        String sql = "INSERT INTO users "
	                   + "(username, password, full_name, role, is_active) "
	                   + "VALUES (?, ?, ?, ?, ?)";

	        try (Connection conn = getConnection()) {
	            if (conn == null) return false;

	            try (PreparedStatement ps = conn.prepareStatement(sql)) {
	                ps.setString(1, u.getUsername());
	                ps.setString(2, u.getPassword());
	                ps.setString(3, u.getFullName());
	                ps.setString(4, u.getRole());
	                ps.setBoolean(5, u.isActive());
	                return ps.executeUpdate() > 0;
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	            return false;
	        }
	    }

	    public static boolean updateUser(User u) {
	        String sql = "UPDATE users SET password = ?, full_name = ?, "
	                   + "role = ?, is_active = ? WHERE username = ?";

	        try (Connection conn = getConnection()) {
	            if (conn == null) return false;

	            try (PreparedStatement ps = conn.prepareStatement(sql)) {
	                ps.setString(1, u.getPassword());
	                ps.setString(2, u.getFullName());
	                ps.setString(3, u.getRole());
	                ps.setBoolean(4, u.isActive());
	                ps.setString(5, u.getUsername());
	                return ps.executeUpdate() > 0;
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	            return false;
	        }
	    }

	    public static boolean deleteUser(String username) {
	        String sql = "DELETE FROM users WHERE username = ?";

	        try (Connection conn = getConnection()) {
	            if (conn == null) return false;

	            try (PreparedStatement ps = conn.prepareStatement(sql)) {
	                ps.setString(1, username);
	                return ps.executeUpdate() > 0;
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	            return false;
	        }
	    }

	    public static boolean setUserActive(String username, boolean active) {
	        String sql = "UPDATE users SET is_active = ? WHERE username = ?";

	        try (Connection conn = getConnection()) {
	            if (conn == null) return false;

	            try (PreparedStatement ps = conn.prepareStatement(sql)) {
	                ps.setBoolean(1, active);
	                ps.setString(2, username);
	                return ps.executeUpdate() > 0;
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	            return false;
	        }
	    }

	    /* =========================================================
	     *  PHASE 2 — PRODUCTS / INVENTORY
	     * ========================================================= */

	    public static List<Product> getAllProducts() {
	        List<Product> products = new ArrayList<>();
	        String sql = "SELECT product_id, product_name, category, "
	                   + "price, stock, low_stock_level "
	                   + "FROM products ORDER BY product_id";

	        try (Connection conn = getConnection()) {
	            if (conn == null) return products;

	            try (PreparedStatement ps = conn.prepareStatement(sql);
	                 ResultSet rs = ps.executeQuery()) {

	                while (rs.next()) {
	                    products.add(new Product(
	                        rs.getString("product_id"),
	                        rs.getString("product_name"),
	                        rs.getString("category"),
	                        rs.getDouble("price"),
	                        rs.getInt("stock"),
	                        rs.getInt("low_stock_level")
	                    ));
	                }
	            }
	        } catch (SQLException e) {
	            System.out.println("Failed to load products!");
	            e.printStackTrace();
	        }
	        return products;
	    }

	    public static boolean addProduct(Product p) {
	        String sql = "INSERT INTO products "
	                + "(product_id, product_name, category, price, stock, low_stock_level) "
	                + "VALUES (?, ?, ?, ?, ?, ?)";

	        try (Connection conn = getConnection()) {
	            if (conn == null) return false;

	            try (PreparedStatement ps = conn.prepareStatement(sql)) {
	                ps.setString(1, p.getId());
	                ps.setString(2, p.getName());
	                ps.setString(3, p.getCategory());
	                ps.setDouble(4, p.getPrice());
	                ps.setInt(5, p.getStock());
	                ps.setInt(6, p.getLowStockLevel());
	                return ps.executeUpdate() > 0;
	            }
	        } catch (SQLException e) {
	            System.out.println("Failed to add product!");
	            e.printStackTrace();
	            return false;
	        }
	    }

	    public static boolean updateProduct(Product p) {
	        String sql = "UPDATE products SET "
	                + "product_name = ?, category = ?, price = ?, "
	                + "stock = ?, low_stock_level = ? "
	                + "WHERE product_id = ?";

	        try (Connection conn = getConnection()) {
	            if (conn == null) return false;

	            try (PreparedStatement ps = conn.prepareStatement(sql)) {
	                ps.setString(1, p.getName());
	                ps.setString(2, p.getCategory());
	                ps.setDouble(3, p.getPrice());
	                ps.setInt(4, p.getStock());
	                ps.setInt(5, p.getLowStockLevel());
	                ps.setString(6, p.getId());
	                return ps.executeUpdate() > 0;
	            }
	        } catch (SQLException e) {
	            System.out.println("Failed to update product!");
	            e.printStackTrace();
	            return false;
	        }
	    }

	    public static boolean deleteProduct(String productId) {
	        String sql = "DELETE FROM products WHERE product_id = ?";

	        try (Connection conn = getConnection()) {
	            if (conn == null) return false;

	            try (PreparedStatement ps = conn.prepareStatement(sql)) {
	                ps.setString(1, productId);
	                return ps.executeUpdate() > 0;
	            }
	        } catch (SQLException e) {
	            System.out.println("Failed to delete product!");
	            e.printStackTrace();
	            return false;
	        }
	    }

	    public static boolean restockProduct(String productId, int qty) {
	        String sql = "UPDATE products SET stock = stock + ? WHERE product_id = ?";

	        try (Connection conn = getConnection()) {
	            if (conn == null) return false;

	            try (PreparedStatement ps = conn.prepareStatement(sql)) {
	                ps.setInt(1, qty);
	                ps.setString(2, productId);
	                return ps.executeUpdate() > 0;
	            }
	        } catch (SQLException e) {
	            System.out.println("Failed to restock product!");
	            e.printStackTrace();
	            return false;
	        }
	    }

	    /* =========================================================
	     *  PHASE 3 & 4 — SALES / SALE ITEMS
	     * ========================================================= */

	    public static List<Sale> getAllSales() {
	        List<Sale> sales = new ArrayList<>();
	        Map<String, Sale> byId = new LinkedHashMap<>();

	        try (Connection conn = getConnection()) {
	            if (conn == null) return sales;

	            // 1. Load sale headers (Gamit ang tamang column names)
	            String salesSql = "SELECT * FROM sales ORDER BY sale_timestamp, sale_id";
	            try (PreparedStatement ps = conn.prepareStatement(salesSql);
	                 ResultSet rs = ps.executeQuery()) {

	                while (rs.next()) {
	                    Sale s = new Sale();
	                    s.setSaleId(rs.getString("sale_id"));
	                    s.setTimestamp(rs.getLong("sale_timestamp"));
	                    s.setCashier(rs.getString("cashier"));
	                    s.setSubtotal(rs.getDouble("subtotal"));
	                    s.setDiscountPercent(rs.getDouble("discount_percent"));
	                    s.setDiscountAmount(rs.getDouble("discount_amount"));
	                    s.setTaxAmount(rs.getDouble("tax_amount"));
	                    s.setTotal(rs.getDouble("total"));
	                    s.setAmountPaid(rs.getDouble("amount_paid"));
	                    s.setChange(rs.getDouble("change_amount"));
	                    sales.add(s);
	                    byId.put(s.getSaleId(), s);
	                }
	            }

	            // 2. Load line items
	            String itemsSql = "SELECT * FROM sale_items ORDER BY id";
	            try (PreparedStatement ps = conn.prepareStatement(itemsSql);
	                 ResultSet rs = ps.executeQuery()) {

	                while (rs.next()) {
	                    String saleId = rs.getString("sale_id");
	                    Sale s = byId.get(saleId);
	                    if (s == null) continue;

	                    s.getItems().add(new SaleItem(
	                        rs.getString("product_id"),
	                        rs.getString("product_name"),
	                        rs.getDouble("unit_price"),
	                        rs.getInt("quantity")
	                    ));
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return sales;
	    }

	    public static boolean addSale(Sale sale) {
	        Connection conn = null;
	        try {
	            conn = getConnection();
	            if (conn == null) return false;

	            conn.setAutoCommit(false);

	            // 1. Insert sale header
	            
	            String saleSql = "INSERT INTO sales "
	                    + "(sale_id, sale_timestamp, cashier, subtotal, "
	                    + "discount_percent, discount_amount, tax_amount, "
	                    + "total, amount_paid, change_amount) "
	                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	            try (PreparedStatement ps = conn.prepareStatement(saleSql)) {
	                ps.setString(1, sale.getSaleId());
	                ps.setLong(2, sale.getTimestamp());
	                ps.setString(3, sale.getCashier());
	                ps.setDouble(4, sale.getSubtotal());
	                ps.setDouble(5, sale.getDiscountPercent());
	                ps.setDouble(6, sale.getDiscountAmount());
	                ps.setDouble(7, sale.getTaxAmount());
	                ps.setDouble(8, sale.getTotal());
	                ps.setDouble(9, sale.getAmountPaid());
	                ps.setDouble(10, sale.getChange());
	                ps.executeUpdate();
	            }

	            //  Insert sale items + decrement stock
	            String itemSql = "INSERT INTO sale_items "
	                    + "(sale_id, product_id, product_name, unit_price, quantity, line_total) "
	                    + "VALUES (?, ?, ?, ?, ?, ?)";

	            String stockSql = "UPDATE products SET stock = stock - ? "
	                    + "WHERE product_id = ? AND stock >= ?";

	            try (PreparedStatement itemPs = conn.prepareStatement(itemSql);
	                 PreparedStatement stockPs = conn.prepareStatement(stockSql)) {

	                for (SaleItem item : sale.getItems()) {
	                    // Insert line item
	                    itemPs.setString(1, sale.getSaleId());
	                    itemPs.setString(2, item.getProductId());
	                    itemPs.setString(3, item.getProductName());
	                    itemPs.setDouble(4, item.getUnitPrice());
	                    itemPs.setInt(5, item.getQuantity());
	                    itemPs.setDouble(6, item.getLineTotal());
	                    itemPs.executeUpdate();

	                    // Decrement stock 
	                    
	                    stockPs.setInt(1, item.getQuantity());
	                    stockPs.setString(2, item.getProductId());
	                    stockPs.setInt(3, item.getQuantity());
	                    int updated = stockPs.executeUpdate();

	                    if (updated == 0) {
	                        System.out.println("Insufficient stock for "
	                                + item.getProductId() + " — rolling back sale.");
	                        conn.rollback();
	                        return false;
	                    }
	                }
	            }

	            conn.commit();
	            return true;

	        } catch (SQLException e) {
	            System.out.println("Failed to save sale: " + e.getMessage());
	            e.printStackTrace();
	            if (conn != null) {
	                try { conn.rollback(); } catch (SQLException ignored) { }
	            }
	            return false;

	        } finally {
	            if (conn != null) {
	                try {
	                    conn.setAutoCommit(true);
	                    conn.close();
	                } catch (SQLException ignored) { }
	            }
	        }
	    }
	}
