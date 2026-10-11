package com.expensetracker.repository;

import com.expensetracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserIdAndBudgetMonthOrderByCategoryAsc(Long userId, String budgetMonth);

    List<Budget> findByUserIdOrderByBudgetMonthDescCategoryAsc(Long userId);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    Optional<Budget> findByUserIdAndCategoryIgnoreCaseAndBudgetMonth(Long userId, String category, String budgetMonth);

    boolean existsByUserIdAndCategoryIgnoreCaseAndBudgetMonth(Long userId, String category, String budgetMonth);

    boolean existsByUserIdAndCategoryIgnoreCaseAndBudgetMonthAndIdNot(Long userId, String category, String budgetMonth, Long id);
}
