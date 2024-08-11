package pl.Aevise.Kafka_library_events_consumer.business;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.db.entity.FailureRecordEntity;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.db.jpa.FailureRecordJpaRepository;

@Service
@Slf4j
@AllArgsConstructor
public class FailureService {

    private FailureRecordJpaRepository failureRecordJpaRepository;

    private static FailureRecordEntity buildFailureRecord(ConsumerRecord<Integer, String> consumerRecord, Exception e, String status) {
        return FailureRecordEntity.builder()
                .id(null)
                .topic(consumerRecord.topic())
                .key_value(consumerRecord.key())
                .errorRecord(consumerRecord.value())
                .partition(consumerRecord.partition())
                .offset_value(consumerRecord.offset())
                .exception(e.getCause().getMessage())
                .status(status)
                .build();
    }

    public void saveFailedRecord(ConsumerRecord<Integer, String> consumerRecord, Exception e, String status) {
        FailureRecordEntity failureRecord = buildFailureRecord(consumerRecord, e, status);

        failureRecordJpaRepository.save(failureRecord);

    }
}
