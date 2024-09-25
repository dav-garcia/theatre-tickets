package com.github.davgarcia.theatre.tickets.infra.dispatch.sync;

import com.github.davgarcia.theatre.tickets.error.CommandException;
import com.github.davgarcia.theatre.tickets.error.InconsistentStateException;
import com.github.davgarcia.theatre.tickets.infra.AggregateRoot;
import com.github.davgarcia.theatre.tickets.infra.Command;
import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandContext;
import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandDispatcher;

public class SynchronizedCommandDispatcher<C extends CommandContext<T, U>, T extends AggregateRoot<U>, U>
        implements CommandDispatcher<C, T, U> {

    private final C context;

    public SynchronizedCommandDispatcher(final C context) {
        this.context = context;
    }

    public C getContext() {
        return context;
    }

    @Override
    public synchronized void dispatch(final Command<C, T, U> command) {
        try {
            command.execute(context);
        } catch (InconsistentStateException e) {
            throw new CommandException("This cannot happen in a single-thread dispatcher", e);
        }
    }
}
