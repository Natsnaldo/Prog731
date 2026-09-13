package com.healthfirst.pims.gui.cashier;

import com.healthfirst.pims.dao.MedicineDAO;
import com.healthfirst.pims.model.Medicine;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Cashier Module - Stock Check.
 * Lets a cashier quickly check the price and availability of a medicine
 * without making a sale. Read-only - cashiers cannot add or edit medicines.
 */
public class StockCheckPanel extends JPanel {

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final JTextField searchField = new JTextField(20);
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Name", "Company", "Type", "Price (R)", "In Stock", "Expiry Date"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };

    public StockCheckPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.add(new JLabel("Medicine name:"));
        searchBar.add(searchField);
        JButton searchBtn = new JButton("Check Stock");
        searchBtn.addActionListener(e -> doSearch());
        searchField.addActionListener(e -> doSearch());
        searchBar.add(searchBtn);
        add(searchBar, BorderLayout.NORTH);

        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);

        doSearch();
    }

    private void doSearch() {
        model.setRowCount(0);
        List<Medicine> results = medicineDAO.searchByName(searchField.getText().trim());
        for (Medicine m : results) {
            model.addRow(new Object[]{
                    m.getName(), m.getCompany(), m.getMedicineType(), m.getPrice(),
                    m.getQuantityInStock(), m.getExpiryDate()
            });
        }
    }
}
