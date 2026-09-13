package com.healthfirst.pims.gui.admin;

import com.healthfirst.pims.dao.SupplierDAO;
import com.healthfirst.pims.model.Supplier;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/** Admin tab: CRUD for supplier details. */
public class SupplierPanel extends JPanel {

    private final SupplierDAO supplierDAO = new SupplierDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Contact Person", "Phone", "Email", "Address"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public SupplierPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Supplier");
        JButton editBtn = new JButton("Edit Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");
        buttons.add(addBtn); buttons.add(editBtn); buttons.add(deleteBtn); buttons.add(refreshBtn);
        add(buttons, BorderLayout.NORTH);

        addBtn.addActionListener(this::onAdd);
        editBtn.addActionListener(this::onEdit);
        deleteBtn.addActionListener(this::onDelete);
        refreshBtn.addActionListener(e -> refresh());

        refresh();
    }

    public void refresh() {
        tableModel.setRowCount(0);
        List<Supplier> suppliers = supplierDAO.getAllSuppliers();
        for (Supplier s : suppliers) {
            tableModel.addRow(new Object[]{
                    s.getSupplierId(), s.getName(), s.getContactPerson(), s.getPhone(), s.getEmail(), s.getAddress()
            });
        }
    }

    private void onAdd(ActionEvent e) {
        showSupplierDialog(null);
    }

    private void onEdit(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a supplier to edit first.");
            return;
        }
        Supplier s = new Supplier(
                (int) tableModel.getValueAt(row, 0),
                (String) tableModel.getValueAt(row, 1),
                (String) tableModel.getValueAt(row, 2),
                (String) tableModel.getValueAt(row, 3),
                (String) tableModel.getValueAt(row, 4),
                (String) tableModel.getValueAt(row, 5)
        );
        showSupplierDialog(s);
    }

    private void onDelete(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a supplier to delete first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this supplier?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (supplierDAO.deleteSupplier(id)) {
                refresh();
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete this supplier.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showSupplierDialog(Supplier existing) {
        JTextField nameField = new JTextField(existing != null ? existing.getName() : "");
        JTextField contactField = new JTextField(existing != null ? existing.getContactPerson() : "");
        JTextField phoneField = new JTextField(existing != null ? existing.getPhone() : "");
        JTextField emailField = new JTextField(existing != null ? existing.getEmail() : "");
        JTextArea addressField = new JTextArea(existing != null ? existing.getAddress() : "", 3, 20);

        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("Name:"));            panel.add(nameField);
        panel.add(new JLabel("Contact Person:"));  panel.add(contactField);
        panel.add(new JLabel("Phone:"));           panel.add(phoneField);
        panel.add(new JLabel("Email:"));           panel.add(emailField);
        panel.add(new JLabel("Address:"));         panel.add(new JScrollPane(addressField));

        String title = existing == null ? "Add Supplier" : "Edit Supplier";
        int result = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.", "Validation error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Supplier s = existing != null ? existing : new Supplier();
        s.setName(name);
        s.setContactPerson(contactField.getText().trim());
        s.setPhone(phoneField.getText().trim());
        s.setEmail(emailField.getText().trim());
        s.setAddress(addressField.getText().trim());

        boolean ok = existing == null ? supplierDAO.addSupplier(s) : supplierDAO.updateSupplier(s);
        if (ok) {
            refresh();
        } else {
            JOptionPane.showMessageDialog(this, "Database operation failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
