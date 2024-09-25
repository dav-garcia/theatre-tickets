package com.github.davgarcia.theatre.tickets.event.customer;

import com.github.davgarcia.theatre.tickets.infra.Event;
import lombok.Value;

@Value
public class CustomerSubscribedEvent implements Event<String> {

    String aggregateRootId;
    String name;
}
