package com.github.davgarcia.theatre.tickets.error;

/**
 * Generic exception related to the execution of a command.
 */
public class CommandException extends RuntimeException {

    public CommandException(final String message) {
        super(message);
    }

    public CommandException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
