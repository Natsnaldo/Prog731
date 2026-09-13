package com.healthfirst.pims.gui;

import com.healthfirst.pims.dao.UserDAO;
import com.healthfirst.pims.gui.admin.AdminDashboard;
import com.healthfirst.pims.gui.cashier.CashierDashboard;
import com.healthfirst.pims.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Authentication Module.
 * Secure login screen for all users, with role-based redirection to either
 * the AdminDashboard or the CashierDashboard, and an error message on a
 * failed attempt.
 */
public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JLabel statusLabel = new JLabel(" ");
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        super("HealthFirst Pharmacy - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        buildUi();
        pack();
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("HealthFirst Pharmacy", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel subtitle = new JLabel("Pharmacy Inventory Management System", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.add(title);
        titlePanel.add(subtitle);
        root.add(titlePanel, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        form.add(passwordField, gbc);

        statusLabel.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        form.add(statusLabel, gbc);

        root.add(form, BorderLayout.CENTER);

        JButton loginButton = new JButton("Log In");
        loginButton.addActionListener(this::onLogin);
        getRootPane().setDefaultButton(loginButton);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        root.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void onLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both a username and password.");
            return;
        }

        User user = userDAO.login(username, password);
        if (user == null) {
            statusLabel.setText("Invalid username or password.");
            passwordField.setText("");
            return;
        }

        dispose();
        if (user.isAdmin()) {
            new AdminDashboard(user).setVisible(true);
        } else {
            new CashierDashboard(user).setVisible(true);
        }
    }
}
