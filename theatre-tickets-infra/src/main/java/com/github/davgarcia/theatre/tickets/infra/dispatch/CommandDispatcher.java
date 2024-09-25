package com.github.davgarcia.theatre.tickets.infra.dispatch;

import com.github.davgarcia.theatre.tickets.error.PreconditionsViolatedException;
import com.github.davgarcia.theatre.tickets.infra.AggregateRoot;
import com.github.davgarcia.theatre.tickets.infra.Command;

/**
 * Contract for the strategy to dispatch commands to an aggregate instance.
 */
public interface CommandDispatcher<C extends CommandContext<T, U>, T extends AggregateRoot<U>, U> {

    /**
     * Dispatches a command to an aggregate instance.
     *
     * @param command Command to execute.
     * @throws PreconditionsViolatedException If the command is rejected because preconditions are not satisfied.
     */
    void dispatch(final Command<C, T, U> command);
}
