package com.expensetracker.service;

import com.expensetracker.dto.BudgetRequest;
import com.expensetracker.dto.BudgetResponse;
import com.expensetracker.entity.Budget;
import com.expensetracker.entity.User;
import com.expensetracker.exception.DuplicateResourceException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.exception.UnauthorizedException;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         TransactionRepository transactionRepository,
                         UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BudgetResponse createBudget(BudgetRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        String normalizedCategory = request.getCategory().trim();
        String budgetMonth = request.getBudgetMonth().trim();

        if (budgetRepository.existsByUserIdAndCategoryIgnoreCaseAndBudgetMonth(userId, normalizedCategory, budgetMonth)) {
            throw new DuplicateResourceException("A budget for category '" + normalizedCategory + "' already exists for month " + budgetMonth);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(normalizedCategory);
        budget.setMonthlyLimit(request.getMonthlyLimit());
        budget.setBudgetMonth(budgetMonth);

        Budget saved = budgetRepository.save(budget);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(String budgetMonth) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Budget> budgets;

        if (budgetMonth != null && !budgetMonth.isBlank()) {
            budgets = budgetRepository.findByUserIdAndBudgetMonthOrderByCategoryAsc(userId, budgetMonth.trim());
        } else {
            budgets = budgetRepository.findByUserIdOrderByBudgetMonthDescCategoryAsc(userId);
        }

        return budgets.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        if (!budget.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view this budget");
        }

        return mapToResponse(budget);
    }

    @Transactional
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        if (!budget.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to update this budget");
        }

        String normalizedCategory = request.getCategory().trim();
        String budgetMonth = request.getBudgetMonth().trim();

        if (budgetRepository.existsByUserIdAndCategoryIgnoreCaseAndBudgetMonthAndIdNot(userId, normalizedCategory, budgetMonth, id)) {
            throw new DuplicateResourceException("A budget for category '" + normalizedCategory + "' already exists for month " + budgetMonth);
        }

        budget.setCategory(normalizedCategory);
        budget.setMonthlyLimit(request.getMonthlyLimit());
        budget.setBudgetMonth(budgetMonth);

        Budget updated = budgetRepository.save(budget);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteBudget(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        if (!budget.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to delete this budget");
        }

        budgetRepository.delete(budget);
    }

    public BudgetResponse mapToResponse(Budget budget) {
        Long userId = budget.getUser().getId();
        YearMonth ym = YearMonth.parse(budget.getBudgetMonth());
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        BigDecimal spent = transactionRepository.sumExpenseByUserIdAndCategoryAndDateBetween(
                userId, budget.getCategory(), start, end);
        if (spent == null) {
            spent = BigDecimal.ZERO;
        }

        BigDecimal remaining = budget.getMonthlyLimit().subtract(spent);

        double percentage = 0.0;
        if (budget.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0) {
            percentage = spent.divide(budget.getMonthlyLimit(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        String status;
        if (percentage > 100.0) {
            status = "EXCEEDED";
        } else if (percentage >= 80.0) {
            status = "WARNING";
        } else {
            status = "NORMAL";
        }

        return new BudgetResponse(
                budget.getId(),
                userId,
                budget.getCategory(),
                budget.getMonthlyLimit(),
                budget.getBudgetMonth(),
                spent,
                remaining,
                Math.round(percentage * 100.0) / 100.0,
                status,
                budget.getCreatedAt()
        );
    }
}
