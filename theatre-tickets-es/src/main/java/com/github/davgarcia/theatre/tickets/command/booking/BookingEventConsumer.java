package com.github.davgarcia.theatre.tickets.command.booking;

import com.github.davgarcia.theatre.tickets.event.booking.BookingAbandonedEvent;
import com.github.davgarcia.theatre.tickets.event.booking.BookingCancelledEvent;
import com.github.davgarcia.theatre.tickets.event.booking.BookingConfirmedEvent;
import com.github.davgarcia.theatre.tickets.event.booking.BookingCreatedEvent;
import com.github.davgarcia.theatre.tickets.event.booking.BookingPaidEvent;
import com.github.davgarcia.theatre.tickets.infra.Event;
import com.github.davgarcia.theatre.tickets.infra.event.EventConsumer;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;

import java.util.UUID;

public class BookingEventConsumer implements EventConsumer<UUID> {

    private final Repository<Booking, UUID> repository;

    public BookingEventConsumer(final Repository<Booking, UUID> repository) {
        this.repository = repository;
    }

    @Override
    public void consume(final long version, final Event<UUID> event) {
        if (event instanceof BookingCreatedEvent) {
            apply(version, (BookingCreatedEvent) event);
        } else if (event instanceof BookingConfirmedEvent) {
            applyStatus(version, event.getAggregateRootId(), Booking.Status.CONFIRMED);
        } else if (event instanceof BookingAbandonedEvent) {
            applyStatus(version, event.getAggregateRootId(), Booking.Status.ABANDONED);
        } else if (event instanceof BookingCancelledEvent) {
            applyStatus(version, event.getAggregateRootId(), Booking.Status.CANCELLED);
        } else if (event instanceof BookingPaidEvent) {
            applyStatus(version, event.getAggregateRootId(), Booking.Status.PAID);
        }
    }

    private void apply(final long version, final BookingCreatedEvent event) {
        final var booking = Booking.builder()
                .id(event.getAggregateRootId())
                .version(version)
                .status(Booking.Status.CREATED)
                .build();

        repository.save(booking);
    }

    private void applyStatus(final long version, final UUID id, final Booking.Status status) {
        final var booking = repository.load(id).orElseThrow();

        booking.setVersion(version);
        booking.setStatus(status);

        repository.save(booking);
    }
}
