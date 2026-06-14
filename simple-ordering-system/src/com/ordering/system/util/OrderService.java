package com.ordering.system.util;

import com.ordering.system.dao.*;
import com.ordering.system.model.*;
import java.util.*;

public class OrderService extends AbstractService {
    private ProductDAO productDao = new ProductDAO();
    private OrderDAO orderDao = new OrderDAO();

    @Override
    public boolean validateInput(Object data) {
        return data != null;
    }

    // Polymorphism: Implementing calculation for an order
    @Override
    public double calculateTotal(List<?> items) {
        double total = 0;
        for (Object item : items) {
            if (item instanceof OrderItem) {
                OrderItem oi = (OrderItem) item;
                total += oi.getUnitPrice() * oi.getQuantity();
            }
        }
        return total;
    }

    public Order placeOrder(User staffUser, int customerId, Map<Integer, Integer> cart) {
        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Product p = productDao.getProductById(entry.getKey());
            if (p == null || p.getItemQty() < entry.getValue()) {
                System.err.println("[Error] Product " + entry.getKey() + " unavailable or insufficient stock.");
                return null;
            }
            orderItems.add(new OrderItem(p.getItemId(), entry.getValue(), p.getItemPrice()));
            total += p.getItemPrice() * entry.getValue();
        }

        // Create Order object using the customer's ID
        Order order = new Order(0, customerId, total);
        
        // Pass the logged-in staff's ID for the staff_id column
        boolean success = orderDao.placeOrderTransaction(order, orderItems, staffUser.getUserId());

        return success ? order : null;
    }
}