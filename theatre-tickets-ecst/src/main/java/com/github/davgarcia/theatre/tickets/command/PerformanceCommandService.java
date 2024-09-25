package com.github.davgarcia.theatre.tickets.command;

import com.github.davgarcia.theatre.tickets.Seat;
import com.github.davgarcia.theatre.tickets.Performance;
import com.github.davgarcia.theatre.tickets.Booking;
import com.github.davgarcia.theatre.tickets.error.PreconditionsViolatedException;
import com.github.davgarcia.theatre.tickets.event.BookingCreatedEvent;
import com.github.davgarcia.theatre.tickets.infra.event.EventPublisher;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Transactional
public class PerformanceCommandService {

    private final Repository<Performance, UUID> performanceRepository;
    private final Repository<Booking, UUID> bookingRepository;
    private final EventPublisher<UUID> bookingPublisher;

    public PerformanceCommandService(final Repository<Performance, UUID> performanceRepository,
                                     final Repository<Booking, UUID> bookingRepository,
                                     final EventPublisher<UUID> bookingPublisher) {
        this.performanceRepository = performanceRepository;
        this.bookingRepository = bookingRepository;
        this.bookingPublisher = bookingPublisher;
    }

    public UUID selectSeats(final UUID performanceId, final Set<Seat> seats, final String email) {
        final var performance = performanceRepository.load(performanceId)
                .filter(r -> r.getAvailableSeats().containsAll(seats))
                .orElseThrow(() -> new PreconditionsViolatedException("Performance doesn't exist or selected seats not available"));
        final var bookingId = UUID.randomUUID();

        performance.setVersion(performance.getVersion() + 1);
        performance.getAvailableSeats().removeAll(seats);

        performanceRepository.save(performance);
        bookingRepository.save(Booking.builder()
                .id(bookingId)
                .version(1L)
                .performance(performanceId)
                .seats(seats)
                .customer(email)
                .build());
        // Double-write!
        bookingPublisher.tryPublish(0L, new BookingCreatedEvent(bookingId, performanceId, seats, email));

        return bookingId;
    }
}
