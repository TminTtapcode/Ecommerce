package com.tmt.ecommerce.payment.factory;

import com.tmt.ecommerce.payment.enums.PaymentMethod;
import com.tmt.ecommerce.payment.exception.UnsupportedPaymentMethodException;
import com.tmt.ecommerce.payment.strategy.PaymentStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentStrategyFactory {

    private final Map<PaymentMethod, PaymentStrategy> strategies;

    public PaymentStrategyFactory(List<PaymentStrategy> strategyList) {
        this.strategies = new EnumMap<>(PaymentMethod.class);
        for (PaymentStrategy strategy : strategyList) {
            this.strategies.put(strategy.getPaymentMethod(), strategy);
        }
    }

    public PaymentStrategy getStrategy(PaymentMethod method) {
        PaymentStrategy strategy = strategies.get(method);
        if (strategy == null) {
            throw new UnsupportedPaymentMethodException("Hệ thống chưa hỗ trợ phương thức thanh toán: " + method);
        }
        return strategy;
    }
}