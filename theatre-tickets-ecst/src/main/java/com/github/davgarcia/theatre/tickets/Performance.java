package com.github.davgarcia.theatre.tickets;

import com.github.davgarcia.theatre.tickets.infra.AggregateRoot;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder(builderClassName = "Builder")
public class Performance implements AggregateRoot<UUID> {

    private final UUID id;
    private long version;
    private final ZonedDateTime when;
    private final Auditorium where;
    private final Set<Seat> availableSeats;
}
