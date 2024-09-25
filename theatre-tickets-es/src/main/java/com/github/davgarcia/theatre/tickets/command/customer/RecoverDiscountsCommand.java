package com.github.davgarcia.theatre.tickets.command.customer;

import com.github.davgarcia.theatre.tickets.event.customer.DiscountsRecoveredEvent;
import com.github.davgarcia.theatre.tickets.infra.Command;
import lombok.Value;

import java.util.UUID;
import java.util.stream.Collectors;

@Value
public class RecoverDiscountsCommand implements Command<CustomerCommandContext, Customer, String> {

    String aggregateRootId;
    UUID fromBooking;

    @Override
    public void execute(final CustomerCommandContext context) {
        final var customer = context.getRepository().load(aggregateRootId).orElseThrow();
        final var discounts = customer.getAppliedDiscounts(fromBooking).stream()
                .map(Discount::getId)
                .collect(Collectors.toList());

        context.getEventPublisher().tryPublish(customer.getVersion(),
                new DiscountsRecoveredEvent(aggregateRootId, fromBooking, discounts));
    }
}
