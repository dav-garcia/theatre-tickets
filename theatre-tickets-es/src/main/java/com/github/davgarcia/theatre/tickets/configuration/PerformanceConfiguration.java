package com.github.davgarcia.theatre.tickets.configuration;

import com.github.davgarcia.theatre.tickets.command.performance.Performance;
import com.github.davgarcia.theatre.tickets.command.performance.PerformanceCommandContext;
import com.github.davgarcia.theatre.tickets.command.performance.PerformanceEventConsumer;
import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandDispatcher;
import com.github.davgarcia.theatre.tickets.infra.dispatch.occ.OccCommandDispatcher;
import com.github.davgarcia.theatre.tickets.infra.event.inmemory.InMemoryEventPublisher;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;
import com.github.davgarcia.theatre.tickets.infra.repository.inmemory.InMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class PerformanceConfiguration {

    @Bean
    public Repository<Performance, UUID> performanceRepository() {
        return new InMemoryRepository<>();
    }

    @Bean
    public InMemoryEventPublisher<UUID> performancePublisher() {
        return new InMemoryEventPublisher<>();
    }

    @Bean
    public PerformanceEventConsumer performanceEventConsumer(final Repository<Performance, UUID> performanceRepository,
                                                             final InMemoryEventPublisher<UUID> performancePublisher) {
        final var result = new PerformanceEventConsumer(performanceRepository);
        performancePublisher.registerEventConsumer(result);
        return result;
    }

    @Bean
    public PerformanceCommandContext performanceCommandContext(final Repository<Performance, UUID> performanceRepository,
                                                               final InMemoryEventPublisher<UUID> performancePublisher) {
        return new PerformanceCommandContext(performanceRepository, performancePublisher);
    }

    @Bean
    public CommandDispatcher<PerformanceCommandContext, Performance, UUID> performanceDispatcher(
            final PerformanceCommandContext context) {
        return new OccCommandDispatcher<>(context);
    }
}
