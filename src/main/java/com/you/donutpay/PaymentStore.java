package com.you.donutpay;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tracks recent payments so we can show 2x buttons for them.
 */
public class PaymentStore {

    public record Payment(
            UUID id,
            String playerName,
            String tag,
            long timestamp,
            double amount
    ) {}

    private static final int MAX = 50;
    private final List<Payment> payments = new ArrayList<>();

    public synchronized void addPayment(Payment p) {
        payments.add(0, p);
        if (payments.size() > MAX) payments.remove(payments.size() - 1);
    }

    public synchronized List<Payment> getPayments() {
        return new ArrayList<>(payments);
    }

    public synchronized void clear() {
        payments.clear();
    }
}
