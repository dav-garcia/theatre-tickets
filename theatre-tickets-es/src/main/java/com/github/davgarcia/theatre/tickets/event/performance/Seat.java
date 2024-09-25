package com.github.davgarcia.theatre.tickets.event.performance;

import lombok.Value;

@Value
public class Seat {

    String row;
    int number;
    int price;
}
