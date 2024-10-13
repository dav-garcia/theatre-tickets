package com.github.davgarcia.theatre.tickets;

import com.github.davgarcia.theatre.tickets.command.customer.Customer;
import com.github.davgarcia.theatre.tickets.command.customer.CustomerCommandContext;
import com.github.davgarcia.theatre.tickets.command.customer.SubscribeCustomerCommand;
import com.github.davgarcia.theatre.tickets.command.payment.ConfirmPaymentCommand;
import com.github.davgarcia.theatre.tickets.command.payment.Payment;
import com.github.davgarcia.theatre.tickets.command.payment.PaymentCommandContext;
import com.github.davgarcia.theatre.tickets.command.performance.CreatePerformanceCommand;
import com.github.davgarcia.theatre.tickets.command.performance.Performance;
import com.github.davgarcia.theatre.tickets.command.performance.PerformanceCommandContext;
import com.github.davgarcia.theatre.tickets.command.performance.SelectSeatsCommand;
import com.github.davgarcia.theatre.tickets.command.ticket.CancelTicketCommand;
import com.github.davgarcia.theatre.tickets.command.ticket.ConfirmTicketCommand;
import com.github.davgarcia.theatre.tickets.command.ticket.Ticket;
import com.github.davgarcia.theatre.tickets.command.ticket.TicketCommandContext;
import com.github.davgarcia.theatre.tickets.configuration.*;
import com.github.davgarcia.theatre.tickets.event.payment.Item;
import com.github.davgarcia.theatre.tickets.event.performance.Seat;
import com.github.davgarcia.theatre.tickets.event.performance.Auditorium;
import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandDispatcher;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;
import com.github.davgarcia.theatre.tickets.query.CustomerHistory;
import com.github.davgarcia.theatre.tickets.saga.TicketSaga;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = {
                PerformanceConfiguration.class, TicketConfiguration.class, CustomerConfiguration.class, PaymentConfiguration.class,
                SagaConfiguration.class, CustomerHistoryConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TheatreTicketsTest {

    private static final Seat A1 = new Seat("A", 1, 10);
    private static final Seat A2 = new Seat("A", 2, 20);
    private static final Seat A3 = new Seat("A", 3, 30);
    private static final Seat B1 = new Seat("B", 1, 10);
    private static final Seat B2 = new Seat("B", 2, 20);
    private static final Seat B3 = new Seat("B", 3, 30);
    private static final Auditorium AUDITORIUM = new Auditorium("SALA", Set.of(A1, A2, A3, B1, B2, B3));

    @Autowired
    private CommandDispatcher<PerformanceCommandContext, Performance, UUID> performanceDispatcher;

    @Autowired
    private CommandDispatcher<TicketCommandContext, Ticket, UUID> ticketDispatcher;

    @Autowired
    private CommandDispatcher<CustomerCommandContext, Customer, String> customerDispatcher;

    @Autowired
    private CommandDispatcher<PaymentCommandContext, Payment, UUID> paymentDispatcher;

    @Autowired
    private TicketSaga ticketSaga; // To simulate ticket abandoned timeout

    @Autowired
    private Repository<Performance, UUID> performanceRepository; // To check seat availability

    @Autowired
    private Repository<CustomerHistory, String> customerHistoryRepository; // To assert actual vs expected results

    @Test
    void whenSeatsSelected_thenTicketCreated() {
        final var performanceId = UUID.randomUUID();
        final var ticketId = UUID.randomUUID();
        final var customerId = RandomStringUtils.randomAlphabetic(10) + "@email.com";

        performanceDispatcher.dispatch(new CreatePerformanceCommand(performanceId, ZonedDateTime.now(), AUDITORIUM));
        performanceDispatcher.dispatch(new SelectSeatsCommand(performanceId, ticketId, Set.of(A1, A2, B3), customerId));

        await().atMost(2, TimeUnit.SECONDS).until(() -> customerHistoryRepository.load(customerId)
                .filter(r -> r.containsTicket(ticketId))
                .isPresent());

        final var performance = performanceRepository.load(performanceId).orElseThrow();
        final var history = customerHistoryRepository.load(customerId).orElseThrow();
        final var ticket = history.getTicket(ticketId);

        assertThat(performance.getAvailableSeats()).containsExactlyInAnyOrder(A3, B1, B2);
        assertThat(history.isSubscribed()).isFalse();
        assertThat(history.getName()).isNull();
        assertThat(history.getDiscounts()).isEmpty();
        assertThat(ticket.getSeats()).containsExactlyInAnyOrder(A1, A2, B3);
        assertThat(ticket.getStatus()).isEqualTo(CustomerHistory.Ticket.Status.CREATED);
    }

    @Test
    void whenTicketAbandoned_thenButacasLiberadas() {
        ticketSaga.setTimeout(1);
        try {
            final var performanceId = UUID.randomUUID();
            final var ticketId = UUID.randomUUID();
            final var customerId = RandomStringUtils.randomAlphabetic(10) + "@email.com";

            performanceDispatcher.dispatch(new CreatePerformanceCommand(performanceId, ZonedDateTime.now(), AUDITORIUM));
            performanceDispatcher.dispatch(new SelectSeatsCommand(performanceId, ticketId, Set.of(A1, A2, B3), customerId));

            await().atMost(2, TimeUnit.SECONDS).until(() -> performanceRepository.load(performanceId)
                    .filter(r -> r.getAvailableSeats().containsAll(Set.of(A1, A2, B3)))
                    .isPresent());

            final var history = customerHistoryRepository.load(customerId).orElseThrow();
            final var ticket = history.getTicket(ticketId);

            assertThat(ticket.getStatus()).isEqualTo(CustomerHistory.Ticket.Status.ABANDONED);
        } finally {
            ticketSaga.setTimeout(null);
        }
    }

    @Test
    void givenDiscount_whenTicketConfirmed_thenDiscountedPaymentPresented() {
        final var performanceId = UUID.randomUUID();
        final var ticketId = UUID.randomUUID();
        final var customerId = RandomStringUtils.randomAlphabetic(10) + "@email.com";

        performanceDispatcher.dispatch(new CreatePerformanceCommand(performanceId, ZonedDateTime.now(), AUDITORIUM));
        performanceDispatcher.dispatch(new SelectSeatsCommand(performanceId, ticketId, Set.of(A1, A2, B3), customerId));
        customerDispatcher.dispatch(new SubscribeCustomerCommand(customerId, "The customer"));
        ticketDispatcher.dispatch(new ConfirmTicketCommand(ticketId));

        await().atMost(2, TimeUnit.SECONDS).until(() -> customerHistoryRepository.load(customerId)
                .filter(h -> h.getTicket(ticketId).getPayment() != null)
                .isPresent());

        final var history = customerHistoryRepository.load(customerId).orElseThrow();
        final var ticket = history.getTicket(ticketId);

        assertThat(history.isSubscribed()).isTrue();
        assertThat(history.getName()).isEqualTo("The customer");
        assertThat(history.getDiscounts()).isEmpty();
        assertThat(ticket.getDiscounts())
                .extracting(CustomerHistory.Discount::getAmount,
                        CustomerHistory.Discount::getValidFrom,
                        CustomerHistory.Discount::getValidUntil)
                .containsExactly(tuple(10, LocalDate.now(), LocalDate.now().plusDays(30)));
        assertThat(ticket.getItems()).containsExactlyInAnyOrder(
                new Item("Seat A1", 10),
                new Item("Seat A2", 20),
                new Item("Seat B3", 30),
                new Item("Loyalty discount", -10));
        assertThat(ticket.getStatus()).isEqualTo(CustomerHistory.Ticket.Status.CONFIRMED);
    }

    @Test
    void whenPaymentConfirmed_thenTicketPaid() {
        final var performanceId = UUID.randomUUID();
        final var ticketId = UUID.randomUUID();
        final var customerId = RandomStringUtils.randomAlphabetic(10) + "@email.com";

        performanceDispatcher.dispatch(new CreatePerformanceCommand(performanceId, ZonedDateTime.now(), AUDITORIUM));
        performanceDispatcher.dispatch(new SelectSeatsCommand(performanceId, ticketId, Set.of(A1, A2, B3), customerId));
        ticketDispatcher.dispatch(new ConfirmTicketCommand(ticketId));

        await().atMost(2, TimeUnit.SECONDS).until(() -> customerHistoryRepository.load(customerId)
                .filter(h -> h.getTicket(ticketId).getPayment() != null)
                .isPresent());

        final var history = customerHistoryRepository.load(customerId).orElseThrow();
        final var ticket = history.getTicket(ticketId);

        paymentDispatcher.dispatch(new ConfirmPaymentCommand(ticket.getPayment()));

        await().atMost(2, TimeUnit.SECONDS).until(() -> customerHistoryRepository.load(customerId)
                .filter(h -> h.getTicket(ticketId).getStatus() == CustomerHistory.Ticket.Status.PAID)
                .isPresent());
    }

    @Test
    void givenDiscount_whenTicketCancelled_thenSeatsReleasedAndDiscountRecovered() {
        final var performanceId = UUID.randomUUID();
        final var ticketId = UUID.randomUUID();
        final var customerId = RandomStringUtils.randomAlphabetic(10) + "@email.com";

        performanceDispatcher.dispatch(new CreatePerformanceCommand(performanceId, ZonedDateTime.now(), AUDITORIUM));
        performanceDispatcher.dispatch(new SelectSeatsCommand(performanceId, ticketId, Set.of(A1, A2, B3), customerId));
        customerDispatcher.dispatch(new SubscribeCustomerCommand(customerId, "The customer"));
        ticketDispatcher.dispatch(new ConfirmTicketCommand(ticketId));

        await().atMost(2, TimeUnit.SECONDS).until(() -> customerHistoryRepository.load(customerId)
                .filter(h -> h.getTicket(ticketId).getPayment() != null)
                .isPresent());

        ticketDispatcher.dispatch(new CancelTicketCommand(ticketId));

        await().atMost(2, TimeUnit.SECONDS).until(() -> performanceRepository.load(performanceId)
                .filter(r -> r.getAvailableSeats().containsAll(Set.of(A1, A2, B3)))
                .isPresent());

        final var history = customerHistoryRepository.load(customerId).orElseThrow();
        final var ticket = history.getTicket(ticketId);

        assertThat(history.getDiscounts())
                .extracting(CustomerHistory.Discount::getAmount,
                        CustomerHistory.Discount::getValidFrom,
                        CustomerHistory.Discount::getValidUntil)
                .containsExactly(tuple(10, LocalDate.now(), LocalDate.now().plusDays(30)));
        assertThat(ticket.getDiscounts()).isEmpty();
        assertThat(ticket.getStatus()).isEqualTo(CustomerHistory.Ticket.Status.CANCELLED);
    }
}
