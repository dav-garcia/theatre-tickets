package com.github.davgarcia.theatre.tickets.command.payment;

import com.github.davgarcia.theatre.tickets.error.PreconditionsViolatedException;
import com.github.davgarcia.theatre.tickets.event.payment.PaymentConfirmedEvent;
import com.github.davgarcia.theatre.tickets.infra.Command;
import lombok.Value;

import java.util.UUID;

@Value
public class ConfirmPaymentCommand implements Command<PaymentCommandContext, Payment, UUID> {

    UUID aggregateRootId;

    @Override
    public void execute(final PaymentCommandContext context) {
        final var payment = context.getRepository().load(aggregateRootId)
                .orElseThrow(() -> new PreconditionsViolatedException("This payment is already confirmed"));

        context.getEventPublisher().tryPublish(payment.getVersion(),
                new PaymentConfirmedEvent(aggregateRootId));
    }
}
