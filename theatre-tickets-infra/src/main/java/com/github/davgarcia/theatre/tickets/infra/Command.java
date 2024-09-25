package com.github.davgarcia.theatre.tickets.infra;

import com.github.davgarcia.theatre.tickets.error.PreconditionsViolatedException;
import com.github.davgarcia.theatre.tickets.error.InconsistentStateException;
import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandContext;
import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandDispatcher;

/**
 * A command changes the internal state of an aggregate.
 */
public interface Command<C extends CommandContext<T, U>, T extends AggregateRoot<U>, U> {

    /**
     * @return id of the aggregate instance that is the target of this command.
     */
    U getAggregateRootId();

    /**
     * Validates the command against the current state of the model and, if valid,
     * executes it by publishing events to the stream.
     * <p>
     * If event publishing fails with {@link InconsistentStateException}, the
     * {@link CommandDispatcher} should retry the command.
     * Therefore, the command logic must be idempotent.
     *
     * @param context Execution context that gives access to the services required by the command.
     * @throws PreconditionsViolatedException If the command's preconditions are not satisfied.
     */
    void execute(final C context);
}
