package com.github.davgarcia.theatre.tickets.command.performance;

import com.github.davgarcia.theatre.tickets.error.PreconditionsViolatedException;
import com.github.davgarcia.theatre.tickets.event.performance.PerformanceCreatedEvent;
import com.github.davgarcia.theatre.tickets.event.performance.Auditorium;
import com.github.davgarcia.theatre.tickets.infra.Command;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.UUID;

@Value
public class CreatePerformanceCommand implements Command<PerformanceCommandContext, Performance, UUID> {

    UUID aggregateRootId;
    ZonedDateTime when;
    Auditorium where;

    @Override
    public void execute(final PerformanceCommandContext context) {
        if (context.getRepository().load(aggregateRootId).isPresent()) {
            throw new PreconditionsViolatedException("This performance already exists");
        }

        context.getEventPublisher().tryPublish(0L,
                new PerformanceCreatedEvent(aggregateRootId, when, where));
    }
}
