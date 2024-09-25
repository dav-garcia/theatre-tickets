package com.github.davgarcia.theatre.tickets.command.performance;

import com.github.davgarcia.theatre.tickets.event.performance.Seat;
import com.github.davgarcia.theatre.tickets.infra.AggregateRoot;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Data
@Builder(builderClassName = "Builder")
public class Performance implements AggregateRoot<UUID> {

    private final UUID id;
    private long version;
    private final Set<Seat> availableSeats;
}
