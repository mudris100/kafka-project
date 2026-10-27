package dev.udris.orderservice.domain;

import dev.udris.http.order.CreateOrderRequestDto;
import dev.udris.http.order.OrderStatus;
import dev.udris.http.payment.CreatePaymentRequestDto;
import dev.udris.http.payment.CreatePaymentResponseDto;
import dev.udris.http.payment.PaymentStatus;
import dev.udris.kafka.DeliveryAssignedEvent;
import dev.udris.kafka.OrderPaidEvent;
import dev.udris.orderservice.api.OrderPaymentRequest;
import dev.udris.orderservice.domain.db.OrderEntity;
import dev.udris.orderservice.domain.db.OrderEntityMapper;
import dev.udris.orderservice.domain.db.OrderItemEntity;
import dev.udris.orderservice.domain.db.OrderJpaRepository;
import dev.udris.orderservice.external.PaymentHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderProcessor {

    private final OrderJpaRepository repository;
    private final OrderEntityMapper mapper;
    private final PaymentHttpClient httpClient;
    private final KafkaTemplate<Long, OrderPaidEvent> kafkaTemplate;

    @Value("${order-paid-topic}")
    private String orderPaidTopic;

    public OrderEntity create(CreateOrderRequestDto requestDto) {
        var entity = mapper.toEntity(requestDto);
        entity.setOrderStatus(OrderStatus.PENDING_PAYMENT);
        calculatePricingForOrder(entity);
        return repository.save(entity);
    }

    public OrderEntity getOrderOrThrow(Long id) {
        var orderEntityOptional = repository.findById(id);
        return orderEntityOptional
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Entity with id '%s' not found".formatted(id)));
    }

    private void calculatePricingForOrder(OrderEntity entity) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItemEntity item : entity.getItems()) {
            long cents = ThreadLocalRandom.current().nextLong(100, 1000);
            item.setPriceAtPurchase(BigDecimal.valueOf(cents, 2));
            totalPrice = item.getPriceAtPurchase()
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .add(totalPrice);
        }
        entity.setTotalAmount(totalPrice);
    }


    public OrderEntity processPayment(Long id, OrderPaymentRequest request) {
        var entity = getOrderOrThrow(id);
        if (!entity.getOrderStatus().equals(OrderStatus.PENDING_PAYMENT)) {
            throw new RuntimeException("Order must be in status PENDING_PAYMENT");
        }
        var response = httpClient.createPayment(CreatePaymentRequestDto.builder()
                .orderId(id)
                .paymentMethod(request.paymentMethod())
                .amount(entity.getTotalAmount())
                .build());
        if (response.paymentStatus().equals(PaymentStatus.PAYMENT_SUCCEEDED)) {
            entity.setOrderStatus(OrderStatus.PAID);
            sendOrderPaidEvent(entity, response);
        } else {
            entity.setOrderStatus(OrderStatus.PAYMENT_FAILED);
        }
        return repository.save(entity);
    }

    private void sendOrderPaidEvent(OrderEntity entity, CreatePaymentResponseDto paymentResponseDto) {
        kafkaTemplate.send(
                orderPaidTopic,
                OrderPaidEvent.builder()
                        .orderId(entity.getId())
                        .amount(entity.getTotalAmount())
                        .paymentId(paymentResponseDto.paymentId())
                        .paymentMethod(paymentResponseDto.paymentMethod())
                        .build()
        ).thenAccept(result -> {
            log.info("Order Paid event sent: id={}", entity.getId());
        });
    }

    public void processDeliveryAssigned(DeliveryAssignedEvent event) {
        var order = getOrderOrThrow(event.orderId());
        if (!order.getOrderStatus().equals(OrderStatus.PAID)) {
            processIncorrectDeliveryState(order);
            return;
        }

        order.setOrderStatus(OrderStatus.DELIVERY_ASSIGNED);
        order.setCourierName(event.courierName());
        order.setEtaMinutes(event.etaMinutes());
        repository.save(order);
        log.info("Order delivery assigned processed: orderId={}", order.getId());
    }

    private void processIncorrectDeliveryState(OrderEntity order) {
        if (order.getOrderStatus().equals(OrderStatus.DELIVERY_ASSIGNED)) {
            log.info("Order delivery already processed: orderId={}", order.getId());
        } else {
            log.error("Trying to assign delivery but order has incorrect state: state={}", order.getId());
        }
    }

}
