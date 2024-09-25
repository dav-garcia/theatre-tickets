package com.github.davgarcia.theatre.tickets.configuration;

import com.github.davgarcia.theatre.tickets.Customer;
import com.github.davgarcia.theatre.tickets.DiscountService;
import com.github.davgarcia.theatre.tickets.infra.event.EventPublisher;
import com.github.davgarcia.theatre.tickets.infra.event.inmemory.InMemoryEventPublisher;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;
import com.github.davgarcia.theatre.tickets.infra.repository.inmemory.DummyPlatformTransactionManager;
import com.github.davgarcia.theatre.tickets.infra.repository.inmemory.InMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class NotificationConfiguration {

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DummyPlatformTransactionManager();
    }

    @Bean
    public Repository<Customer, String> customerRepository() {
        return new InMemoryRepository<>();
    }

    @Bean
    public InMemoryEventPublisher<String> customerPublisher() {
        return new InMemoryEventPublisher<>();
    }

    @Bean
    public DiscountService discountService(
            final Repository<Customer, String> customerRepository,
            final EventPublisher<String> customerPublisher) {
        return new DiscountService(customerRepository, customerPublisher);
    }
}
