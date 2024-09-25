package com.github.davgarcia.theatre.tickets.query;

import com.github.davgarcia.theatre.tickets.event.customer.CustomerSubscribedEvent;
import com.github.davgarcia.theatre.tickets.event.customer.DiscountGrantedEvent;
import com.github.davgarcia.theatre.tickets.event.customer.DiscountsAppliedEvent;
import com.github.davgarcia.theatre.tickets.event.customer.DiscountsRecoveredEvent;
import com.github.davgarcia.theatre.tickets.event.payment.PaymentPresentedEvent;
import com.github.davgarcia.theatre.tickets.event.booking.BookingAbandonedEvent;
import com.github.davgarcia.theatre.tickets.event.booking.BookingCancelledEvent;
import com.github.davgarcia.theatre.tickets.event.booking.BookingConfirmedEvent;
import com.github.davgarcia.theatre.tickets.event.booking.BookingCreatedEvent;
import com.github.davgarcia.theatre.tickets.event.booking.BookingPaidEvent;
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
                BookingCreatedEvent.class, e -> apply((BookingCreatedEvent) e),
                BookingConfirmedEvent.class, e ->
                        applyStatus(((BookingConfirmedEvent) e).getAggregateRootId(), CustomerHistory.Booking.Status.CONFIRMED),
                BookingAbandonedEvent.class, e ->
                        applyStatus(((BookingAbandonedEvent) e).getAggregateRootId(), CustomerHistory.Booking.Status.ABANDONED),
                BookingCancelledEvent.class, e ->
                        applyStatus(((BookingCancelledEvent) e).getAggregateRootId(), CustomerHistory.Booking.Status.CANCELLED),
                BookingPaidEvent.class, e ->
                        applyStatus(((BookingPaidEvent) e).getAggregateRootId(), CustomerHistory.Booking.Status.PAID),
                // EmailRegisteredEvent is not needed
                CustomerSubscribedEvent.class, e -> apply((CustomerSubscribedEvent) e),
                DiscountGrantedEvent.class, e -> apply((DiscountGrantedEvent) e),
                DiscountsAppliedEvent.class, e -> apply((DiscountsAppliedEvent) e),
                DiscountsRecoveredEvent.class, e -> apply((DiscountsRecoveredEvent) e),
                PaymentPresentedEvent.class, e -> apply((PaymentPresentedEvent) e)
                // PaymentConfirmedEvent & PaymentCancelledEvent don't directly impact the customer's history
        );
    }

    private void apply(final BookingCreatedEvent event) {
        final var history = customerHistoryRepository.load(event.getCustomer())
                .orElseGet(() -> CustomerHistory.builder()
                        .id(event.getCustomer())
                        .discounts(new ArrayList<>(2))
                        .bookings(new HashMap<>(2))
                        .build());
        history.addBooking(CustomerHistory.Booking.builder()
                .id(event.getAggregateRootId())
                .seats(event.getSeats())
                .discounts(new ArrayList<>(2))
                .items(new ArrayList<>(4))
                .status(CustomerHistory.Booking.Status.CREATED)
                .build());
        customerHistoryRepository.save(history);
    }

    private void applyStatus(final UUID bookindId, final CustomerHistory.Booking.Status status) {
        final var history = customerHistoryRepository.find(h -> h.containsBooking(bookindId)).getFirst();
        history.getBooking(bookindId).setStatus(status);
        customerHistoryRepository.save(history);
    }

    private void apply(final CustomerSubscribedEvent event) {
        final var history = customerHistoryRepository.load(event.getAggregateRootId())
                .orElseGet(() -> CustomerHistory.builder()
                        .id(event.getAggregateRootId())
                        .discounts(new ArrayList<>(2))
                        .bookings(new HashMap<>(2))
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
        history.applyDiscounts(event.getToBooking(), event.getDiscounts());
        customerHistoryRepository.save(history);
    }

    private void apply(final DiscountsRecoveredEvent event) {
        final var history = customerHistoryRepository.load(event.getAggregateRootId()).orElseThrow();
        history.recoverDiscounts(event.getFromBooking(), event.getDiscounts());
        customerHistoryRepository.save(history);
    }

    private void apply(final PaymentPresentedEvent event) {
        final var history = customerHistoryRepository.load(event.getCustomer()).orElseThrow();
        final var booking = history.getBooking(event.getBooking());
        booking.setPayment(event.getAggregateRootId());
        booking.getItems().addAll(event.getItems());
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
