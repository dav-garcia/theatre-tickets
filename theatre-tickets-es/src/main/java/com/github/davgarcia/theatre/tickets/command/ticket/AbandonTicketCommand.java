package com.github.davgarcia.theatre.tickets.command.ticket;

import com.github.davgarcia.theatre.tickets.error.PreconditionsViolatedException;
import com.github.davgarcia.theatre.tickets.event.ticket.TicketAbandonedEvent;
import com.github.davgarcia.theatre.tickets.infra.Command;
import lombok.Value;

import java.util.UUID;

@Value
public class AbandonTicketCommand implements Command<TicketCommandContext, Ticket, UUID> {

    UUID aggregateRootId;

    @Override
    public void execute(final TicketCommandContext context) {
        final var ticket = context.getRepository().load(aggregateRootId)
                .filter(r -> r.getStatus() != Ticket.Status.PAID)
                .orElseThrow(() -> new PreconditionsViolatedException("Ticket already paid, so it cannot be abandoned"));

        context.getEventPublisher().tryPublish(ticket.getVersion(),
                new TicketAbandonedEvent(aggregateRootId));
    }
}
