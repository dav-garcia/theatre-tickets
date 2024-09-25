package com.github.davgarcia.theatre.tickets.command.booking;

import com.github.davgarcia.theatre.tickets.infra.AggregateRoot;
import lombok.*;

import java.util.UUID;

@Data
@Builder(builderClassName = "Builder")
public class Booking implements AggregateRoot<UUID> {

    public enum Status {
        CREATED,
        CONFIRMED,
        PAID,
        ABANDONED,
        CANCELLED
    }

    private final UUID id;
    private long version;
    private Status status;
}
