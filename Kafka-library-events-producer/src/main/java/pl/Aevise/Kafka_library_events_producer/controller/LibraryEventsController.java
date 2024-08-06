package pl.Aevise.Kafka_library_events_producer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.Aevise.Kafka_library_events_producer.controller.exception.InvalidBookData;
import pl.Aevise.Kafka_library_events_producer.domain.LibraryEvent;
import pl.Aevise.Kafka_library_events_producer.domain.LibraryEventType;
import pl.Aevise.Kafka_library_events_producer.producer.LibraryEventsProducer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
@AllArgsConstructor
public class LibraryEventsController {
    public static final String LIBRARY_EVENT_SYNC = "/v1/sync/libraryEvent";
    public static final String LIBRARY_EVENT_ASYNC = "/v1/async/libraryEvent";
    private final LibraryEventsProducer libraryEventsProducer;

    private static void validateLibraryUpdateEvent(LibraryEvent libraryEvent) {
        if (libraryEvent.libraryEventId() == null) {
            throw new InvalidBookData("Library event Id must not be null");
        }

        if (!libraryEvent.libraryEventType().equals(LibraryEventType.UPDATE)) {
            throw new InvalidBookData("Library event type must be of type: " + LibraryEventType.UPDATE);
        }
    }

    @PostMapping(LIBRARY_EVENT_ASYNC)
    public ResponseEntity<LibraryEvent> postLibraryEventAsync(
            @Valid @RequestBody LibraryEvent libraryEvent
    ) throws JsonProcessingException {
        log.info("Library Event : {}", libraryEvent);

        //invoke kafka producer
//        libraryEventsProducer.asynchronousSendLibraryEvent(libraryEvent);
        libraryEventsProducer.asynchronousSendLibraryEventWithProducerRecord(libraryEvent);

        log.info("After sending Library Event : ");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(libraryEvent);
    }

    @PostMapping(LIBRARY_EVENT_SYNC)
    public ResponseEntity<LibraryEvent> postLibraryEventSync(
            @Valid @RequestBody LibraryEvent libraryEvent
    ) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        log.info("Library Event : {}", libraryEvent);

        //invoke kafka producer
//        libraryEventsProducer.synchronousSendLibraryEvent(libraryEvent);
        libraryEventsProducer.synchronousSendLibraryEventWithProducerRecord(libraryEvent);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(libraryEvent);
    }

    @PutMapping(LIBRARY_EVENT_ASYNC)
    public ResponseEntity<LibraryEvent> putLibraryEventAsync(
            @Valid @RequestBody LibraryEvent libraryEvent
    ) throws JsonProcessingException {
        log.info("Library Event : {}", libraryEvent);

        validateLibraryUpdateEvent(libraryEvent);

        //invoke kafka producer
//        libraryEventsProducer.asynchronousSendLibraryEvent(libraryEvent);
        libraryEventsProducer.asynchronousSendLibraryEventWithProducerRecord(libraryEvent);

        log.info("After sending Library Event : ");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(libraryEvent);
    }
}
