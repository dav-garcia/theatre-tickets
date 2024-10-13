package com.github.davgarcia.theatre.tickets.command.performance;

import com.github.davgarcia.theatre.tickets.error.PreconditionsViolatedException;
import com.github.davgarcia.theatre.tickets.event.performance.Seat;
import com.github.davgarcia.theatre.tickets.event.performance.SeatsSelectedEvent;
import com.github.davgarcia.theatre.tickets.infra.Command;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
public class SelectSeatsCommand implements Command<PerformanceCommandContext, Performance, UUID> {

    UUID aggregateRootId;
    UUID forTicket;
    Set<Seat> seats;
    String email;

    @Override
    public void execute(final PerformanceCommandContext context) {
        final var performance = context.getRepository().load(aggregateRootId)
                .filter(r -> r.getAvailableSeats().containsAll(seats))
                .orElseThrow(() -> new PreconditionsViolatedException("Performance doesn't exists or the seats aren't available"));

        context.getEventPublisher().tryPublish(performance.getVersion(),
                new SeatsSelectedEvent(aggregateRootId, forTicket, seats, email));
    }
}
