package com.synpulse8.challenge.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ExternalApiService {

    @Value("${app.api.exchangerate.url}")
    private String externalApiUrl;

    @Value("${app.api.exchangerate.apikey}")
    private String apiKey;

    @Autowired
    RestTemplate restTemplate;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public BigDecimal getCurrentExchangeRateInHKDFromAPI(String currency) {
        String url = externalApiUrl + "/" + apiKey + "/latest/" + currency;
        BigDecimal currentExchangeRate = null;

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        if (response != null & response.getStatusCode().value() == 200) {
            Map rates = (Map) response.getBody().get("conversion_rates");
            currentExchangeRate = BigDecimal.valueOf((Double) rates.get("HKD"));
        }
        return currentExchangeRate;
    }

}
