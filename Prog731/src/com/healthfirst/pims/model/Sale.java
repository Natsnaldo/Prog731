package com.healthfirst.pims.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** Plain data object mirroring the "sales" table (transaction header). */
public class Sale {
    private int saleId;
    private Timestamp saleDate;
    private BigDecimal totalAmount;
    private Integer userId;
    private String cashierName; // convenience field for reports

    public Sale() {}

    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public Timestamp getSaleDate() { return saleDate; }
    public void setSaleDate(Timestamp saleDate) { this.saleDate = saleDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }
}
