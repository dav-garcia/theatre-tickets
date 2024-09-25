package com.github.davgarcia.theatre.tickets.configuration;

import com.github.davgarcia.theatre.tickets.command.payment.Payment;
import com.github.davgarcia.theatre.tickets.command.payment.PaymentCommandContext;
import com.github.davgarcia.theatre.tickets.command.payment.PaymentEventConsumer;
import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandDispatcher;
import com.github.davgarcia.theatre.tickets.infra.dispatch.occ.OccCommandDispatcher;
import com.github.davgarcia.theatre.tickets.infra.event.inmemory.InMemoryEventPublisher;
import com.github.davgarcia.theatre.tickets.infra.payment.PaymentGateway;
import com.github.davgarcia.theatre.tickets.infra.payment.inmemory.InMemoryPaymentGateway;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;
import com.github.davgarcia.theatre.tickets.infra.repository.inmemory.InMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class PaymentConfiguration {

    @Bean
    public Repository<Payment, UUID> paymentRepository() {
        return new InMemoryRepository<>();
    }

    @Bean
    public InMemoryEventPublisher<UUID> paymentPublisher() {
        return new InMemoryEventPublisher<>();
    }

    @Bean
    public PaymentGateway paymentGateway() {
        return new InMemoryPaymentGateway();
    }

    @Bean
    public PaymentEventConsumer paymentEventConsumer(final Repository<Payment, UUID> paymentRepository,
                                                     final InMemoryEventPublisher<UUID> paymentPublisher) {
        final var result = new PaymentEventConsumer(paymentRepository);
        paymentPublisher.registerEventConsumer(result);
        return result;
    }

    @Bean
    public PaymentCommandContext paymentCommandContext(final Repository<Payment, UUID> paymentRepository,
                                                       final InMemoryEventPublisher<UUID> paymentPublisher,
                                                       final PaymentGateway paymentGateway) {
        return new PaymentCommandContext(paymentRepository, paymentPublisher, paymentGateway);
    }

    @Bean
    public CommandDispatcher<PaymentCommandContext, Payment, UUID> paymentDispatcher(
            final PaymentCommandContext context) {
        return new OccCommandDispatcher<>(context);
    }
}
