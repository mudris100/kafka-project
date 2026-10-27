package dev.udris.paymentservice.domain.db;
import dev.udris.http.payment.CreatePaymentRequestDto;
import dev.udris.http.payment.CreatePaymentResponseDto;
import org.springframework.stereotype.Component;

@Component
public class PaymentEntityMapper {
    public PaymentEntity toEntity(CreatePaymentRequestDto requestDto) {
        var paymentEntity = new PaymentEntity();
        paymentEntity.setOrderId(requestDto.orderId());
        paymentEntity.setAmount(requestDto.amount());
        paymentEntity.setPaymentMethod(requestDto.paymentMethod());
        return paymentEntity;
    }

    public CreatePaymentResponseDto toResponseDto(PaymentEntity paymentEntity) {
        return new CreatePaymentResponseDto(
                paymentEntity.getId(),
                paymentEntity.getOrderId(),
                paymentEntity.getAmount(),
                paymentEntity.getPaymentStatus(),
                paymentEntity.getPaymentMethod()
        );
    }
}

