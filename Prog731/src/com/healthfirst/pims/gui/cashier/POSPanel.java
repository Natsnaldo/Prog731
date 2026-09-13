package com.healthfirst.pims.gui.cashier;

import com.healthfirst.pims.dao.MedicineDAO;
import com.healthfirst.pims.dao.SaleDAO;
import com.healthfirst.pims.model.Medicine;
import com.healthfirst.pims.model.SaleItem;
import com.healthfirst.pims.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Cashier Module - Point of Sale (POS) Interface.
 * A fast screen for making sales (simulation only), building a cart of
 * items, and checking out to generate a bill. Cashiers cannot add or edit
 * medicines here - this screen only reads stock and prices.
 */
public class POSPanel extends JPanel {

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final User cashier;

    private final JTextField searchField = new JTextField(18);
    private final DefaultTableModel searchModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Price (R)", "In Stock"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable searchTable = new JTable(searchModel);

    private final DefaultTableModel cartModel = new DefaultTableModel(
            new Object[]{"Medicine", "Unit Price", "Qty", "Line Total"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable cartTable = new JTable(cartModel);
    private final List<Medicine> cartMedicines = new ArrayList<>();
    private final List<Integer> cartQuantities = new ArrayList<>();

    private final JLabel totalLabel = new JLabel("Total: R 0.00");

    public POSPanel(User cashier) {
        this.cashier = cashier;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- Top: search bar ----
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.add(new JLabel("Search medicine:"));
        searchBar.add(searchField);
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> doSearch());
        searchField.addActionListener(e -> doSearch());
        searchBar.add(searchBtn);
        add(searchBar, BorderLayout.NORTH);

        // ---- Center split: results (left) + cart (right) ----
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Available Medicines"));
        resultsPanel.add(new JScrollPane(searchTable), BorderLayout.CENTER);
        JButton addToCartBtn = new JButton("Add to Cart");
        addToCartBtn.addActionListener(e -> addSelectedToCart());
        JPanel resultsButtonBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        resultsButtonBar.add(addToCartBtn);
        resultsPanel.add(resultsButtonBar, BorderLayout.SOUTH);

        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBorder(BorderFactory.createTitledBorder("Cart"));
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel cartFooter = new JPanel(new BorderLayout());
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 16f));
        cartFooter.add(totalLabel, BorderLayout.WEST);

        JPanel cartButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton removeBtn = new JButton("Remove Item");
        removeBtn.addActionListener(e -> removeSelectedFromCart());
        JButton clearBtn = new JButton("Clear Cart");
        clearBtn.addActionListener(e -> clearCart());
        JButton checkoutBtn = new JButton("Checkout");
        checkoutBtn.setFont(checkoutBtn.getFont().deriveFont(Font.BOLD));
        checkoutBtn.addActionListener(e -> checkout());
        cartButtons.add(removeBtn);
        cartButtons.add(clearBtn);
        cartButtons.add(checkoutBtn);
        cartFooter.add(cartButtons, BorderLayout.EAST);
        cartPanel.add(cartFooter, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, resultsPanel, cartPanel);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);

        doSearch(); // show everything on load
    }

    private void doSearch() {
        searchModel.setRowCount(0);
        List<Medicine> results = medicineDAO.searchByName(searchField.getText().trim());
        for (Medicine m : results) {
            searchModel.addRow(new Object[]{m.getMedicineId(), m.getName(), m.getPrice(), m.getQuantityInStock()});
        }
    }

    private void addSelectedToCart() {
        int row = searchTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a medicine first.");
            return;
        }
        int medicineId = (int) searchModel.getValueAt(row, 0);
        Medicine m = medicineDAO.getById(medicineId);
        if (m == null) return;

        if (m.getQuantityInStock() <= 0) {
            JOptionPane.showMessageDialog(this, m.getName() + " is out of stock.", "Out of stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String qtyStr = JOptionPane.showInputDialog(this, "Quantity for " + m.getName() +
                " (in stock: " + m.getQuantityInStock() + "):", "1");
        if (qtyStr == null) return;
        int qty;
        try {
            qty = Integer.parseInt(qtyStr.trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a whole number.", "Invalid quantity", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (qty <= 0 || qty > m.getQuantityInStock()) {
            JOptionPane.showMessageDialog(this, "Quantity must be between 1 and " + m.getQuantityInStock() + ".",
                    "Invalid quantity", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // If it's already in the cart, just bump the quantity instead of adding a duplicate row.
        int existingIndex = -1;
        for (int i = 0; i < cartMedicines.size(); i++) {
            if (cartMedicines.get(i).getMedicineId() == m.getMedicineId()) { existingIndex = i; break; }
        }
        if (existingIndex >= 0) {
            int newQty = cartQuantities.get(existingIndex) + qty;
            if (newQty > m.getQuantityInStock()) {
                JOptionPane.showMessageDialog(this, "That would exceed available stock.", "Invalid quantity", JOptionPane.WARNING_MESSAGE);
                return;
            }
            cartQuantities.set(existingIndex, newQty);
        } else {
            cartMedicines.add(m);
            cartQuantities.add(qty);
        }
        rebuildCartTable();
    }

    private void removeSelectedFromCart() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a cart item to remove first.");
            return;
        }
        cartMedicines.remove(row);
        cartQuantities.remove(row);
        rebuildCartTable();
    }

    private void clearCart() {
        cartMedicines.clear();
        cartQuantities.clear();
        rebuildCartTable();
    }

    private void rebuildCartTable() {
        cartModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < cartMedicines.size(); i++) {
            Medicine m = cartMedicines.get(i);
            int qty = cartQuantities.get(i);
            BigDecimal lineTotal = m.getPrice().multiply(BigDecimal.valueOf(qty));
            total = total.add(lineTotal);
            cartModel.addRow(new Object[]{m.getName(), m.getPrice(), qty, lineTotal});
        }
        totalLabel.setText(String.format("Total: R %.2f", total));
    }

    private void checkout() {
        if (cartMedicines.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The cart is empty.");
            return;
        }

        List<SaleItem> items = new ArrayList<>();
        for (int i = 0; i < cartMedicines.size(); i++) {
            Medicine m = cartMedicines.get(i);
            items.add(new SaleItem(m.getMedicineId(), m.getName(), cartQuantities.get(i), m.getPrice()));
        }

        int saleId = saleDAO.checkout(cashier.getUserId(), items);
        if (saleId == -1) {
            JOptionPane.showMessageDialog(this, "Checkout failed - stock may have changed. Please refresh and try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (SaleItem item : items) total = total.add(item.getLineTotal());

        new BillWindow(saleId, cashier.getFullName(), items, total).setVisible(true);

        clearCart();
        doSearch(); // refresh stock figures shown in the search results
    }
}
