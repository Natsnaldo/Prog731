package com.healthfirst.pims;

import com.healthfirst.pims.gui.LoginFrame;

import javax.swing.*;

/** Application entry point. Launches the login screen on the Swing event thread. */
public class Main {
    public static void main(String[] args) {
        // Use the host OS look-and-feel so the app fits in on Windows/macOS/Linux.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back silently to the default cross-platform look and feel.
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
