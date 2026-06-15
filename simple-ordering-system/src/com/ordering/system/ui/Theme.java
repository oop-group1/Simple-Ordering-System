package com.ordering.system.ui;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;

/**
 * Single source of truth for the app's look (matcha + coffee theme).
 * Every screen pulls its colors, fonts and shared widget styling from here,
 * so the whole program can be re-themed by editing this one file.
 */
public class Theme {

    // --- Colors ---
    public static final Color BACKGROUND   = new Color(0xEDE6D3); // latte cream window
    public static final Color HEADER       = new Color(0x6F4E37); // coffee brown bar
    public static final Color HEADER_DARK  = new Color(0x563B29); // shadow line under the header
    public static final Color PRIMARY      = new Color(0x5F7D3B); // matcha green (main buttons)
    public static final Color PRIMARY_DARK = new Color(0x4C6630); // matcha button border
    public static final Color SECONDARY    = new Color(0xD8C3A5); // tan (secondary buttons)
    public static final Color FIELD        = new Color(0xFBF8F0); // cream text fields / cards
    public static final Color BORDER       = new Color(0xC9B79C); // tan outlines
    public static final Color TEXT         = new Color(0x4B3621); // dark coffee text
    public static final Color WHITE        = Color.WHITE;

    // --- Fonts ---
    public static final Font TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font SECTION = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font HEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BUTTON  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font NORMAL  = new Font("Segoe UI", Font.PLAIN, 14);

    // --- Shared styling ---

    /** Green button for the main action on a screen (Login, Place Order...). */
    public static void primary(JButton button) {
        styleButton(button, PRIMARY, WHITE, PRIMARY_DARK);
    }

    /** Tan button for secondary actions (Delete, Clear, Refresh...). */
    public static void button(JButton button) {
        styleButton(button, SECONDARY, TEXT, BORDER);
    }

    // Switching to BasicButtonUI is the key step: it gives a flat button that
    // actually paints our background color (Nimbus otherwise hides it). The
    // rounded edge comes from LineBorder's "rounded" flag.
    private static void styleButton(JButton button, Color bg, Color fg, Color border) {
        button.setUI(new BasicButtonUI());
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFont(BUTTON);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(border, 1, true),
                BorderFactory.createEmptyBorder(7, 16, 7, 16)));
    }

    /** Cream fill with a rounded outline for text fields. */
    public static void field(JComponent field) {
        field.setBackground(FIELD);
        field.setForeground(TEXT);
        field.setFont(NORMAL);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    }

    /** Formats a number as pesos, e.g. 89.0 -> "₱89.00". */
    public static String money(double amount) {
        return "₱" + String.format("%.2f", amount);
    }

    /**
     * Loads an image and scales it to the given height while keeping its
     * width ratio, so pictures of any size are never stretched.
     * Returns null if the file is missing, letting callers fall back.
     */
    public static ImageIcon icon(String path, int height) {
        ImageIcon raw = new ImageIcon(path);
        if (raw.getIconWidth() <= 0) return null;
        int width = height * raw.getIconWidth() / raw.getIconHeight();
        Image scaled = raw.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
