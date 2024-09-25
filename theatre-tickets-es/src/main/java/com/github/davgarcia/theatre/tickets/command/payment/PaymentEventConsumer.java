package com.github.davgarcia.theatre.tickets.command.payment;

import com.github.davgarcia.theatre.tickets.event.payment.PaymentConfirmedEvent;
import com.github.davgarcia.theatre.tickets.event.payment.PaymentPresentedEvent;
import com.github.davgarcia.theatre.tickets.infra.Event;
import com.github.davgarcia.theatre.tickets.infra.event.EventConsumer;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;

import java.util.UUID;

public class PaymentEventConsumer implements EventConsumer<UUID> {

    private final Repository<Payment, UUID> repository;

    public PaymentEventConsumer(final Repository<Payment, UUID> repository) {
        this.repository = repository;
    }

    @Override
    public void consume(final long version, final Event<UUID> event) {
        if (event instanceof PaymentPresentedEvent) {
            apply(version, (PaymentPresentedEvent) event);
        } else if (event instanceof PaymentConfirmedEvent) {
            apply(version, (PaymentConfirmedEvent) event);
        }
    }

    private void apply(final long version, final PaymentPresentedEvent event) {
        final var payment = Payment.builder()
                .id(event.getAggregateRootId())
                .version(version)
                .paymentCode(event.getPaymentCode())
                .build();

        repository.save(payment);
    }

    @SuppressWarnings("java:S1172")
    private void apply(final long version, final PaymentConfirmedEvent event) {
        repository.delete(event.getAggregateRootId());
    }
}
