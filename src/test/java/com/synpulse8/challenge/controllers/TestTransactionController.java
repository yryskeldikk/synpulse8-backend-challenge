package com.synpulse8.challenge.controllers;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import com.synpulse8.challenge.domain.Transaction;
import com.synpulse8.challenge.dto.TransactionDto;
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

        Calendar calendar = Calendar.getInstance();
        calendar.set(2023, 10, 10, 12, 0, 0);
        Transaction t1 = new Transaction("1", BigDecimal.valueOf(123.45), "EUR", "CH93-0000-0000-0000-0000-0",
                "Online Payment", calendar.getTime());

        calendar.set(2023, 10, 10, 13, 0, 0);
        Transaction t2 = new Transaction("2", BigDecimal.valueOf(175.45), "EUR", "CH93-0000-0000-0000-0000-0",
                "Online Payment", calendar.getTime());

        TransactionDto transactionDto = new TransactionDto(List.of(t1, t2), BigDecimal.valueOf(2700),
                BigDecimal.valueOf(1200), PageRequest.of(2, 1), 1000, 500);

        when(transactionService.getTransactionDtoForUserInMonth(userId, year, month, PageRequest.of(page, size)))
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
}
