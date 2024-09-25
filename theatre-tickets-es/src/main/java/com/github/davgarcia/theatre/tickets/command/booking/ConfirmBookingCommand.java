package com.github.davgarcia.theatre.tickets.command.booking;

import com.github.davgarcia.theatre.tickets.error.PreconditionsViolatedException;
import com.github.davgarcia.theatre.tickets.event.booking.BookingConfirmedEvent;
import com.github.davgarcia.theatre.tickets.infra.Command;
import lombok.Value;

import java.util.UUID;

@Value
public class ConfirmBookingCommand implements Command<BookingCommandContext, Booking, UUID> {

    UUID aggregateRootId;

    @Override
    public void execute(final BookingCommandContext context) {
        final var booking = context.getRepository().load(aggregateRootId)
                .filter(r -> r.getStatus() == Booking.Status.CREATED)
                .orElseThrow(() -> new PreconditionsViolatedException("Cannot confirm booking"));

        context.getEventPublisher().tryPublish(booking.getVersion(),
                new BookingConfirmedEvent(aggregateRootId));
    }
}
