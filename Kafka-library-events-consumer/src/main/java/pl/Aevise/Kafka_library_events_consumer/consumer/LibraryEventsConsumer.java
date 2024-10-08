package pl.Aevise.Kafka_library_events_consumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.Aevise.Kafka_library_events_consumer.business.LibraryEventsService;

@Component
@Slf4j
@AllArgsConstructor
public class LibraryEventsConsumer {
    private LibraryEventsService libraryEventsService;

    @KafkaListener(
            topics = {"library-events"},
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeRecord(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        log.info("Consumer Record: {}", consumerRecord);
        libraryEventsService.processLibraryEvent(consumerRecord);

    }
}
