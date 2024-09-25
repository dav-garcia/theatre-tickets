package com.github.davgarcia.theatre.tickets.command.booking;

import com.github.davgarcia.theatre.tickets.event.performance.Seat;
import com.github.davgarcia.theatre.tickets.error.PreconditionsViolatedException;
import com.github.davgarcia.theatre.tickets.event.booking.BookingCreatedEvent;
import com.github.davgarcia.theatre.tickets.infra.Command;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
public class CreateBookingCommand implements Command<BookingCommandContext, Booking, UUID> {

    UUID aggregateRootId;
    UUID performance;
    Set<Seat> seats;
    String customer;

    @Override
    public void execute(final BookingCommandContext context) {
        if (context.getRepository().load(aggregateRootId).isPresent()) {
            throw new PreconditionsViolatedException("This booking already exists");
        }

        context.getEventPublisher().tryPublish(0L,
                new BookingCreatedEvent(aggregateRootId, performance, seats, customer));
    }
}
