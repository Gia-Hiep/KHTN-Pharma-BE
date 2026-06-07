package com.pharmacy.sales_service.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    /**
     * Tạo Stripe PaymentIntent.
     * ⚠️ VND là zero-decimal currency — truyền đúng số nguyên, KHÔNG nhân 100.
     *    Ví dụ: 50.000đ → amount = 50000 (ĐÚNG)
     *                    → amount = 5000000 (SAI — khách bị trừ 5 triệu!)
     */
    public PaymentIntent createIntent(Long amountVnd, Long orderId) throws StripeException {
        return PaymentIntent.create(
            PaymentIntentCreateParams.builder()
                .setAmount(amountVnd)     // VND zero-decimal: 50000 = 50,000đ
                .setCurrency("vnd")
                .putMetadata("orderId", String.valueOf(orderId))
                .build()
        );
    }

    public PaymentIntent retrieveIntent(String intentId) throws StripeException {
        return PaymentIntent.retrieve(intentId);
    }
}
