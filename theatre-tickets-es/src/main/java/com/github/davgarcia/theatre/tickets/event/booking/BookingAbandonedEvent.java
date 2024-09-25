package com.github.davgarcia.theatre.tickets.event.booking;

import com.github.davgarcia.theatre.tickets.infra.Event;
import lombok.Value;

import java.util.UUID;

@Value
public class BookingAbandonedEvent implements Event<UUID> {

    UUID aggregateRootId;
}
