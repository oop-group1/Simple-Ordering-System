package com.ordering.system.ui;

import com.ordering.system.model.Product;
import com.ordering.system.util.ProductService;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Reports tab: summary statistics and a full product list with each item's
 * value and stock status. Everything is computed from the product list, so it
 * needs no extra backend method.
 */
public class ReportPanel extends JPanel {

    // --- Fields ---

    private final ProductService productService;

    // Stock below this number is flagged as "LOW".
    private static final int LOW_STOCK_LEVEL = 10;

    private JLabel totalProductsLabel = new JLabel();
    private JLabel totalStockLabel = new JLabel();
    private JLabel totalValueLabel = new JLabel();
    private JLabel lowStockLabel = new JLabel();
    private JLabel outOfStockLabel = new JLabel();
    private JLabel avgPriceLabel = new JLabel();

    private DefaultTableModel tableModel =
            new ReadOnlyTableModel(new String[]{"ID", "Name", "Stock", "Price", "Value", "Status"});
    private JTable table = new JTable(tableModel);

    public ReportPanel(ProductService productService) {
        this.productService = productService;

        setLayout(new BorderLayout(5, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Theme.BACKGROUND);
        add(buildSummary(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);

        generateReport();
    }

    // --- Layout ---

    private JPanel buildSummary() {
        JLabel title = new JLabel("Inventory Summary");
        title.setFont(Theme.SECTION);
        title.setForeground(Theme.TEXT);

        JButton refreshBtn = new JButton("Refresh Report");
        refreshBtn.addActionListener(e -> generateReport());
        Theme.primary(refreshBtn);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(title, BorderLayout.WEST);
        headerRow.add(refreshBtn, BorderLayout.EAST);

        // Six stat cards in a 2x3 grid (icon + big number + caption).
        JPanel stats = new JPanel(new GridLayout(2, 3, 10, 10));
        stats.setOpaque(false);
        stats.add(statCard("products.png", totalProductsLabel, "Total products"));
        stats.add(statCard("stock.png",    totalStockLabel,    "Total stock units"));
        stats.add(statCard("value.png",    totalValueLabel,    "Inventory value"));
        stats.add(statCard("low.png",      lowStockLabel,      "Low stock items"));
        stats.add(statCard("out.png",      outOfStockLabel,    "Out of stock"));
        stats.add(statCard("avg.png",      avgPriceLabel,      "Average price"));

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(headerRow, BorderLayout.NORTH);
        top.add(stats, BorderLayout.CENTER);
        return top;
    }

    // One stat card: icon on the left, big number with a small caption on the right.
    private JPanel statCard(String iconFile, JLabel valueLabel, String caption) {
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(Theme.TEXT);

        JLabel cap = new JLabel(caption);
        cap.setFont(Theme.NORMAL);
        cap.setForeground(Theme.MUTED);

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cap.setAlignmentX(Component.LEFT_ALIGNMENT);
        textCol.add(valueLabel);
        textCol.add(cap);

        ImageIcon ic = Theme.image("icons/" + iconFile);
        JLabel icon = (ic != null) ? new JLabel(ic) : new JLabel();
        icon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.FIELD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        card.add(icon, BorderLayout.WEST);
        card.add(textCol, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTable() {
        JLabel title = new JLabel("All Products");
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.TEXT);

        Theme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);   // narrow ID column

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(Theme.BORDER, 1, true));

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // --- Report ---

    // One pass over the products fills the table and totals every statistic.
    private void generateReport() {
        List<Product> products = productService.getAllProducts();
        int totalStock = 0;
        double totalValue = 0;
        double priceSum = 0;
        int lowCount = 0;
        int outCount = 0;
        tableModel.setRowCount(0);

        for (Product p : products) {
            int qty = p.getItemQty();
            double value = p.getItemPrice() * qty;
            totalStock += qty;
            totalValue += value;
            priceSum += p.getItemPrice();

            String status;
            if (qty == 0) {
                status = "OUT OF STOCK";
                outCount++;
            } else if (qty < LOW_STOCK_LEVEL) {
                status = "LOW";
                lowCount++;
            } else {
                status = "OK";
            }

            tableModel.addRow(new Object[]{
                p.getItemId(), p.getItemName(), qty,
                Theme.money(p.getItemPrice()), Theme.money(value), status
            });
        }

        double avgPrice = products.isEmpty() ? 0 : priceSum / products.size();

        // The cards show just the number; the caption text is set in statCard().
        totalProductsLabel.setText("" + products.size());
        totalStockLabel.setText("" + totalStock);
        totalValueLabel.setText(Theme.money(totalValue));
        lowStockLabel.setText("" + lowCount);
        outOfStockLabel.setText("" + outCount);
        avgPriceLabel.setText(Theme.money(avgPrice));
    }
}
