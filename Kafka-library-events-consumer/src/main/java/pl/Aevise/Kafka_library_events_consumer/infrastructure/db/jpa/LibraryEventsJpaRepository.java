package pl.Aevise.Kafka_library_events_consumer.infrastructure.db.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.db.entity.LibraryEventEntity;

public interface LibraryEventsJpaRepository extends JpaRepository<LibraryEventEntity, Integer> {

}
