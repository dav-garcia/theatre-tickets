package com.github.davgarcia.theatre.tickets.infra.event;

import com.github.davgarcia.theatre.tickets.infra.Event;

/**
 * Event consumers must implement this interface.
 */
@FunctionalInterface
public interface EventConsumer<U> {

    /**
     * Invoked when an aggregate instance receives an event.
     *
     * @param version Version number that must be stored alongside the model.
     * @param event Event to be consumed.
     */
    void consume(final long version, final Event<U> event);
}
