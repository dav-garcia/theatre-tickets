package com.github.davgarcia.theatre.tickets.configuration;

import com.github.davgarcia.theatre.tickets.command.customer.Customer;
import com.github.davgarcia.theatre.tickets.command.customer.CustomerCommandContext;
import com.github.davgarcia.theatre.tickets.command.customer.CustomerEventConsumer;
import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandDispatcher;
import com.github.davgarcia.theatre.tickets.infra.dispatch.occ.OccCommandDispatcher;
import com.github.davgarcia.theatre.tickets.infra.event.inmemory.InMemoryEventPublisher;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;
import com.github.davgarcia.theatre.tickets.infra.repository.inmemory.InMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerConfiguration {

    @Bean
    public Repository<Customer, String> customerRepository() {
        return new InMemoryRepository<>();
    }

    @Bean
    public InMemoryEventPublisher<String> customerPublisher() {
        return new InMemoryEventPublisher<>();
    }

    @Bean
    public CustomerEventConsumer customerEventConsumer(final Repository<Customer, String> customerRepository,
                                                       final InMemoryEventPublisher<String> customerPublisher) {
        final var result = new CustomerEventConsumer(customerRepository);
        customerPublisher.registerEventConsumer(result);
        return result;
    }

    @Bean
    public CustomerCommandContext customerCommandContext(final Repository<Customer, String> customerRepository,
                                                         final InMemoryEventPublisher<String> customerPublisher) {
        return new CustomerCommandContext(customerRepository, customerPublisher);
    }

    @Bean
    public CommandDispatcher<CustomerCommandContext, Customer, String> customerDispatcher(
            final CustomerCommandContext context) {
        return new OccCommandDispatcher<>(context);
    }
}
