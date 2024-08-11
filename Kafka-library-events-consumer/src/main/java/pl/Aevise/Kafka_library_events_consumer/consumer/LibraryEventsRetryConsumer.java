package pl.Aevise.Kafka_library_events_consumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.Aevise.Kafka_library_events_consumer.business.LibraryEventsService;

@Component
@Slf4j
@AllArgsConstructor
public class LibraryEventsRetryConsumer {

    private LibraryEventsService libraryEventsService;
    @KafkaListener(
            topics = {"${topics.retry}"},
            autoStartup = "${retryListener.startup:true}",
            groupId = "retry-listener-group"
    )
    public void consumeRecord(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        log.info("Consumer Record in Retry Listener: {}", consumerRecord);
        consumerRecord.headers()
                        .forEach(header ->
                                log.info("Key: {} , Value: {}", header.key(), new String(header.value())));
        libraryEventsService.processLibraryEvent(consumerRecord);
    }
}
