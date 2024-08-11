package pl.Aevise.Kafka_library_events_consumer.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Service;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.db.entity.LibraryEventEntity;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.db.jpa.LibraryEventsJpaRepository;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class LibraryEventsService {

    ObjectMapper objectMapper;
    private LibraryEventsJpaRepository libraryEventRepository;

    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        LibraryEventEntity libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEventEntity.class);
        log.info("Library Event : {}", libraryEvent);

        if(libraryEvent != null && (libraryEvent.getLibraryEventId() != null && libraryEvent.getLibraryEventId() == 999)){
            throw new RecoverableDataAccessException("Temporary Network Issue");
        }

        switch (Objects.requireNonNull(libraryEvent).getLibraryEventType()) {
            case NEW:
                save(libraryEvent);
                //save operation
                break;
            case UPDATE:
                //validate
                validate(libraryEvent);
                save(libraryEvent);
                //update operation
                break;
            default:
                log.warn("Invalid Library Event Type");
        }
    }

    private void validate(LibraryEventEntity libraryEventEntity) {
        Integer libraryEventId = libraryEventEntity.getLibraryEventId();

        if(libraryEventId == null){
            throw new IllegalArgumentException("Library Event Id can not be null");
        }
        Optional<LibraryEventEntity> libraryEvent = libraryEventRepository.findById(libraryEventEntity.getLibraryEventId());
        if(libraryEvent.isEmpty()){
            throw new IllegalArgumentException("Not a valid library Event");
        }
        log.info("Validation is successful for the library Event : {}", libraryEvent.get());
    }

    //insert data into db
    private void save(LibraryEventEntity libraryEventEntity) {
        libraryEventEntity.getBook().setLibraryEventEntity(libraryEventEntity);
        libraryEventRepository.save(libraryEventEntity);
        log.info("Successfully persisted the library event : {}", libraryEventEntity);
    }
}
