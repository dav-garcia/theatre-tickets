package com.github.davgarcia.theatre.tickets.error;

/**
 * Commands throw this exception when the business preconditions to execute it are not met.
 */
public class PreconditionsViolatedException extends CommandException {

    public PreconditionsViolatedException(final String message) {
        super(message);
    }

    public PreconditionsViolatedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
