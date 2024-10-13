package com.github.davgarcia.theatre.tickets.event.ticket;

import com.github.davgarcia.theatre.tickets.event.performance.Seat;
import com.github.davgarcia.theatre.tickets.infra.Event;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
public class TicketCreatedEvent implements Event<UUID> {

    UUID aggregateRootId;
    UUID performance;
    Set<Seat> seats;
    String customer;
}
