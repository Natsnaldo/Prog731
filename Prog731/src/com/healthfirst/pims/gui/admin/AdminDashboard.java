package com.healthfirst.pims.gui.admin;

import com.healthfirst.pims.gui.LoginFrame;
import com.healthfirst.pims.model.User;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

/**
 * Administrator Module.
 * Tabs: Manage Medicines, Manage Suppliers, Manage Users, Reports.
 */
public class AdminDashboard extends JFrame {

    private final MedicinePanel medicinePanel = new MedicinePanel();
    private final SupplierPanel supplierPanel = new SupplierPanel();
    private final UserPanel userPanel = new UserPanel();
    private final ReportsPanel reportsPanel = new ReportsPanel();

    public AdminDashboard(User admin) {
        super("HealthFirst Pharmacy - Admin Dashboard (" + admin.getFullName() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);

        JPanel top = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("  Logged in as: " + admin.getFullName() + " (Administrator)");
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD));
        top.add(welcome, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Log Out");
        logoutBtn.addActionListener(e -> logout());
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.add(logoutBtn);
        top.add(logoutPanel, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Manage Medicines", medicinePanel);
        tabs.addTab("Manage Suppliers", supplierPanel);
        tabs.addTab("Manage Users", userPanel);
        tabs.addTab("Reports", reportsPanel);

        // Keep every tab's data fresh whenever the admin switches to it -
        // e.g. adding a supplier should immediately show up in the medicine
        // dialog's supplier drop-down, and stock changes should show up in
        // the reports straight away.
        tabs.addChangeListener((ChangeEvent e) -> {
            int index = tabs.getSelectedIndex();
            String title = tabs.getTitleAt(index);
            switch (title) {
                case "Manage Medicines" -> medicinePanel.refresh();
                case "Manage Suppliers" -> supplierPanel.refresh();
                case "Manage Users" -> userPanel.refresh();
                case "Reports" -> reportsPanel.refreshAll();
            }
        });

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}
