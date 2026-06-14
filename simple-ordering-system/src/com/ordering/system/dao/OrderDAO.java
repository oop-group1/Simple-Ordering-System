package com.ordering.system.dao;

import com.ordering.system.model.*;
import java.sql.*;
import java.util.List;

public class OrderDAO {
    private SQLConn db = SQLConn.getInstance();

    /**
     * Handles the order process as a single ACID Transaction (Atomicity, Consistency, Isolation, and Durability).
     * If any part fails, everything is rolled back.
     */
    public boolean placeOrderTransaction(Order order, List<OrderItem> itemDetails, int staffId) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // Create the Order record
            String orderSql = "INSERT INTO tblorders (user_id, staff_id, total_amount, status) VALUES (?, ?, ?, 'PENDING')";
            try (PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setInt(1, order.getUserId());
                orderStmt.setInt(2, staffId);
                orderStmt.setDouble(3, order.getTotalAmount());
                orderStmt.executeUpdate();

                ResultSet rs = orderStmt.getGeneratedKeys();
                if (!rs.next()) throw new SQLException("Order ID generation failed.");
                int generatedOrderId = rs.getInt(1);

                // Insert items and deduct stock
                String itemSql = "INSERT INTO tblorder_items (order_id, item_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";
                String stockSql = "UPDATE tblproducts SET item_qty = item_qty - ? WHERE item_id = ?";

                try (PreparedStatement itemStmt = conn.prepareStatement(itemSql);
                     PreparedStatement stockStmt = conn.prepareStatement(stockSql)) {
                    
                    for (OrderItem item : itemDetails) {
                        itemStmt.setInt(1, generatedOrderId);
                        itemStmt.setInt(2, item.getProductId());
                        itemStmt.setInt(3, item.getQuantity());
                        itemStmt.setDouble(4, item.getUnitPrice());
                        itemStmt.addBatch();

                        stockStmt.setInt(1, item.getQuantity());
                        stockStmt.setInt(2, item.getProductId());
                        stockStmt.addBatch();
                    }
                    itemStmt.executeBatch();
                    stockStmt.executeBatch();
                }
            }

            conn.commit(); // Save all changes
            return true;
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }
}