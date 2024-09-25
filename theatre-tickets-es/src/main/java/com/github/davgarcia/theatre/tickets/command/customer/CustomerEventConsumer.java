package com.github.davgarcia.theatre.tickets.command.customer;

import com.github.davgarcia.theatre.tickets.event.customer.CustomerSubscribedEvent;
import com.github.davgarcia.theatre.tickets.event.customer.DiscountGrantedEvent;
import com.github.davgarcia.theatre.tickets.event.customer.DiscountsAppliedEvent;
import com.github.davgarcia.theatre.tickets.event.customer.DiscountsRecoveredEvent;
import com.github.davgarcia.theatre.tickets.event.customer.EmailRegisteredEvent;
import com.github.davgarcia.theatre.tickets.infra.Event;
import com.github.davgarcia.theatre.tickets.infra.event.EventConsumer;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;

import java.util.ArrayList;

public class CustomerEventConsumer implements EventConsumer<String> {

    private final Repository<Customer, String> repository;

    public CustomerEventConsumer(final Repository<Customer, String> repository) {
        this.repository = repository;
    }

    @Override
    public void consume(final long version, final Event<String> event) {
        if (event instanceof EmailRegisteredEvent) {
            apply(version, (EmailRegisteredEvent) event);
        } else if (event instanceof CustomerSubscribedEvent) {
            apply(version, (CustomerSubscribedEvent) event);
        } else if (event instanceof DiscountGrantedEvent) {
            apply(version, (DiscountGrantedEvent) event);
        } else if (event instanceof DiscountsAppliedEvent) {
            apply(version, (DiscountsAppliedEvent) event);
        } else if (event instanceof DiscountsRecoveredEvent) {
            apply(version, (DiscountsRecoveredEvent) event);
        }
    }

    private void apply(final long version, final EmailRegisteredEvent event) {
        final var customer = Customer.builder()
                .id(event.getAggregateRootId())
                .version(version)
                .subscribed(false)
                .discounts(new ArrayList<>())
                .build();

        repository.save(customer);
    }

    private void apply(final long version, final CustomerSubscribedEvent event) {
        final var customer = repository.load(event.getAggregateRootId())
                .orElseGet(() -> Customer.builder()
                        .id(event.getAggregateRootId())
                        .discounts(new ArrayList<>())
                        .build());

        customer.setVersion(version);
        customer.setSubscribed(true);

        repository.save(customer);
    }

    private void apply(final long version, final DiscountGrantedEvent event) {
        final var customer = repository.load(event.getAggregateRootId()).orElseThrow();

        if (customer.getDiscounts().stream().noneMatch(d -> d.getId().equals(event.getId()))) { // Idempotencia
            customer.setVersion(version);
            customer.getDiscounts().add(Discount.builder()
                    .id(event.getId())
                    .description(event.getDescription())
                    .amount(event.getAmount())
                    .validFrom(event.getValidFrom())
                    .validUntil(event.getValidUntil())
                    .build());

            repository.save(customer);
        }
    }

    private void apply(final long version, final DiscountsAppliedEvent event) {
        final var customer = repository.load(event.getAggregateRootId()).orElseThrow();

        customer.setVersion(version);
        customer.getDiscounts().stream()
                .filter(d -> event.getDiscounts().contains(d.getId()))
                .forEach(d -> d.setAppliedToBooking(event.getToBooking()));

        repository.save(customer);
    }

    private void apply(final long version, final DiscountsRecoveredEvent event) {
        final var customer = repository.load(event.getAggregateRootId()).orElseThrow();

        customer.setVersion(version);
        customer.getDiscounts().stream()
                .filter(d -> event.getDiscounts().contains(d.getId()))
                .forEach(d -> d.setAppliedToBooking(null));

        repository.save(customer);
    }
}
