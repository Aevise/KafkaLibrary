package pl.Aevise.Kafka_library_events_consumer.infrastructure.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.Aevise.Kafka_library_events_consumer.business.LibraryEventsService;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.configuration.utils.RecordStatus;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.db.entity.FailureRecordEntity;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.db.jpa.FailureRecordJpaRepository;

import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class RetryScheduler {

    private FailureRecordJpaRepository failureRecordJpaRepository;
    private LibraryEventsService libraryEventsService;

    @Scheduled(fixedRate = 10000)
    public void retryFailedRecords(){
        log.info("Retry Scheduler started working");
        List<FailureRecordEntity> failedRecordsToRetry = failureRecordJpaRepository .findAllByStatus(RecordStatus.RETRY.name());

        log.info("Amount of messages to retry: {}", failedRecordsToRetry.size());
        failedRecordsToRetry.forEach(
                failureRecordEntity -> {
                    var consumerRecord = buildConsumerRecord(failureRecordEntity);
                    log.info("Retry Failed Record: {}", failedRecordsToRetry);
                    try {
                        libraryEventsService.processLibraryEvent(consumerRecord);
                        failureRecordEntity.setStatus(RecordStatus.SUCCESS.name());
                    } catch (Exception e) {
                        log.error("Exception in retryFailedRecords : {}", e.getMessage(), e);
                    }
                }
        );

        failureRecordJpaRepository.saveAllAndFlush(failedRecordsToRetry);
        log.info("Retry Scheduler work ended");
    }

    private ConsumerRecord<Integer, String> buildConsumerRecord(FailureRecordEntity failureRecordEntity) {
        return new ConsumerRecord<>(
                failureRecordEntity.getTopic(),
                failureRecordEntity.getPartition(),
                failureRecordEntity.getOffset_value(),
                failureRecordEntity.getKey_value(),
                failureRecordEntity.getErrorRecord()
        );
    }

}
