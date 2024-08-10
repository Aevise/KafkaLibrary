package pl.Aevise.Kafka_library_events_consumer.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.db.entity.LibraryEventEntity;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.db.jpa.LibraryEventsJpaRepository;

@Service
@Slf4j
public class LibraryEventsService {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private LibraryEventsJpaRepository libraryEventRepository;

    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        LibraryEventEntity libraryEventEntity = objectMapper.readValue(consumerRecord.value(), LibraryEventEntity.class);
        log.info("Library Event : {}", libraryEventEntity);

        switch (libraryEventEntity.getLibraryEventType()) {
            case NEW:
                save(libraryEventEntity);
                //save operation
                break;
            case UPDATE:
                //update operation
                break;
            default:
                log.warn("Invalid Library Event Type");
        }
    }

    //insert data into db
    private void save(LibraryEventEntity libraryEventEntity) {
        libraryEventEntity.getBook().setLibraryEventEntity(libraryEventEntity);
        libraryEventRepository.save(libraryEventEntity);
        log.info("Successfully persisted the library event : {}", libraryEventEntity);
    }
}
