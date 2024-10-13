package com.github.davgarcia.theatre.tickets.configuration;

import com.github.davgarcia.theatre.tickets.Performance;
import com.github.davgarcia.theatre.tickets.Ticket;
import com.github.davgarcia.theatre.tickets.command.PerformanceCommandService;
import com.github.davgarcia.theatre.tickets.infra.event.EventPublisher;
import com.github.davgarcia.theatre.tickets.infra.event.inmemory.InMemoryEventPublisher;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;
import com.github.davgarcia.theatre.tickets.infra.repository.inmemory.DummyPlatformTransactionManager;
import com.github.davgarcia.theatre.tickets.infra.repository.inmemory.InMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.UUID;

@Configuration
@EnableTransactionManagement
public class EcstConfiguration {

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DummyPlatformTransactionManager();
    }

    @Bean
    public Repository<Performance, UUID> performanceRepository() {
        return new InMemoryRepository<>();
    }

    @Bean
    public Repository<Ticket, UUID> ticketRepository() {
        return new InMemoryRepository<>();
    }

    @Bean
    public InMemoryEventPublisher<UUID> ticketPublisher() {
        return new InMemoryEventPublisher<>();
    }

    @Bean
    public PerformanceCommandService performanceCommandService(
            final Repository<Performance, UUID> performanceRepository,
            final Repository<Ticket, UUID> ticketRepository,
            final EventPublisher<UUID> ticketPublisher) {
        return new PerformanceCommandService(performanceRepository, ticketRepository, ticketPublisher);
    }
}
