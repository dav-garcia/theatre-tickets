package com.github.davgarcia.theatre.tickets.command.customer;

import com.github.davgarcia.theatre.tickets.infra.dispatch.CommandContext;
import com.github.davgarcia.theatre.tickets.infra.event.EventPublisher;
import com.github.davgarcia.theatre.tickets.infra.repository.Repository;

public class CustomerCommandContext extends CommandContext<Customer, String> {

    public CustomerCommandContext(final Repository<Customer, String> repository,
                                  final EventPublisher<String> eventPublisher) {
        super(repository, eventPublisher);
    }
}
