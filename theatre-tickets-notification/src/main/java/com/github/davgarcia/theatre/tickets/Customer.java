package com.github.davgarcia.theatre.tickets;

import com.github.davgarcia.theatre.tickets.infra.AggregateRoot;
import lombok.*;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder(builderClassName = "Builder")
public class Customer implements AggregateRoot<String> {

    private final String id;
    private long version;
    private boolean subscribed;
    private final List<Discount> discounts;

    @NonNull
    public List<Discount> getApplicableDiscounts(final LocalDate when, final int maxAmount) {
        final var applicable = new ArrayList<Discount>(discounts.size());

        int remaining = maxAmount;
        for (final Discount discount : discounts) {
            if (!discount.getValidFrom().isAfter(when) &&
                    !discount.getValidUntil().isBefore(when) &&
                remaining - discount.getAmount() >= 0) {
                applicable.add(discount);
                remaining -= discount.getAmount();
            }
        }

        return applicable;
    }

    @NonNull
    public List<Discount> getAppliedDiscounts(final UUID ticket) {
        return discounts.stream()
                .filter(d -> Objects.equals(d.getAppliedToTicket(), ticket))
                .collect(Collectors.toList());
    }
}
