package com.synpulse8.challenge.controllers;

import com.synpulse8.challenge.dto.TransactionDto;
import com.synpulse8.challenge.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/transactions/{userId}")
    public ResponseEntity<TransactionDto> getTransactionsForUser(
            @PathVariable String userId,
            @RequestParam int year,
            @RequestParam int month,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(transactionService.getTransactionDtoForUserInMonth(userId, year, month, pageable));
    }
}