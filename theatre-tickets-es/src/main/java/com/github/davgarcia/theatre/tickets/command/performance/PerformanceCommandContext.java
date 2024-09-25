package com.github.davgarcia.theatre.tickets.command.performance;

import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandContext;
import com.github.davgarcia.theatre.tickets.infra.event.EventPublisher;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;

import java.util.UUID;

public class PerformanceCommandContext extends CommandContext<Performance, UUID> {

    public PerformanceCommandContext(final Repository<Performance, UUID> repository,
                                     final EventPublisher<UUID> eventPublisher) {
        super(repository, eventPublisher);
    }
}
