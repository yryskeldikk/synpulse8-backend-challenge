package com.synpulse8.challenge.service;

import com.synpulse8.challenge.domain.BankAccount;
import com.synpulse8.challenge.domain.Transaction;
import com.synpulse8.challenge.dto.TransactionDto;
import com.synpulse8.challenge.repository.BankAccountRepository;
import com.synpulse8.challenge.repository.TransactionRepository;
import com.synpulse8.challenge.utils.DateUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionService {
    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    BankAccountRepository bankAccountRepository;

    @Autowired
    ExternalApiService externalApiServer;

    public Page<Transaction> getTransactionsForUserInMonth(String userId, int year, int month, Pageable pageable) {
        // Retrieve all bank accounts for the user
        List<BankAccount> userBankAccounts = bankAccountRepository.findByUserId(userId);

        // Collect IBANs from user bank accounts
        List<String> userIbans = userBankAccounts.stream()
                .map(BankAccount::getIban)
                .collect(Collectors.toList());

        // Calculate the start and end dates for the desired month
        Date startDate = DateUtils.getStartDate(year, month);
        Date endDate = DateUtils.getEndDate(year, month);

        // Retrieve transactions for the user and month with pagination
        return transactionRepository.findByIbanInAndTransactionDateBetween(userIbans, startDate, endDate, pageable);
    }

    public TransactionDto getTransactionDtoForUserInMonth(String userId, int year, int month, Pageable pageable) {
        TransactionDto transactionDto = new TransactionDto();
        Page<Transaction> transactionPage = this.getTransactionsForUserInMonth(userId, year, month, pageable);
        transactionDto.setTransactionList(transactionPage.getContent());
        transactionDto.setPageable(transactionPage.getPageable());
        transactionDto.setTotalElements(transactionPage.getTotalElements());
        transactionDto.setTotalPages(transactionPage.getTotalPages());

        BigDecimal totalCreditInHKD = BigDecimal.ZERO;
        BigDecimal totalDebitInHKD = BigDecimal.ZERO;

        for (Transaction transaction : transactionDto.getTransactionList()) {
            BigDecimal currentExchageRateHKD = externalApiServer
                    .getCurrentExchangeRateInHKDFromAPI(transaction.getCurrency());
            BigDecimal convertedValue = transaction.getValue().multiply(currentExchageRateHKD);

            if (transaction.getValue().compareTo(BigDecimal.ZERO) < 0) {
                totalCreditInHKD = totalCreditInHKD.add(convertedValue);
            } else {
                totalDebitInHKD = totalDebitInHKD.add(convertedValue);
            }
        }
        transactionDto.setTotalCreditInHKD(totalCreditInHKD);
        transactionDto.setTotalCreditInHKD(totalCreditInHKD);
        transactionDto.setTotalDebitInHKD(totalDebitInHKD);
        return transactionDto;
    }

}
