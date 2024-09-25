package com.github.davgarcia.theatre.tickets.saga;

import com.github.davgarcia.theatre.tickets.command.customer.Discount;
import com.github.davgarcia.theatre.tickets.event.payment.Item;
import com.github.davgarcia.theatre.tickets.command.payment.Payment;
import com.github.davgarcia.theatre.tickets.command.payment.PaymentCommandContext;
import com.github.davgarcia.theatre.tickets.command.payment.PresentPaymentIdempotentCommand;
import com.github.davgarcia.theatre.tickets.event.performance.Seat;
import com.github.davgarcia.theatre.tickets.event.customer.DiscountGrantedEvent;
import com.github.davgarcia.theatre.tickets.event.customer.DiscountsAppliedEvent;
import com.github.davgarcia.theatre.tickets.event.customer.DiscountsRecoveredEvent;
import com.github.davgarcia.theatre.tickets.infra.Event;
import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandDispatcher;
import com.github.davgarcia.theatre.tickets.infra.event.EventConsumer;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;

import java.util.ArrayList;
import java.util.UUID;

public class CustomerSaga implements EventConsumer<String> {

    private final Repository<ProcessState, UUID> repository;
    private final Repository<Discount, UUID> discountRepository;
    private final CommandDispatcher<PaymentCommandContext, Payment, UUID> paymentDispatcher;

    public CustomerSaga(final Repository<ProcessState, UUID> repository,
                        final Repository<Discount, UUID> discountRepository,
                        final CommandDispatcher<PaymentCommandContext, Payment, UUID> paymentDispatcher) {
        this.repository = repository;
        this.discountRepository = discountRepository;
        this.paymentDispatcher = paymentDispatcher;
    }

    @Override
    public void consume(final long version, final Event<String> event) {
        if (event instanceof DiscountGrantedEvent) {
            process((DiscountGrantedEvent) event);
        } else if (event instanceof DiscountsAppliedEvent) {
            process((DiscountsAppliedEvent) event);
        } else if (event instanceof DiscountsRecoveredEvent) {
            process((DiscountsRecoveredEvent) event);
        }
    }

    private void process(final DiscountGrantedEvent event) {
        final var discount = Discount.builder()
                .id(event.getId())
                .description(event.getDescription())
                .amount(event.getAmount())
                .validFrom(event.getValidFrom())
                .validUntil(event.getValidUntil())
                .build();

        discountRepository.save(discount);
    }

    private void process(final DiscountsAppliedEvent event) {
        final var state = repository.load(event.getToBooking()).orElseThrow();

        final var items = new ArrayList<Item>();

        for (final Seat seat : state.getSeats()) {
            items.add(new Item("Seat " + seat.getRow() + seat.getNumber(), seat.getPrice()));
        }
        for (final Discount discount : discountRepository.find(d -> event.getDiscounts().contains(d.getId()))) {
            discount.setAppliedToBooking(event.getToBooking());
            discountRepository.save(discount);

            items.add(new Item(discount.getDescription(), -discount.getAmount()));
        }

        paymentDispatcher.dispatch(new PresentPaymentIdempotentCommand(
                UUID.randomUUID(), event.getToBooking(), event.getAggregateRootId(), items));
    }

    private void process(final DiscountsRecoveredEvent event) {
        for (final Discount discount : discountRepository.find(d -> event.getDiscounts().contains(d.getId()))) {
            discount.setAppliedToBooking(null);
            discountRepository.save(discount);
        }
    }
}
