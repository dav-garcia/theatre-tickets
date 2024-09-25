package com.github.davgarcia.theatre.tickets.command.customer;

import com.github.davgarcia.theatre.tickets.error.PreconditionsViolatedException;
import com.github.davgarcia.theatre.tickets.event.customer.CustomerSubscribedEvent;
import com.github.davgarcia.theatre.tickets.event.customer.DiscountGrantedEvent;
import com.github.davgarcia.theatre.tickets.infra.Command;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Value
public class SubscribeCustomerCommand implements Command<CustomerCommandContext, Customer, String> {

    private static final String DISCOUNT_DESCRIPTION = "Loyalty discount";
    private static final int DISCOUNT_AMOUNT = 10;
    private static final int DISCOUNT_DURATION = 30;

    String aggregateRootId;
    String name;

    @Override
    public void execute(final CustomerCommandContext context) {
        final var optionalCustomer = context.getRepository().load(aggregateRootId);
        final boolean subscribed = optionalCustomer.map(Customer::isSubscribed).orElse(false);
        final long version = optionalCustomer.map(Customer::getVersion).orElse(0L);

        if (subscribed) {
            throw new PreconditionsViolatedException("This customer is already subscribed");
        }

        context.getEventPublisher().tryPublish(version, List.of(
                new CustomerSubscribedEvent(aggregateRootId, name),
                new DiscountGrantedEvent(aggregateRootId, UUID.randomUUID(),
                        DISCOUNT_DESCRIPTION, DISCOUNT_AMOUNT,
                        LocalDate.now(), LocalDate.now().plusDays(DISCOUNT_DURATION))));
    }
}
