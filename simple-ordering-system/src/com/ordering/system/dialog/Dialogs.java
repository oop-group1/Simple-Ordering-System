package com.ordering.system.dialog;

import javax.swing.JOptionPane;

/**
 * Small wrappers around JOptionPane so the rest of this version reads cleanly.
 * Every dialog in the program goes through one of these three methods.
 */
public class Dialogs {

    /** Asks the user to type something; returns null if they cancel. */
    public static String ask(String message) {
        return JOptionPane.showInputDialog(message);
    }

    /** Shows an information or result message. */
    public static void info(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    /** Shows a Yes/No question; returns true only if Yes is chosen. */
    public static boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(null, message, "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
