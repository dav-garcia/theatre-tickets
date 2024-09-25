package com.github.davgarcia.theatre.tickets.infra.payment;

/**
 * Following the ports & adapters pattern, this is the Payments Provider port interface.
 */
public interface PaymentGateway {

    enum Status {
        PENDING,
        ACCEPTED,
        CANCELLED
    }

    String initiatePayment(final String email, final String description, final int amount);
    Status getPaymentStatus(final  String paymentId);
    boolean cancelPayment(final String paymentId);
}
