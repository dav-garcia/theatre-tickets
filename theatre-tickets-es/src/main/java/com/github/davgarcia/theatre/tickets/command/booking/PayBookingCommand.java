package com.github.davgarcia.theatre.tickets.command.booking;

import com.github.davgarcia.theatre.tickets.error.PreconditionsViolatedException;
import com.github.davgarcia.theatre.tickets.event.booking.BookingPaidEvent;
import com.github.davgarcia.theatre.tickets.infra.Command;
import lombok.Value;

import java.util.UUID;

@Value
public class PayBookingCommand implements Command<BookingCommandContext, Booking, UUID> {

    UUID aggregateRootId;

    @Override
    public void execute(final BookingCommandContext context) {
        final var booking = context.getRepository().load(aggregateRootId)
                .filter(r -> r.getStatus() != Booking.Status.PAID)
                .orElseThrow(() -> new PreconditionsViolatedException("Cannot pay a booking twice"));

        context.getEventPublisher().tryPublish(booking.getVersion(),
                new BookingPaidEvent(aggregateRootId));
    }
}
