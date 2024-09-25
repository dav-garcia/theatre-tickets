package com.github.davgarcia.theatre.tickets.event.payment;

import lombok.Value;

@Value
public class Item {

    String description;
    int amount;
}
