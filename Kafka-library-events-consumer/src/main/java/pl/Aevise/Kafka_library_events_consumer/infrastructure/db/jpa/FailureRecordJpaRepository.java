package pl.Aevise.Kafka_library_events_consumer.infrastructure.db.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.db.entity.FailureRecordEntity;

import java.util.List;

public interface FailureRecordJpaRepository extends JpaRepository<FailureRecordEntity, Integer> {
    List<FailureRecordEntity> findAllByStatus(String status);
}
