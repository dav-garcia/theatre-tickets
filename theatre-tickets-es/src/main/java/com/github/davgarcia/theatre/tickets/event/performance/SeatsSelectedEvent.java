package com.github.davgarcia.theatre.tickets.event.performance;

import com.github.davgarcia.theatre.tickets.infra.Event;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
public class SeatsSelectedEvent implements Event<UUID> {

    UUID aggregateRootId;
    UUID forTicket;
    Set<Seat> seats;
    String email;
}
