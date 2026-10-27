package dev.udris.http.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record CreateOrderRequestDto(
        @Positive
        Long customerId,
        String address,
        @Valid
        Set<OrderItemRequestDto> items
) {
}
