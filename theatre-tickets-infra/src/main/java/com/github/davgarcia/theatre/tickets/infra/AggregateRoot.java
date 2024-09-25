package com.github.davgarcia.theatre.tickets.infra;

/**
 * All aggregates must implement this interface.
 */
public interface AggregateRoot<T> extends Entity<T> {

    long getVersion();
}
