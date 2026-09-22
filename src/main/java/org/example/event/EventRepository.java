package org.example.event;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for event aggregates.
 *
 * <p>CRUD methods are inherited from {@link JpaRepository}; no custom query is
 * required because result calculation is intentionally handled by the service.</p>
 */
 
public interface EventRepository extends JpaRepository<Event, Long> {
}
