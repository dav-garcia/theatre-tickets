package com.github.davgarcia.theatre.tickets.event.booking;

import com.github.davgarcia.theatre.tickets.infra.Event;
import lombok.Value;

import java.util.UUID;

@Value
public class BookingCancelledEvent implements Event<UUID> {

    UUID aggregateRootId;
}
