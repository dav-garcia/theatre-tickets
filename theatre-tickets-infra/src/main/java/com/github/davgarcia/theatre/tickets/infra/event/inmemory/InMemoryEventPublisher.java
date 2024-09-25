package com.github.davgarcia.theatre.tickets.infra.event.inmemory;

import com.github.davgarcia.theatre.tickets.error.EventException;
import com.github.davgarcia.theatre.tickets.error.InconsistentStateException;
import com.github.davgarcia.theatre.tickets.infra.Event;
import com.github.davgarcia.theatre.tickets.infra.event.EventConsumer;
import com.github.davgarcia.theatre.tickets.infra.event.EventPublisher;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryEventPublisher<U> implements EventPublisher<U> {

    private final Set<EventConsumer<U>> eventConsumers;
    private final ConcurrentMap<U, AtomicLong> currentVersions;

    public InMemoryEventPublisher() {
        eventConsumers = new HashSet<>();
        currentVersions = new ConcurrentHashMap<>();
    }

    public Set<EventConsumer<U>> getEventConsumers() {
        return Collections.unmodifiableSet(eventConsumers);
    }

    public void registerEventConsumer(final EventConsumer<U> eventConsumer) {
        eventConsumers.add(eventConsumer);
    }

    @Override
    public void tryPublish(final long expectedVersion, final List<Event<U>> events) {
        if (events.stream().map(Event::getAggregateRootId).distinct().count() > 1) {
            throw new EventException("The given list of events to more than one instance");
        }

        final var aggregateRootId = events.getFirst().getAggregateRootId();
        final var newVersion = expectedVersion + events.size();
        long currentVersion = casCurrentVersion(aggregateRootId, expectedVersion, newVersion);

        if (expectedVersion != currentVersion) {
            throw new InconsistentStateException(String.format("Versions don't match: %d vs %d", expectedVersion, currentVersion));
        }

        for (final Event<U> event : events) {
            notifyEventConsumers(++currentVersion, event);
        }
    }

    private long casCurrentVersion(final U id, long expectedVersion, long newVersion) {
        final var currentVersion = currentVersions.computeIfAbsent(id, i -> new AtomicLong(expectedVersion));
        return currentVersion.compareAndExchange(expectedVersion, newVersion);
    }

    private void notifyEventConsumers(final long version, final Event<U> event) {
        eventConsumers.forEach(c -> c.consume(version,  event));
    }
}
