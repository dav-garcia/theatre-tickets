package com.github.davgarcia.theatre.tickets.infra.event;

import com.github.davgarcia.theatre.tickets.error.EventException;
import com.github.davgarcia.theatre.tickets.error.InconsistentStateException;
import com.github.davgarcia.theatre.tickets.infra.Event;

import java.util.List;

/**
 * An aggregate's event publisher must implement this interface.
 */
public interface EventPublisher<U> {

    /**
     * Tries to publish a list of events in an aggregate's stream.
     *
     * @param expectedVersion Aggregate instance's model version on which the events should be applied.
     * @param events List of events to be published.
     * @throws InconsistentStateException If the latest version doesn't match the given expected version.
     * @throws EventException If not all events refer to the same instance.
     */
    void tryPublish(final long expectedVersion, List<Event<U>> events);

    /**
     * Tries to publish the given event in an aggregate's stream.
     *
     * @param expectedVersion Aggregate instance's model version on which the events should be applied.
     * @param event Event to be published.
     * @throws InconsistentStateException If the latest version doesn't match the given expected version.
     */
    default void tryPublish(final long expectedVersion, Event<U> event) {
        tryPublish(expectedVersion, List.of(event));
    }
}
