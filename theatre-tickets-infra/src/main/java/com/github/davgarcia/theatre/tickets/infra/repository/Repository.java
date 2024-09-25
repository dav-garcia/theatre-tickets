package com.github.davgarcia.theatre.tickets.infra.repository;

import com.github.davgarcia.theatre.tickets.infra.Entity;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Persistent repository for aggregates' projections.
 */
public interface Repository<T extends Entity<U>, U> {

    /**
     * Stores an instance (new or updated).
     *
     * @param instance Instance to be stored.
     */
    void save(final T instance);

    /**
     * Loads an instance, if it exists.
     *
     * @param id Instance id.
     * @return Instance or empty if it doesn't exist.
     */
    @NonNull
    Optional<T> load(final U id);

    /**
     * Finds and returns instances passing the given filter.
     *
     * @param filter Filter that must be met by returned instances.
     * @return Instances that pass the filter.
     */
    @NonNull
    List<T> find(final Predicate<T> filter);

    /**
     * Deletes an instance.
     *
     * @param id Instance id.
     */
    void delete(final U id);
}
