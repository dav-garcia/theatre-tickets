package com.github.davgarcia.theatre.tickets.command.payment;

import com.github.davgarcia.theatre.tickets.error.PreconditionsViolatedException;
import com.github.davgarcia.theatre.tickets.event.payment.Item;
import com.github.davgarcia.theatre.tickets.event.payment.PaymentPresentedEvent;
import com.github.davgarcia.theatre.tickets.infra.Command;
import com.github.davgarcia.theatre.tickets.infra.payment.PaymentGateway;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
public class PresentPaymentRollbackCommand implements Command<PaymentCommandContext, Payment, UUID> {

    UUID aggregateRootId;
    UUID ticket;
    String customer;
    List<Item> items;

    @Override
    public void execute(final PaymentCommandContext context) {
        if (context.getRepository().load(aggregateRootId).isPresent()) {
            throw new PreconditionsViolatedException("The payment has already been presented");
        }

        final var paymentCode = startPayment(context.getPaymentGateway());
        try {
            context.getEventPublisher().tryPublish(0L,
                    new PaymentPresentedEvent(aggregateRootId, ticket, customer, items, paymentCode));
        } catch (Exception exception) { // Rollback if new state cannot be saved
            cancelPayment(context.getPaymentGateway(), paymentCode);
        }
    }

    private String startPayment(final PaymentGateway paymentGateway) {
        final var description = "Ticket id " + ticket;
        final var amount = items.stream()
                .mapToInt(Item::getAmount)
                .sum();

        return paymentGateway.initiatePayment(customer, description, amount);
    }

    private void cancelPayment(final PaymentGateway paymentGateway, final String paymentCode) {
        paymentGateway.cancelPayment(paymentCode);
    }
}
