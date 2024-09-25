package com.github.davgarcia.theatre.tickets.command.customer;

import com.github.davgarcia.theatre.tickets.event.customer.EmailRegisteredEvent;
import com.github.davgarcia.theatre.tickets.infra.Command;
import lombok.Value;

@Value
public class RegisterEmailCommand implements Command<CustomerCommandContext, Customer, String> {

    String aggregateRootId;

    @Override
    public void execute(final CustomerCommandContext context) {
        if (context.getRepository().load(aggregateRootId).isEmpty()) { // No hace nada si email ya registrado
            context.getEventPublisher().tryPublish(0L,
                    new EmailRegisteredEvent(aggregateRootId));
        }
    }
}
