package com.github.davgarcia.theatre.tickets.saga;

import com.github.davgarcia.theatre.tickets.command.customer.Customer;
import com.github.davgarcia.theatre.tickets.command.customer.CustomerCommandContext;
import com.github.davgarcia.theatre.tickets.command.customer.RegisterEmailCommand;
import com.github.davgarcia.theatre.tickets.command.booking.CreateBookingCommand;
import com.github.davgarcia.theatre.tickets.command.booking.Booking;
import com.github.davgarcia.theatre.tickets.command.booking.BookingCommandContext;
import com.github.davgarcia.theatre.tickets.event.performance.SeatsSelectedEvent;
import com.github.davgarcia.theatre.tickets.infra.Event;
import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandDispatcher;
import com.github.davgarcia.theatre.tickets.infra.event.EventConsumer;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;

import java.util.UUID;

public class PerformanceSaga implements EventConsumer<UUID> {

    private final Repository<ProcessState, UUID> repository;
    private final CommandDispatcher<BookingCommandContext, Booking, UUID> bookingDispatcher;
    private final CommandDispatcher<CustomerCommandContext, Customer, String> customerDispatcher;

    public PerformanceSaga(final Repository<ProcessState, UUID> repository,
                           final CommandDispatcher<BookingCommandContext, Booking, UUID> bookingDispatcher,
                           final CommandDispatcher<CustomerCommandContext, Customer, String> customerDispatcher) {
        this.repository = repository;
        this.bookingDispatcher = bookingDispatcher;
        this.customerDispatcher = customerDispatcher;
    }

    @Override
    public void consume(final long version, final Event<UUID> event) {
        if (event instanceof SeatsSelectedEvent) {
            process((SeatsSelectedEvent) event);
        }
    }

    private void process(final SeatsSelectedEvent event) {
        createState(event);

        customerDispatcher.dispatch(new RegisterEmailCommand(event.getEmail()));
        bookingDispatcher.dispatch(new CreateBookingCommand(event.getForBooking(),
                event.getAggregateRootId(), event.getSeats(), event.getEmail()));
    }

    private void createState(final SeatsSelectedEvent event) {
        repository.save(ProcessState.builder()
                .id(event.getForBooking())
                .performance(event.getAggregateRootId())
                .customer(event.getEmail())
                .seats(event.getSeats())
                .build());
    }
}
