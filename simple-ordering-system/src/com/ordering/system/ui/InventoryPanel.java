package com.ordering.system.ui;

import com.ordering.system.model.Product;
import com.ordering.system.util.ProductService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventory tab: add, view, search, update stock and delete products.
 * All data work is delegated to ProductService.
 */
public class InventoryPanel extends JPanel {

    // --- Fields ---

    private final ProductService productService;

    private DefaultTableModel tableModel =
            new ReadOnlyTableModel(new String[]{"ID", "Name", "Description", "Price", "Stock"});
    private JTable table = new JTable(tableModel);
    private JTextField searchField = new JTextField(15);
    private JLabel countLabel = new JLabel(" ");

    // The product list is loaded once and kept here so the live search can
    // filter it in memory instead of querying the database on every keystroke.
    private List<Product> allProducts = new ArrayList<>();

    public InventoryPanel(ProductService productService) {
        this.productService = productService;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Theme.BACKGROUND);
        Theme.field(searchField);

        countLabel.setFont(Theme.NORMAL);
        countLabel.setForeground(Theme.TEXT);
        countLabel.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 0));

        add(buildTopBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(countLabel, BorderLayout.SOUTH);   // footer showing how many rows

        showAll();
    }

    // --- Layout ---

    private JPanel buildTopBar() {
        JButton addBtn    = new JButton("Add");
        JButton updateBtn = new JButton("Update Stock");
        JButton deleteBtn = new JButton("Delete");

        addBtn.addActionListener(e -> addProduct());
        updateBtn.addActionListener(e -> updateStock());
        deleteBtn.addActionListener(e -> deleteProduct());

        Theme.primary(addBtn);
        Theme.button(updateBtn);
        Theme.button(deleteBtn);

        // A DocumentListener fires on every text change, giving live search
        // without a separate Search button.
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { search(); }
            public void removeUpdate(DocumentEvent e)  { search(); }
            public void changedUpdate(DocumentEvent e) { search(); }
        });

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(Theme.HEADING);
        searchLabel.setForeground(Theme.TEXT);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setOpaque(false);
        bar.add(searchLabel);
        bar.add(searchField);
        bar.add(addBtn);
        bar.add(updateBtn);
        bar.add(deleteBtn);
        return bar;
    }

    // --- Loading the table ---

    // Refreshes the in-memory list from the database, then shows everything.
    private void showAll() {
        allProducts = productService.getAllProducts();
        fillTable(allProducts);
    }

    // Filters the cached list by ID or name. Safe to run on every keystroke
    // because it never touches the database.
    private void search() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            fillTable(allProducts);
            return;
        }

        List<Product> found = new ArrayList<>();
        for (Product p : allProducts) {
            boolean matchesId   = String.valueOf(p.getItemId()).contains(keyword);
            boolean matchesName = p.getItemName().toLowerCase().contains(keyword);
            if (matchesId || matchesName) {
                found.add(p);
            }
        }
        fillTable(found);
    }

    private void fillTable(List<Product> products) {
        tableModel.setRowCount(0);
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                p.getItemId(), p.getItemName(), p.getItemDesc(),
                Theme.money(p.getItemPrice()), p.getItemQty()
            });
        }
        countLabel.setText(products.isEmpty()
                ? "No products found"
                : "Showing " + products.size() + " product(s)");
    }

    // --- Actions ---

    private void addProduct() {
        // Each dialog returns null when the user cancels, so we stop early.
        String name = JOptionPane.showInputDialog(this, "Product name:");
        if (name == null) return;
        String desc = JOptionPane.showInputDialog(this, "Description:");
        if (desc == null) return;
        String priceText = JOptionPane.showInputDialog(this, "Price:");
        if (priceText == null) return;
        String qtyText = JOptionPane.showInputDialog(this, "Quantity:");
        if (qtyText == null) return;

        try {
            double price = Double.parseDouble(priceText.trim());
            int qty = Integer.parseInt(qtyText.trim());
            if (productService.addNewProduct(new Product(0, name, desc, price, qty))) {
                message("Product added.");
                showAll();
            } else {
                message("Invalid details. Name is required and price must be above 0.");
            }
        } catch (NumberFormatException ex) {
            // Reached when price or quantity isn't a valid number.
            message("Price and quantity must be numbers.");
        }
    }

    private void updateStock() {
        int id = selectedId();
        if (id == -1) return;

        String changeText = JOptionPane.showInputDialog(this,
                "Enter stock change (use - to reduce, e.g. -5):");
        if (changeText == null) return;

        try {
            int change = Integer.parseInt(changeText.trim());
            if (productService.updateStock(id, change)) {
                message("Stock updated.");
                showAll();
            } else {
                message("Update failed. Stock cannot go below 0.");
            }
        } catch (NumberFormatException ex) {
            message("Please enter a whole number.");
        }
    }

    private void deleteProduct() {
        int id = selectedId();
        if (id == -1) return;

        if (confirm("Delete this product?")) {
            if (productService.deleteProduct(id)) {
                message("Product deleted.");
                showAll();
            } else {
                message("Delete failed.");
            }
        }
    }

    // --- Small helpers ---

    // Returns the selected row's product ID, or -1 (after warning) if none.
    private int selectedId() {
        int row = table.getSelectedRow();
        if (row == -1) {
            message("Please select a product first.");
            return -1;
        }
        return (Integer) tableModel.getValueAt(row, 0);
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(this, text);
    }

    private boolean confirm(String text) {
        return JOptionPane.showConfirmDialog(this, text, "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
