package com.synpulse8.challenge.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

import lombok.Data;

@Entity
@Table(name = "bank_account")
@Data
public class BankAccount {
    @Id
    @Column(name = "iban")
    private String iban;

    @Column(name = "user_id")
    private String userId;
}
