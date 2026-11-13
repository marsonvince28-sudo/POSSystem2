package com.pos.utils;

import java.util.ArrayList;
import java.util.List;

import com.pos.database.DBconnection;
import com.pos.models.Product;
import com.pos.models.Sale;
import com.pos.models.User;

public class DataStore {

    private static DataStore instance;

    private final List<Product> products = new ArrayList<>();
    private final List<User>    users    = new ArrayList<>();
    private final List<Sale>    sales    = new ArrayList<>();

    private DataStore() { }

    public static synchronized DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    public List<Product> getProducts() { return products; }
    public List<User>    getUsers()    { return users; }
    public List<Sale>    getSales()    { return sales; }

    public Product findProductById(String id) {
        for (Product p : products) {
            if (p.getId().equalsIgnoreCase(id)) return p;
        }
        return null;
    }

    public User findUser(String username) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) return u;
        }
        return null;
    }

    public List<String> getCategories() {
        List<String> cats = new ArrayList<>();
        for (Product p : products) {
            if (!cats.contains(p.getCategory())) cats.add(p.getCategory());
        }
        return cats;
    }

    public String nextSaleId() {
        int n = sales.size() + 1;
        String id;
        boolean clash;
        do {
            id = String.format("S%05d", n++);
            clash = false;
            for (Sale s : sales) {
                if (s.getSaleId().equals(id)) { clash = true; break; }
            }
        } while (clash);
        return id;
    }

    public void load() {
        products.clear();
        products.addAll(DBconnection.getAllProducts());

        users.clear();
        users.addAll(DBconnection.getAllUsers());
        if (users.isEmpty()) {
            DBconnection.addUser(new User("admin", "admin123", "System Administrator", User.ROLE_ADMIN, true));
            DBconnection.addUser(new User("cashier", "cashier123", "Juan Dela Cruz", User.ROLE_CASHIER, true));
            users.addAll(DBconnection.getAllUsers());
        }

        sales.clear();
        sales.addAll(DBconnection.getAllSales());
    }
}