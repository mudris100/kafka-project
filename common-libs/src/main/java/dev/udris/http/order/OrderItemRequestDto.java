package dev.udris.http.order;

import jakarta.validation.constraints.Positive;

public record OrderItemRequestDto(
        Long itemId,
        @Positive
        Integer quantity,
        String itemName
) {
}
