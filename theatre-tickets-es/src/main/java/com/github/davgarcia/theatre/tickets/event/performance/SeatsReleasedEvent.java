package com.github.davgarcia.theatre.tickets.event.performance;

import com.github.davgarcia.theatre.tickets.infra.Event;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
public class SeatsReleasedEvent implements Event<UUID> {

    UUID aggregateRootId;
    Set<Seat> seats;
}
