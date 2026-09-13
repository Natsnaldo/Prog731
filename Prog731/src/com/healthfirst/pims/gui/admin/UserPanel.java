package com.healthfirst.pims.gui.admin;

import com.healthfirst.pims.dao.UserDAO;
import com.healthfirst.pims.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Admin tab: User Management.
 * Create, delete, and manage Cashier (and Admin) accounts.
 */
public class UserPanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Username", "Full Name", "Role"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public UserPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Create User");
        JButton resetPwBtn = new JButton("Reset Password");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");
        buttons.add(addBtn); buttons.add(resetPwBtn); buttons.add(deleteBtn); buttons.add(refreshBtn);
        add(buttons, BorderLayout.NORTH);

        addBtn.addActionListener(this::onAdd);
        resetPwBtn.addActionListener(this::onResetPassword);
        deleteBtn.addActionListener(this::onDelete);
        refreshBtn.addActionListener(e -> refresh());

        refresh();
    }

    public void refresh() {
        tableModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            tableModel.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getFullName(), u.getRole()});
        }
    }

    private void onAdd(ActionEvent e) {
        JTextField usernameField = new JTextField();
        JTextField fullNameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        String[] roles = {"Cashier", "Admin"};
        JComboBox<String> roleBox = new JComboBox<>(roles);

        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("Username:"));  panel.add(usernameField);
        panel.add(new JLabel("Full Name:")); panel.add(fullNameField);
        panel.add(new JLabel("Password:"));  panel.add(passwordField);
        panel.add(new JLabel("Role:"));      panel.add(roleBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create User", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleBox.getSelectedItem();

        if (username.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (userDAO.addUser(username, password, role, fullName)) {
            refresh();
        } else {
            JOptionPane.showMessageDialog(this, "Could not create user (username may already exist).",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onResetPassword(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        JPasswordField newPassword = new JPasswordField();
        int result = JOptionPane.showConfirmDialog(this, newPassword, "Enter new password", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String pwd = new String(newPassword.getPassword());
        if (pwd.isEmpty()) return;

        if (userDAO.updatePassword(id, pwd)) {
            JOptionPane.showMessageDialog(this, "Password updated.");
        } else {
            JOptionPane.showMessageDialog(this, "Could not update password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user to delete first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete user \"" + username + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (userDAO.deleteUser(id)) {
                refresh();
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete this user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
