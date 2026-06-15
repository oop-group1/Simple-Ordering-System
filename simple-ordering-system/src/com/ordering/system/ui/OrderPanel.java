package com.ordering.system.ui;

import com.ordering.system.model.Order;
import com.ordering.system.model.Product;
import com.ordering.system.model.User;
import com.ordering.system.util.OrderService;
import com.ordering.system.util.ProductService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * New Order tab: pick products on the left, build a cart on the right, then
 * place the order. The order itself is handled by OrderService.
 */
public class OrderPanel extends JPanel {

    // --- Fields ---

    private final ProductService productService;
    private final OrderService orderService;
    private final User staffUser;

    private DefaultTableModel productModel =
            new ReadOnlyTableModel(new String[]{"ID", "Name", "Price", "Stock"});
    private JTable productTable = new JTable(productModel);

    private DefaultTableModel cartModel =
            new ReadOnlyTableModel(new String[]{"ID", "Name", "Qty", "Subtotal"});
    private JTable cartTable = new JTable(cartModel);

    private List<Product> products = new ArrayList<>();    // loaded products (for lookups)
    private Map<Integer, Integer> cart = new HashMap<>();  // product id -> quantity ordered

    private JTextField qtyField = new JTextField("1", 4);
    private JComboBox<String> customerBox = new JComboBox<>();
    private JLabel totalLabel = new JLabel("Total: ₱0.00");

    public OrderPanel(ProductService productService, OrderService orderService, User staffUser) {
        this.productService = productService;
        this.orderService = orderService;
        this.staffUser = staffUser;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Theme.BACKGROUND);
        Theme.field(qtyField);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);

        loadCustomers();
        loadProducts();
    }

    // --- Layout ---

    // Two equal-width halves: products to choose from, and the cart.
    private JPanel buildCenter() {
        JButton addBtn = new JButton("Add to Cart");
        addBtn.addActionListener(e -> addToCart());
        Theme.primary(addBtn);
        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addRow.setOpaque(false);
        addRow.add(new JLabel("Qty:"));
        addRow.add(qtyField);
        addRow.add(addBtn);

        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.setOpaque(false);
        left.add(sectionLabel("Products"), BorderLayout.NORTH);
        left.add(new JScrollPane(productTable), BorderLayout.CENTER);
        left.add(addRow, BorderLayout.SOUTH);

        JButton removeBtn = new JButton("Remove Selected");
        removeBtn.addActionListener(e -> removeFromCart());
        Theme.button(removeBtn);
        JButton clearBtn = new JButton("Clear Cart");
        clearBtn.addActionListener(e -> clearCart());
        Theme.button(clearBtn);
        JPanel cartButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cartButtons.setOpaque(false);
        cartButtons.add(removeBtn);
        cartButtons.add(clearBtn);

        JPanel right = new JPanel(new BorderLayout(5, 5));
        right.setOpaque(false);
        right.add(sectionLabel("Cart"), BorderLayout.NORTH);
        right.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        right.add(cartButtons, BorderLayout.SOUTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 10, 0));
        center.setOpaque(false);
        center.add(left);
        center.add(right);
        return center;
    }

    // Customer + refresh on the left, total + place-order on the right.
    private JPanel buildBottom() {
        JButton placeBtn = new JButton("Place Order");
        placeBtn.addActionListener(e -> placeOrder());
        Theme.primary(placeBtn);
        JButton refreshBtn = new JButton("Refresh Products");
        refreshBtn.addActionListener(e -> loadProducts());
        Theme.button(refreshBtn);

        totalLabel.setFont(Theme.SECTION);
        totalLabel.setForeground(Theme.TEXT);

        JLabel custLabel = new JLabel("Customer:");
        custLabel.setFont(Theme.HEADING);
        custLabel.setForeground(Theme.TEXT);

        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        leftSide.setOpaque(false);
        leftSide.add(custLabel);
        leftSide.add(customerBox);
        leftSide.add(refreshBtn);

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        rightSide.setOpaque(false);
        rightSide.add(totalLabel);
        rightSide.add(placeBtn);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        bottom.add(leftSide, BorderLayout.WEST);
        bottom.add(rightSide, BorderLayout.EAST);
        return bottom;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.SECTION);
        label.setForeground(Theme.TEXT);
        return label;
    }

    // --- Data loading ---

    // Fixed list that matches the customers seeded in the database. Loading it
    // from the database would need a new backend method, so it is set here.
    private void loadCustomers() {
        customerBox.addItem("1 - Test Customer");
        customerBox.addItem("2 - Juan Dela Cruz");
        customerBox.addItem("3 - Maria Santos");
        customerBox.addItem("4 - Pedro Reyes");
        customerBox.addItem("5 - Ana Lim");
    }

    private void loadProducts() {
        products = productService.getAllProducts();
        productModel.setRowCount(0);
        for (Product p : products) {
            productModel.addRow(new Object[]{
                p.getItemId(), p.getItemName(), Theme.money(p.getItemPrice()), p.getItemQty()
            });
        }
    }

    private Product findProduct(int id) {
        for (Product p : products) {
            if (p.getItemId() == id) return p;
        }
        return null;
    }

    // --- Cart ---

    private void addToCart() {
        int row = productTable.getSelectedRow();
        if (row == -1) {
            message("Please select a product.");
            return;
        }
        int id = (Integer) productModel.getValueAt(row, 0);
        Product p = findProduct(id);

        try {
            int qty = Integer.parseInt(qtyField.getText().trim());
            if (qty <= 0) {
                message("Quantity must be at least 1.");
                return;
            }
            // Keep the cart from exceeding what's actually in stock.
            int inCart = cart.getOrDefault(id, 0);
            if (inCart + qty > p.getItemQty()) {
                message("Not enough stock. Only " + p.getItemQty() + " available.");
                return;
            }
            cart.put(id, inCart + qty);
            refreshCart();
        } catch (NumberFormatException ex) {
            message("Quantity must be a number.");
        }
    }

    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row == -1) {
            message("Please select a cart item.");
            return;
        }
        int id = (Integer) cartModel.getValueAt(row, 0);
        cart.remove(id);
        refreshCart();
    }

    private void clearCart() {
        if (cart.isEmpty()) {
            message("The cart is already empty.");
            return;
        }
        if (confirm("Clear the whole cart?")) {
            cart.clear();
            refreshCart();
        }
    }

    // Redraws the cart table and recalculates the running total.
    private void refreshCart() {
        cartModel.setRowCount(0);
        double total = 0;
        for (Integer id : cart.keySet()) {
            Product p = findProduct(id);
            int qty = cart.get(id);
            double subtotal = p.getItemPrice() * qty;
            total += subtotal;
            cartModel.addRow(new Object[]{p.getItemId(), p.getItemName(), qty, Theme.money(subtotal)});
        }
        totalLabel.setText("Total: " + Theme.money(total));
    }

    // --- Placing the order ---

    private void placeOrder() {
        if (cart.isEmpty()) {
            message("Your cart is empty.");
            return;
        }

        // The dropdown items look like "2 - Juan Dela Cruz"; take the leading id.
        String selected = (String) customerBox.getSelectedItem();
        int customerId = Integer.parseInt(selected.split(" - ")[0]);

        if (!confirm("Place this order for " + selected + "?")) return;

        Order order = orderService.placeOrder(staffUser, customerId, cart);
        if (order != null) {
            message(buildReceipt(selected, order.getTotalAmount()));
            cart.clear();
            refreshCart();
            loadProducts();   // stock was deducted, so reload the product list
        } else {
            message("Order failed.\nThe customer may not exist, or there is not enough stock.");
        }
    }

    private String buildReceipt(String customer, double grandTotal) {
        String receipt = "ORDER RECEIPT\n";
        receipt += "Customer: " + customer + "\n";
        receipt += "----------------------------\n";
        for (Integer id : cart.keySet()) {
            Product p = findProduct(id);
            int qty = cart.get(id);
            receipt += p.getItemName() + " x" + qty + "  =  " + Theme.money(p.getItemPrice() * qty) + "\n";
        }
        receipt += "----------------------------\n";
        receipt += "TOTAL: " + Theme.money(grandTotal);
        return receipt;
    }

    // --- Small helpers ---

    private void message(String text) {
        JOptionPane.showMessageDialog(this, text);
    }

    private boolean confirm(String text) {
        return JOptionPane.showConfirmDialog(this, text, "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
