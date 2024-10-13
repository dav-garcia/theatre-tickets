package com.github.davgarcia.theatre.tickets.saga;

import com.github.davgarcia.theatre.tickets.command.customer.Customer;
import com.github.davgarcia.theatre.tickets.command.customer.CustomerCommandContext;
import com.github.davgarcia.theatre.tickets.command.customer.RecoverDiscountsCommand;
import com.github.davgarcia.theatre.tickets.command.performance.ReleaseSeatsCommand;
import com.github.davgarcia.theatre.tickets.command.performance.Performance;
import com.github.davgarcia.theatre.tickets.command.performance.PerformanceCommandContext;
import com.github.davgarcia.theatre.tickets.command.ticket.PayTicketCommand;
import com.github.davgarcia.theatre.tickets.command.ticket.Ticket;
import com.github.davgarcia.theatre.tickets.command.ticket.TicketCommandContext;
import com.github.davgarcia.theatre.tickets.event.payment.PaymentCancelledEvent;
import com.github.davgarcia.theatre.tickets.event.payment.PaymentConfirmedEvent;
import com.github.davgarcia.theatre.tickets.event.payment.PaymentPresentedEvent;
import com.github.davgarcia.theatre.tickets.infra.Event;
import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandDispatcher;
import com.github.davgarcia.theatre.tickets.infra.event.EventConsumer;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;

import java.util.Objects;
import java.util.UUID;

public class PaymentSaga implements EventConsumer<UUID> {

    private final Repository<ProcessState, UUID> repository;
    private final CommandDispatcher<PerformanceCommandContext, Performance, UUID> performanceDispatcher;
    private final CommandDispatcher<TicketCommandContext, Ticket, UUID> ticketDispatcher;
    private final CommandDispatcher<CustomerCommandContext, Customer, String> customerDispatcher;

    public PaymentSaga(final Repository<ProcessState, UUID> repository,
                       final CommandDispatcher<PerformanceCommandContext, Performance, UUID> performanceDispatcher,
                       final CommandDispatcher<TicketCommandContext, Ticket, UUID> ticketDispatcher,
                       final CommandDispatcher<CustomerCommandContext, Customer, String> customerDispatcher) {
        this.repository = repository;
        this.performanceDispatcher = performanceDispatcher;
        this.ticketDispatcher = ticketDispatcher;
        this.customerDispatcher = customerDispatcher;
    }

    @Override
    public void consume(final long version, final Event<UUID> event) {
        if (event instanceof PaymentPresentedEvent) {
            process((PaymentPresentedEvent) event);
        } else if (event instanceof PaymentConfirmedEvent) {
            process((PaymentConfirmedEvent) event);
        } else if (event instanceof PaymentCancelledEvent) {
            process((PaymentCancelledEvent) event);
        }
    }

    private void process(final PaymentPresentedEvent event) {
        final var state = repository.load(event.getTicket()).orElseThrow();
        state.setPayment(event.getAggregateRootId());
        repository.save(state);
    }

    private void process(final PaymentConfirmedEvent event) {
        final var state = repository.find(e -> Objects.equals(e.getPayment(), event.getAggregateRootId()))
                .getFirst();

        ticketDispatcher.dispatch(new PayTicketCommand(state.getTicket()));
    }

    private void process(final PaymentCancelledEvent event) {
        final var state = repository.find(e -> Objects.equals(e.getPayment(), event.getAggregateRootId()))
                .getFirst();
        state.setPayment(null);
        repository.save(state);

        recoverDiscounts(state);
        releaseSeats(state);
    }

    private void recoverDiscounts(final ProcessState state) {
        customerDispatcher.dispatch(new RecoverDiscountsCommand(state.getCustomer(), state.getTicket()));
    }

    private void releaseSeats(final ProcessState state) {
        performanceDispatcher.dispatch(new ReleaseSeatsCommand(state.getPerformance(), state.getSeats()));
    }
}
