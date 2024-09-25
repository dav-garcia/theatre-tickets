package com.github.davgarcia.theatre.tickets.saga;

import com.github.davgarcia.theatre.tickets.event.performance.Seat;
import com.github.davgarcia.theatre.tickets.infra.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(builderClassName = "Builder")
public class ProcessState implements Entity<UUID> {

    private final UUID id; // It's the booking id
    private final UUID performance;
    private final String customer;
    private final Set<Seat> seats;
    private UUID payment;
}
