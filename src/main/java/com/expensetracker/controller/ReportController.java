package com.expensetracker.controller;

import com.expensetracker.dto.ApiResponse;
import com.expensetracker.dto.CategoryBreakdownDto;
import com.expensetracker.dto.MonthlySummaryDto;
import com.expensetracker.entity.TransactionType;
import com.expensetracker.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<MonthlySummaryDto>>> getMonthlyReport(
            @RequestParam(defaultValue = "12") int months) {
        List<MonthlySummaryDto> report = reportService.getMonthlyReport(months);
        return ResponseEntity.ok(ApiResponse.success("Monthly report retrieved successfully", report));
    }

    @GetMapping("/category")
    public ResponseEntity<ApiResponse<List<CategoryBreakdownDto>>> getCategoryReport(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<CategoryBreakdownDto> report = reportService.getCategoryReport(type, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Category report retrieved successfully", report));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search) {

        byte[] csvData = reportService.exportTransactionsToCsv(type, category, startDate, endDate, search);

        String filename = "transactions_export_" + LocalDate.now() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .contentLength(csvData.length)
                .body(csvData);
    }
}
