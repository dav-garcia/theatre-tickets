package com.github.davgarcia.theatre.tickets.infra.dispatch;

import com.github.davgarcia.theatre.tickets.infra.AggregateRoot;
import com.github.davgarcia.theatre.tickets.infra.event.EventPublisher;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;
import org.springframework.lang.NonNull;

/**
 * The command context gives access to services and infrastructure needed for command execution.
 */
public class CommandContext<T extends AggregateRoot<U>, U> {

    private final Repository<T, U> repository;
    private final EventPublisher<U> eventPublisher;

    public CommandContext(final Repository<T, U> repository, final EventPublisher<U> eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @NonNull
    public Repository<T, U> getRepository() {
        return repository;
    }

    @NonNull
    public EventPublisher<U> getEventPublisher() {
        return eventPublisher;
    }
}
