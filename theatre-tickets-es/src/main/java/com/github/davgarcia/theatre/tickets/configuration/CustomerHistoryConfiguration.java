package com.github.davgarcia.theatre.tickets.configuration;

import com.github.davgarcia.theatre.tickets.infra.event.EventConsumer;
import com.github.davgarcia.theatre.tickets.infra.event.inmemory.InMemoryEventPublisher;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;
import com.github.davgarcia.theatre.tickets.infra.repository.inmemory.InMemoryRepository;
import com.github.davgarcia.theatre.tickets.query.CustomerHistory;
import com.github.davgarcia.theatre.tickets.query.CustomerHistoryEventConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class CustomerHistoryConfiguration {

    @Bean
    public Repository<CustomerHistory, String> customerHistoryRepository() {
        return new InMemoryRepository<>();
    }

    @Bean
    public CustomerHistoryEventConsumer customerHistoryEventConsumer(final Repository<CustomerHistory, String> customerHistoryRepository,
                                                                     final InMemoryEventPublisher<UUID> performancePublisher,
                                                                     final InMemoryEventPublisher<UUID> ticketPublisher,
                                                                     final InMemoryEventPublisher<String> customerPublisher,
                                                                     final InMemoryEventPublisher<UUID> paymentPublisher) {
        final var result = new CustomerHistoryEventConsumer(customerHistoryRepository);
        performancePublisher.registerEventConsumer((EventConsumer) result);
        ticketPublisher.registerEventConsumer((EventConsumer) result);
        customerPublisher.registerEventConsumer((EventConsumer) result);
        paymentPublisher.registerEventConsumer((EventConsumer) result);
        return result;
    }
}
