package com.synpulse8.challenge.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.EnableRetry;

import com.synpulse8.challenge.domain.BankAccount;
import com.synpulse8.challenge.domain.Transaction;
import com.synpulse8.challenge.dto.TransactionDto;
import com.synpulse8.challenge.repository.BankAccountRepository;
import com.synpulse8.challenge.repository.TransactionRepository;
import com.synpulse8.challenge.utils.DateUtils;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

@EnableRetry
@ExtendWith(MockitoExtension.class)
public class TestTransactionService {
    @Spy
    @InjectMocks
    TransactionService transactionService;

    @Mock
    ExternalApiService externalApiService;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    BankAccountRepository bankAccountRepository;

    @Test
    public void testTransactionDto() {
        // Input Values
        String userId = "testUserId";
        int year = 2023;
        int month = 10;
        Pageable pageable = PageRequest.of(1, 3);

        Transaction t1 = new Transaction("1", new BigDecimal("100"), "USD");
        Transaction t2 = new Transaction("2", new BigDecimal("-50"), "EUR");
        Transaction t3 = new Transaction("3", new BigDecimal("75"), "GBP");

        Page<Transaction> tPage = new PageImpl<>(List.of(t1, t2, t3), pageable, 1000);

        Mockito.doReturn(tPage).when(transactionService).getTransactionsForUserInMonth(userId, year, month, pageable);

        Mockito.when(externalApiService.getCurrentExchangeRateInHKDFromAPI("USD")).thenReturn(new BigDecimal("7.8"));
        Mockito.when(externalApiService.getCurrentExchangeRateInHKDFromAPI("EUR")).thenReturn(new BigDecimal("8.9"));
        Mockito.when(externalApiService.getCurrentExchangeRateInHKDFromAPI("GBP")).thenReturn(new BigDecimal("9.7"));

        TransactionDto transactionDto = transactionService.getTransactionDtoForUserInMonth(userId, year, month,
                pageable);

        BigDecimal expectedTotalCreditInHKD = new BigDecimal(-50 * 8.9);
        BigDecimal expectedTotalDebitInHKD = new BigDecimal(100 * 7.8 + 75 * 9.7);

        assertThat(expectedTotalCreditInHKD, comparesEqualTo(transactionDto.getTotalCreditInHKD()));
        assertThat(expectedTotalDebitInHKD, comparesEqualTo(transactionDto.getTotalDebitInHKD()));
        assertEquals(1000, transactionDto.getTotalElements());
        assertEquals(Math.ceil((double) 1000 / 3), transactionDto.getTotalPages());
    }

    @Test
    public void testNoTransactions() {
        // Input Values
        String userId = "testUserId";
        int year = 2023;
        int month = 10;
        Pageable pageable = PageRequest.of(1, 3);

        // Mock the getTransactionsForUserInMonth method to return an empty Page
        Mockito.doReturn(new PageImpl<>(Collections.emptyList(), pageable, 0))
                .when(transactionService).getTransactionsForUserInMonth(userId, year, month, pageable);

        TransactionDto transactionDto = transactionService.getTransactionDtoForUserInMonth(userId, year, month,
                pageable);

        // Assertions for default values
        assertEquals(0, transactionDto.getTotalElements());
        assertEquals(0, transactionDto.getTotalPages());
        assertEquals(BigDecimal.ZERO, transactionDto.getTotalCreditInHKD());
        assertEquals(BigDecimal.ZERO, transactionDto.getTotalDebitInHKD());
        assertEquals(Collections.emptyList(), transactionDto.getTransactionList());
    }

    @Test
    public void testGetTransactionsForUserInMonth() {
        String userId = "testUser";
        int year = 2023;
        int month = 10;
        Pageable pageable = PageRequest.of(0, 10);

        // Mock user bank accounts
        List<BankAccount> userBankAccounts = new ArrayList<>();
        userBankAccounts.add(new BankAccount("IBAN1", userId));
        userBankAccounts.add(new BankAccount("IBAN2", userId));
        Mockito.lenient().when(bankAccountRepository.findByUserId(userId)).thenReturn(userBankAccounts);

        // Mock transactions
        Date startDate = DateUtils.getStartDate(year, month);
        Date endDate = DateUtils.getEndDate(year, month);
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("1", BigDecimal.TEN, "USD"));
        transactions.add(new Transaction("2", BigDecimal.TEN, "EUR"));
        transactions.add(new Transaction("3", BigDecimal.TEN, "GBP"));
        Mockito.lenient().when(transactionRepository.findByIbanInAndTransactionDateBetween(
                Arrays.asList("IBAN1", "IBAN2"), startDate, endDate, pageable))
                .thenReturn(new PageImpl<>(transactions));

        Page<Transaction> result = transactionService.getTransactionsForUserInMonth(userId, year, month, pageable);

        // Assertions
        Mockito.verify(bankAccountRepository).findByUserId(userId);

        ArgumentCaptor<List<String>> ibanListCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Date> startDateCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Date> endDateCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        Mockito.verify(transactionRepository).findByIbanInAndTransactionDateBetween(
                ibanListCaptor.capture(), startDateCaptor.capture(), endDateCaptor.capture(), pageableCaptor.capture());

        assertThat(ibanListCaptor.getValue(), contains("IBAN1", "IBAN2"));
    }

}
