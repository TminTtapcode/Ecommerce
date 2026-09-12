package com.tmt.ecommerce.payment.service;

import com.tmt.ecommerce.common.exception.AppException;
import com.tmt.ecommerce.order.api.dto.RefundableOrderSnapshot;
import com.tmt.ecommerce.payment.entity.Payment;
import com.tmt.ecommerce.payment.entity.RefundAttempt;
import com.tmt.ecommerce.payment.enums.PaymentMethod;
import com.tmt.ecommerce.payment.enums.PaymentStatus;
import com.tmt.ecommerce.payment.enums.RefundStatus;
import com.tmt.ecommerce.payment.repository.PaymentRepository;
import com.tmt.ecommerce.payment.repository.RefundAttemptRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RefundReservationServiceTest {
    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final RefundAttemptRepository refunds = mock(RefundAttemptRepository.class);
    private final RefundReservationService service = new RefundReservationService(payments, refunds);

    @Test
    void reservesServerCalculatedOrderRemainderAgainstTheOnlyCanonicalPayment() {
        Payment payment = payment(10L, "300.00");
        when(refunds.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        when(payments.findSuccessfulVnpayByGroupForUpdate("group")).thenReturn(List.of(payment));
        when(refunds.sumReservedAmountByOrderId(eq(7L), any())).thenReturn(new BigDecimal("20.00"));
        when(refunds.sumReservedAmountByPaymentId(eq(10L), any())).thenReturn(new BigDecimal("50.00"));
        when(refunds.save(any())).thenAnswer(i -> i.getArgument(0));

        RefundAttempt result = service.reserve(order("120.00"), 99L, "key-1", "Cancelled by vendor");

        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(RefundStatus.REQUESTED, result.getStatus());
        assertEquals(10L, result.getPaymentId());
        assertNotNull(result.getMerchantRequestId());
    }

    @Test
    void rejectsAmbiguousSuccessfulAttemptsInsteadOfChoosingLatest() {
        when(refunds.findByIdempotencyKey("key-2")).thenReturn(Optional.empty());
        when(payments.findSuccessfulVnpayByGroupForUpdate("group"))
                .thenReturn(List.of(payment(10L, "300.00"), payment(11L, "300.00")));

        AppException error = assertThrows(AppException.class,
                () -> service.reserve(order("120.00"), 99L, "key-2", "Cancelled by vendor"));

        assertEquals("REFUND_NOT_ACTIONABLE", error.getErrorCode().name());
        verify(refunds, never()).save(any());
    }

    @Test
    void sameIdempotencyKeyReturnsExistingAttemptWithoutSelectingOrCallingGateway() {
        RefundAttempt existing = RefundAttempt.builder().id(3L).orderId(7L).actorUserId(99L)
                .reason("Cancelled by vendor").status(RefundStatus.UNKNOWN).build();
        when(refunds.findByIdempotencyKey("key-3")).thenReturn(Optional.of(existing));

        assertSame(existing, service.reserve(order("120.00"), 99L, "key-3", "Cancelled by vendor"));
        verifyNoInteractions(payments);
    }

    private static RefundableOrderSnapshot order(String amount) {
        return new RefundableOrderSnapshot(7L, 1L, 2L, "group", "VNPAY", "CANCELLED", new BigDecimal(amount));
    }
    private static Payment payment(long id, String amount) {
        return Payment.builder().id(id).paymentGroupId("group").amount(new BigDecimal(amount)).method(PaymentMethod.VNPAY)
                .status(PaymentStatus.SUCCESS).transactionId("PAY_REFERENCE").gatewayTransactionId("123456")
                .vnpayTransactionDate("20260910120000").build();
    }
}
