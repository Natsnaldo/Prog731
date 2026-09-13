-- ============================================================
-- Pharmacy Inventory Management System (PIMS) - HealthFirst
-- Database creation script
-- Programming 732 Assignment
-- ============================================================

DROP DATABASE IF EXISTS pims;
CREATE DATABASE pims;
USE pims;

-- ------------------------------------------------------------
-- Table: users
-- ------------------------------------------------------------
CREATE TABLE users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,       -- SHA-256 hash, not plain text
    role        ENUM('Admin','Cashier') NOT NULL,
    full_name   VARCHAR(100) NOT NULL
);

-- ------------------------------------------------------------
-- Table: suppliers
-- ------------------------------------------------------------
CREATE TABLE suppliers (
    supplier_id     INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    contact_person  VARCHAR(100),
    phone           VARCHAR(20),
    email           VARCHAR(100),
    address         TEXT
);

-- ------------------------------------------------------------
-- Table: medicines
-- ------------------------------------------------------------
CREATE TABLE medicines (
    medicine_id         INT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(150) NOT NULL,
    company             VARCHAR(100),
    medicine_type       VARCHAR(50),          -- Tablet, Capsule, Syrup, Injection, Cream
    price               DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    quantity_in_stock   INT NOT NULL DEFAULT 0,
    reorder_level       INT NOT NULL DEFAULT 0,
    expiry_date         DATE,
    supplier_id         INT,
    CONSTRAINT fk_medicine_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
        ON DELETE SET NULL
);

-- ------------------------------------------------------------
-- Table: sales  (transaction header)
-- ------------------------------------------------------------
CREATE TABLE sales (
    sale_id       INT AUTO_INCREMENT PRIMARY KEY,
    sale_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    user_id       INT,
    CONSTRAINT fk_sale_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE SET NULL
);

-- ------------------------------------------------------------
-- Table: sale_items (transaction line items - normalised)
-- ------------------------------------------------------------
CREATE TABLE sale_items (
    sale_item_id    INT AUTO_INCREMENT PRIMARY KEY,
    sale_id         INT NOT NULL,
    medicine_id     INT NOT NULL,
    quantity_sold   INT NOT NULL,
    price_at_sale   DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_saleitem_sale
        FOREIGN KEY (sale_id) REFERENCES sales(sale_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_saleitem_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)
);

-- ============================================================
-- Sample data
-- ============================================================

-- Default users (passwords stored as SHA-256 hashes, see util/PasswordUtil.java).
-- admin    / admin123
-- cashier  / cash123
-- Run: java com.healthfirst.pims.util.PasswordUtil  to regenerate/verify these hashes.
INSERT INTO users (username, password, role, full_name) VALUES
('admin',   '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Admin',   'System Administrator'),
('cashier', 'c246650737293ddc18fc357393db78d1ecc9d1fd1af95469115e4a29f983359a', 'Cashier', 'Front Till Cashier');

-- Suppliers
INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES
('MediSupply SA (Pty) Ltd', 'Thabo Nkosi',    '011 555 0101', 'sales@medisupply.co.za',   '12 Voortrekker Rd, Johannesburg'),
('PharmaCorp Distributors', 'Anisha Pillay',  '021 555 0202', 'orders@pharmacorp.co.za',  '45 Main Rd, Cape Town'),
('HealthLine Wholesalers',  'Johan van Wyk',  '031 555 0303', 'info@healthline.co.za',    '8 Umgeni Rd, Durban');

-- Medicines (mix of near-expiry, low-stock and normal stock for demo/report purposes)
INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES
('Paracetamol 500mg',      'Adcock Ingram', 'Tablet',    25.50,  120, 30, DATE_ADD(CURDATE(), INTERVAL 18 MONTH), 1),
('Amoxicillin 250mg',      'Aspen Pharma',  'Capsule',   65.00,   40, 20, DATE_ADD(CURDATE(), INTERVAL 20 DAY),  1),
('Cough Syrup 100ml',      'Cipla',         'Syrup',     45.75,   15, 25, DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 2),
('Insulin Injection',      'Novo Nordisk',  'Injection', 210.00,   8, 10, DATE_ADD(CURDATE(), INTERVAL 10 DAY),  2),
('Hydrocortisone Cream',   'GSK',           'Cream',     38.90,   50, 15, DATE_ADD(CURDATE(), INTERVAL 24 MONTH), 3),
('Ibuprofen 200mg',        'Pharma Dynamics','Tablet',   30.00,   90, 25, DATE_ADD(CURDATE(), INTERVAL 6 MONTH),  1),
('Vitamin C 1000mg',       'Adcock Ingram', 'Tablet',    55.00,    5,  20, DATE_ADD(CURDATE(), INTERVAL 15 MONTH), 3);
