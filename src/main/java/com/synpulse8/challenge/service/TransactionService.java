package com.synpulse8.challenge.service;

import com.synpulse8.challenge.domain.BankAccount;
import com.synpulse8.challenge.domain.Transaction;
import com.synpulse8.challenge.dto.TransactionDto;
import com.synpulse8.challenge.repository.BankAccountRepository;
import com.synpulse8.challenge.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Value("${app.api.exchangerate.url}")
    private String exchangeRateUrl;

    @Value("${app.api.exchangerate.apikey}")
    private String apiKey;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    BankAccountRepository bankAccountRepository;

    @Autowired
    RestTemplate restTemplate;

    public Page<Transaction> getTransactionsForUserInMonth(String userId, int year, int month, Pageable pageable) {
        // Retrieve all bank accounts for the user
        List<BankAccount> userBankAccounts = bankAccountRepository.findByUserId(userId);

        // Collect IBANs from user bank accounts
        List<String> userIbans = userBankAccounts.stream()
                .map(BankAccount::getIban)
                .collect(Collectors.toList());

        // Calculate the start and end dates for the desired month
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1, 0, 0, 0);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 1);
        Date endDate = calendar.getTime();

        // Retrieve transactions for the user and month with pagination
        return transactionRepository.findByIbanInAndTransactionDateBetween(userIbans, startDate, endDate, pageable);
    }

    public TransactionDto getTransactionDtoForUserInMonth(String userId, int year, int month, Pageable pageable) {
        TransactionDto transactionDto = new TransactionDto();
        Page<Transaction> transactionPage = getTransactionsForUserInMonth(userId, year, month, pageable);
        transactionDto.setTransactionList(transactionPage.getContent());
        transactionDto.setPageable(transactionPage.getPageable());
        transactionDto.setTotalElements(transactionPage.getTotalElements());
        transactionDto.setTotalPages(transactionPage.getTotalPages());

        BigDecimal totalCreditInHKD = BigDecimal.ZERO;
        BigDecimal totalDebitInHKD = BigDecimal.ZERO;

        for (Transaction transaction: transactionDto.getTransactionList()){
            BigDecimal currentExchageRateHKD = getCurrentExchangeRateInHKD(transaction.getCurrency());
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

    private BigDecimal getCurrentExchangeRateInHKD(String currency) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(exchangeRateUrl)
                .queryParam("access_key", apiKey)
                .queryParam("base", currency)
                .queryParam("symbols", "HKD");

        BigDecimal currentExchangeRate = null;

        ResponseEntity<Map> response = restTemplate.getForEntity(builder.toUriString(), Map.class);
        if (response.getStatusCode().value() == 200) {
            Map rates = (Map) response.getBody().get("rates");
            currentExchangeRate = BigDecimal.valueOf((Double)rates.get("HKD"));
        }
        return currentExchangeRate;
    }
}
