package com.expensetracker.dto;

import java.math.BigDecimal;

public class CategoryBreakdownDto {
    private String category;
    private BigDecimal totalAmount;
    private double percentage;
    private long count;

    public CategoryBreakdownDto() {
        this.totalAmount = BigDecimal.ZERO;
    }

    public CategoryBreakdownDto(String category, BigDecimal totalAmount, double percentage, long count) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
        this.count = count;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
