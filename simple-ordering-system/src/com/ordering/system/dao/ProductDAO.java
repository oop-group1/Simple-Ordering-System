package com.ordering.system.dao;

import com.ordering.system.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private SQLConn db = SQLConn.getInstance();

    public Product getProductById(int itemId) {
        String sql = "SELECT * FROM tblproducts WHERE item_id = ?";
        try {
            ResultSet rs = db.executeQuery(sql, itemId);
            if (rs.next()) {
                return new Product(rs.getInt("item_id"), rs.getString("item_name"), 
                                   rs.getString("item_desc"), rs.getDouble("item_price"), 
                                   rs.getInt("item_qty"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addProduct(Product product) {
        String sql = "INSERT INTO tblproducts (item_name, item_desc, item_price, item_qty) VALUES (?, ?, ?, ?)";
        try {
            return db.executeUpdateDelete(sql, product.getItemName(), product.getItemDesc(), 
                                         product.getItemPrice(), product.getItemQty()) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM tblproducts ORDER BY item_name";
        try {
            ResultSet rs = db.executeQuery(sql);
            while (rs.next()) {
                products.add(new Product(rs.getInt("item_id"), rs.getString("item_name"), 
                                         rs.getString("item_desc"), rs.getDouble("item_price"), 
                                         rs.getInt("item_qty")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public boolean updateStock(int itemId, int newQty) {
        String sql = "UPDATE tblproducts SET item_qty = ? WHERE item_id = ?";
        try {
            return db.executeUpdateDelete(sql, newQty, itemId) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteProduct(int itemId) {
        String sql = "DELETE FROM tblproducts WHERE item_id = ?";
        try {
            return db.executeUpdateDelete(sql, itemId) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Product searchProduct(String query) {
        String sql = "SELECT * FROM tblproducts WHERE item_name LIKE ?";
        try {
            ResultSet rs = db.executeQuery(sql, "%" + query + "%");
            if (rs.next()) {
                return new Product(rs.getInt("item_id"), rs.getString("item_name"), 
                                   rs.getString("item_desc"), rs.getDouble("item_price"), 
                                   rs.getInt("item_qty"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}