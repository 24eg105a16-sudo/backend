package com.expensetracker.service;

import com.expensetracker.dto.CategoryBreakdownDto;
import com.expensetracker.dto.MonthlySummaryDto;
import com.expensetracker.dto.TransactionResponse;
import com.expensetracker.entity.TransactionType;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {

    private final DashboardService dashboardService;
    private final TransactionService transactionService;

    public ReportService(DashboardService dashboardService, TransactionService transactionService) {
        this.dashboardService = dashboardService;
        this.transactionService = transactionService;
    }

    public List<MonthlySummaryDto> getMonthlyReport(int months) {
        return dashboardService.getMonthlySummary(months);
    }

    public List<CategoryBreakdownDto> getCategoryReport(TransactionType type, LocalDate startDate, LocalDate endDate) {
        return dashboardService.getCategoryBreakdown(type, startDate, endDate);
    }

    public byte[] exportTransactionsToCsv(
            TransactionType type,
            String category,
            LocalDate startDate,
            LocalDate endDate,
            String search) {

        List<TransactionResponse> transactions = transactionService.getAllTransactionsFiltered(
                type, category, startDate, endDate, search, "transactionDate", "DESC");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            // Write UTF-8 BOM so Excel opens it with proper encoding
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            // CSV Header
            writer.println("Transaction ID,Type,Amount,Category,Description,Date,Created At");

            for (TransactionResponse tx : transactions) {
                writer.printf("%d,%s,%.2f,\"%s\",\"%s\",%s,%s%n",
                        tx.getId(),
                        tx.getType(),
                        tx.getAmount(),
                        escapeCsv(tx.getCategory()),
                        escapeCsv(tx.getDescription() != null ? tx.getDescription() : ""),
                        tx.getTransactionDate(),
                        tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : ""
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV export", e);
        }

        return out.toByteArray();
    }

    private String escapeCsv(String input) {
        if (input == null) return "";
        return input.replace("\"", "\"\"");
    }
}
