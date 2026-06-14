package com.ordering.system.ui;

import com.ordering.system.model.*;
import com.ordering.system.util.*;
import java.util.*;

public class OrderingSystemGUI {
    private static UserService userService = new UserService();
    private static ProductService productService = new ProductService();
    private static OrderService orderService = new OrderService();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        User currentUser = null;

        try {
            while (true) {
                if (currentUser == null) {
                    currentUser = showLoginModule(scanner);
                } else {
                    displayMainMenu(currentUser);
                    String choice = scanner.nextLine().toUpperCase();

                    if (choice.equals("L")) {
                        currentUser = null;
                    } else if (choice.equals("P")) {
                        productMenu(scanner);
                    } else if (choice.equals("O")) {
                        checkoutModule(scanner, currentUser);
                    } else {
                        System.out.println("Invalid option.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static User showLoginModule(Scanner scanner) {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Email: "); String email = scanner.nextLine();
        System.out.print("Password: "); String pass = scanner.nextLine();
        User user = userService.loginUser(email, pass);
        if (user == null) System.out.println("Invalid credentials.");
        return user;
    }

    private static void displayMainMenu(User user) {
        System.out.println("\n=========================================");
        System.out.printf(" Welcome, %s (Role: %s)%n", user.getUsername(), user.getRole());
        System.out.println(" P - Product Inventory Management");
        System.out.println(" O - Place New Order");
        System.out.println(" L - Logout");
        System.out.println("=========================================");
        System.out.print("Select option: ");
    }

    private static void productMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n--- INVENTORY MANAGEMENT ---");
            System.out.println("A - Add Product | V - View All | S - Search | U - Update Stock | D - Delete | B - Back");
            String choice = scanner.nextLine().toUpperCase();

            if (choice.equals("B")) break;
            switch (choice) {
                case "A":
                    System.out.print("Name: "); String name = scanner.nextLine();
                    System.out.print("Desc: "); String desc = scanner.nextLine();
                    System.out.print("Price: "); double price = Double.parseDouble(scanner.nextLine());
                    System.out.print("Qty: "); int qty = Integer.parseInt(scanner.nextLine());
                    productService.addNewProduct(new Product(0, name, desc, price, qty));
                    break;
                case "V":
                    productService.getAllProducts().forEach(System.out::println);
                    break;
                case "S":
                    System.out.print("Search query: ");
                    productService.searchProduct(scanner.nextLine()).ifPresentOrElse(
                        System.out::println, () -> System.out.println("Not found."));
                    break;
                case "U":
                    System.out.print("ID: "); int id = Integer.parseInt(scanner.nextLine());
                    System.out.print("Change (+/-): "); int change = Integer.parseInt(scanner.nextLine());
                    productService.updateStock(id, change);
                    break;
                case "D":
                    System.out.print("ID to delete: ");
                    int delId = Integer.parseInt(scanner.nextLine());
                    productService.deleteProduct(delId);
                    break;
            }
        }
    }

    private static void checkoutModule(Scanner scanner, User user) {
        System.out.print("Enter Customer ID (usr_id): ");
        int customerId;
        try {
            customerId = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid Customer ID format. Checkout cancelled.");
            return;
        }

        Map<Integer, Integer> cart = new HashMap<>();
        System.out.println("Enter 'ID Qty' (e.g. 1 5). Type 'DONE' to finish.");
        while (true) {
            String line = scanner.nextLine();
            if (line.equalsIgnoreCase("DONE")) break;
            try {
                String[] parts = line.split(" ");
                cart.put(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            } catch (Exception e) {
                System.out.println("Invalid format. Use 'ID Qty'.");
            }
        }
        
        Order order = orderService.placeOrder(user, customerId, cart);
        if (order != null) {
            System.out.println("Order placed successfully! Total: $" + order.getTotalAmount());
        } else {
            System.out.println("Order failed. Please ensure the Customer ID is valid and stock is available.");
        }
    }
}