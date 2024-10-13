package com.github.davgarcia.theatre.tickets.configuration;

import com.github.davgarcia.theatre.tickets.command.customer.CustomerCommandContext;
import com.github.davgarcia.theatre.tickets.command.payment.PaymentCommandContext;
import com.github.davgarcia.theatre.tickets.command.performance.PerformanceCommandContext;
import com.github.davgarcia.theatre.tickets.command.ticket.TicketCommandContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void givenFullConfigurationThenDependenciesInjected() {
        contextRunner.withUserConfiguration(
                PerformanceConfiguration.class,
                TicketConfiguration.class,
                CustomerConfiguration.class,
                PaymentConfiguration.class,
                SagaConfiguration.class,
                CustomerHistoryConfiguration.class).run(context -> {
            assertPerformancePublisher(context);
            assertPerformanceDispatcher(context);
            assertTicketPublisher(context);
            assertTicketDispatcher(context);
            assertCustomerPublisher(context);
            assertCustomerDispatcher(context);
            assertPaymentPublisher(context);
            assertPaymentDispatcher(context);
        });
    }

    private void assertPerformancePublisher(final AssertableApplicationContext context) {
        assertThat(context).getBean("performancePublisher")
                .hasFieldOrPropertyWithValue("eventConsumers", Set.of(
                        context.getBean("performanceEventConsumer"),
                        context.getBean("performanceSaga"),
                        context.getBean("customerHistoryEventConsumer")));
    }

    private void assertPerformanceDispatcher(final AssertableApplicationContext context) {
        assertThat(context).getBean("performanceDispatcher")
                .extracting("context").isOfAnyClassIn(PerformanceCommandContext.class);
    }

    private void assertTicketPublisher(final AssertableApplicationContext context) {
        assertThat(context).getBean("ticketPublisher")
                .hasFieldOrPropertyWithValue("eventConsumers", Set.of(
                        context.getBean("ticketEventConsumer"),
                        context.getBean("ticketSaga"),
                        context.getBean("customerHistoryEventConsumer")));
    }

    private void assertTicketDispatcher(final AssertableApplicationContext context) {
        assertThat(context).getBean("ticketDispatcher")
                .extracting("context").isOfAnyClassIn(TicketCommandContext.class);
    }

    private void assertCustomerPublisher(final AssertableApplicationContext context) {
        assertThat(context).getBean("customerPublisher")
                .hasFieldOrPropertyWithValue("eventConsumers", Set.of(
                        context.getBean("customerEventConsumer"),
                        context.getBean("customerSaga"),
                        context.getBean("customerHistoryEventConsumer")));
    }

    private void assertCustomerDispatcher(final AssertableApplicationContext context) {
        assertThat(context).getBean("customerDispatcher")
                .extracting("context").isOfAnyClassIn(CustomerCommandContext.class);
    }

    private void assertPaymentPublisher(final AssertableApplicationContext context) {
        assertThat(context).getBean("paymentPublisher")
                .hasFieldOrPropertyWithValue("eventConsumers", Set.of(
                        context.getBean("paymentEventConsumer"),
                        context.getBean("paymentSaga"),
                        context.getBean("customerHistoryEventConsumer")));
    }

    private void assertPaymentDispatcher(final AssertableApplicationContext context) {
        assertThat(context).getBean("paymentDispatcher")
                .extracting("context").isOfAnyClassIn(PaymentCommandContext.class);
    }
}
