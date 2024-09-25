package com.github.davgarcia.theatre.tickets.event.performance;

import com.github.davgarcia.theatre.tickets.infra.Event;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.UUID;

@Value
public class PerformanceCreatedEvent implements Event<UUID> {

    UUID aggregateRootId;
    ZonedDateTime when;
    Auditorium where;
}
