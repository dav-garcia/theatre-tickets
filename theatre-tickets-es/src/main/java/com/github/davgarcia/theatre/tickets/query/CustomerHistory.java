package com.github.davgarcia.theatre.tickets.query;

import com.github.davgarcia.theatre.tickets.event.payment.Item;
import com.github.davgarcia.theatre.tickets.event.performance.Seat;
import com.github.davgarcia.theatre.tickets.infra.Entity;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder(builderClassName = "Builder")
public class CustomerHistory implements Entity<String> {

    @Value
    public static class Discount {
        UUID id;
        String description;
        int amount;
        LocalDate validFrom;
        LocalDate validUntil;
    }

    @Data
    @lombok.Builder(builderClassName = "Builder")
    public static class Ticket {

        public enum Status {
            CREATED,
            CONFIRMED,
            PAID,
            ABANDONED,
            CANCELLED
        }

        private final UUID id;
        private final Set<Seat> seats;
        private final List<Discount> discounts;
        private UUID payment;
        private List<Item> items;
        private Status status;
    }

    private final String id;
    private String name;
    private boolean subscribed;
    private final List<Discount> discounts;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<UUID, Ticket> tickets;

    public void applyDiscounts(final UUID ticketId, final List<UUID> discountIds) {
        final var ticket = tickets.get(ticketId);
        final var applied = discounts.stream()
                .filter(d -> discountIds.contains(d.getId()))
                .toList();
        discounts.removeAll(applied);
        ticket.discounts.addAll(applied);
    }

    public void recoverDiscounts(final UUID ticketId, final List<UUID> discountIds) {
        final var ticket = tickets.get(ticketId);
        final var recovered = ticket.discounts.stream()
                .filter(d -> discountIds.contains(d.getId()))
                .toList();
        ticket.discounts.removeAll(recovered);
        discounts.addAll(recovered);
    }

    public void addTicket(final Ticket ticket) {
        tickets.put(ticket.getId(), ticket);
    }

    public Ticket getTicket(final UUID ticketId) {
        return tickets.get(ticketId);
    }

    public boolean containsTicket(final UUID ticketId) {
        return tickets.containsKey(ticketId);
    }
}
