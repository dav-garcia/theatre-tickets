package com.github.davgarcia.theatre.tickets.query;

import com.github.davgarcia.theatre.tickets.event.customer.CustomerSubscribedEvent;
import com.github.davgarcia.theatre.tickets.event.customer.DiscountGrantedEvent;
import com.github.davgarcia.theatre.tickets.event.customer.DiscountsAppliedEvent;
import com.github.davgarcia.theatre.tickets.event.customer.DiscountsRecoveredEvent;
import com.github.davgarcia.theatre.tickets.event.payment.PaymentPresentedEvent;
import com.github.davgarcia.theatre.tickets.event.ticket.TicketAbandonedEvent;
import com.github.davgarcia.theatre.tickets.event.ticket.TicketCancelledEvent;
import com.github.davgarcia.theatre.tickets.event.ticket.TicketConfirmedEvent;
import com.github.davgarcia.theatre.tickets.event.ticket.TicketCreatedEvent;
import com.github.davgarcia.theatre.tickets.event.ticket.TicketPaidEvent;
import com.github.davgarcia.theatre.tickets.infra.Event;
import com.github.davgarcia.theatre.tickets.infra.event.EventConsumer;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class CustomerHistoryEventConsumer implements EventConsumer<Object> {

    private final Repository<CustomerHistory, String> customerHistoryRepository;
    private final Map<Class<?>, Consumer<Event<?>>> consumers;

    public CustomerHistoryEventConsumer(final Repository<CustomerHistory, String> customerHistoryRepository) {
        this.customerHistoryRepository = customerHistoryRepository;
        this.consumers = buildConsumers();
    }

    private Map<Class<?>, Consumer<Event<?>>> buildConsumers() {
        return Map.of(
                TicketCreatedEvent.class, e -> apply((TicketCreatedEvent) e),
                TicketConfirmedEvent.class, e ->
                        applyStatus(((TicketConfirmedEvent) e).getAggregateRootId(), CustomerHistory.Ticket.Status.CONFIRMED),
                TicketAbandonedEvent.class, e ->
                        applyStatus(((TicketAbandonedEvent) e).getAggregateRootId(), CustomerHistory.Ticket.Status.ABANDONED),
                TicketCancelledEvent.class, e ->
                        applyStatus(((TicketCancelledEvent) e).getAggregateRootId(), CustomerHistory.Ticket.Status.CANCELLED),
                TicketPaidEvent.class, e ->
                        applyStatus(((TicketPaidEvent) e).getAggregateRootId(), CustomerHistory.Ticket.Status.PAID),
                // EmailRegisteredEvent is not needed
                CustomerSubscribedEvent.class, e -> apply((CustomerSubscribedEvent) e),
                DiscountGrantedEvent.class, e -> apply((DiscountGrantedEvent) e),
                DiscountsAppliedEvent.class, e -> apply((DiscountsAppliedEvent) e),
                DiscountsRecoveredEvent.class, e -> apply((DiscountsRecoveredEvent) e),
                PaymentPresentedEvent.class, e -> apply((PaymentPresentedEvent) e)
                // PaymentConfirmedEvent & PaymentCancelledEvent don't directly impact the customer's history
        );
    }

    private void apply(final TicketCreatedEvent event) {
        final var history = customerHistoryRepository.load(event.getCustomer())
                .orElseGet(() -> CustomerHistory.builder()
                        .id(event.getCustomer())
                        .discounts(new ArrayList<>(2))
                        .tickets(new HashMap<>(2))
                        .build());
        history.addTicket(CustomerHistory.Ticket.builder()
                .id(event.getAggregateRootId())
                .seats(event.getSeats())
                .discounts(new ArrayList<>(2))
                .items(new ArrayList<>(4))
                .status(CustomerHistory.Ticket.Status.CREATED)
                .build());
        customerHistoryRepository.save(history);
    }

    private void applyStatus(final UUID bookindId, final CustomerHistory.Ticket.Status status) {
        final var history = customerHistoryRepository.find(h -> h.containsTicket(bookindId)).getFirst();
        history.getTicket(bookindId).setStatus(status);
        customerHistoryRepository.save(history);
    }

    private void apply(final CustomerSubscribedEvent event) {
        final var history = customerHistoryRepository.load(event.getAggregateRootId())
                .orElseGet(() -> CustomerHistory.builder()
                        .id(event.getAggregateRootId())
                        .discounts(new ArrayList<>(2))
                        .tickets(new HashMap<>(2))
                        .build());
        history.setName(event.getName());
        history.setSubscribed(true);
        customerHistoryRepository.save(history);
    }

    private void apply(final DiscountGrantedEvent event) {
        final var history = customerHistoryRepository.load(event.getAggregateRootId()).orElseThrow();
        history.getDiscounts().add(new CustomerHistory.Discount(
                event.getId(), event.getDescription(), event.getAmount(),
                event.getValidFrom(), event.getValidUntil()));
        customerHistoryRepository.save(history);
    }

    private void apply(final DiscountsAppliedEvent event) {
        final var history = customerHistoryRepository.load(event.getAggregateRootId()).orElseThrow();
        history.applyDiscounts(event.getToTicket(), event.getDiscounts());
        customerHistoryRepository.save(history);
    }

    private void apply(final DiscountsRecoveredEvent event) {
        final var history = customerHistoryRepository.load(event.getAggregateRootId()).orElseThrow();
        history.recoverDiscounts(event.getFromTicket(), event.getDiscounts());
        customerHistoryRepository.save(history);
    }

    private void apply(final PaymentPresentedEvent event) {
        final var history = customerHistoryRepository.load(event.getCustomer()).orElseThrow();
        final var ticket = history.getTicket(event.getTicket());
        ticket.setPayment(event.getAggregateRootId());
        ticket.getItems().addAll(event.getItems());
        customerHistoryRepository.save(history);
    }

    @Override
    public void consume(final long version, final Event<Object> event) {
        final var consumer = consumers.get(event.getClass());
        if (consumer != null) {
            consumer.accept(event);
        }
    }
}
