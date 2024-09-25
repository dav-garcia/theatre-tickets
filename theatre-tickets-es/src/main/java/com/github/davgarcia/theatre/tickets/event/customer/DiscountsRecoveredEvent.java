package com.github.davgarcia.theatre.tickets.event.customer;

import com.github.davgarcia.theatre.tickets.infra.Event;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
public class DiscountsRecoveredEvent implements Event<String> {

    String aggregateRootId;
    UUID fromBooking;
    List<UUID> discounts;
}
