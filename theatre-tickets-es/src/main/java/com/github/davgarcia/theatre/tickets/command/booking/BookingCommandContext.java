package com.github.davgarcia.theatre.tickets.command.booking;

import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandContext;
import com.github.davgarcia.theatre.tickets.infra.event.EventPublisher;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;

import java.util.UUID;

public class BookingCommandContext extends CommandContext<Booking, UUID> {

    public BookingCommandContext(final Repository<Booking, UUID> repository,
                                 final EventPublisher<UUID> eventPublisher) {
        super(repository, eventPublisher);
    }
}
