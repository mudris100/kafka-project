package dev.udris.paymentservice.api;

import dev.udris.http.payment.CreatePaymentRequestDto;
import dev.udris.http.payment.CreatePaymentResponseDto;
import dev.udris.paymentservice.domain.PaymentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public CreatePaymentResponseDto createPayment(@RequestBody CreatePaymentRequestDto request) {
        log.info("Received request: paymentRequest={}", request);
        return paymentService.createPayment(request);
    }

    @GetMapping("/{orderId}")
    public CreatePaymentResponseDto getPaymentByOrderId(@PathVariable Long orderId) {
        return paymentService.getPaymentByOrderId(orderId);
    }

    @GetMapping
    public List<CreatePaymentResponseDto> getAll() {
        return paymentService.getAllPayments();

    }
}
