package com.github.davgarcia.theatre.tickets.command.payment;

import com.github.davgarcia.theatre.tickets.error.PreconditionsViolatedException;
import com.github.davgarcia.theatre.tickets.event.payment.Item;
import com.github.davgarcia.theatre.tickets.event.payment.PaymentPresentedEvent;
import com.github.davgarcia.theatre.tickets.infra.Command;
import com.github.davgarcia.theatre.tickets.infra.payment.PaymentGateway;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.List;
import java.util.UUID;

@Value
@RequiredArgsConstructor
public class PresentPaymentIdempotentCommand implements Command<PaymentCommandContext, Payment, UUID> {

    UUID aggregateRootId;
    UUID booking;
    String customer;
    List<Item> items;

    @NonFinal
    String paymentCode;

    @Override
    public void execute(final PaymentCommandContext context) {
        if (context.getRepository().load(aggregateRootId).isPresent()) {
            if (paymentCode != null) {
                cancelPayment(context.getPaymentGateway(), paymentCode);
            }
            throw new PreconditionsViolatedException("The payment has already been presented");
        }

        if (paymentCode == null) { // Idempotencia con el proveedor externo en caso de repetición
            paymentCode = startPayment(context.getPaymentGateway());
        }
        context.getEventPublisher().tryPublish(0L,
                new PaymentPresentedEvent(aggregateRootId, booking, customer, items, paymentCode));
    }

    private String startPayment(final PaymentGateway paymentGateway) {
        final var description = "Booking id " + booking;
        final var amount = items.stream()
                .mapToInt(Item::getAmount)
                .sum();

        return paymentGateway.initiatePayment(customer, description, amount);
    }

    private void cancelPayment(final PaymentGateway paymentGateway, final String paymentCode) {
        paymentGateway.cancelPayment(paymentCode);
    }
}
