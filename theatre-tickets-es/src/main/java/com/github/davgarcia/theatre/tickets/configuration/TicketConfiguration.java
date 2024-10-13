package com.github.davgarcia.theatre.tickets.configuration;

import com.github.davgarcia.theatre.tickets.command.ticket.Ticket;
import com.github.davgarcia.theatre.tickets.command.ticket.TicketCommandContext;
import com.github.davgarcia.theatre.tickets.command.ticket.TicketEventConsumer;
import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandDispatcher;
import com.github.davgarcia.theatre.tickets.infra.dispatch.occ.OccCommandDispatcher;
import com.github.davgarcia.theatre.tickets.infra.event.inmemory.InMemoryEventPublisher;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;
import com.github.davgarcia.theatre.tickets.infra.repository.inmemory.InMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class TicketConfiguration {

    @Bean
    public Repository<Ticket, UUID> ticketRepository() {
        return new InMemoryRepository<>();
    }

    @Bean
    public InMemoryEventPublisher<UUID> ticketPublisher() {
        return new InMemoryEventPublisher<>();
    }

    @Bean
    public TicketEventConsumer ticketEventConsumer(final Repository<Ticket, UUID> ticketRepository,
                                                    final InMemoryEventPublisher<UUID> ticketPublisher) {
        final var result = new TicketEventConsumer(ticketRepository);
        ticketPublisher.registerEventConsumer(result);
        return result;
    }

    @Bean
    public TicketCommandContext ticketCommandContext(final Repository<Ticket, UUID> ticketRepository,
                                                      final InMemoryEventPublisher<UUID> ticketPublisher) {
        return new TicketCommandContext(ticketRepository, ticketPublisher);
    }

    @Bean
    public CommandDispatcher<TicketCommandContext, Ticket, UUID> ticketDispatcher(
            final TicketCommandContext context) {
        return new OccCommandDispatcher<>(context);
    }
}
