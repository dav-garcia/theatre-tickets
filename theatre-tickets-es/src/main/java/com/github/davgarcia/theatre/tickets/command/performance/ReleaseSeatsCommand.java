package com.github.davgarcia.theatre.tickets.command.performance;

import com.github.davgarcia.theatre.tickets.error.PreconditionsViolatedException;
import com.github.davgarcia.theatre.tickets.event.performance.Seat;
import com.github.davgarcia.theatre.tickets.event.performance.SeatsReleasedEvent;
import com.github.davgarcia.theatre.tickets.infra.Command;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
public class ReleaseSeatsCommand implements Command<PerformanceCommandContext, Performance, UUID> {

    UUID aggregateRootId;
    Set<Seat> seats;

    @Override
    public void execute(final PerformanceCommandContext context) {
        final var performance = context.getRepository().load(aggregateRootId)
                .orElseThrow(() -> new PreconditionsViolatedException("This performance doesn't exist"));

        context.getEventPublisher().tryPublish(performance.getVersion(),
                new SeatsReleasedEvent(aggregateRootId, seats));
    }
}
