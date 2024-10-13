package com.github.davgarcia.theatre.tickets.command;

import com.github.davgarcia.theatre.tickets.Seat;
import com.github.davgarcia.theatre.tickets.Performance;
import com.github.davgarcia.theatre.tickets.Ticket;
import com.github.davgarcia.theatre.tickets.error.PreconditionsViolatedException;
import com.github.davgarcia.theatre.tickets.event.TicketCreatedEvent;
import com.github.davgarcia.theatre.tickets.infra.event.EventPublisher;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Transactional
public class PerformanceCommandService {

    private final Repository<Performance, UUID> performanceRepository;
    private final Repository<Ticket, UUID> ticketRepository;
    private final EventPublisher<UUID> ticketPublisher;

    public PerformanceCommandService(final Repository<Performance, UUID> performanceRepository,
                                     final Repository<Ticket, UUID> ticketRepository,
                                     final EventPublisher<UUID> ticketPublisher) {
        this.performanceRepository = performanceRepository;
        this.ticketRepository = ticketRepository;
        this.ticketPublisher = ticketPublisher;
    }

    public UUID selectSeats(final UUID performanceId, final Set<Seat> seats, final String email) {
        final var performance = performanceRepository.load(performanceId)
                .filter(r -> r.getAvailableSeats().containsAll(seats))
                .orElseThrow(() -> new PreconditionsViolatedException("Performance doesn't exist or selected seats not available"));
        final var ticketId = UUID.randomUUID();

        performance.setVersion(performance.getVersion() + 1);
        performance.getAvailableSeats().removeAll(seats);

        performanceRepository.save(performance);
        ticketRepository.save(Ticket.builder()
                .id(ticketId)
                .version(1L)
                .performance(performanceId)
                .seats(seats)
                .customer(email)
                .build());
        // Double-write!
        ticketPublisher.tryPublish(0L, new TicketCreatedEvent(ticketId, performanceId, seats, email));

        return ticketId;
    }
}
