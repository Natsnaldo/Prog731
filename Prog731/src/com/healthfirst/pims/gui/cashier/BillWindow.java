package com.healthfirst.pims.gui.cashier;

import com.healthfirst.pims.model.SaleItem;

import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Billing: shows a generated bill for the customer after checkout, and lets
 * the cashier save it to a text file ("print/save it", per the brief - a
 * simple desktop app has no physical printer, so Save-to-file stands in for
 * printing).
 */
public class BillWindow extends JFrame {

    public BillWindow(int saleId, String cashierName, List<SaleItem> items, BigDecimal total) {
        super("Bill - Sale #" + saleId);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(420, 500);
        setLocationRelativeTo(null);

        String billText = buildBillText(saleId, cashierName, items, total);

        JTextArea textArea = new JTextArea(billText);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JButton saveBtn = new JButton("Save / Print Bill");
        saveBtn.addActionListener(e -> saveBillToFile(saleId, billText));

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.add(saveBtn);
        buttons.add(closeBtn);

        setLayout(new BorderLayout());
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private String buildBillText(int saleId, String cashierName, List<SaleItem> items, BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("        HEALTHFIRST PHARMACY\n");
        sb.append("========================================\n");
        sb.append("Sale ID   : ").append(saleId).append('\n');
        sb.append("Cashier   : ").append(cashierName).append('\n');
        sb.append("Date/Time : ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append('\n');
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-20s %5s %10s%n", "Item", "Qty", "Amount"));
        sb.append("----------------------------------------\n");
        for (SaleItem item : items) {
            sb.append(String.format("%-20s %5d %10.2f%n",
                    trim(item.getMedicineName(), 20), item.getQuantitySold(), item.getLineTotal()));
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-26s R %8.2f%n", "TOTAL", total));
        sb.append("========================================\n");
        sb.append("       Thank you for your purchase!\n");
        return sb.toString();
    }

    private String trim(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }

    private void saveBillToFile(int saleId, String billText) {
        String fileName = "bill_sale_" + saleId + ".txt";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(billText);
            JOptionPane.showMessageDialog(this, "Bill saved as " + fileName +
                    " in the application folder.\n(Hand this file to a printer to print it.)");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not save bill: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
