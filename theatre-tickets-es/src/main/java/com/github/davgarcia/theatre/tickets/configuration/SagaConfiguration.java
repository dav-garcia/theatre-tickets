package com.github.davgarcia.theatre.tickets.configuration;

import com.github.davgarcia.theatre.tickets.command.customer.Customer;
import com.github.davgarcia.theatre.tickets.command.customer.CustomerCommandContext;
import com.github.davgarcia.theatre.tickets.command.customer.Discount;
import com.github.davgarcia.theatre.tickets.command.payment.Payment;
import com.github.davgarcia.theatre.tickets.command.payment.PaymentCommandContext;
import com.github.davgarcia.theatre.tickets.command.performance.Performance;
import com.github.davgarcia.theatre.tickets.command.performance.PerformanceCommandContext;
import com.github.davgarcia.theatre.tickets.command.booking.Booking;
import com.github.davgarcia.theatre.tickets.command.booking.BookingCommandContext;
import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandDispatcher;
import com.github.davgarcia.theatre.tickets.infra.event.inmemory.InMemoryEventPublisher;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;
import com.github.davgarcia.theatre.tickets.infra.repository.inmemory.InMemoryRepository;
import com.github.davgarcia.theatre.tickets.infra.task.TaskScheduler;
import com.github.davgarcia.theatre.tickets.saga.CustomerSaga;
import com.github.davgarcia.theatre.tickets.saga.ProcessState;
import com.github.davgarcia.theatre.tickets.saga.PaymentSaga;
import com.github.davgarcia.theatre.tickets.saga.PerformanceSaga;
import com.github.davgarcia.theatre.tickets.saga.BookingSaga;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class SagaConfiguration {

    @Bean
    public TaskScheduler taskScheduler() {
        return new TaskScheduler();
    }

    @Bean
    public Repository<ProcessState, UUID> processStateRepository() {
        return new InMemoryRepository<>();
    }

    @Bean
    public Repository<Discount, UUID> discountRepository() {
        return new InMemoryRepository<>();
    }

    @Bean
    public PerformanceSaga performanceSaga(
            final Repository<ProcessState, UUID> repository,
            final CommandDispatcher<BookingCommandContext, Booking, UUID> bookingDispatcher,
            final CommandDispatcher<CustomerCommandContext, Customer, String> customerDispatcher,
            final InMemoryEventPublisher<UUID> performancePublisher) {
        final var result = new PerformanceSaga(repository, bookingDispatcher, customerDispatcher);
        performancePublisher.registerEventConsumer(result);
        return result;
    }

    @Bean
    public BookingSaga bookingSaga(
            final Repository<ProcessState, UUID> repository,
            final CommandDispatcher<PerformanceCommandContext, Performance, UUID> performanceDispatcher,
            final CommandDispatcher<BookingCommandContext, Booking, UUID> bookingDispatcher,
            final CommandDispatcher<CustomerCommandContext, Customer, String> customerDispatcher,
            final CommandDispatcher<PaymentCommandContext, Payment, UUID> paymentDispatcher,
            final TaskScheduler taskScheduler,
            final InMemoryEventPublisher<UUID> bookingPublisher) {
        final var result = new BookingSaga(repository,
                performanceDispatcher, bookingDispatcher, customerDispatcher, paymentDispatcher, taskScheduler);
        bookingPublisher.registerEventConsumer(result);
        return result;
    }

    @Bean
    public CustomerSaga customerSaga(
            final Repository<ProcessState, UUID> repository,
            final Repository<Discount, UUID> discountRepository,
            final CommandDispatcher<PaymentCommandContext, Payment, UUID> paymentDispatcher,
            final InMemoryEventPublisher<String> customerPublisher) {
        final var result = new CustomerSaga(repository, discountRepository, paymentDispatcher);
        customerPublisher.registerEventConsumer(result);
        return result;
    }

    @Bean
    public PaymentSaga paymentSaga(
            final Repository<ProcessState, UUID> repository,
            final CommandDispatcher<PerformanceCommandContext, Performance, UUID> performanceDispatcher,
            final CommandDispatcher<BookingCommandContext, Booking, UUID> bookingDispatcher,
            final CommandDispatcher<CustomerCommandContext, Customer, String> customerDispatcher,
            final InMemoryEventPublisher<UUID> paymentPublisher) {
        final var result = new PaymentSaga(repository, performanceDispatcher, bookingDispatcher, customerDispatcher);
        paymentPublisher.registerEventConsumer(result);
        return result;
    }
}
