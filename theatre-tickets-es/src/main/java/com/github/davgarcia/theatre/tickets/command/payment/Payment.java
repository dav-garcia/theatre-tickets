package com.github.davgarcia.theatre.tickets.command.payment;

import com.github.davgarcia.theatre.tickets.infra.AggregateRoot;
import lombok.*;

import java.util.UUID;

@Data
@Builder(builderClassName = "Builder")
public class Payment implements AggregateRoot<UUID> {

    private final UUID id;
    private long version;
    private String paymentCode;
}
