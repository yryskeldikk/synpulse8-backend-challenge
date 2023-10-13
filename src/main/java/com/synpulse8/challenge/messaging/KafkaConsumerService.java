package com.synpulse8.challenge.messaging;

import com.synpulse8.challenge.domain.Transaction;
import com.synpulse8.challenge.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {
    @Autowired
    TransactionRepository transactionRepository;

    @KafkaListener(topics = "#{'${spring.kafka.server.topic.string}'}", groupId = "string_group")
    public void consumeString(String message) {
        log.info("Received String message: {}", message);
    }

    @KafkaListener(topics = "#{'${spring.kafka.server.topic.transaction}'}", groupId = "transaction_group", containerFactory = "transactionKafkaListenerFactory")
    public void consumeTransaction(Transaction transaction) {
        log.info("Received transaction " + transaction);
        transactionRepository.save(transaction);
        log.info("Saved to db with uid {}", transaction.getUid());
    }
}
