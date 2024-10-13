package com.github.davgarcia.theatre.tickets;

import com.github.davgarcia.theatre.tickets.infra.AggregateRoot;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Data
@Builder(builderClassName = "Builder")
public class Ticket implements AggregateRoot<UUID> {

    private final UUID id;
    private long version;
    private final UUID performance;
    private final Set<Seat> seats;
    private final String customer;
}
