package com.github.davgarcia.theatre.tickets.configuration;

import com.github.davgarcia.theatre.tickets.command.booking.Booking;
import com.github.davgarcia.theatre.tickets.command.booking.BookingCommandContext;
import com.github.davgarcia.theatre.tickets.command.booking.BookingEventConsumer;
import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandDispatcher;
import com.github.davgarcia.theatre.tickets.infra.dispatch.occ.OccCommandDispatcher;
import com.github.davgarcia.theatre.tickets.infra.event.inmemory.InMemoryEventPublisher;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;
import com.github.davgarcia.theatre.tickets.infra.repository.inmemory.InMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class BookingConfiguration {

    @Bean
    public Repository<Booking, UUID> bookingRepository() {
        return new InMemoryRepository<>();
    }

    @Bean
    public InMemoryEventPublisher<UUID> bookingPublisher() {
        return new InMemoryEventPublisher<>();
    }

    @Bean
    public BookingEventConsumer bookingEventConsumer(final Repository<Booking, UUID> bookingRepository,
                                                     final InMemoryEventPublisher<UUID> bookingPublisher) {
        final var result = new BookingEventConsumer(bookingRepository);
        bookingPublisher.registerEventConsumer(result);
        return result;
    }

    @Bean
    public BookingCommandContext bookingCommandContext(final Repository<Booking, UUID> bookingRepository,
                                                       final InMemoryEventPublisher<UUID> bookingPublisher) {
        return new BookingCommandContext(bookingRepository, bookingPublisher);
    }

    @Bean
    public CommandDispatcher<BookingCommandContext, Booking, UUID> bookingDispatcher(
            final BookingCommandContext context) {
        return new OccCommandDispatcher<>(context);
    }
}
