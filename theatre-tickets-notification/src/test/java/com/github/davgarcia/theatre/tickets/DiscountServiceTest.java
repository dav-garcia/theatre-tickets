package com.github.davgarcia.theatre.tickets;

import com.github.davgarcia.theatre.tickets.configuration.NotificationConfiguration;
import com.github.davgarcia.theatre.tickets.event.DiscountGrantedEvent;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = NotificationConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class DiscountServiceTest {

    @Autowired
    private Repository<Customer, String> customerRepository;

    @Autowired
    private InMemoryEventPublisher<String> customerPublisher;

    @Autowired
    private DiscountService sut;

    @MockBean
    private EventConsumer<String> customerEventConsumer;

    @Test
    void givenCustomer_whenDiscountGranted_thenGetDiscount() {
        final var email = RandomStringUtils.randomAlphabetic(10) + "@email.com";
        final var anotherDiscountId = UUID.randomUUID();
        final var description = RandomStringUtils.randomAlphabetic(10);
        final var amount = 5;
        final var validFrom = LocalDate.now();
        final var validUntil = validFrom.plusDays(10);

        customerRepository.save(Customer.builder()
                .id(email)
                .version(1L)
                .discounts(new ArrayList<>(List.of(Discount.builder()
                        .id(anotherDiscountId)
                        .build())))
                .build());
        customerPublisher.registerEventConsumer(customerEventConsumer);

        // Invoked from a marketing campaign process, for example.
        final var discountId = sut.grantDiscount(email, description, amount, validFrom, validUntil);

        // Wait for the notification
        final var eventCaptor = ArgumentCaptor.forClass(DiscountGrantedEvent.class);
        verify(customerEventConsumer).consume(eq(2L), eventCaptor.capture());
        await().atMost(2, TimeUnit.SECONDS).until(() -> eventCaptor.getValue() != null);

        // Invoked by the notification receiver
        final var discount = sut.getDiscount(email, discountId).orElseThrow();

        final var expectedDiscount = Discount.builder()
                .id(discountId)
                .description(description)
                .amount(amount)
                .validFrom(validFrom)
                .validUntil(validUntil)
                .build();
        assertThat(eventCaptor.getValue().getAggregateRootId()).isEqualTo(email);
        assertThat(eventCaptor.getValue().getId()).isEqualTo(discountId);
        assertThat(discount).isEqualTo(expectedDiscount);
        assertThat(customerRepository.load(email).orElseThrow().getDiscounts()).containsExactly(
                Discount.builder().id(anotherDiscountId).build(),
                expectedDiscount);
    }
}
