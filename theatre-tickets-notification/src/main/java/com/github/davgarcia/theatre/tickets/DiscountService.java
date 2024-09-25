package com.github.davgarcia.theatre.tickets;

import com.github.davgarcia.theatre.tickets.error.PreconditionsViolatedException;
import com.github.davgarcia.theatre.tickets.event.DiscountGrantedEvent;
import com.github.davgarcia.theatre.tickets.infra.event.EventPublisher;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Transactional
public class DiscountService {

    private final Repository<Customer, String> customerRepository;
    private final EventPublisher<String> customerPublisher;

    public DiscountService(final Repository<Customer, String> customerRepository, final EventPublisher<String> customerPublisher) {
        this.customerRepository = customerRepository;
        this.customerPublisher = customerPublisher;
    }

    public UUID grantDiscount(final String email, final String description, final int amount,
                              final LocalDate validFrom, final LocalDate validUntil) {
        final var discountId = UUID.randomUUID();
        final var customer = customerRepository.load(email)
                .orElseThrow(() -> new PreconditionsViolatedException("Customer doesn't exist: " + email));
        final var discount = Discount.builder()
                .id(discountId)
                .description(description)
                .amount(amount)
                .validFrom(validFrom)
                .validUntil(validUntil)
                .build();

        customer.getDiscounts().add(discount);
        customerRepository.save(customer);
        customerPublisher.tryPublish(customer.getVersion(), new DiscountGrantedEvent(email, discountId));

        return discountId;
    }

    public Optional<Discount> getDiscount(final String email, final UUID discountId) {
        final var customer = customerRepository.load(email)
                .orElseThrow(() -> new PreconditionsViolatedException("Customer doesn't exist: " + email));
        return customer.getDiscounts().stream()
                .filter(d -> d.getId().equals(discountId))
                .findAny();
    }
}
