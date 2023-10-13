package com.synpulse8.challenge.repository;

import com.synpulse8.challenge.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Page<Transaction> findByIbanInAndTransactionDateBetween(List<String> userIbans, Date startDate, Date endDate,
            Pageable pageable);
}
