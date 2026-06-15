package com.ordering.system.ui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;

/**
 * Starting point of the GUI program. (The text-based OrderingSystemGUI is
 * kept as a backup and is not used here.)
 */
public class OrderingSystemApp {

    public static void main(String[] args) {
        try {
            // Nimbus is a modern built-in look-and-feel.
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (info.getName().equals("Nimbus")) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            // Nimbus derives every component color from a handful of base colors.
            // Overriding those base colors recolors the parts we can't reach
            // directly (tabs, table headers, scrollbars) to match the theme.
            UIManager.put("control", new Color(0xEDE6D3));               // panels / background
            UIManager.put("nimbusBase", new Color(0x6F4E37));            // accents (selected tab)
            UIManager.put("nimbusBlueGrey", new Color(0xCBC3B0));        // grey areas -> warm grey
            UIManager.put("nimbusLightBackground", new Color(0xFBF8F0)); // tables / fields
            UIManager.put("text", new Color(0x4B3621));                  // text
            UIManager.put("nimbusFocus", new Color(0x5F7D3B));           // focus outline
            UIManager.put("nimbusSelectionBackground", new Color(0xCBD9B5)); // selected table row
        } catch (Exception e) {
            System.out.println("Could not set Nimbus look: " + e.getMessage());
        }

        // Swing screens must be built on the event-dispatch thread, not main.
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
