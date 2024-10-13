package com.github.davgarcia.theatre.tickets.saga;

import com.github.davgarcia.theatre.tickets.event.performance.Seat;
import com.github.davgarcia.theatre.tickets.infra.Entity;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(builderClassName = "Builder")
public class ProcessState implements Entity<UUID> {

    private final UUID ticket;
    private final UUID performance;
    private final String customer;
    private final Set<Seat> seats;
    private UUID payment;

    @Override
    public @NonNull UUID getId() {
        return ticket;
    }
}
