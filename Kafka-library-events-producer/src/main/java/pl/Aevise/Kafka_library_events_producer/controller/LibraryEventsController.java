package pl.Aevise.Kafka_library_events_producer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.Aevise.Kafka_library_events_producer.domain.LibraryEvent;
import pl.Aevise.Kafka_library_events_producer.producer.LibraryEventsProducer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
@AllArgsConstructor
public class LibraryEventsController {
    private static final String LIBRARY_EVENT = "/v1/libraryEvent";
    private final LibraryEventsProducer libraryEventsProducer;


    @PostMapping(LIBRARY_EVENT)
    public ResponseEntity<LibraryEvent> postLibraryEvent(
            @RequestBody LibraryEvent libraryEvent
    ) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        log.info("Library Event : {}", libraryEvent);

        //invoke kafka producer
//        libraryEventsProducer.asynchronousSendLibraryEvent(libraryEvent);
//        libraryEventsProducer.synchronousSendLibraryEvent(libraryEvent);

        libraryEventsProducer.asynchronousSendLibraryEventWithProducerRecord(libraryEvent);
//        libraryEventsProducer.synchronousSendLibraryEventWithProducerRecord(libraryEvent);

        log.info("After sending Library Event : ");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(libraryEvent);
    }
}
