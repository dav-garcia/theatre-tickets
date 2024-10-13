package com.github.davgarcia.theatre.tickets;

import com.github.davgarcia.theatre.tickets.infra.Entity;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder(builderClassName = "Builder")
public class Discount implements Entity<UUID> {

    private final UUID id;
    private final String description;
    private final int amount;
    private final LocalDate validFrom;
    private final LocalDate validUntil;
    UUID appliedToTicket;
}
