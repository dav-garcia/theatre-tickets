package com.github.davgarcia.theatre.tickets.command.performance;

import com.github.davgarcia.theatre.tickets.event.performance.SeatsReleasedEvent;
import com.github.davgarcia.theatre.tickets.event.performance.SeatsSelectedEvent;
import com.github.davgarcia.theatre.tickets.event.performance.PerformanceCreatedEvent;
import com.github.davgarcia.theatre.tickets.infra.Event;
import com.github.davgarcia.theatre.tickets.infra.event.EventConsumer;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;

import java.util.HashSet;
import java.util.UUID;

public class PerformanceEventConsumer implements EventConsumer<UUID> {

    private final Repository<Performance, UUID> repository;

    public PerformanceEventConsumer(final Repository<Performance, UUID> repository) {
        this.repository = repository;
    }

    @Override
    public void consume(long version, Event<UUID> event) {
        if (event instanceof PerformanceCreatedEvent) {
            apply(version, (PerformanceCreatedEvent) event);
        } else if (event instanceof SeatsSelectedEvent) {
            apply(version, (SeatsSelectedEvent) event);
        } else if (event instanceof SeatsReleasedEvent) {
            apply(version, (SeatsReleasedEvent) event);
        }
    }

    public void apply(final long version, final PerformanceCreatedEvent event) {
        final var performance = Performance.builder()
                .id(event.getAggregateRootId())
                .version(version)
                .availableSeats(new HashSet<>(event.getWhere().getSeats()))
                .build();

        repository.save(performance);
    }

    public void apply(final long version, final SeatsSelectedEvent event) {
        final var performance = repository.load(event.getAggregateRootId()).orElseThrow();

        performance.setVersion(version);
        performance.getAvailableSeats().removeAll(event.getSeats());

        repository.save(performance);
    }

    private void apply(final long version, final SeatsReleasedEvent event) {
        final var performance = repository.load(event.getAggregateRootId()).orElseThrow();

        performance.setVersion(version);
        performance.getAvailableSeats().addAll(event.getSeats());

        repository.save(performance);
    }
}
