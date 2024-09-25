package com.github.davgarcia.theatre.tickets.event;

import com.github.davgarcia.theatre.tickets.infra.Event;
import lombok.Value;

import java.util.UUID;

@Value
public class DiscountGrantedEvent implements Event<String> {

    String aggregateRootId;
    UUID id;
}
