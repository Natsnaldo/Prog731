package com.healthfirst.pims.gui.admin;

import com.healthfirst.pims.dao.MedicineDAO;
import com.healthfirst.pims.dao.SaleDAO;
import com.healthfirst.pims.model.Medicine;
import com.healthfirst.pims.model.Sale;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Admin tab: Report Generation.
 * Four analytical reports the pharmacy owner/manager needs:
 *   Sales report, Item-Wise report, Low Stock report, Expiry report
 *   (medicines expiring within the next month).
 */
public class ReportsPanel extends JPanel {

    private final SaleDAO saleDAO = new SaleDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();

    private final JTextField fromField = new JTextField(LocalDate.now().minusDays(30).toString(), 10);
    private final JTextField toField = new JTextField(LocalDate.now().toString(), 10);

    private final DefaultTableModel salesModel = new DefaultTableModel(
            new Object[]{"Sale ID", "Date/Time", "Cashier", "Total (R)"}, 0);
    private final DefaultTableModel itemWiseModel = new DefaultTableModel(
            new Object[]{"Medicine", "Qty Sold", "Revenue (R)"}, 0);
    private final DefaultTableModel lowStockModel = new DefaultTableModel(
            new Object[]{"Medicine", "In Stock", "Reorder Level", "Supplier"}, 0);
    private final DefaultTableModel expiryModel = new DefaultTableModel(
            new Object[]{"Medicine", "Expiry Date", "In Stock", "Supplier"}, 0);

    public ReportsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel dateBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dateBar.add(new JLabel("From (YYYY-MM-DD):"));
        dateBar.add(fromField);
        dateBar.add(new JLabel("To:"));
        dateBar.add(toField);
        JButton runBtn = new JButton("Run Sales / Item-Wise Reports");
        runBtn.addActionListener(e -> runDateRangeReports());
        dateBar.add(runBtn);
        add(dateBar, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Sales Report", new JScrollPane(new JTable(salesModel)));
        tabs.addTab("Item-Wise Report", new JScrollPane(new JTable(itemWiseModel)));
        tabs.addTab("Low Stock Report", new JScrollPane(new JTable(lowStockModel)));
        tabs.addTab("Expiry Report (next 1 month)", new JScrollPane(new JTable(expiryModel)));
        add(tabs, BorderLayout.CENTER);

        refreshStockAndExpiryReports();
        runDateRangeReports();
    }

    private void runDateRangeReports() {
        LocalDate from, to;
        try {
            from = LocalDate.parse(fromField.getText().trim());
            to = LocalDate.parse(toField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid dates in YYYY-MM-DD format.",
                    "Invalid date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        salesModel.setRowCount(0);
        List<Sale> sales = saleDAO.getSalesReport(from, to);
        for (Sale s : sales) {
            salesModel.addRow(new Object[]{
                    s.getSaleId(), s.getSaleDate(),
                    s.getCashierName() == null ? "-" : s.getCashierName(),
                    s.getTotalAmount()
            });
        }

        itemWiseModel.setRowCount(0);
        List<SaleDAO.ItemWiseRow> rows = saleDAO.getItemWiseReport(from, to);
        for (SaleDAO.ItemWiseRow row : rows) {
            itemWiseModel.addRow(new Object[]{row.medicineName, row.totalQuantitySold, row.totalRevenue});
        }
    }

    private void refreshStockAndExpiryReports() {
        lowStockModel.setRowCount(0);
        List<Medicine> lowStock = medicineDAO.getLowStockMedicines();
        for (Medicine m : lowStock) {
            lowStockModel.addRow(new Object[]{
                    m.getName(), m.getQuantityInStock(), m.getReorderLevel(),
                    m.getSupplierName() == null ? "-" : m.getSupplierName()
            });
        }

        expiryModel.setRowCount(0);
        List<Medicine> expiring = medicineDAO.getExpiringWithinNextMonth();
        for (Medicine m : expiring) {
            expiryModel.addRow(new Object[]{
                    m.getName(), m.getExpiryDate(), m.getQuantityInStock(),
                    m.getSupplierName() == null ? "-" : m.getSupplierName()
            });
        }
    }

    /** Called whenever the Reports tab is shown, so figures are always current. */
    public void refreshAll() {
        refreshStockAndExpiryReports();
        runDateRangeReports();
    }
}
