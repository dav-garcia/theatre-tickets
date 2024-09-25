package com.github.davgarcia.theatre.tickets.error;

/**
 * Generic exception related to the publication or consumption of an event.
 */
public class EventException extends RuntimeException {

    public EventException(final String message) {
        super(message);
    }

    public EventException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
