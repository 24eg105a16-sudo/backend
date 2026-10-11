package com.expensetracker;

import com.expensetracker.dto.BudgetResponse;
import com.expensetracker.entity.Budget;
import com.expensetracker.entity.User;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.service.BudgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User sampleUser;
    private Budget sampleBudget;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Alex Johnson", "alex@example.com", "pass");
        sampleBudget = new Budget(10L, sampleUser, "Food", BigDecimal.valueOf(1000.00), "2026-10");
    }

    @Test
    void testBudgetStatus_Normal() {
        // Spent 500 out of 1000 (50%) -> NORMAL
        when(transactionRepository.sumExpenseByUserIdAndCategoryAndDateBetween(
                eq(1L), eq("Food"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.valueOf(500.00));

        BudgetResponse response = budgetService.mapToResponse(sampleBudget);

        assertEquals("NORMAL", response.getStatus());
        assertEquals(50.0, response.getPercentageUsed());
        assertEquals(BigDecimal.valueOf(500.00), response.getRemainingAmount());
    }

    @Test
    void testBudgetStatus_Warning() {
        // Spent 850 out of 1000 (85%) -> WARNING (>= 80%)
        when(transactionRepository.sumExpenseByUserIdAndCategoryAndDateBetween(
                eq(1L), eq("Food"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.valueOf(850.00));

        BudgetResponse response = budgetService.mapToResponse(sampleBudget);

        assertEquals("WARNING", response.getStatus());
        assertEquals(85.0, response.getPercentageUsed());
        assertEquals(BigDecimal.valueOf(150.00), response.getRemainingAmount());
    }

    @Test
    void testBudgetStatus_Exceeded() {
        // Spent 1200 out of 1000 (120%) -> EXCEEDED (> 100%)
        when(transactionRepository.sumExpenseByUserIdAndCategoryAndDateBetween(
                eq(1L), eq("Food"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.valueOf(1200.00));

        BudgetResponse response = budgetService.mapToResponse(sampleBudget);

        assertEquals("EXCEEDED", response.getStatus());
        assertEquals(120.0, response.getPercentageUsed());
        assertEquals(BigDecimal.valueOf(-200.00), response.getRemainingAmount());
    }
}
