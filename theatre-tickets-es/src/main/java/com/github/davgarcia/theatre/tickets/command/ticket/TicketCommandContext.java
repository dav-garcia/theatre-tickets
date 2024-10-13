package com.github.davgarcia.theatre.tickets.command.ticket;

import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandContext;
import com.github.davgarcia.theatre.tickets.infra.event.EventPublisher;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;

import java.util.UUID;

public class TicketCommandContext extends CommandContext<Ticket, UUID> {

    public TicketCommandContext(final Repository<Ticket, UUID> repository,
                                final EventPublisher<UUID> eventPublisher) {
        super(repository, eventPublisher);
    }
}
