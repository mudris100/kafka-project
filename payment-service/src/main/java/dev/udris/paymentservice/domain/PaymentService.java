package dev.udris.paymentservice.domain;

import dev.udris.http.payment.CreatePaymentRequestDto;
import dev.udris.http.payment.CreatePaymentResponseDto;
import dev.udris.http.payment.PaymentMethod;
import dev.udris.http.payment.PaymentStatus;
import dev.udris.paymentservice.domain.db.PaymentEntityMapper;
import dev.udris.paymentservice.domain.db.PaymentEntityRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class PaymentService {
    private final PaymentEntityRepository repository;
    private final PaymentEntityMapper mapper;

    public CreatePaymentResponseDto createPayment(CreatePaymentRequestDto request) {
        var found = repository.findByOrderId(request.orderId());
        if (found.isPresent()) {
            log.info("Payment already exists for orderId={}", request.orderId());
            return mapper.toResponseDto(found.get());
        }
        var entity = mapper.toEntity(request);
        var status = request.paymentMethod().equals(PaymentMethod.QR)
                ? PaymentStatus.PAYMENT_FAILED
                : PaymentStatus.PAYMENT_SUCCEEDED;

        entity.setPaymentStatus(status);
        var savedEntity = repository.save(entity);
        return mapper.toResponseDto(savedEntity);

    }

    public CreatePaymentResponseDto getPaymentByOrderId(Long orderId) {
        var found = repository.findByOrderId(orderId);
        if (found.isEmpty()) {
            throw new IllegalArgumentException("Payment not found for orderId: " + orderId);
        }
        return mapper.toResponseDto(found.get());
    }

    public List<CreatePaymentResponseDto> getAllPayments() {
        return repository.findAll().stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

}
