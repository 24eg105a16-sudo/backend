package com.expensetracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public class BudgetRequest {

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Monthly limit is required")
    @DecimalMin(value = "0.01", message = "Monthly limit must be greater than zero")
    private BigDecimal monthlyLimit;

    @NotBlank(message = "Budget month is required (format YYYY-MM)")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Budget month must follow YYYY-MM format (e.g. 2026-10)")
    private String budgetMonth;

    public BudgetRequest() {
    }

    public BudgetRequest(String category, BigDecimal monthlyLimit, String budgetMonth) {
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        this.budgetMonth = budgetMonth;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public String getBudgetMonth() {
        return budgetMonth;
    }

    public void setBudgetMonth(String budgetMonth) {
        this.budgetMonth = budgetMonth;
    }
}
