package com.github.davgarcia.theatre.tickets.event.customer;

import com.github.davgarcia.theatre.tickets.infra.Event;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
public class DiscountsAppliedEvent implements Event<String> {

    String aggregateRootId;
    UUID toBooking;
    List<UUID> discounts;
}
