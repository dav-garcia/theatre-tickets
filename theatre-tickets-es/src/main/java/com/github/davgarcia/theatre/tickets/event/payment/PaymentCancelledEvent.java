package com.github.davgarcia.theatre.tickets.event.payment;

import com.github.davgarcia.theatre.tickets.infra.Event;
import lombok.Value;

import java.util.UUID;

@Value
public class PaymentCancelledEvent implements Event<UUID> {

    UUID aggregateRootId;
}
