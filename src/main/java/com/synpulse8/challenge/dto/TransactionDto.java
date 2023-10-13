package com.synpulse8.challenge.dto;

import com.synpulse8.challenge.domain.Transaction;
import lombok.Data;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TransactionDto {
    private List<Transaction> transactionList;
    private BigDecimal totalCreditInHKD;
    private BigDecimal totalDebitInHKD;
    private Pageable pageable;
    private long totalElements;
    private int totalPages;

    public TransactionDto() {
    }

    public TransactionDto(List<Transaction> transactionList,
            BigDecimal totalCreditInHKD,
            BigDecimal totalDebitInHKD,
            Pageable pageable,
            long totalElements,
            int totalPages) {
        this.transactionList = transactionList;
        this.totalCreditInHKD = totalCreditInHKD;
        this.totalDebitInHKD = totalDebitInHKD;
        this.pageable = pageable;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }
}
