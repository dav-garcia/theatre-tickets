package com.github.davgarcia.theatre.tickets.command.ticket;

import com.github.davgarcia.theatre.tickets.event.performance.Seat;
import com.github.davgarcia.theatre.tickets.error.PreconditionsViolatedException;
import com.github.davgarcia.theatre.tickets.event.ticket.TicketCreatedEvent;
import com.github.davgarcia.theatre.tickets.infra.Command;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
public class CreateTicketCommand implements Command<TicketCommandContext, Ticket, UUID> {

    UUID aggregateRootId;
    UUID performance;
    Set<Seat> seats;
    String customer;

    @Override
    public void execute(final TicketCommandContext context) {
        if (context.getRepository().load(aggregateRootId).isPresent()) {
            throw new PreconditionsViolatedException("This ticket already exists");
        }

        context.getEventPublisher().tryPublish(0L,
                new TicketCreatedEvent(aggregateRootId, performance, seats, customer));
    }
}
