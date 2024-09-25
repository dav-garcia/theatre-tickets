package com.github.davgarcia.theatre.tickets.command.payment;

import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandContext;
import com.github.davgarcia.theatre.tickets.infra.event.EventPublisher;
import com.github.davgarcia.theatre.tickets.infra.payment.PaymentGateway;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;
import org.springframework.lang.NonNull;

import java.util.UUID;

public class PaymentCommandContext extends CommandContext<Payment, UUID> {

    private final PaymentGateway paymentGateway;

    public PaymentCommandContext(final Repository<Payment, UUID> repository,
                                 final EventPublisher<UUID> eventPublisher,
                                 final PaymentGateway paymentGateway) {
        super(repository, eventPublisher);
        this.paymentGateway = paymentGateway;
    }

    @NonNull
    public PaymentGateway getPaymentGateway() {
        return paymentGateway;
    }
}
