package com.github.davgarcia.theatre.tickets.saga;

import com.github.davgarcia.theatre.tickets.command.customer.ApplyDiscountsCommand;
import com.github.davgarcia.theatre.tickets.command.customer.Customer;
import com.github.davgarcia.theatre.tickets.command.customer.CustomerCommandContext;
import com.github.davgarcia.theatre.tickets.command.customer.RecoverDiscountsCommand;
import com.github.davgarcia.theatre.tickets.command.payment.CancelPaymentCommand;
import com.github.davgarcia.theatre.tickets.command.payment.Payment;
import com.github.davgarcia.theatre.tickets.command.payment.PaymentCommandContext;
import com.github.davgarcia.theatre.tickets.event.performance.Seat;
import com.github.davgarcia.theatre.tickets.command.performance.ReleaseSeatsCommand;
import com.github.davgarcia.theatre.tickets.command.performance.Performance;
import com.github.davgarcia.theatre.tickets.command.performance.PerformanceCommandContext;
import com.github.davgarcia.theatre.tickets.command.ticket.AbandonTicketCommand;
import com.github.davgarcia.theatre.tickets.command.ticket.Ticket;
import com.github.davgarcia.theatre.tickets.command.ticket.TicketCommandContext;
import com.github.davgarcia.theatre.tickets.event.ticket.TicketAbandonedEvent;
import com.github.davgarcia.theatre.tickets.event.ticket.TicketCancelledEvent;
import com.github.davgarcia.theatre.tickets.event.ticket.TicketConfirmedEvent;
import com.github.davgarcia.theatre.tickets.event.ticket.TicketCreatedEvent;
import com.github.davgarcia.theatre.tickets.event.ticket.TicketPaidEvent;
import com.github.davgarcia.theatre.tickets.infra.Event;
import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandDispatcher;
import com.github.davgarcia.theatre.tickets.infra.event.EventConsumer;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;
import com.github.davgarcia.theatre.tickets.infra.task.TaskScheduler;

import java.util.UUID;

public class TicketSaga implements EventConsumer<UUID> {

    private static final String TASK_TYPE = "timeout";
    private static final int DEFAULT_TIMEOUT = 10 * 60;

    private final Repository<ProcessState, UUID> repository;
    private final CommandDispatcher<PerformanceCommandContext, Performance, UUID> performanceDispatcher;
    private final CommandDispatcher<TicketCommandContext, Ticket, UUID> ticketDispatcher;
    private final CommandDispatcher<CustomerCommandContext, Customer, String> customerDispatcher;
    private final CommandDispatcher<PaymentCommandContext, Payment, UUID> paymentDispatcher;
    private final TaskScheduler taskScheduler;

    private int timeout;

    public TicketSaga(final Repository<ProcessState, UUID> repository,
                      final CommandDispatcher<PerformanceCommandContext, Performance, UUID> performanceDispatcher,
                      final CommandDispatcher<TicketCommandContext, Ticket, UUID> ticketDispatcher,
                      final CommandDispatcher<CustomerCommandContext, Customer, String> customerDispatcher,
                      final CommandDispatcher<PaymentCommandContext, Payment, UUID> paymentDispatcher,
                      final TaskScheduler taskScheduler) {
        this.repository = repository;
        this.performanceDispatcher = performanceDispatcher;
        this.ticketDispatcher = ticketDispatcher;
        this.customerDispatcher = customerDispatcher;
        this.paymentDispatcher = paymentDispatcher;
        this.taskScheduler = taskScheduler;

        timeout = DEFAULT_TIMEOUT;
    }

    public void setTimeout(final Integer timeout) {
        this.timeout = timeout == null ? DEFAULT_TIMEOUT : timeout;
    }

    @Override
    public void consume(final long version, final Event<UUID> event) {
        if (event instanceof TicketCreatedEvent) {
            process((TicketCreatedEvent) event);
        } else if (event instanceof TicketConfirmedEvent) {
            process((TicketConfirmedEvent) event);
        } else if (event instanceof TicketAbandonedEvent) {
            process((TicketAbandonedEvent) event);
        } else if (event instanceof TicketCancelledEvent) {
            process((TicketCancelledEvent) event);
        } else if (event instanceof TicketPaidEvent) {
            process((TicketPaidEvent) event);
        }
    }

    private void process(final TicketCreatedEvent event) {
        final var id = event.getAggregateRootId();

        taskScheduler.scheduleTask(TASK_TYPE, id, () -> ticketDispatcher.dispatch(new AbandonTicketCommand(id)), timeout);
    }

    private void process(final TicketConfirmedEvent event) {
        final var state = repository.load(event.getAggregateRootId()).orElseThrow();
        final var maxAmount = state.getSeats().stream()
                .mapToInt(Seat::getPrice)
                .sum();

        customerDispatcher.dispatch(new ApplyDiscountsCommand(state.getCustomer(), state.getTicket(), maxAmount));
    }

    private void process(final TicketAbandonedEvent event) {
        final var state = repository.load(event.getAggregateRootId()).orElseThrow();

        if (!cancelPayment(state)) {
            recoverDiscounts(state);
            releaseSeats(state);
        }
    }

    private void process(final TicketCancelledEvent event) {
        final var state = repository.load(event.getAggregateRootId()).orElseThrow();

        taskScheduler.cancelTask(TASK_TYPE, event.getAggregateRootId());

        if (!cancelPayment(state)) {
            recoverDiscounts(state);
            releaseSeats(state);
        }
    }

    private void process(final TicketPaidEvent event) {
        taskScheduler.cancelTask(TASK_TYPE, event.getAggregateRootId());

        repository.delete(event.getAggregateRootId());
    }

    private boolean cancelPayment(final ProcessState state) {
        if (state.getPayment() == null) {
            return false;
        }

        paymentDispatcher.dispatch(new CancelPaymentCommand(state.getPayment()));
        return true;
    }

    private void recoverDiscounts(final ProcessState state) {
        customerDispatcher.dispatch(new RecoverDiscountsCommand(state.getCustomer(), state.getTicket()));
    }

    private void releaseSeats(final ProcessState state) {
        performanceDispatcher.dispatch(new ReleaseSeatsCommand(state.getPerformance(), state.getSeats()));
    }
}
