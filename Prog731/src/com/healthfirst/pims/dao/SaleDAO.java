package com.healthfirst.pims.dao;

import com.healthfirst.pims.db.DBConnection;
import com.healthfirst.pims.model.Sale;
import com.healthfirst.pims.model.SaleItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access class for "sales" and "sale_items" - the Point of Sale checkout,
 * and the Sales / Item-Wise reports used by the Admin module.
 */
public class SaleDAO {

    private final MedicineDAO medicineDAO = new MedicineDAO();

    /**
     * Records a completed sale as a single database transaction: insert the
     * sale header, insert each line item, and decrement stock for each
     * medicine sold. If anything fails, the whole sale is rolled back so the
     * database is never left half-updated (per the JDBC study guide's
     * setAutoCommit(false) / commit() / rollback() pattern).
     *
     * @return the generated sale_id, or -1 on failure.
     */
    public int checkout(int cashierUserId, List<SaleItem> items) {
        if (items == null || items.isEmpty()) return -1;

        BigDecimal total = BigDecimal.ZERO;
        for (SaleItem item : items) total = total.add(item.getLineTotal());

        String insertSale = "INSERT INTO sales (total_amount, user_id) VALUES (?, ?)";
        String insertItem = "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int saleId;
            try (PreparedStatement stmt = conn.prepareStatement(insertSale, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setBigDecimal(1, total);
                stmt.setInt(2, cashierUserId);
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Could not obtain generated sale_id");
                    saleId = keys.getInt(1);
                }
            }

            for (SaleItem item : items) {
                // Enforce stock availability inside the same transaction.
                boolean stockOk = medicineDAO.reduceStock(conn, item.getMedicineId(), item.getQuantitySold());
                if (!stockOk) {
                    throw new SQLException("Insufficient stock for medicine id " + item.getMedicineId());
                }
                try (PreparedStatement stmt = conn.prepareStatement(insertItem)) {
                    stmt.setInt(1, saleId);
                    stmt.setInt(2, item.getMedicineId());
                    stmt.setInt(3, item.getQuantitySold());
                    stmt.setBigDecimal(4, item.getPriceAtSale());
                    stmt.executeUpdate();
                }
            }

            conn.commit();
            return saleId;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException rollbackEx) { rollbackEx.printStackTrace(); }
            }
            return -1;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public List<SaleItem> getItemsForSale(int saleId) {
        List<SaleItem> list = new ArrayList<>();
        String sql = "SELECT si.*, m.name AS medicine_name FROM sale_items si " +
                "JOIN medicines m ON si.medicine_id = m.medicine_id WHERE si.sale_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, saleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem(
                            rs.getInt("medicine_id"),
                            rs.getString("medicine_name"),
                            rs.getInt("quantity_sold"),
                            rs.getBigDecimal("price_at_sale")
                    );
                    item.setSaleItemId(rs.getInt("sale_item_id"));
                    item.setSaleId(saleId);
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Report: every sale between two dates (inclusive), most recent first. */
    public List<Sale> getSalesReport(java.time.LocalDate from, java.time.LocalDate to) {
        List<Sale> list = new ArrayList<>();
        String sql = "SELECT s.*, u.full_name AS cashier_name FROM sales s " +
                "LEFT JOIN users u ON s.user_id = u.user_id " +
                "WHERE DATE(s.sale_date) BETWEEN ? AND ? ORDER BY s.sale_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(from));
            stmt.setDate(2, Date.valueOf(to));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = new Sale();
                    sale.setSaleId(rs.getInt("sale_id"));
                    sale.setSaleDate(rs.getTimestamp("sale_date"));
                    sale.setTotalAmount(rs.getBigDecimal("total_amount"));
                    int userId = rs.getInt("user_id");
                    sale.setUserId(rs.wasNull() ? null : userId);
                    sale.setCashierName(rs.getString("cashier_name"));
                    list.add(sale);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Convenience row for the Item-Wise report. */
    public static class ItemWiseRow {
        public String medicineName;
        public int totalQuantitySold;
        public BigDecimal totalRevenue;
    }

    /** Report: total quantity sold and revenue generated per medicine. */
    public List<ItemWiseRow> getItemWiseReport(java.time.LocalDate from, java.time.LocalDate to) {
        List<ItemWiseRow> list = new ArrayList<>();
        String sql = "SELECT m.name AS medicine_name, SUM(si.quantity_sold) AS qty, " +
                "SUM(si.quantity_sold * si.price_at_sale) AS revenue " +
                "FROM sale_items si " +
                "JOIN sales s ON si.sale_id = s.sale_id " +
                "JOIN medicines m ON si.medicine_id = m.medicine_id " +
                "WHERE DATE(s.sale_date) BETWEEN ? AND ? " +
                "GROUP BY m.medicine_id, m.name ORDER BY revenue DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(from));
            stmt.setDate(2, Date.valueOf(to));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ItemWiseRow row = new ItemWiseRow();
                    row.medicineName = rs.getString("medicine_name");
                    row.totalQuantitySold = rs.getInt("qty");
                    row.totalRevenue = rs.getBigDecimal("revenue");
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
