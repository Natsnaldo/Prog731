package com.healthfirst.pims.gui.cashier;

import com.healthfirst.pims.gui.LoginFrame;
import com.healthfirst.pims.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Cashier Module container.
 * Tabs: Point of Sale, Stock Check.
 */
public class CashierDashboard extends JFrame {

    public CashierDashboard(User cashier) {
        super("HealthFirst Pharmacy - Cashier Till (" + cashier.getFullName() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 550);
        setLocationRelativeTo(null);

        JPanel top = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("  Logged in as: " + cashier.getFullName() + " (Cashier)");
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD));
        top.add(welcome, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Log Out");
        logoutBtn.addActionListener(e -> logout());
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.add(logoutBtn);
        top.add(logoutPanel, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Point of Sale", new POSPanel(cashier));
        tabs.addTab("Stock Check", new StockCheckPanel());

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}
