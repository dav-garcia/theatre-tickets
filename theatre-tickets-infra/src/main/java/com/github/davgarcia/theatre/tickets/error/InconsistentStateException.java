package com.github.davgarcia.theatre.tickets.error;

/**
 * An event publisher signals that publishing an event would leave the aggregate in an inconsistent state,
 * therefore the event is rejected and the command should be retried.
 */
public class InconsistentStateException extends EventException {

    public InconsistentStateException(final String message) {
        super(message);
    }

    public InconsistentStateException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
