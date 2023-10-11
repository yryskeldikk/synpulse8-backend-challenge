package com.synpulse8.challenge.domain;

import jakarta.persistence.*;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "transaction")
@Data
public class Transaction {
    @Id
    @Column(name = "uid")
    private String uid;

    @Column(name = "value")
    private BigDecimal value;

    @Column(name = "currency")
    private String currency;

    @Column(name = "iban")
    private String iban;

    @Column(name = "description")
    private String description;

    @Column(name = "transaction_date")
    private Date transactionDate;

    public Transaction(String uid, BigDecimal value, String currency, String iban, String description,
            Date transactionDate) {
        this.uid = uid;
        this.value = value;
        this.currency = currency;
        this.iban = iban;
        this.description = description;
        this.transactionDate = transactionDate;
    }
}
