package com.github.davgarcia.theatre.tickets.infra;

/**
 * An event applies the state change on an aggregate instance.
 */
public interface Event<U> {

    /**
     * @return id of the aggregate instance that produced this event.
     */
    U getAggregateRootId();
}
