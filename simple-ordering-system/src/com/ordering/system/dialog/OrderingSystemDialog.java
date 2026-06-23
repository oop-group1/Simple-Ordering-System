package com.ordering.system.dialog;

import com.ordering.system.model.Order;
import com.ordering.system.model.Product;
import com.ordering.system.model.User;
import com.ordering.system.util.OrderService;
import com.ordering.system.util.ProductService;
import com.ordering.system.util.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Backup version of the Ordering System that uses ONLY JOptionPane dialogs
 * (no Swing windows). It is menu-driven and calls the same service layer as
 * the GUI, so no backend logic is changed. 
 */
public class OrderingSystemDialog {

    private static UserService userService = new UserService();
    private static ProductService productService = new ProductService();
    private static OrderService orderService = new OrderService();

    public static void main(String[] args) {
        User user = login();
        if (user == null) return;              // cancelled or failed

        String menu = "MAIN MENU  (" + user.getUsername() + ")\n\n"
                + "1 - Add Product\n"
                + "2 - View Products\n"
                + "3 - Search Product\n"
                + "4 - Update Stock\n"
                + "5 - Delete Product\n"
                + "6 - Place Order\n"
                + "7 - Report\n"
                + "0 - Logout\n\n"
                + "Enter your choice:";

        while (true) {
            String choice = Dialogs.ask(menu);
            if (choice == null) choice = "0";   // closing the box = logout

            switch (choice.trim()) {
                case "1": addProduct();     break;
                case "2": viewProducts();   break;
                case "3": searchProduct();  break;
                case "4": updateStock();    break;
                case "5": deleteProduct();  break;
                case "6": placeOrder(user); break;
                case "7": report();         break;
                case "0": Dialogs.info("Logged out. Goodbye!"); return;
                default:  Dialogs.info("Invalid choice.");
            }
        }
    }

    // --- Login ---

    private static User login() {
        while (true) {
            String email = Dialogs.ask("LOGIN\n\nEmail (default: admin@system.com):");
            if (email == null) return null;
            String password = Dialogs.ask("Password (default: admin123):");
            if (password == null) return null;

            try {
                User user = userService.loginUser(email.trim(), password);
                if (user != null) {
                    Dialogs.info("Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
                    return user;
                }
                Dialogs.info("Invalid email or password.");   // loop and ask again
            } catch (Exception e) {
                Dialogs.info("Login error: " + e.getMessage());
                return null;
            }
        }
    }

    // --- Product features ---

    private static void addProduct() {
        String name = Dialogs.ask("Product name:");
        if (name == null) return;
        String desc = Dialogs.ask("Description:");
        if (desc == null) return;
        String priceText = Dialogs.ask("Price:");
        if (priceText == null) return;
        String qtyText = Dialogs.ask("Quantity:");
        if (qtyText == null) return;

        try {
            double price = Double.parseDouble(priceText.trim());
            int qty = Integer.parseInt(qtyText.trim());
            if (productService.addNewProduct(new Product(0, name, desc, price, qty))) {
                Dialogs.info("Product added.");
            } else {
                Dialogs.info("Invalid details. Name is required and price must be above 0.");
            }
        } catch (NumberFormatException e) {
            Dialogs.info("Price and quantity must be numbers.");
        }
    }

    private static void viewProducts() {
        List<Product> products = productService.getAllProducts();
        if (products.isEmpty()) {
            Dialogs.info("No products yet.");
        } else {
            Dialogs.info(listProducts(products));
        }
    }

    private static void searchProduct() {
        String keyword = Dialogs.ask("Enter a product name or ID to search:");
        if (keyword == null) return;
        keyword = keyword.trim().toLowerCase();

        String found = "";
        for (Product p : productService.getAllProducts()) {
            boolean matchesId   = String.valueOf(p.getItemId()).contains(keyword);
            boolean matchesName = p.getItemName().toLowerCase().contains(keyword);
            if (matchesId || matchesName) {
                found += "ID " + p.getItemId() + " | " + p.getItemName()
                        + " | " + peso(p.getItemPrice()) + " | stock " + p.getItemQty() + "\n";
            }
        }

        if (found.isEmpty()) {
            Dialogs.info("No product found.");
        } else {
            Dialogs.info("SEARCH RESULTS:\n" + found);
        }
    }

    private static void updateStock() {
        Product p = askForProduct("update");
        if (p == null) return;

        String changeText = Dialogs.ask(
                productDetails(p) + "\n\nStock change (use - to reduce, e.g. -5):");
        if (changeText == null) return;

        try {
            int change = Integer.parseInt(changeText.trim());
            if (productService.updateStock(p.getItemId(), change)) {
                Dialogs.info("Stock updated. New stock: " + (p.getItemQty() + change));
            } else {
                Dialogs.info("Update failed. Stock cannot go below 0.");
            }
        } catch (NumberFormatException e) {
            Dialogs.info("Change must be a whole number.");
        }
    }

    private static void deleteProduct() {
        Product p = askForProduct("delete");
        if (p == null) return;

        // Show the details first so the user can be sure before deleting.
        if (Dialogs.confirm("Delete this product?\n\n" + productDetails(p))) {
            if (productService.deleteProduct(p.getItemId())) {
                Dialogs.info("Product deleted.");
            } else {
                Dialogs.info("Delete failed.");
            }
        }
    }

    // --- Ordering & report ---

    private static void placeOrder(User user) {
        List<Product> products = productService.getAllProducts();
        if (products.isEmpty()) {
            Dialogs.info("No products to order.");
            return;
        }

        String custText = Dialogs.ask("Enter Customer ID (1 to 5):");
        if (custText == null) return;
        int customerId;
        try {
            customerId = Integer.parseInt(custText.trim());
        } catch (NumberFormatException e) {
            Dialogs.info("Customer ID must be a number.");
            return;
        }

        // Build the cart, showing the running cart and total each time.
        Map<Integer, Integer> cart = new HashMap<>();
        while (true) {
            String line = Dialogs.ask(listProducts(products) + "\n" + cartSummary(cart, products)
                    + "\nType 'productID quantity' (e.g. 1 2) to add an item.\nLeave blank to finish.");
            if (line == null || line.trim().isEmpty()) break;
            try {
                String[] parts = line.trim().split(" ");
                int id = Integer.parseInt(parts[0]);
                int qty = Integer.parseInt(parts[1]);
                if (findById(id, products) == null) {
                    Dialogs.info("No product with ID " + id + ".");
                    continue;
                }
                int current = cart.containsKey(id) ? cart.get(id) : 0;
                cart.put(id, current + qty);
            } catch (Exception e) {
                Dialogs.info("Please type: productID quantity (e.g. 1 2).");
            }
        }
        if (cart.isEmpty()) {
            Dialogs.info("No items added. Order cancelled.");
            return;
        }

        // Show the full summary and confirm before saving the order.
        if (!Dialogs.confirm("CONFIRM ORDER  (Customer " + customerId + ")\n\n"
                + cartSummary(cart, products) + "\nPlace this order?")) {
            Dialogs.info("Order cancelled.");
            return;
        }

        Order order = orderService.placeOrder(user, customerId, cart);
        if (order != null) {
            Dialogs.info("ORDER RECEIPT  (Customer " + customerId + ")\n\n"
                    + cartSummary(cart, products)
                    + "\nORDER PLACED!  Total: " + peso(order.getTotalAmount()));
        } else {
            Dialogs.info("Order failed. Check the customer ID and the stock.");
        }
    }

    private static void report() {
        List<Product> products = productService.getAllProducts();
        int totalStock = 0, low = 0, out = 0;
        double totalValue = 0;
        for (Product p : products) {
            totalStock += p.getItemQty();
            totalValue += p.getItemPrice() * p.getItemQty();
            if (p.getItemQty() == 0) out++;
            else if (p.getItemQty() < 10) low++;
        }
        Dialogs.info("INVENTORY REPORT\n"
                + "-----------------------------\n"
                + "Total products: " + products.size() + "\n"
                + "Total stock units: " + totalStock + "\n"
                + "Inventory value: " + peso(totalValue) + "\n"
                + "Low stock items (below 10): " + low + "\n"
                + "Out of stock: " + out);
    }

    // --- Helpers ---

    // Asks for a product ID and returns the matching product (or null, with a
    // message, if the input is invalid or no product has that ID).
    private static Product askForProduct(String action) {
        List<Product> products = productService.getAllProducts();
        String idText = Dialogs.ask(listProducts(products) + "\nEnter product ID to " + action + ":");
        if (idText == null) return null;
        try {
            int id = Integer.parseInt(idText.trim());
            Product p = findById(id, products);
            if (p == null) Dialogs.info("No product with ID " + id + ".");
            return p;
        } catch (NumberFormatException e) {
            Dialogs.info("ID must be a whole number.");
            return null;
        }
    }

    private static Product findById(int id, List<Product> products) {
        for (Product p : products) {
            if (p.getItemId() == id) return p;
        }
        return null;
    }

    private static String productDetails(Product p) {
        return "ID: " + p.getItemId()
                + "\nName: " + p.getItemName()
                + "\nDescription: " + p.getItemDesc()
                + "\nPrice: " + peso(p.getItemPrice())
                + "\nStock: " + p.getItemQty();
    }

    // A simple text list of products (table-like display).
    private static String listProducts(List<Product> products) {
        String text = "PRODUCTS:\n";
        for (Product p : products) {
            text += "ID " + p.getItemId() + " | " + p.getItemName()
                  + " | " + peso(p.getItemPrice()) + " | stock " + p.getItemQty() + "\n";
        }
        return text;
    }

    // The current cart shown with each subtotal and a running total.
    private static String cartSummary(Map<Integer, Integer> cart, List<Product> products) {
        if (cart.isEmpty()) return "CART: (empty)\n";
        String text = "CART:\n";
        double total = 0;
        for (Integer id : cart.keySet()) {
            Product p = findById(id, products);
            int qty = cart.get(id);
            double subtotal = p.getItemPrice() * qty;
            total += subtotal;
            text += "  " + p.getItemName() + " x" + qty + " = " + peso(subtotal) + "\n";
        }
        text += "  TOTAL: " + peso(total) + "\n";
        return text;
    }

    private static String peso(double amount) {
        return "₱" + String.format("%.2f", amount);
    }
}
