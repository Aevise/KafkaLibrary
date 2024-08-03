package pl.Aevise.Kafka_library_events_producer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.Aevise.Kafka_library_events_producer.domain.LibraryEvent;

@RestController
@Slf4j
public class LibraryEventsController {

    private static final String LIBRARY_EVENT = "/v1/libraryEvent";

    @PostMapping(LIBRARY_EVENT)
    public ResponseEntity<LibraryEvent> postLibraryEvent(
            @RequestBody LibraryEvent libraryEvent
    ){
        log.info("library event : {}", libraryEvent);
        //invoke kafka producer

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(libraryEvent);
    }
}
