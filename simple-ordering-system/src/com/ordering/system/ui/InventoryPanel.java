package com.ordering.system.ui;

import com.ordering.system.model.Product;
import com.ordering.system.util.ProductService;

import javax.swing.*;
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

    public InventoryPanel(ProductService productService) {
        this.productService = productService;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Theme.BACKGROUND);
        Theme.field(searchField);

        Theme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);   // ID
        table.getColumnModel().getColumn(4).setPreferredWidth(60);   // Stock

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
        JButton searchBtn  = new JButton("Search");
        JButton showAllBtn = new JButton("Show All");
        JButton addBtn     = new JButton("Add");
        JButton updateBtn  = new JButton("Update Stock");
        JButton deleteBtn  = new JButton("Delete");

        searchBtn.addActionListener(e -> search());
        showAllBtn.addActionListener(e -> showAll());
        addBtn.addActionListener(e -> addProduct());
        updateBtn.addActionListener(e -> updateStock());
        deleteBtn.addActionListener(e -> deleteProduct());

        Theme.primary(addBtn);     // main action = green
        Theme.button(searchBtn);   // the rest = tan
        Theme.button(showAllBtn);
        Theme.button(updateBtn);
        Theme.button(deleteBtn);

        // Hover hints
        searchField.setToolTipText("Type a product name or ID, then click Search");
        addBtn.setToolTipText("Add a new product");
        updateBtn.setToolTipText("Change the stock of the selected product");
        deleteBtn.setToolTipText("Delete the selected product");
        showAllBtn.setToolTipText("Show all products");

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(Theme.HEADING);
        searchLabel.setForeground(Theme.TEXT);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setOpaque(false);
        bar.add(searchLabel);
        bar.add(searchField);
        bar.add(searchBtn);
        bar.add(showAllBtn);
        bar.add(addBtn);
        bar.add(updateBtn);
        bar.add(deleteBtn);
        return bar;
    }

    // --- Loading the table ---

    private void showAll() {
        searchField.setText("");
        fillTable(productService.getAllProducts());
    }

    // Shows only the products whose ID or name matches the search box.
    private void search() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            showAll();
            return;
        }

        List<Product> found = new ArrayList<>();
        for (Product p : productService.getAllProducts()) {
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
