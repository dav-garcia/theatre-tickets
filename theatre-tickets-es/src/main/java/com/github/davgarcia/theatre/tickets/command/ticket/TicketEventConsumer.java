package com.github.davgarcia.theatre.tickets.command.ticket;

import com.github.davgarcia.theatre.tickets.event.ticket.TicketAbandonedEvent;
import com.github.davgarcia.theatre.tickets.event.ticket.TicketCancelledEvent;
import com.github.davgarcia.theatre.tickets.event.ticket.TicketConfirmedEvent;
import com.github.davgarcia.theatre.tickets.event.ticket.TicketCreatedEvent;
import com.github.davgarcia.theatre.tickets.event.ticket.TicketPaidEvent;
import com.github.davgarcia.theatre.tickets.infra.Event;
import com.github.davgarcia.theatre.tickets.infra.event.EventConsumer;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;

import java.util.UUID;

public class TicketEventConsumer implements EventConsumer<UUID> {

    private final Repository<Ticket, UUID> repository;

    public TicketEventConsumer(final Repository<Ticket, UUID> repository) {
        this.repository = repository;
    }

    @Override
    public void consume(final long version, final Event<UUID> event) {
        if (event instanceof TicketCreatedEvent) {
            apply(version, (TicketCreatedEvent) event);
        } else if (event instanceof TicketConfirmedEvent) {
            applyStatus(version, event.getAggregateRootId(), Ticket.Status.CONFIRMED);
        } else if (event instanceof TicketAbandonedEvent) {
            applyStatus(version, event.getAggregateRootId(), Ticket.Status.ABANDONED);
        } else if (event instanceof TicketCancelledEvent) {
            applyStatus(version, event.getAggregateRootId(), Ticket.Status.CANCELLED);
        } else if (event instanceof TicketPaidEvent) {
            applyStatus(version, event.getAggregateRootId(), Ticket.Status.PAID);
        }
    }

    private void apply(final long version, final TicketCreatedEvent event) {
        final var ticket = Ticket.builder()
                .id(event.getAggregateRootId())
                .version(version)
                .status(Ticket.Status.CREATED)
                .build();

        repository.save(ticket);
    }

    private void applyStatus(final long version, final UUID id, final Ticket.Status status) {
        final var ticket = repository.load(id).orElseThrow();

        ticket.setVersion(version);
        ticket.setStatus(status);

        repository.save(ticket);
    }
}
