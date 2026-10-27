package dev.udris.orderservice.api;

import dev.udris.http.payment.PaymentMethod;

public record OrderPaymentRequest(
        PaymentMethod paymentMethod
) {
}
