package com.healthfirst.pims.gui.admin;

import com.healthfirst.pims.dao.MedicineDAO;
import com.healthfirst.pims.dao.SupplierDAO;
import com.healthfirst.pims.model.Medicine;
import com.healthfirst.pims.model.Supplier;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Admin tab: full CRUD (Create, Read, Update, Delete) for medicines. */
public class MedicinePanel extends JPanel {

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Company", "Type", "Price", "Stock", "Reorder Lvl", "Expiry", "Supplier"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public MedicinePanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Medicine");
        JButton editBtn = new JButton("Edit Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");
        buttons.add(addBtn);
        buttons.add(editBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);
        add(buttons, BorderLayout.NORTH);

        addBtn.addActionListener(this::onAdd);
        editBtn.addActionListener(this::onEdit);
        deleteBtn.addActionListener(this::onDelete);
        refreshBtn.addActionListener(e -> refresh());

        refresh();
    }

    public void refresh() {
        tableModel.setRowCount(0);
        List<Medicine> medicines = medicineDAO.getAllMedicines();
        for (Medicine m : medicines) {
            tableModel.addRow(new Object[]{
                    m.getMedicineId(), m.getName(), m.getCompany(), m.getMedicineType(),
                    "R " + m.getPrice(), m.getQuantityInStock(), m.getReorderLevel(),
                    m.getExpiryDate(), m.getSupplierName() == null ? "-" : m.getSupplierName()
            });
        }
    }

    private void onAdd(ActionEvent e) {
        showMedicineDialog(null);
    }

    private void onEdit(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a medicine to edit first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        Medicine m = medicineDAO.getById(id);
        showMedicineDialog(m);
    }

    private void onDelete(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a medicine to delete first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + name + "\"? This cannot be undone.", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (medicineDAO.deleteMedicine(id)) {
                refresh();
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete this medicine " +
                        "(it may already be referenced by a recorded sale).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Shared Add/Edit dialog. Pass null to add a new medicine. */
    private void showMedicineDialog(Medicine existing) {
        JTextField nameField = new JTextField(existing != null ? existing.getName() : "");
        JTextField companyField = new JTextField(existing != null ? existing.getCompany() : "");
        String[] types = {"Tablet", "Capsule", "Syrup", "Injection", "Cream"};
        JComboBox<String> typeBox = new JComboBox<>(types);
        if (existing != null && existing.getMedicineType() != null) typeBox.setSelectedItem(existing.getMedicineType());
        JTextField priceField = new JTextField(existing != null ? existing.getPrice().toString() : "0.00");
        JTextField stockField = new JTextField(existing != null ? String.valueOf(existing.getQuantityInStock()) : "0");
        JTextField reorderField = new JTextField(existing != null ? String.valueOf(existing.getReorderLevel()) : "0");
        JTextField expiryField = new JTextField(existing != null && existing.getExpiryDate() != null ? existing.getExpiryDate().toString() : "");
        expiryField.setToolTipText("Format: YYYY-MM-DD");

        List<Supplier> suppliers = supplierDAO.getAllSuppliers();
        JComboBox<Supplier> supplierBox = new JComboBox<>(suppliers.toArray(new Supplier[0]));
        supplierBox.insertItemAt(null, 0);
        supplierBox.setSelectedIndex(0);
        if (existing != null && existing.getSupplierId() != null) {
            for (Supplier s : suppliers) {
                if (s.getSupplierId() == existing.getSupplierId()) { supplierBox.setSelectedItem(s); break; }
            }
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        panel.add(new JLabel("Name:"));            panel.add(nameField);
        panel.add(new JLabel("Company:"));         panel.add(companyField);
        panel.add(new JLabel("Type:"));            panel.add(typeBox);
        panel.add(new JLabel("Price (R):"));       panel.add(priceField);
        panel.add(new JLabel("Quantity in stock:"));panel.add(stockField);
        panel.add(new JLabel("Reorder level:"));   panel.add(reorderField);
        panel.add(new JLabel("Expiry date:"));     panel.add(expiryField);
        panel.add(new JLabel("Supplier:"));        panel.add(supplierBox);

        String title = existing == null ? "Add Medicine" : "Edit Medicine";
        int result = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            Medicine m = existing != null ? existing : new Medicine();
            m.setName(nameField.getText().trim());
            m.setCompany(companyField.getText().trim());
            m.setMedicineType((String) typeBox.getSelectedItem());
            m.setPrice(new BigDecimal(priceField.getText().trim()));
            m.setQuantityInStock(Integer.parseInt(stockField.getText().trim()));
            m.setReorderLevel(Integer.parseInt(reorderField.getText().trim()));
            String expiryText = expiryField.getText().trim();
            m.setExpiryDate(expiryText.isEmpty() ? null : LocalDate.parse(expiryText));
            Supplier chosen = (Supplier) supplierBox.getSelectedItem();
            m.setSupplierId(chosen != null ? chosen.getSupplierId() : null);

            if (m.getName().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required.", "Validation error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = existing == null ? medicineDAO.addMedicine(m) : medicineDAO.updateMedicine(m);
            if (ok) {
                refresh();
            } else {
                JOptionPane.showMessageDialog(this, "Database operation failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException | java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Please check the price, stock, reorder level and expiry date (YYYY-MM-DD) fields.",
                    "Invalid input", JOptionPane.WARNING_MESSAGE);
        }
    }
}
