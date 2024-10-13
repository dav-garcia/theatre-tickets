package com.github.davgarcia.theatre.tickets.event.ticket;

import com.github.davgarcia.theatre.tickets.infra.Event;
import lombok.Value;

import java.util.UUID;

@Value
public class TicketAbandonedEvent implements Event<UUID> {

    UUID aggregateRootId;
}
