package com.github.davgarcia.theatre.tickets.infra.repository.inmemory;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

public class DummyPlatformTransactionManager extends AbstractPlatformTransactionManager {

    @Override
    protected Object doGetTransaction() {
        return null;
    }

    @Override
    protected void doBegin(final Object transaction, final TransactionDefinition definition) {
        // Empty
    }

    @Override
    protected void doCommit(final DefaultTransactionStatus status) {
        // Empty
    }

    @Override
    protected void doRollback(final DefaultTransactionStatus status) {
        // Empty
    }
}
