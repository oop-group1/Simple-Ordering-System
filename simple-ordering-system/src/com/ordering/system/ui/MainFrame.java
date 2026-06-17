package com.ordering.system.ui;

import com.ordering.system.model.User;
import com.ordering.system.util.OrderService;
import com.ordering.system.util.ProductService;

import javax.swing.*;
import java.awt.*;

/**
 * Main window after login. A header bar sits on top and the three features
 * (Inventory, New Order, Reports) live in tabs below.
 */
public class MainFrame extends JFrame {

    private final User currentUser;

    // Created once here and shared with every tab so they all use the same
    // service objects.
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();

    public MainFrame(User user) {
        this.currentUser = user;

        setTitle("Simple Ordering System");
        ImageIcon appIcon = Theme.image("cart.png");
        if (appIcon != null) setIconImage(appIcon.getImage());   // cart in the title bar
        setSize(820, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
    }

    // --- Layout ---

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.HEADER);
        // Matte bottom line = a thin shadow that gives the header some depth.
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, Theme.HEADER_DARK),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));

        JLabel title = new JLabel("Simple Ordering System");
        title.setForeground(Theme.WHITE);
        title.setFont(Theme.TITLE);

        JLabel welcome = new JLabel("Welcome, " + currentUser.getUsername()
                + "  (" + currentUser.getRole() + ")");
        welcome.setForeground(new Color(0xF0E8D8));
        welcome.setFont(Theme.NORMAL);

        JButton logout = new JButton("Logout");
        Theme.button(logout);
        logout.addActionListener(e -> handleLogout());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);
        right.add(welcome);
        right.add(logout);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Theme.BACKGROUND);
        tabs.addTab("Inventory", new InventoryPanel(productService));
        tabs.addTab("New Order", new OrderPanel(productService, orderService, currentUser));
        tabs.addTab("Reports", new ReportPanel(productService));
        return tabs;
    }

    // --- Action ---

    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Logout",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}
