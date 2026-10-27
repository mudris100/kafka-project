package dev.udris.orderservice.api;

import dev.udris.http.order.CreateOrderRequestDto;
import dev.udris.http.order.OrderDto;
import dev.udris.orderservice.domain.OrderProcessor;
import dev.udris.orderservice.domain.db.OrderEntityMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderProcessor processor;
    private final OrderEntityMapper mapper;

    @PostMapping
    public OrderDto create(@Valid @RequestBody CreateOrderRequestDto request) {
        log.info("Creating order: request={}", request);
        var saved = processor.create(request);
        return mapper.toOrderDto(saved);
    }

    @GetMapping("/{id}")
    public OrderDto getOne(@PathVariable Long id) {
        log.info("Retrieving order with id={}", id);
        var found = processor.getOrderOrThrow(id);
        return mapper.toOrderDto(found);
    }

    @PostMapping("/{id}/pay")
    public OrderDto payOrder(@PathVariable Long id, @RequestBody OrderPaymentRequest request){
        log.info("Paying order with id={}, request={}", id, request);
        var entity = processor.processPayment(id, request);
        return  mapper.toOrderDto(entity);
    }
}
