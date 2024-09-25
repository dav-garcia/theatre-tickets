package com.github.davgarcia.theatre.tickets.event.customer;

import com.github.davgarcia.theatre.tickets.infra.Event;
import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

@Value
public class DiscountGrantedEvent implements Event<String> {

    String aggregateRootId;
    UUID id;
    String description;
    int amount;
    LocalDate validFrom;
    LocalDate validUntil;
}
