package com.expensetracker.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DashboardSummaryResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal availableBalance;
    private BigDecimal currentMonthSpending;
    private BigDecimal currentMonthIncome;
    private List<TransactionResponse> recentTransactions = new ArrayList<>();
    private List<BudgetResponse> monthlyBudgetUsage = new ArrayList<>();

    public DashboardSummaryResponse() {
        this.totalIncome = BigDecimal.ZERO;
        this.totalExpenses = BigDecimal.ZERO;
        this.availableBalance = BigDecimal.ZERO;
        this.currentMonthSpending = BigDecimal.ZERO;
        this.currentMonthIncome = BigDecimal.ZERO;
    }

    public DashboardSummaryResponse(BigDecimal totalIncome, BigDecimal totalExpenses, BigDecimal availableBalance, BigDecimal currentMonthSpending, BigDecimal currentMonthIncome, List<TransactionResponse> recentTransactions, List<BudgetResponse> monthlyBudgetUsage) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.availableBalance = availableBalance;
        this.currentMonthSpending = currentMonthSpending;
        this.currentMonthIncome = currentMonthIncome;
        this.recentTransactions = recentTransactions;
        this.monthlyBudgetUsage = monthlyBudgetUsage;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getCurrentMonthSpending() {
        return currentMonthSpending;
    }

    public void setCurrentMonthSpending(BigDecimal currentMonthSpending) {
        this.currentMonthSpending = currentMonthSpending;
    }

    public BigDecimal getCurrentMonthIncome() {
        return currentMonthIncome;
    }

    public void setCurrentMonthIncome(BigDecimal currentMonthIncome) {
        this.currentMonthIncome = currentMonthIncome;
    }

    public List<TransactionResponse> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<TransactionResponse> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public List<BudgetResponse> getMonthlyBudgetUsage() {
        return monthlyBudgetUsage;
    }

    public void setMonthlyBudgetUsage(List<BudgetResponse> monthlyBudgetUsage) {
        this.monthlyBudgetUsage = monthlyBudgetUsage;
    }
}
