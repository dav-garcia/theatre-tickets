package com.github.davgarcia.theatre.tickets.command;

import com.github.davgarcia.theatre.tickets.Seat;
import com.github.davgarcia.theatre.tickets.Performance;
import com.github.davgarcia.theatre.tickets.Booking;
import com.github.davgarcia.theatre.tickets.Auditorium;
import com.github.davgarcia.theatre.tickets.configuration.EcstConfiguration;
import com.github.davgarcia.theatre.tickets.event.BookingCreatedEvent;
import com.github.davgarcia.theatre.tickets.infra.event.EventConsumer;
import com.github.davgarcia.theatre.tickets.infra.event.inmemory.InMemoryEventPublisher;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = EcstConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PerformanceCommandServiceTest {

    private static final Seat A1 = new Seat("A", 1);
    private static final Seat A2 = new Seat("A", 2);
    private static final Seat A3 = new Seat("A", 3);
    private static final Seat B1 = new Seat("B", 1);
    private static final Seat B2 = new Seat("B", 2);
    private static final Seat B3 = new Seat("B", 3);
    private static final Auditorium AUDITORIUM = new Auditorium("MAIN", Set.of(A1, A2, A3, B1, B2, B3));

    @Autowired
    private Repository<Performance, UUID> performanceRepository;

    @Autowired
    private Repository<Booking, UUID> bookingRepository;

    @Autowired
    private InMemoryEventPublisher<UUID> bookingPublisher;

    @Autowired
    private PerformanceCommandService sut;

    @MockBean
    private EventConsumer<UUID> bookingEventConsumer;

    @Test
    void givenPerformance_whenSelectSeats_thenBookingCreated() {
        final var performanceId = UUID.randomUUID();
        final var customerId = RandomStringUtils.randomAlphabetic(10) + "@email.com";

        performanceRepository.save(Performance.builder()
                .id(performanceId)
                .version(1L)
                .when(ZonedDateTime.now())
                .where(AUDITORIUM)
                .availableSeats(new HashSet<>(AUDITORIUM.getSeats()))
                .build());
        bookingPublisher.registerEventConsumer(bookingEventConsumer);

        final var bookingId = sut.selectSeats(performanceId, Set.of(A1, A2, B3), customerId);

        final var eventCaptor = ArgumentCaptor.forClass(BookingCreatedEvent.class);
        verify(bookingEventConsumer).consume(eq(1L), eventCaptor.capture());
        await().atMost(2, TimeUnit.SECONDS).until(() -> eventCaptor.getValue() != null);

        final var performance = performanceRepository.load(performanceId).orElseThrow();
        assertThat(performance.getVersion()).isEqualTo(2L);
        assertThat(performance.getAvailableSeats()).containsExactlyInAnyOrder(A3, B1, B2);
        assertThat(bookingRepository.load(bookingId).orElseThrow()).isEqualTo(Booking.builder()
                .id(bookingId)
                .version(1L)
                .performance(performanceId)
                .seats(Set.of(A1, A2, B3))
                .customer(customerId)
                .build());
        assertThat(eventCaptor.getValue()).isEqualTo(
                new BookingCreatedEvent(bookingId, performanceId, Set.of(A1, A2, B3), customerId));
    }
}
