package dev.udris.orderservice.domain.db;

import dev.udris.http.order.CreateOrderRequestDto;
import dev.udris.http.order.OrderDto;
import dev.udris.http.order.OrderItemDto;
import dev.udris.http.order.OrderItemRequestDto;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderEntityMapper {
    public OrderEntity toEntity(CreateOrderRequestDto dto) {
        OrderEntity order = new OrderEntity();
        order.setCustomerId(dto.customerId());
        order.setAddress(dto.address());
        dto.items().stream()
                .map(this::toItemEntity)
                .forEach(item -> addItem(order, item));
        return order;

    }

    private OrderItemEntity toItemEntity(OrderItemRequestDto dto) {
        OrderItemEntity item = new OrderItemEntity();
        item.setItemId(dto.itemId());
        item.setItemName(dto.itemName());
        item.setQuantity(dto.quantity());
        return item;
    }

    private OrderItemDto toOrderItemDto(OrderItemEntity orderItem) {
        return new OrderItemDto(
                orderItem.getId(),
                orderItem.getItemId(),
                orderItem.getQuantity(),
                orderItem.getPriceAtPurchase()
        );
    }

    public OrderDto toOrderDto(OrderEntity entity) {
        return new OrderDto(
                entity.getId(),
                entity.getCustomerId(),
                entity.getAddress(),
                entity.getTotalAmount(),
                entity.getCourierName(),
                entity.getEtaMinutes(),
                entity.getOrderStatus(),
                entity.getItems().stream().map(this::toOrderItemDto)
                        .collect(Collectors.toSet())
        );
    }

    // Keep both sides of the bidirectional relation in sync
    private void addItem(OrderEntity order, OrderItemEntity item) {
        item.setOrder(order);
        order.getItems().add(item);
    }
}
