package com.github.davgarcia.theatre.tickets.configuration;

import com.github.davgarcia.theatre.tickets.Performance;
import com.github.davgarcia.theatre.tickets.Booking;
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
    public Repository<Booking, UUID> bookingRepository() {
        return new InMemoryRepository<>();
    }

    @Bean
    public InMemoryEventPublisher<UUID> bookingPublisher() {
        return new InMemoryEventPublisher<>();
    }

    @Bean
    public PerformanceCommandService performanceCommandService(
            final Repository<Performance, UUID> performanceRepository,
            final Repository<Booking, UUID> bookingRepository,
            final EventPublisher<UUID> bookingPublisher) {
        return new PerformanceCommandService(performanceRepository, bookingRepository, bookingPublisher);
    }
}
