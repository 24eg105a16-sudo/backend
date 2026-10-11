package com.expensetracker;

import com.expensetracker.dto.TransactionRequest;
import com.expensetracker.dto.TransactionResponse;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.TransactionType;
import com.expensetracker.entity.User;
import com.expensetracker.exception.UnauthorizedException;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.UserPrincipal;
import com.expensetracker.service.TransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User(1L, "Alice", "alice@example.com", "pass");
        user2 = new User(2L, "Bob", "bob@example.com", "pass");

        // Set security context as user 1
        UserPrincipal principal = new UserPrincipal(1L, "Alice", "alice@example.com", "pass", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTransaction_Success() {
        TransactionRequest request = new TransactionRequest(
                TransactionType.EXPENSE,
                BigDecimal.valueOf(45.50),
                "Food",
                "Lunch with colleagues",
                LocalDate.now()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        Transaction saved = new Transaction(100L, user1, request.getType(), request.getAmount(), request.getCategory(), request.getDescription(), request.getTransactionDate());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        TransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(BigDecimal.valueOf(45.50), response.getAmount());
        assertEquals("Food", response.getCategory());
        assertEquals(1L, response.getUserId());
    }

    @Test
    void getTransactionById_OwnershipCheck_ThrowsUnauthorizedException() {
        // Transaction belongs to user 2, but current authenticated user is user 1
        Transaction txBelongingToUser2 = new Transaction(200L, user2, TransactionType.INCOME, BigDecimal.valueOf(500), "Salary", "Bonus", LocalDate.now());
        when(transactionRepository.findById(200L)).thenReturn(Optional.of(txBelongingToUser2));

        assertThrows(UnauthorizedException.class, () -> transactionService.getTransactionById(200L));
    }

    @Test
    void deleteTransaction_Success() {
        Transaction tx = new Transaction(100L, user1, TransactionType.EXPENSE, BigDecimal.valueOf(20), "Bills", "Phone bill", LocalDate.now());
        when(transactionRepository.findById(100L)).thenReturn(Optional.of(tx));

        transactionService.deleteTransaction(100L);

        verify(transactionRepository).delete(tx);
    }
}
