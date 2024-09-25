package com.github.davgarcia.theatre.tickets.infra;

import org.springframework.lang.NonNull;

public interface Entity<T> {

    @NonNull
    T getId();
}
