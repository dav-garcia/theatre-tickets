package com.github.davgarcia.theatre.tickets.command.customer;

import com.github.davgarcia.theatre.tickets.event.customer.DiscountsAppliedEvent;
import com.github.davgarcia.theatre.tickets.infra.Command;
import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

@Value
public class ApplyDiscountsCommand implements Command<CustomerCommandContext, Customer, String> {

    String aggregateRootId;
    UUID toBooking;
    int maxAmount;

    @Override
    public void execute(final CustomerCommandContext context) {
        final var customer = context.getRepository().load(aggregateRootId).orElseThrow();
        final var discounts = customer.getApplicableDiscounts(LocalDate.now(), maxAmount).stream()
                .map(Discount::getId)
                .collect(Collectors.toList());

        context.getEventPublisher().tryPublish(customer.getVersion(),
                new DiscountsAppliedEvent(aggregateRootId, toBooking, discounts));
    }
}
