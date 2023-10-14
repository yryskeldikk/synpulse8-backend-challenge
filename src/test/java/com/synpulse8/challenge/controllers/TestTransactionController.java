package com.synpulse8.challenge.controllers;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.synpulse8.challenge.domain.Transaction;
import com.synpulse8.challenge.dto.TransactionDto;
import com.synpulse8.challenge.exception.InvalidDateInputException;
import com.synpulse8.challenge.service.TransactionService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;

@WebMvcTest(TransactionController.class)
public class TestTransactionController {

        @Autowired
        private MockMvc mvc;

        @MockBean
        private TransactionService transactionService;

        @Test
        public void testGetTransactionDto() throws Exception {
                // Input Values
                String userId = "12345";
                int year = 2023;
                int month = 10;
                int page = 1;
                int size = 2;
                Transaction t1 = new Transaction("1", BigDecimal.valueOf(123.45), "EUR", "CH93-0000-0000-0000-0000-0",
                                "Online Payment", new Date());
                Transaction t2 = new Transaction("2", BigDecimal.valueOf(175.45), "EUR", "CH93-0000-0000-0000-0000-0",
                                "Online Payment", new Date());

                TransactionDto transactionDto = new TransactionDto(List.of(t1, t2), BigDecimal.valueOf(2700),
                                BigDecimal.valueOf(1200), PageRequest.of(page, size), 1000, 500);

                when(transactionService.getTransactionDtoForUserInMonth(userId, year, month,
                                PageRequest.of(page, size)))
                                .thenReturn(transactionDto);

                this.mvc.perform(get("/transactions/{userId}", userId)
                                .param("year", String.valueOf(year))
                                .param("month", String.valueOf(month))
                                .param("page", String.valueOf(page))
                                .param("size", String.valueOf(size)))
                                .andExpect(status().isOk())
                                .andExpect(header().string("Content-Type", "application/json"))
                                .andExpect(jsonPath("$.transactionList", hasSize(2)))
                                .andExpect(jsonPath("$.transactionList[0].uid", is("1")))
                                .andExpect(jsonPath("$.transactionList[1].uid", is("2")))
                                .andExpect(jsonPath("$.totalCreditInHKD", is(2700)))
                                .andExpect(jsonPath("$.totalDebitInHKD", is(1200)))
                                .andExpect(jsonPath("$.totalElements", is(1000)))
                                .andExpect(jsonPath("$.totalPages", is(500)));
        }

        @Test
        public void testInvalidDateInputException() throws Exception {
                String userId = "12345";
                int invalidYear = 2200;
                int month = 13;
                Pageable pageable = PageRequest.of(1, 2);

                // Mock the service to throw InvalidDateInputException
                doThrow(new InvalidDateInputException("Invalid year input. Year must be within a reasonable range."))
                                .when(transactionService)
                                .getTransactionDtoForUserInMonth(userId, invalidYear, month, pageable);

                mvc.perform(get("/transactions/{userId}", userId)
                                .param("year", String.valueOf(invalidYear))
                                .param("month", String.valueOf(month))
                                .param("page", "1")
                                .param("size", "2"))
                                .andExpect(status().isBadRequest())
                                .andExpect(content()
                                                .string("Invalid year input. Year must be within a reasonable range."));
        }
}
