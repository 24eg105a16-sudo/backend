package com.expensetracker.service;

import com.expensetracker.dto.CategoryBreakdownDto;
import com.expensetracker.dto.DashboardSummaryResponse;
import com.expensetracker.dto.MonthlySummaryDto;
import com.expensetracker.dto.TransactionResponse;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.TransactionType;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.security.SecurityUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final BudgetService budgetService;

    public DashboardService(TransactionRepository transactionRepository, BudgetService budgetService) {
        this.transactionRepository = transactionRepository;
        this.budgetService = budgetService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        Long userId = SecurityUtils.getCurrentUserId();

        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.INCOME);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;

        BigDecimal totalExpenses = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.EXPENSE);
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        BigDecimal availableBalance = totalIncome.subtract(totalExpenses);

        YearMonth currentYearMonth = YearMonth.now();
        LocalDate startOfMonth = currentYearMonth.atDay(1);
        LocalDate endOfMonth = currentYearMonth.atEndOfMonth();

        BigDecimal currentMonthSpending = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId, TransactionType.EXPENSE, startOfMonth, endOfMonth);
        if (currentMonthSpending == null) currentMonthSpending = BigDecimal.ZERO;

        BigDecimal currentMonthIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                userId, TransactionType.INCOME, startOfMonth, endOfMonth);
        if (currentMonthIncome == null) currentMonthIncome = BigDecimal.ZERO;

        // Recent 5 transactions
        List<Transaction> recent = transactionRepository.findByUserIdOrderByTransactionDateDescCreatedAtDesc(
                userId, PageRequest.of(0, 5));
        List<TransactionResponse> recentResponses = recent.stream().map(TransactionResponse::fromEntity).toList();

        // Monthly budget usage for current month
        String currentMonthStr = currentYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        var budgetUsage = budgetService.getBudgets(currentMonthStr);

        return new DashboardSummaryResponse(
                totalIncome,
                totalExpenses,
                availableBalance,
                currentMonthSpending,
                currentMonthIncome,
                recentResponses,
                budgetUsage
        );
    }

    @Transactional(readOnly = true)
    public List<MonthlySummaryDto> getMonthlySummary(int months) {
        Long userId = SecurityUtils.getCurrentUserId();
        int monthsCount = months > 0 ? Math.min(months, 24) : 6;

        YearMonth endYm = YearMonth.now();
        YearMonth startYm = endYm.minusMonths(monthsCount - 1);

        LocalDate startDate = startYm.atDay(1);
        LocalDate endDate = endYm.atEndOfMonth();

        List<Object[]> rawData = transactionRepository.getMonthlySummaryData(userId, startDate, endDate);

        // Pre-populate all months in chronological order with zero totals
        Map<String, MonthlySummaryDto> map = new LinkedHashMap<>();
        YearMonth cursor = startYm;
        while (!cursor.isAfter(endYm)) {
            String monthKey = cursor.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            map.put(monthKey, new MonthlySummaryDto(monthKey, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
            cursor = cursor.plusMonths(1);
        }

        for (Object[] row : rawData) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            TransactionType type = (TransactionType) row[2];
            BigDecimal amount = (BigDecimal) row[3];

            String monthKey = String.format("%04d-%02d", year, month);
            MonthlySummaryDto dto = map.get(monthKey);
            if (dto != null) {
                if (type == TransactionType.INCOME) {
                    dto.setIncome(amount);
                } else if (type == TransactionType.EXPENSE) {
                    dto.setExpense(amount);
                }
                dto.setBalance(dto.getIncome().subtract(dto.getExpense()));
            }
        }

        return new ArrayList<>(map.values());
    }

    @Transactional(readOnly = true)
    public List<CategoryBreakdownDto> getCategoryBreakdown(TransactionType type, LocalDate startDate, LocalDate endDate) {
        Long userId = SecurityUtils.getCurrentUserId();

        TransactionType filterType = type != null ? type : TransactionType.EXPENSE;
        LocalDate start = startDate != null ? startDate : YearMonth.now().atDay(1);
        LocalDate end = endDate != null ? endDate : YearMonth.now().atEndOfMonth();

        List<Object[]> rows = transactionRepository.getCategoryBreakdown(userId, filterType, start, end);

        BigDecimal totalSum = BigDecimal.ZERO;
        for (Object[] row : rows) {
            BigDecimal amount = (BigDecimal) row[1];
            if (amount != null) {
                totalSum = totalSum.add(amount);
            }
        }

        List<CategoryBreakdownDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            long count = ((Number) row[2]).longValue();

            double percentage = 0.0;
            if (totalSum.compareTo(BigDecimal.ZERO) > 0 && amount != null) {
                percentage = amount.divide(totalSum, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }

            result.add(new CategoryBreakdownDto(
                    category,
                    amount != null ? amount : BigDecimal.ZERO,
                    Math.round(percentage * 10.0) / 10.0,
                    count
            ));
        }

        return result;
    }
}
