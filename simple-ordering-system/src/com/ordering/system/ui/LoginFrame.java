package com.ordering.system.ui;

import com.ordering.system.model.User;
import com.ordering.system.util.UserService;

import javax.swing.*;
import java.awt.*;

/**
 * Login screen. Authenticates the entered email/password against the database
 * through UserService and opens the MainFrame on success.
 */
public class LoginFrame extends JFrame {

    // --- Fields ---

    private final UserService userService = new UserService();

    private JTextField emailField = new JTextField(15);
    private JPasswordField passwordField = new JPasswordField(15);
    private JLabel messageLabel = new JLabel(" ");   // shows login errors in red

    public LoginFrame() {
        setTitle("Simple Ordering System - Login");
        ImageIcon appIcon = Theme.image("cart.png");
        if (appIcon != null) setIconImage(appIcon.getImage());   // cart in the title bar
        setSize(420, 470);
        setLocationRelativeTo(null);          // open in the middle of the screen
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        add(buildContent());
    }

    // --- Layout ---

    // A vertical (BoxLayout) stack so the spacing between each part is exact.
    private JPanel buildContent() {
        Theme.field(emailField);
        Theme.field(passwordField);
        sizeField(emailField);
        sizeField(passwordField);

        JButton loginButton = new JButton("Login");
        Theme.primary(loginButton);
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginButton.addActionListener(e -> handleLogin());

        messageLabel.setForeground(Color.RED);
        messageLabel.setFont(Theme.NORMAL);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel panel = new JPanel();
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 35, 20, 35));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Glue above and below centers the whole form vertically in the window.
        panel.add(Box.createVerticalGlue());
        panel.add(buildLogo());
        panel.add(Box.createVerticalStrut(15));
        panel.add(fieldLabel("Email"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(emailField);
        panel.add(Box.createVerticalStrut(12));
        panel.add(fieldLabel("Password"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(12));
        panel.add(loginButton);
        panel.add(Box.createVerticalStrut(8));
        panel.add(messageLabel);
        panel.add(buildHint());
        panel.add(Box.createVerticalGlue());

        getRootPane().setDefaultButton(loginButton);    // pressing Enter logs in
        return panel;
    }

    // Cart picture on top, with the title under it. Uses an emoji if the
    // image file can't be found.
    private JComponent buildLogo() {
        ImageIcon cart = Theme.image("cart.png");
        JLabel logo;
        if (cart != null) {
            logo = new JLabel(cart);
        } else {                                 // fallback if the image is missing
            logo = new JLabel("🛒");
            logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        }

        JLabel title = new JLabel("Ordering System");
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.TEXT);

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(logo);
        box.add(Box.createVerticalStrut(6));
        box.add(title);
        // Let the box fill the width so the logo and title stay centered.
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, box.getPreferredSize().height));
        return box;
    }

    private JLabel buildHint() {
        JLabel hint = new JLabel("Default: admin@system.com / admin123");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(Theme.TEXT);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        return hint;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Theme.TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // In a BoxLayout a field would grow to any height, so we cap the height
    // while still allowing it to stretch full width.
    private void sizeField(JComponent field) {
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
    }

    // --- Action ---

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill in both fields.");
            return;
        }

        // try/catch guards against database/connection errors during login.
        try {
            User user = userService.loginUser(email, password);
            if (user != null) {
                new MainFrame(user).setVisible(true);
                dispose();
            } else {
                messageLabel.setText("Invalid email or password.");
                passwordField.setText("");
            }
        } catch (Exception ex) {
            messageLabel.setText("Login error: " + ex.getMessage());
        }
    }
}
