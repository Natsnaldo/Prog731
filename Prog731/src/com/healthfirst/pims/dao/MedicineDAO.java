package com.healthfirst.pims.dao;

import com.healthfirst.pims.db.DBConnection;
import com.healthfirst.pims.model.Medicine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Data-access class for the "medicines" table - full CRUD plus stock-related report queries. */
public class MedicineDAO {

    private static final String BASE_SELECT =
            "SELECT m.*, s.name AS supplier_name FROM medicines m " +
            "LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id ";

    public List<Medicine> getAllMedicines() {
        List<Medicine> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY m.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Used by the cashier's Stock Check screen and the POS search box. */
    public List<Medicine> searchByName(String keyword) {
        List<Medicine> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE m.name LIKE ? ORDER BY m.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Medicine getById(int medicineId) {
        String sql = BASE_SELECT + "WHERE m.medicine_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, medicineId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addMedicine(Medicine m) {
        String sql = "INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, " +
                "reorder_level, expiry_date, supplier_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindMedicine(stmt, m);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateMedicine(Medicine m) {
        String sql = "UPDATE medicines SET name = ?, company = ?, medicine_type = ?, price = ?, " +
                "quantity_in_stock = ?, reorder_level = ?, expiry_date = ?, supplier_id = ? WHERE medicine_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindMedicine(stmt, m);
            stmt.setInt(9, m.getMedicineId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteMedicine(int medicineId) {
        String sql = "DELETE FROM medicines WHERE medicine_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, medicineId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Reduces stock after a sale. Called from within SaleDAO's transaction. */
    public boolean reduceStock(Connection conn, int medicineId, int quantitySold) throws SQLException {
        String sql = "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? " +
                "WHERE medicine_id = ? AND quantity_in_stock >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantitySold);
            stmt.setInt(2, medicineId);
            stmt.setInt(3, quantitySold);
            return stmt.executeUpdate() > 0;
        }
    }

    /** Report: medicines at or below their reorder level. */
    public List<Medicine> getLowStockMedicines() {
        List<Medicine> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE m.quantity_in_stock <= m.reorder_level ORDER BY m.quantity_in_stock";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Report: medicines expiring within the next calendar month. */
    public List<Medicine> getExpiringWithinNextMonth() {
        List<Medicine> list = new ArrayList<>();
        String sql = BASE_SELECT +
                "WHERE m.expiry_date IS NOT NULL " +
                "AND m.expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 1 MONTH) " +
                "ORDER BY m.expiry_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void bindMedicine(PreparedStatement stmt, Medicine m) throws SQLException {
        stmt.setString(1, m.getName());
        stmt.setString(2, m.getCompany());
        stmt.setString(3, m.getMedicineType());
        stmt.setBigDecimal(4, m.getPrice());
        stmt.setInt(5, m.getQuantityInStock());
        stmt.setInt(6, m.getReorderLevel());
        if (m.getExpiryDate() != null) {
            stmt.setDate(7, Date.valueOf(m.getExpiryDate()));
        } else {
            stmt.setNull(7, Types.DATE);
        }
        if (m.getSupplierId() != null) {
            stmt.setInt(8, m.getSupplierId());
        } else {
            stmt.setNull(8, Types.INTEGER);
        }
    }

    private Medicine mapRow(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setMedicineId(rs.getInt("medicine_id"));
        m.setName(rs.getString("name"));
        m.setCompany(rs.getString("company"));
        m.setMedicineType(rs.getString("medicine_type"));
        m.setPrice(rs.getBigDecimal("price"));
        m.setQuantityInStock(rs.getInt("quantity_in_stock"));
        m.setReorderLevel(rs.getInt("reorder_level"));
        Date expiry = rs.getDate("expiry_date");
        m.setExpiryDate(expiry != null ? expiry.toLocalDate() : null);
        int supplierId = rs.getInt("supplier_id");
        m.setSupplierId(rs.wasNull() ? null : supplierId);
        m.setSupplierName(rs.getString("supplier_name"));
        return m;
    }
}
