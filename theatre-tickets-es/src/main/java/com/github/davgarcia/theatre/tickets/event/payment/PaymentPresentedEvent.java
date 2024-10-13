package com.github.davgarcia.theatre.tickets.event.payment;

import com.github.davgarcia.theatre.tickets.infra.Event;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
public class PaymentPresentedEvent implements Event<UUID> {

    UUID aggregateRootId;
    UUID ticket;
    String customer;
    List<Item> items;
    String paymentCode;
}
