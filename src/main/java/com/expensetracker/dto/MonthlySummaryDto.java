package com.expensetracker.dto;

import java.math.BigDecimal;

public class MonthlySummaryDto {
    private String month;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal balance;

    public MonthlySummaryDto() {
        this.income = BigDecimal.ZERO;
        this.expense = BigDecimal.ZERO;
        this.balance = BigDecimal.ZERO;
    }

    public MonthlySummaryDto(String month, BigDecimal income, BigDecimal expense, BigDecimal balance) {
        this.month = month;
        this.income = income;
        this.expense = expense;
        this.balance = balance;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public BigDecimal getExpense() {
        return expense;
    }

    public void setExpense(BigDecimal expense) {
        this.expense = expense;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
