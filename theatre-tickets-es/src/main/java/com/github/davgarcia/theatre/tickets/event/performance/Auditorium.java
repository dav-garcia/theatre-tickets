package com.github.davgarcia.theatre.tickets.event.performance;

import lombok.Value;

import java.util.Set;

@Value
public class Auditorium {

    String name;
    Set<Seat> seats;
}
