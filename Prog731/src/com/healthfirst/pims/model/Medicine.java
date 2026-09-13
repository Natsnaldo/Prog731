package com.healthfirst.pims.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Plain data object mirroring the "medicines" table. */
public class Medicine {
    private int medicineId;
    private String name;
    private String company;
    private String medicineType;
    private BigDecimal price;
    private int quantityInStock;
    private int reorderLevel;
    private LocalDate expiryDate;
    private Integer supplierId;   // may be null
    private String supplierName;  // convenience field, filled in by joined queries

    public Medicine() {}

    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getMedicineType() { return medicineType; }
    public void setMedicineType(String medicineType) { this.medicineType = medicineType; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(int quantityInStock) { this.quantityInStock = quantityInStock; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Integer getSupplierId() { return supplierId; }
    public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public boolean isLowStock() { return quantityInStock <= reorderLevel; }

    @Override
    public String toString() { return name; }
}
