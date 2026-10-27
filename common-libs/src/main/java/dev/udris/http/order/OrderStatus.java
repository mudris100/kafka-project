package dev.udris.http.order;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    PAYMENT_FAILED,
    DELIVERY_ASSIGNED,
    DELIVERED
}
